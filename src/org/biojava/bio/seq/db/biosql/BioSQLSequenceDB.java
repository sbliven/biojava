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
import org.biojava.utils.cache.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.taxa.*;

/**
 * SequenceDB keyed off a BioSQL database.
 *
 * @author Thomas Down
 * @author Matthew Pocock
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

	Connection conn = null;
	try {
	    conn = pool.takeConnection();
	    conn.setAutoCommit(false);
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

	    // System.err.println("Created bioentry");

	    int bioentry_id = getDBHelper().getInsertID(conn, "bioentry", "bioentry_id");
	    
	    if (isAssemblySupported() && seq.filter(new FeatureFilter.ByClass(ComponentFeature.class), false).countFeatures() > 0) {
		PreparedStatement create_assembly = conn.prepareStatement("insert into assembly " +
									  "       (bioentry_id, length, molecule) " +
									  "values (?, ?, ?)");
		create_assembly.setInt(1, bioentry_id);
		create_assembly.setInt(2, seq.length());
		create_assembly.setString(3, seqAlpha.getName());
		create_assembly.executeUpdate();
		create_assembly.close();
		int assembly_id = getDBHelper().getInsertID(conn, "assembly", "assembly_id");

		FeatureHolder components = seq.filter(new FeatureFilter.ByClass(ComponentFeature.class), false);
		PreparedStatement create_fragment = conn.prepareStatement(
		        "insert into assembly_fragment " +
			"       (assembly_id, fragment_name, assembly_start, assembly_end, fragment_start, fragment_end, strand) " +
			"values (?,           ?,             ?,              ?,            ?,              ?,            ?)");
		for (Iterator i = components.features(); i.hasNext(); ) {
		    ComponentFeature cf = (ComponentFeature) i.next();
		    if (cf.getType().equals("static_golden_path_clone")) {
			System.err.println("Ensembl hack: skipping clone...");
			continue;
		    }

		    create_fragment.setInt(1, assembly_id);
		    create_fragment.setString(2, cf.getComponentSequenceName());
		    create_fragment.setInt(3, cf.getLocation().getMin());
		    create_fragment.setInt(4, cf.getLocation().getMax());
		    create_fragment.setInt(5, cf.getComponentLocation().getMin());
		    create_fragment.setInt(6, cf.getComponentLocation().getMax());
		    create_fragment.setInt(7, cf.getStrand().getValue());
		    create_fragment.executeUpdate();
		}
		create_fragment.close();
	    } else {
		PreparedStatement create_biosequence = conn.prepareStatement("insert into biosequence " +
									     "(bioentry_id, seq_version, biosequence_str, molecule) " +
									     "values (?, ?, ?, ?)");
		create_biosequence.setInt(1, bioentry_id);
		create_biosequence.setInt(2, version);
		create_biosequence.setString(3, seqToke.tokenizeSymbolList(seq));
		create_biosequence.setString(4, seqAlpha.getName());
		create_biosequence.executeUpdate();
		create_biosequence.close();
		int biosequence_id = getDBHelper().getInsertID(conn, "biosequence", "biosequence_id");
	    }
	    
	    // System.err.println("Stored sequence");

	    // 
	    // Store the features
	    //

	    FeatureHolder features = seq;
	    int num = features.countFeatures();
	    if (!isHierarchySupported()) {
		features = features.filter(FeatureFilter.all, true);
		if (features.countFeatures() != num) {
		    System.err.println("*** Warning: feature hierarchy was lost when adding sequence to BioSQL");
		}
	    }
	    persistFeatures(conn, bioentry_id, features, -1);
	    
	    // System.err.println("Stored features");

            //
            // we will need this annotation bundle for various things
            //
            
            Annotation ann = seq.getAnnotation();
            
	    //
	    // Store organism
	    //
	    
	    if(ann.containsProperty(OrganismParser.PROPERTY_ORGANISM)) {
                Taxa taxa = (Taxa) ann.getProperty(OrganismParser.PROPERTY_ORGANISM);
                Annotation ta = taxa.getAnnotation();
                int taxaID;
                {
		    Object t  = ta.getProperty(EbiFormat.PROPERTY_NCBI_TAXA);
		    if(t instanceof List) {
			t = (String) ((List) t).get(0);
		    }
		    taxaID = Integer.parseInt((String) t);
                }
                
                int taxa_id;
                PreparedStatement select_taxa = conn.prepareStatement(
								      "select taxa_id " +
								      "from taxa " +
								      "where ncbi_taxa_id = ? "
								      );
                select_taxa.setInt(1, taxaID);
                ResultSet trs = select_taxa.executeQuery();
                if(trs.next()) {
		    // entry exists - link to it
		    taxa_id = trs.getInt(1);
                } else {
		    // entry does not exist - create it and link to it
		    String name = EbiFormat.getInstance().serialize(taxa);
		    String common = taxa.getCommonName();
		    PreparedStatement create_taxa = conn.prepareStatement(
									  "insert into taxa " +
									  "(full_lineage, common_name, ncbi_taxa_id) " +
									  "values (?, ?, ?)"
									  );
		    create_taxa.setString(1, name);
		    create_taxa.setString(2, common);
		    create_taxa.setInt(3, taxaID);
		    create_taxa.executeUpdate();
		    create_taxa.close();
		    taxa_id = getDBHelper().getInsertID(conn, "taxa", "taxa_id");
                }
                select_taxa.close();
                
                PreparedStatement create_bioentry_taxa = conn.prepareStatement(
									       "insert into bioentry_taxa " +
									       "(bioentry_id, taxa_id) " +
									       "values (?, ?)"
									       );
                
                create_bioentry_taxa.setInt(1, bioentry_id);
                create_bioentry_taxa.setInt(2, taxa_id);
                create_bioentry_taxa.executeUpdate();
                create_bioentry_taxa.close();
	    }

	    conn.commit();
	    pool.putConnection(conn);
	} catch (SQLException ex) {
	    boolean rolledback = false;
	    if (conn != null) {
		try {
		    conn.rollback();
		    rolledback = true;
		} catch (SQLException ex2) {}
	    }
	    throw new BioRuntimeException(ex, "Error adding BioSQL tables" + (rolledback ? " (rolled back successfully)" : ""));
	}
    }

    void persistFeatures(Connection conn, int bioentry_id, FeatureHolder features, int parent)
        throws BioException, SQLException
    {
	for (Iterator fi = features.features(); fi.hasNext(); ) {
	    Feature f = (Feature) fi.next();
	    
	    if (! (f instanceof ComponentFeature) /* && !(f.getType().equals("similarity")) */) {
		int id = persistFeature(conn, bioentry_id, f, parent);
		if (isHierarchySupported()) {
		    persistFeatures(conn, bioentry_id, f, id);
		}
	    }
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

	    PreparedStatement get_bioentry = conn.prepareStatement("select bioentry.bioentry_id " +
								   "from bioentry " +
								   "where bioentry.accession = ?");
	    get_bioentry.setString(1, id);
	    ResultSet rs = get_bioentry.executeQuery();
	    int bioentry_id = -1;
	    if (rs.next()) {
		bioentry_id = rs.getInt(1);
	    } 
	    get_bioentry.close();

	    if (bioentry_id < 0) {
		pool.putConnection(conn);
		throw new IllegalIDException("No bioentry with accession " + id);
	    }

	    if (seq == null) {
		PreparedStatement get_biosequence = conn.prepareStatement("select biosequence_id, molecule " +
									  "from   biosequence " +
									  "where  bioentry_id = ?");
		get_biosequence.setInt(1, bioentry_id);
		rs = get_biosequence.executeQuery();
		if (rs.next()) {
		    int biosequence_id = rs.getInt(1);
		    String molecule = rs.getString(2);
		    seq = new BioSQLSequence(this, id, bioentry_id, biosequence_id, molecule);
		}
		get_biosequence.close();
	    }

	    if (seq == null && isAssemblySupported()) {
		PreparedStatement get_assembly = conn.prepareStatement("select assembly_id, length, molecule " + 
								       "from   assembly " +
								       "where  bioentry_id = ?");
		get_assembly.setInt(1, bioentry_id);
		rs = get_assembly.executeQuery();
		if (rs.next()) {
		    int assembly_id = rs.getInt(1);
		    int length = rs.getInt(2);
		    String molecule = rs.getString(3);
		    seq = new BioSQLAssembly(this, id, bioentry_id, assembly_id, molecule, length);
		}
		get_assembly.close();
	    }

	    pool.putConnection(conn);

	    if (seq != null) {
		outstandingSequences.put(id, seq);
		return seq;
	    }
	} catch (SQLException ex) {
	    throw new BioException(ex, "Error accessing BioSQL tables");
	}

	throw new BioException("BioEntry " + id + " has unknown sequence type");
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
	    ResultSet rs = st.executeQuery("select bioentry.accession from bioentry");
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

    int persistFeature(Connection conn,
		       int bioentry_id,
		       Feature f,
		       int parent_id)
	throws BioException, SQLException
    {
	int id = -1;
	boolean locationWritten = false;

	if (isSPASupported()) {
	    if (f.getLocation().isContiguous()) {
		Location loc = f.getLocation();

		PreparedStatement add_feature = conn.prepareStatement(
		        "select create_seqfeature_onespan(?, ?, ?, ?, ?, ?)"
		);
		add_feature.setInt(1, bioentry_id);
		add_feature.setString(2, f.getType());
		add_feature.setString(3, f.getSource());
		add_feature.setInt(4, loc.getMin());
		add_feature.setInt(5, loc.getMax());
		if (f instanceof StrandedFeature) {
		    StrandedFeature.Strand s = ((StrandedFeature) f).getStrand();
		    if (s == StrandedFeature.POSITIVE) {
			add_feature.setInt(6, 1);
		    } else if (s== StrandedFeature.NEGATIVE) {
			add_feature.setInt(6, -1);
		    } else {
			add_feature.setInt(6, 0);
		    }
		} else {
		    add_feature.setInt(6, 0);
		}
		ResultSet rs = add_feature.executeQuery();
		if (rs.next()) {
		    id = rs.getInt(1);
		}
		add_feature.close();

		locationWritten = true;
	    } else {
		PreparedStatement add_feature = conn.prepareStatement(
		        "select create_seqfeature(?, ?, ?)"
		);
		add_feature.setInt(1, bioentry_id);
		add_feature.setString(2, f.getType());
		add_feature.setString(3, f.getSource());
		ResultSet rs = add_feature.executeQuery();
		if (rs.next()) {
		    id = rs.getInt(1);
		}
		add_feature.close();
	    }
	} else {
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

	    id = getDBHelper().getInsertID(conn, "seqfeature", "seqfeature_id"); 
	}

	if (!locationWritten) {
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
	} 
	
	// 
	// Persist anything in the annotation bundle, as well.
	//	

	for (Iterator ai = f.getAnnotation().asMap().entrySet().iterator(); ai.hasNext(); ) {
	    Map.Entry akv = (Map.Entry) ai.next();
	    persistProperty(conn, id, akv.getKey(), akv.getValue(), false);
	}

	//
	// Persist link to parent
	//
	
	if (parent_id >= 0) {
	    PreparedStatement add_hierarchy = conn.prepareStatement(
		"insert into seqfeature_hierarchy "+
		"       (parent, child) " +
		"values (?, ?)"
		);
	    add_hierarchy.setInt(1, parent_id);
	    add_hierarchy.setInt(2, id);
	    add_hierarchy.executeUpdate();
	    add_hierarchy.close();
	}

	return id;
    }
			       
    void persistProperty(Connection conn,
			 int feature_id,
			 Object key,
			 Object value,
			 boolean removeFirst)
        throws SQLException
    {
	String keyString = key.toString();
	
	if (removeFirst) {
	    int id = intern_seqfeature_qualifier(conn, keyString);
	    PreparedStatement remove_old_value = conn.prepareStatement("delete from seqfeature_qualifier_value " +
								       " where seqfeature_id = ? and seqfeature_qualifier_id = ?");
	    remove_old_value.setInt(1, feature_id);
	    remove_old_value.setInt(2, id);
	    remove_old_value.executeUpdate();
	    remove_old_value.close();
	}
	
	PreparedStatement insert_new;
	if (isSPASupported()) {
	    insert_new= conn.prepareStatement("insert into seqfeature_qualifier_value " +
                                              "       (seqfeature_id, seqfeature_qualifier_id, seqfeature_qualifier_rank, qualifier_value) " +
					      "values (?, intern_seqfeature_qualifier( ? ), ?, ?)");
	    insert_new.setString(2, keyString);
	} else {
	    insert_new= conn.prepareStatement("insert into seqfeature_qualifier_value " +
                                              "       (seqfeature_id, seqfeature_qualifier_id, seqfeature_qualifier_rank, qualifier_value) " +
					      "values (?, ?, ?, ?)");
	    insert_new.setInt(2, intern_seqfeature_qualifier(conn, keyString));
	}

	insert_new.setInt(1, feature_id);	
	if (value instanceof Collection) {
	    int cnt = 0;
	    for (Iterator i = ((Collection) value).iterator(); i.hasNext(); ) {
		insert_new.setInt(3, ++cnt);
		insert_new.setString(4, i.next().toString());
		insert_new.executeUpdate();
	    }
	} else {
	    insert_new.setInt(3, 1);
	    insert_new.setString(4, value.toString());
	    insert_new.executeUpdate();
	}
	insert_new.close();
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

    int intern_seqfeature_qualifier(Connection conn, String s)
        throws SQLException
    {
	PreparedStatement get = conn.prepareStatement("select seqfeature_qualifier_id from seqfeature_qualifier where qualifier_name = ?");
	get.setString(1, s);
	ResultSet rs = get.executeQuery();
	if (rs.next()) {
	    int id = rs.getInt(1);
	    get.close();
	    return id;
	}
	get.close();

	PreparedStatement insert = conn.prepareStatement("insert into seqfeature_qualifier (qualifier_name) values ( ? )");
	insert.setString(1, s);
	insert.executeUpdate();
	insert.close();

	int id = getDBHelper().getInsertID(conn, "seqfeature_qualifier", "seqfeature_qualifier_id");
	return id;		      
    }    

    private boolean hierarchyChecked = false;
    private boolean hierarchySupported = false;

    boolean isHierarchySupported() {
	if (!hierarchyChecked) {
	    try {
		Connection conn = pool.takeConnection();
		PreparedStatement ps = conn.prepareStatement("select * from seqfeature_hierarchy limit 1");
		try {
		    ps.executeQuery();
		    hierarchySupported = true;
		} catch (SQLException ex) {
		    hierarchySupported = false;
		}
		ps.close();
		pool.putConnection(conn);
	    } catch (SQLException ex) {
		throw new BioRuntimeException(ex);
	    }
	    hierarchyChecked = true;
	}

	return hierarchySupported;
    }

    private boolean assemblyChecked = false;
    private boolean assemblySupported = false;

    boolean isAssemblySupported() {
	if (!assemblyChecked) {
	    try {
		Connection conn = pool.takeConnection();
		PreparedStatement ps = conn.prepareStatement("select * from assembly limit 1");
		try {
		    ps.executeQuery();
		    assemblySupported = true;
		} catch (SQLException ex) {
		    assemblySupported = false;
		}
		ps.close();
		pool.putConnection(conn);
	    } catch (SQLException ex) {
		throw new BioRuntimeException(ex);
	    }
	    assemblyChecked = true;
	}

	return assemblySupported;
    }

    private boolean spaChecked = false;
    private boolean spaSupported = false;

    boolean isSPASupported() {
	if (!spaChecked) {
	    try {
		spaSupported = false;
		Connection conn = pool.takeConnection();
		PreparedStatement ps = conn.prepareStatement("select biosql_accelerators_level()");
		try {
		    ResultSet rs = ps.executeQuery();
		    if (rs.next()) {
			int level = rs.getInt(1);
			if (level >= 1) {
			    spaSupported = true;
			    System.err.println("*** Accelerators present in the database: level " + level);
			}
		    }
		} catch (SQLException ex) {
		}
		ps.close();
		pool.putConnection(conn);

		spaChecked = true;
	    } catch (SQLException ex) {
		throw new BioRuntimeException(ex);
	    }	    
	}

	return spaSupported;
    }
}
