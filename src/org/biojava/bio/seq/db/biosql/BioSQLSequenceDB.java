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
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;

/**
 * SequenceDB keyed off a BioSQL database.
 *
 * @author Thomas Down
 * @since 1.3
 */

public class BioSQLSequenceDB extends AbstractSequenceDB implements SequenceDB {
    private JDBCConnectionPool pool;
    private int dbid = -1;
    private String name;
    private IDMaker idmaker = new IDMaker.ByName();
    private WeakCacheMap outstandingSequences = new WeakCacheMap();
    private DBHelper helper;

    JDBCConnectionPool getPool() {
	return pool;
    }

    public DBHelper getDBHelper() {
	return helper;
    }

    public BioSQLSequenceDB(String dbURL,
			    String dbUser,
			    String dbPass,
			    String biodatabase,
			    boolean create,
			    DBHelper helper)
	throws BioException
    {
	this.helper = helper;
	pool = new JDBCConnectionPool(dbURL, dbUser, dbPass);
	try {
	    Connection conn = pool.takeConnection();
	    PreparedStatement getID = conn.prepareStatement("select * from biodatabase where name = ?");
	    getID.setString(1, biodatabase);
	    ResultSet rs = getID.executeQuery();
	    if (rs.next()) {
		dbid = rs.getInt(1);
		name = rs.getString(2);
		getID.close();
		pool.putConnection(conn);
		return;
	    }

	    if (create) {
		PreparedStatement createdb = conn.prepareStatement("insert into biodatabase (name) values ( ? )");
		createdb.setString(1, biodatabase);
		createdb.executeUpdate();
		createdb.close();

		dbid = getDBHelper().getInsertID(conn, "biodatabase", "biodatabase_id");
	    } else {
		throw new BioException("Biodatabase " + biodatabase + " doesn't exist");
	    }
	} catch (SQLException ex) {
	    throw new BioException(ex, "Error connecting to BioSQL database");
	} 
    }

    public String getName() {
	return name;
    }

    public void addSequence(Sequence seq)
	throws IllegalIDException, ChangeVetoException, BioException
    {   
	if (changeSupport == null) {
	    _addSequence(seq);
	} else {
	    synchronized (changeSupport) {
		ChangeEvent cev = new ChangeEvent(this, SequenceDB.SEQUENCES, seq);
		changeSupport.firePreChangeEvent(cev);
		_addSequence(seq);
		changeSupport.firePostChangeEvent(cev);
	    }
	}
    }

    private void _addSequence(Sequence seq)
        throws IllegalIDException, ChangeVetoException, BioException
    {
	String seqName = idmaker.calcID(seq);
	int version = 1;

	Alphabet seqAlpha = seq.getAlphabet();
	SymbolTokenization seqToke;
	try {
	    seqToke = seqAlpha.getTokenization("token");
	} catch (Exception ex) {
	    throw new BioException(ex, "Can't store sequences in BioSQL unless they can be sensibly tokenized/detokenized");
	}

	try {
	    Connection conn = pool.takeConnection();
	    ResultSet rs;

	    PreparedStatement create_bioentry = conn.prepareStatement(
                    "insert into bioentry " +
                    "(biodatabase_id, display_id, accession, entry_version, division) " +
		    "values (?, ?, ?, ?, ?)");
	    create_bioentry.setInt(1, dbid);
	    create_bioentry.setString(2, seqName);
	    create_bioentry.setString(3, seqName);
	    create_bioentry.setInt(4, version);
	    create_bioentry.setString(5, "?");
	    create_bioentry.executeUpdate();
	    create_bioentry.close();

	    int bioentry_id = getDBHelper().getInsertID(conn, "bioentry", "bioentry_id");
	    
	    PreparedStatement create_biosequence = conn.prepareStatement(
                    "insert into biosequence " +
                    "(bioentry_id, seq_version, biosequence_str, molecule) " +
		    "values (?, ?, ?, ?)");
	    create_biosequence.setInt(1, bioentry_id);
	    create_biosequence.setInt(2, version);
	    create_biosequence.setString(3, seqToke.tokenizeSymbolList(seq));
	    create_biosequence.setString(4, seqAlpha.getName());
	    create_biosequence.executeUpdate();
	    create_biosequence.close();

	    int biosequence_id = getDBHelper().getInsertID(conn, "biosequence", "biosequence_id");

	    // 
	    // Store the features
	    //

	    for (Iterator fi = seq.filter(FeatureFilter.all, true).features(); fi.hasNext(); ) {
		Feature f = (Feature) fi.next();
		persistFeature(conn, bioentry_id, f);
	    }

	    pool.putConnection(conn);
	} catch (SQLException ex) {
	    throw new BioException(ex, "Error inserting data into BioSQL tables");
	}
    }

    public Sequence getSequence(String id)
        throws BioException, IllegalIDException
    {
	Sequence seq = (Sequence) outstandingSequences.get(id);
	if (seq != null) {
	    return seq;
	}

	try {
	    Connection conn = pool.takeConnection();

	    PreparedStatement get_sequence = conn.prepareStatement("select bioentry.bioentry_id, biosequence.biosequence_id " +
								   "from bioentry, biosequence " +
								   "where bioentry.accession = ? and " +
								   "      biosequence.bioentry_id = bioentry.bioentry_id");
	    get_sequence.setString(1, id);
	    ResultSet rs = get_sequence.executeQuery();
	    if (rs.next()) {
		int bioentry_id = rs.getInt(1);
		int biosequence_id = rs.getInt(2);

		seq = new BioSQLSequence(this, id, bioentry_id, biosequence_id);
	    } 
	    get_sequence.close();

	    pool.putConnection(conn);

	    if (seq != null) {
		outstandingSequences.put(id, seq);
		return seq;
	    }
	} catch (SQLException ex) {
	    throw new BioException(ex, "Error accessing BioSQL tables");
	}

	throw new IllegalIDException("No bioentry with accession " + id);
    }

    public void removeSequence(String id)
	throws IllegalIDException, ChangeVetoException, BioException
    {   
	if (changeSupport == null) {
	    _removeSequence(id);
	} else {
	    synchronized (changeSupport) {
		ChangeEvent cev = new ChangeEvent(this, SequenceDB.SEQUENCES, null);
		changeSupport.firePreChangeEvent(cev);
		_removeSequence(id);
		changeSupport.firePostChangeEvent(cev);
	    }
	}
    }

    private void _removeSequence(String id) 
        throws BioException, IllegalIDException, ChangeVetoException
    {
	Sequence seq = (Sequence) outstandingSequences.get(id);
	if (seq != null) {
	    seq = null;  // Don't want to be holding the reference ourselves!
	    try {
		Thread.sleep(100L);
		System.gc();
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	    seq = (Sequence) outstandingSequences.get(id);
	    if (seq != null) {
		throw new BioException("There are still references to sequence with ID " + id + " from this database.");
	    }
	}

	Connection conn = null;
	try {
	    conn = pool.takeConnection();
	    conn.setAutoCommit(false);
	    
	    PreparedStatement get_sequence = conn.prepareStatement("select bioentry.bioentry_id, biosequence.biosequence_id " +
								   "from bioentry, biosequence " +
								   "where bioentry.accession = ? and " +
								   "      biosequence.bioentry_id = bioentry.bioentry_id");
	    get_sequence.setString(1, id);
	    ResultSet rs = get_sequence.executeQuery();
	    boolean exists;
	    if ((exists = rs.next())) {
		int bioentry_id = rs.getInt(1);
		int biosequence_id = rs.getInt(2);

		PreparedStatement delete_biosequence = conn.prepareStatement("delete from biosequence where biosequence_id = ?");
		delete_biosequence.setInt(1, biosequence_id);
		delete_biosequence.executeUpdate();
		delete_biosequence.close();

		PreparedStatement delete_entry = conn.prepareStatement("delete from bioentry where bioentry_id = ?");
		delete_entry.setInt(1, bioentry_id);
		delete_entry.executeUpdate();
		delete_entry.close();
	    } 
	    get_sequence.close();

	    conn.commit();
	    pool.putConnection(conn);

	    if (!exists) {
		throw new IllegalIDException("Sequence " + id + " didn't exist");
	    }
	} catch (SQLException ex) {
	    boolean rolledback = false;
	    if (conn != null) {
		try {
		    conn.rollback();
		    rolledback = true;
		} catch (SQLException ex2) {}
	    }
	    throw new BioException(ex, "Error removing from BioSQL tables" + (rolledback ? " (rolled back successfully)" : ""));
	}
    }

    public Set ids() {
	try {
	    Set _ids = new HashSet();
	    Connection conn = pool.takeConnection();

	    Statement st = conn.createStatement();
	    ResultSet rs = st.executeQuery("select bioentry.accession accession from bioentry, biosequence where bioentry.bioentry_id = biosequence.bioentry_id");
	    while (rs.next()) {
		_ids.add(rs.getString(1));
	    }
	    st.close();

	    pool.putConnection(conn);
	    return Collections.unmodifiableSet(_ids);
	} catch (SQLException ex) {
	    throw new BioRuntimeException(ex, "Error reading from BioSQL tables");
	}
    }

    //
    // Sequence support
    //

    void persistFeature(Connection conn,
			int bioentry_id,
			Feature f)
	throws BioException, SQLException
    {
	int seqfeature_key = intern_seqfeature_key(conn, f.getType());
	int seqfeature_source = intern_seqfeature_source(conn, f.getSource());
	
	PreparedStatement add_feature = conn.prepareStatement(
		"insert into seqfeature "+
		"       (bioentry_id, seqfeature_key_id, seqfeature_source_id) " +
		"values (?, ?,  ?)"
		);
	add_feature.setInt(1, bioentry_id);
	add_feature.setInt(2, seqfeature_key);
	add_feature.setInt(3, seqfeature_source);
	add_feature.executeUpdate();
	add_feature.close();

	int id = getDBHelper().getInsertID(conn, "seqfeature", "seqfeature_id"); 

	PreparedStatement add_locationspan = conn.prepareStatement(
                "insert into seqfeature_location " +
	        "       (seqfeature_id, seq_start, seq_end, seq_strand, location_rank) " +
		"values (?, ?, ?, ?, ?)"
		);
	add_locationspan.setInt(1, id);
	if (f instanceof StrandedFeature) {
	    StrandedFeature.Strand s = ((StrandedFeature) f).getStrand();
	    if (s == StrandedFeature.POSITIVE) {
		add_locationspan.setInt(4, 1);
	    } else if (s== StrandedFeature.NEGATIVE) {
		add_locationspan.setInt(4, -1);
	    } else {
		add_locationspan.setInt(4, 0);
	    }
	} else {
	    add_locationspan.setInt(4, 0);
	}

	int rank = 0;
	for (Iterator i = f.getLocation().blockIterator(); i.hasNext(); ) {
	    Location bloc = (Location) i.next();
	    add_locationspan.setInt(2, bloc.getMin());
	    add_locationspan.setInt(3, bloc.getMax());
	    add_locationspan.setInt(5, ++rank);
	    add_locationspan.executeUpdate();
	}
	add_locationspan.close();
	
	// 
	// Persist anything in the annotation bundle, too?
	//	
    }
			       

    int intern_seqfeature_key(Connection conn, String s)
        throws SQLException
    {
	PreparedStatement get = conn.prepareStatement("select seqfeature_key_id from seqfeature_key where key_name = ?");
	get.setString(1, s);
	ResultSet rs = get.executeQuery();
	if (rs.next()) {
	    int id = rs.getInt(1);
	    get.close();
	    return id;
	}
	get.close();

	PreparedStatement insert = conn.prepareStatement("insert into seqfeature_key (key_name) values ( ? )");
	insert.setString(1, s);
	insert.executeUpdate();
	insert.close();

	int id = getDBHelper().getInsertID(conn, "seqfeature_key", "seqfeature_key_id");
	return id;		      
    }

    int intern_seqfeature_source(Connection conn, String s)
        throws SQLException
    {
	PreparedStatement get = conn.prepareStatement("select seqfeature_source_id from seqfeature_source where source_name = ?");
	get.setString(1, s);
	ResultSet rs = get.executeQuery();
	if (rs.next()) {
	    int id = rs.getInt(1);
	    get.close();
	    return id;
	}
	get.close();

	PreparedStatement insert = conn.prepareStatement("insert into seqfeature_source (source_name) values ( ? )");
	insert.setString(1, s);
	insert.executeUpdate();
	insert.close();

	int id = getDBHelper().getInsertID(conn, "seqfeature_source", "seqfeature_source_id");
	return id;		      
    }    
}
