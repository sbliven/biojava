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
 * SequenceDB keyed off a BioSQL database.
 *
 * @author Thomas Down
 * @since 1.3
 */

class BioSQLSequence implements Sequence, RealizingFeatureHolder {
    private BioSQLSequenceDB seqDB;
    private String name;
    private int bioentry_id;
    private int biosequence_id;
    private ChangeSupport changeSupport;

    private void initChangeSupport() {
	changeSupport = new ChangeSupport();
    }

    private DBHelper getDBHelper() {
	return seqDB.getDBHelper();
    }

    BioSQLSequence(BioSQLSequenceDB seqDB,
		   String name,
		   int bioentry_id,
		   int biosequence_id)
    {
	this.seqDB = seqDB;
	this.name = name;
	this.bioentry_id = bioentry_id;
	this.biosequence_id = biosequence_id;
    }

    public String getName() {
	return name;
    }

    public String getURN() {
	return name;
    }

    //
    // implements Annotatable
    //

    public Annotation getAnnotation() {
	return Annotation.EMPTY_ANNOTATION;
    }

    //
    // implements SymbolList
    //

    public Alphabet getAlphabet() {
	return getSymbols().getAlphabet();
    }

    public int length() {
	return getSymbols().length();
    }

    public Symbol symbolAt(int i) {
	return getSymbols().symbolAt(i);
    }

    public SymbolList subList(int start, int end) {
	return getSymbols().subList(start, end);
    }

    public List toList() {
	return getSymbols().toList();
    }

    public Iterator iterator() {
	return getSymbols().iterator();
    }

    public String seqString() {
	return getSymbols().seqString();
    }

    public String subStr(int start, int end) {
	return getSymbols().subStr(start, end);
    }

    public void edit(Edit e) 
        throws ChangeVetoException 
    {
	throw new ChangeVetoException("Can't edit sequence in BioSQL -- or at least not yet...");
    }    

    private SymbolList symbols;

    protected synchronized SymbolList getSymbols()
        throws BioRuntimeException
    {
	if (symbols == null) {
	    try {
		Connection conn = seqDB.getPool().takeConnection();
		
		PreparedStatement get_symbols = conn.prepareStatement("select molecule, biosequence_str " +
								      "from biosequence " +
								      "where biosequence_id = ?");
		get_symbols.setInt(1, biosequence_id);
		ResultSet rs = get_symbols.executeQuery();
		String alphaName  = null;
		String seqString = null;
		if (rs.next()) {
		    alphaName = rs.getString(1).toUpperCase(); // FIXME
		    seqString = rs.getString(2);  // FIXME should do something stream-y
		}
		get_symbols.close();

		seqDB.getPool().putConnection(conn);

		if (alphaName != null) {
		    try {
			Alphabet alpha = AlphabetManager.alphabetForName(alphaName);
			SymbolTokenization toke = alpha.getTokenization("token");
			symbols = new SimpleSymbolList(toke, seqString);
		    } catch (Exception ex) {
			throw new BioRuntimeException(ex, "Couldn't parse tokenized symbols of type " + alphaName);
		    }
		} else {
		    throw new BioRuntimeException("Sequence " + name + " has gone missing!");
		}
	    } catch (SQLException ex) {
		throw new BioRuntimeException(ex, "Unknown error getting symbols from BioSQL.  Oh dear.");
	    }
	}

	return symbols;
    }

    //
    // implements FeatureHolder
    //

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
	Feature f = realizeFeature(this, ft);
	if (changeSupport == null) {
	    persistFeature(f);
	    getFeatures().addFeature(f);
	} else {
	    synchronized (changeSupport) {
		ChangeEvent cev = new ChangeEvent(this, FeatureHolder.FEATURES, f);
		changeSupport.firePreChangeEvent(cev);
		persistFeature(f);
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
		    StrandedFeature.Template templ = new StrandedFeature.Template();
		    templ.type = rs.getString(2);
		    templ.source = rs.getString(3);
		    templ.annotation = new SmallAnnotation();
		    fmap.put(new Integer(rs.getInt(1)), templ);
		}
		get_features.close();

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

		seqDB.getPool().putConnection(conn);
		conn = null;

		features = new SimpleFeatureHolder();
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

		    try {
			features.addFeature(FeatureImpl.DEFAULT.realizeFeature(this, this, templ));
		    } catch (BioException ex) {
			throw new BioRuntimeException(ex);
		    } catch (ChangeVetoException ex) {
			throw new BioError("Assertion failure: couldn't add to newly created FeatureHolder");
		    }
		}
	    } catch (SQLException ex) {
		throw new BioRuntimeException("Oooops, couldn't read features!");
	    }
	}

	return features;
    }

    //
    // implements RealizingFeatureHolder
    //

    public Feature realizeFeature(FeatureHolder parent, Feature.Template templ)
        throws BioException
    {
	if (parent != this) {
	    throw new BioException("BioSQL only (currently) supports top-level features");
	}

	Feature f = FeatureImpl.DEFAULT.realizeFeature(this, this, templ);
	return f;
    }

    //
    // Feature persistance
    //

    void persistFeature(Feature f)
        throws BioException
    {
	Connection conn = null;
	try {
	    conn = seqDB.getPool().takeConnection();

	    seqDB.persistFeature(conn, bioentry_id, f);

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
