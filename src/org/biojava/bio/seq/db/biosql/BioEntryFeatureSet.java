/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */

package org.biojava.bio.seq.db.biosql;

import java.sql.*;
import java.util.*;

import org.biojava.utils.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;

/**
 * Top-level SeqFeature set for a BioEntry
 *
 * @author Thomas Down
 * @since 1.3
 */

class BioEntryFeatureSet implements FeatureHolder, RealizingFeatureHolder {
    private Sequence seq;
    private BioSQLSequenceDB seqDB;
    private int bioentry_id;
    private ChangeSupport changeSupport;

    
    BioEntryFeatureSet(Sequence seq,
		       BioSQLSequenceDB seqDB,
		       int bioentry_id)
    {
	this.seq = seq;
	this.seqDB = seqDB;
	this.bioentry_id = bioentry_id;
    }

    private DBHelper getDBHelper() {
	return seqDB.getDBHelper();
    }

    public Iterator features() {
	return getFeatures().features();
    }

    public int countFeatures() {
	return getFeatures().countFeatures();
    }

    public boolean containsFeature(Feature f) {
	return getFeatures().containsFeature(f);
    }

    public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
	return getFeatures().filter(ff, recurse);
    }


    public Feature createFeature(Feature.Template ft)
        throws ChangeVetoException, BioException
    {
	Feature f = realizeFeature(seq, ft);
	if (changeSupport == null) {
	    persistFeature(f, -1);
	    getFeatures().addFeature(f);
	} else {
	    synchronized (changeSupport) {
		ChangeEvent cev = new ChangeEvent(seq, FeatureHolder.FEATURES, f);
		changeSupport.firePreChangeEvent(cev);
		persistFeature(f, -1); // No parent
		getFeatures().addFeature(f);
		changeSupport.firePostChangeEvent(cev);
	    }
	}

	return f;
    }

    public void removeFeature(Feature f)
        throws ChangeVetoException
    {
	throw new ChangeVetoException("Don't (yet) support feature removal");
    }

    private SimpleFeatureHolder features;

    protected synchronized SimpleFeatureHolder getFeatures() {
	if (features == null) {
	    try {
		Connection conn = seqDB.getPool().takeConnection();
		Map fmap = new HashMap();
		Map lmap = new HashMap();

		PreparedStatement get_features = conn.prepareStatement(
                        "select seqfeature.seqfeature_id, " +
			"seqfeature_key.key_name, " +
			"seqfeature_source.source_name " +
			"from seqfeature, seqfeature_key, seqfeature_source " +
			"where seqfeature_key.seqfeature_key_id = seqfeature.seqfeature_key_id and " +
			"      seqfeature_source.seqfeature_source_id = seqfeature.seqfeature_source_id and " +
			"      seqfeature.bioentry_id = ?"
			);
		get_features.setInt(1, bioentry_id);
		ResultSet rs = get_features.executeQuery();
		while (rs.next()) {
		    int feature_id = rs.getInt(1);
		    StrandedFeature.Template templ = new StrandedFeature.Template();
		    templ.type = rs.getString(2);
		    templ.source = rs.getString(3);
		    templ.annotation = new BioSQLFeatureAnnotation(seqDB, feature_id);
		    fmap.put(new Integer(feature_id), templ);
		}
		get_features.close();

		// Fetch locations

		PreparedStatement get_locations = conn.prepareStatement(
		        "select seqfeature_location.seqfeature_id, " +
			"seqfeature_location.seq_start, " +
			"seqfeature_location.seq_end, " +
			"seqfeature_location.seq_strand " +
			"from seqfeature, seqfeature_location " +
			"where seqfeature_location.seqfeature_id = seqfeature.seqfeature_id and " +
			"seqfeature.bioentry_id = ?"
			);
		get_locations.setInt(1, bioentry_id);
		rs = get_locations.executeQuery();
		while (rs.next()) {
		    Integer fid = new Integer(rs.getInt(1));
		    int start = rs.getInt(2);
		    int end = rs.getInt(3);
		    int istrand = rs.getInt(4);

		    StrandedFeature.Strand strand = StrandedFeature.UNKNOWN;
		    if (istrand > 0) {
			strand = StrandedFeature.POSITIVE;
		    } else if (istrand < 0) {
			strand = StrandedFeature.NEGATIVE;
		    }
		    StrandedFeature.Template templ = (StrandedFeature.Template) fmap.get(fid);
		    if (templ.strand != null && templ.strand != strand) {
			throw new BioRuntimeException("Feature strands don't match");
		    } else {
			templ.strand = strand;
		    }

		    Location bloc = new RangeLocation(start, end);
		    List ll = (List) lmap.get(fid);
		    if (ll == null) {
			ll = new ArrayList();
			lmap.put(fid, ll);
		    }
		    ll.add(bloc);
		}
		get_locations.close();

		// Bind location information to features
		
		for (Iterator i = fmap.entrySet().iterator(); i.hasNext(); ) {
		    Map.Entry me = (Map.Entry) i.next();
		    Integer fid = (Integer) me.getKey();
		    StrandedFeature.Template templ = (StrandedFeature.Template) me.getValue();
		    
		    List ll = (List) lmap.get(fid);
		    if (ll == null) {
			throw new BioRuntimeException("BioSQL SeqFeature doesn't have any associated location spans");
		    }

		    Location loc = null;
		    if (ll.size() == 1) {
			loc = (Location) ll.get(0);
		    } else {
			loc = LocationTools.union(ll);
		    }
		    templ.location = loc;
		}

		// Check hierarchy

		Set toplevelFeatures = new HashSet(fmap.keySet());
		Map featureHierarchy = new HashMap();
		if (seqDB.isHierarchySupported()) {
		    PreparedStatement get_hierarchy = conn.prepareStatement("select parent, child from seqfeature_hierarchy, seqfeature where parent = seqfeature.seqfeature_id and seqfeature.bioentry_id = ?");
		    get_hierarchy.setInt(1, bioentry_id);
		    rs = get_hierarchy.executeQuery();
		    while (rs.next()) {
			Integer parent = new Integer(rs.getInt(1));
			Integer child = new Integer(rs.getInt(2));

			toplevelFeatures.remove(child);
			List cl = (List) featureHierarchy.get(parent);
			if (cl == null) {
			    cl = new ArrayList();
			    featureHierarchy.put(parent, cl);
			}
			cl.add(child);
		    }
		    get_hierarchy.close();
		}

		seqDB.getPool().putConnection(conn);
		conn = null;
		
		features = new SimpleFeatureHolder();
		for (Iterator tlfi = toplevelFeatures.iterator(); tlfi.hasNext(); ) {
		    Integer fid = (Integer) tlfi.next();
		    try {
			Feature f = reRealizeFeature(fid, fmap, featureHierarchy, seq);
			features.addFeature(f);
		    } catch (BioException ex) {
			throw new BioRuntimeException(ex);
		    } catch (ChangeVetoException ex) {
			throw new BioError("Assertion failure: couldn't add to newly created FeatureHolder");
		    }
		}
	    } catch (SQLException ex) {
		throw new BioRuntimeException(ex, "Oooops, couldn't read features!");
	    }
	}

	return features;
    }

    private Feature reRealizeFeature(Integer fid, Map fmap, Map featureHierarchy, FeatureHolder parent)
        throws BioException, ChangeVetoException
    {
	Feature.Template templ = (Feature.Template) fmap.get(fid);
	Feature f = realizeFeature(parent, templ);
	if (f instanceof BioSQLFeatureI) {
	    ((BioSQLFeatureI) f)._setInternalID(fid.intValue());
	    ((BioSQLFeatureI) f)._setAnnotation(new BioSQLFeatureAnnotation(seqDB, fid.intValue()));
	    List children = (List) featureHierarchy.get(fid);
	    if (children != null) {
		for (Iterator ci = children.iterator(); ci.hasNext(); ) {
		    Integer childID = (Integer) ci.next();
		    ((BioSQLFeatureI) f)._addFeature(reRealizeFeature(childID, fmap, featureHierarchy, f));
		}
	    }
	}
	return f;
    }

    //
    // implements RealizingFeatureHolder
    //

    public Feature realizeFeature(FeatureHolder parent, Feature.Template templ)
        throws BioException
    {
	if (parent != seq && !seqDB.isHierarchySupported()) {
	    throw new BioException("This database doesn't support feature hierarchy.  Please create a seqfeature_hierarchy table");
	}

	if (templ instanceof StrandedFeature.Template && seq.getAlphabet() == DNATools.getDNA()) {
	    return new BioSQLStrandedFeature(seq, parent, (StrandedFeature.Template) templ);
	} else {
	    return new BioSQLFeature(seq, parent, templ);
	}
    }

    //
    // Feature persistance
    //

    void persistFeature(Feature f, int parent_id)
        throws BioException
    {
	Connection conn = null;
	try {
	    conn = seqDB.getPool().takeConnection();
	    conn.setAutoCommit(false);
	    int f_id = seqDB.persistFeature(conn, bioentry_id, f, parent_id);
	    if (f instanceof BioSQLFeatureI) {
		((BioSQLFeatureI) f)._setInternalID(f_id);
		((BioSQLFeatureI) f)._setAnnotation(new BioSQLFeatureAnnotation(seqDB, f_id));
	    }
	    conn.commit();
	    seqDB.getPool().putConnection(conn);
	} catch (SQLException ex) {
	    boolean rolledback = false;
	    if (conn != null) {
		try {
		    conn.rollback();
		    rolledback = true;
		} catch (SQLException ex2) {}
	    }
	    throw new BioException(ex, "Error adding BioSQL tables" + (rolledback ? " (rolled back successfully)" : ""));
	}
    }

    
    // 
    // Changeable
    //

    private void initChangeSupport() {
	changeSupport = new ChangeSupport();
    }

    public void addChangeListener(ChangeListener cl) {
	addChangeListener(cl, ChangeType.UNKNOWN);
    }
	
    public void addChangeListener(ChangeListener cl, ChangeType ct) {
	if (changeSupport == null) {
	    initChangeSupport();
	}

	changeSupport.addChangeListener(cl, ct);
    }

    public void removeChangeListener(ChangeListener cl) {
	removeChangeListener(cl, ChangeType.UNKNOWN);
    }

    public void removeChangeListener(ChangeListener cl, ChangeType ct) {
	if (changeSupport != null) {
	    changeSupport.removeChangeListener(cl, ct);
	}
    }
}
