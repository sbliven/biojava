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

    private static class LocationQualifierMemento {
	public String qualifier_name;
	public String qualifier_value;
	public int qualifier_int;
    }

    protected synchronized SimpleFeatureHolder getFeatures() {
	if (features == null) {
	    try {
		Connection conn = seqDB.getPool().takeConnection();
		Map fmap = new HashMap();
		Map qmap = new HashMap();
		Map lmap = new HashMap();

		PreparedStatement get_features = conn.prepareStatement(
                        "select seqfeature.seqfeature_id, " +
			"ontology_term.term_name, " +
			"seqfeature_source.source_name " +
			"from seqfeature, ontology_term, seqfeature_source " +
			"where ontology_term.ontology_term_id = seqfeature.seqfeature_key_id and " +
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

		// Fetch those crappy location qualifiers first...

		if (seqDB.isLocationQualifierSupported()) {
		    PreparedStatement get_location_crap = conn.prepareStatement(
			    "select location_qualifier_value.seqfeature_location_id, " +
			    "       seqfeature_qualifier.qualifier_name, " +
			    "       location_qualifier_value.qualifier_value, " +
			    "       location_qualifier_value.qualifier_int_value " +
			    "  from location_qualifier_value, seqfeature_location, seqfeature, seqfeature_qualifier " +
			    " where seqfeature.bioentry_id = ? and " +
			    "       seqfeature_location.seqfeature_id = seqfeature.seqfeature_id and " +
			    "       location_qualifier_value.seqfeature_location_id = seqfeature_location.seqfeature_location_id and " +
			    "       seqfeature_qualifier.seqfeature_qualifier_id = location_qualifier_value.seqfeature_qualifier_id");
		    get_location_crap.setInt(1, bioentry_id);
		    rs = get_location_crap.executeQuery();
		    while (rs.next()) {
			LocationQualifierMemento lqm = new LocationQualifierMemento();
			int location_id = rs.getInt(1);
			lqm.qualifier_name = rs.getString(2);
			lqm.qualifier_value = rs.getString(3);
			lqm.qualifier_int = rs.getInt(4);
   
			Integer location_id_boxed = new Integer(location_id);
			List l = (List) qmap.get(location_id_boxed);
			if (l == null) {
			    l = new ArrayList();
			    qmap.put(location_id_boxed, l);
			}
			l.add(lqm);
		    }
		}

		// Fetch locations

		PreparedStatement get_locations = conn.prepareStatement(
		        "select seqfeature_location.seqfeature_location_id, " +
			"seqfeature_location.seqfeature_id, " +
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
		    Integer lid = new Integer(rs.getInt(1));
		    Integer fid = new Integer(rs.getInt(2));
		    int start = rs.getInt(3);
		    int end = rs.getInt(4);
		    int istrand = rs.getInt(5);

		    StrandedFeature.Strand strand = StrandedFeature.UNKNOWN;
		    if (istrand > 0) {
			strand = StrandedFeature.POSITIVE;
		    } else if (istrand < 0) {
			strand = StrandedFeature.NEGATIVE;
		    }
		    StrandedFeature.Template templ = (StrandedFeature.Template) fmap.get(fid);
		    if (templ.strand != null && templ.strand != strand) {
			// throw new BioRuntimeException("Feature strands don't match");
			// Really don't want to support these at all, but...
			templ.strand = StrandedFeature.UNKNOWN;
		    } else {
			templ.strand = strand;
		    }

		    Location bloc;
		    if (start == end) {
			bloc = new PointLocation(start);
		    } else {
			bloc = new RangeLocation(start, end);
		    }

		    List locationCrap = (List) qmap.get(lid);
		    if (locationCrap != null) {
			int min_start = -1;
			int min_end = -1;
			int max_start = -1;
			int max_end = -1;
			boolean unknown_start = false;
			boolean unknown_end = false;
			boolean unbounded_start = false;
			boolean unbounded_end = false;
			boolean isFuzzy = false;

			for (Iterator i = locationCrap.iterator(); i.hasNext(); ) {
			    LocationQualifierMemento lqm = (LocationQualifierMemento) i.next();
			    String qname = lqm.qualifier_name;
			    
			    if ("min_start".equals(qname)) {
				min_start = lqm.qualifier_int;
				isFuzzy = true;
			    } else if ("max_start".equals(qname)) {
				max_start = lqm.qualifier_int;
				isFuzzy = true;
			    } else if ("min_end".equals(qname)) {
				min_end = lqm.qualifier_int;
				isFuzzy = true;
			    } else if ("max_end".equals(qname)) {
				max_end = lqm.qualifier_int;
				isFuzzy = true;
			    } else if ("start_pos_type".equals(qname)) {
				if ("BEFORE".equalsIgnoreCase(lqm.qualifier_value)) {
				    unbounded_start = true;
				    isFuzzy = true;
				}
			    } if ("end_pos_type".equals(qname)) {
				if ("AFTER".equalsIgnoreCase(lqm.qualifier_value)) {
				    unbounded_end = true;
				    isFuzzy = true;
				}
			    } 
			}

			if (isFuzzy) {
			    if (unknown_start) {
				min_start = Integer.MIN_VALUE;
				max_start = Integer.MAX_VALUE;
			    }
			    if (unbounded_start) {
				min_start = Integer.MIN_VALUE;
			    }
			    if (unknown_end) {
				min_end = Integer.MIN_VALUE;
				max_end = Integer.MAX_VALUE;
			    }
			    if (unbounded_end) {
				max_end = Integer.MAX_VALUE;
			    }

			    if (min_start == -1) {
				min_start = bloc.getMin();
			    }
			    if (max_start == -1) {
				max_start = bloc.getMin();
			    }
			    if (min_end == -1) {
				min_end = bloc.getMax();
			    } 
			    if (max_end == -1) {
				max_end = bloc.getMax();
			    }

			    bloc = new FuzzyLocation(min_start,
						     max_end,
						     max_start,
						     min_end,
						     FuzzyLocation.RESOLVE_INNER);
			}
		    }

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
		    PreparedStatement get_hierarchy = conn.prepareStatement("select parent_seqfeature_id, child_seqfeature_id from seqfeature_relationship, seqfeature where parent = seqfeature.seqfeature_id and seqfeature.bioentry_id = ?");
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
