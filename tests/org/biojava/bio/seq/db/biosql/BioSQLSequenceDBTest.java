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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.biojava.bio.seq.db.AbstractSequenceDBTest;
import org.biojava.bio.seq.db.SequenceDB;

/**
 * Really rudimentary test case for biosql sequencedb. We could make
 * this an abstract class and have a subclass for each supported
 * RDBMS.
 * 
 * @author Len Trigg
 */
public class BioSQLSequenceDBTest extends AbstractSequenceDBTest {
    
    static final boolean HAVE_DB;
    static {
        boolean haveDB = false;
        try {
            Class.forName("org.hsqldb.jdbcDriver");
            haveDB = true;
        } catch (ClassNotFoundException e) {
            System.err.println("No hsqldb driver found.");
        }
        HAVE_DB = haveDB;
    }

    protected static String DB_URL = "jdbc:hsqldb:.";
    protected static String DB_USER = "sa";
    protected static String DB_PW = "";
    protected static String DB_BIODB = "biosql";
    protected static String DB_CREATE_RESOURCE = "files/biosqldb-hsqldb.sql";



    //
    // The rest of this should in theory be independent of the particular RDBMS
    //

    private static String CREATE_SQL = null;
    private static String DROP_SQL = null;
    private static String[] TABLES = new String[] {
        "anncomment",
        "biodatabase",
        "bioentry",
        "bioentry_dbxref",
        "bioentry_path",
        "bioentry_qualifier_value",
        "bioentry_reference",
        "bioentry_relationship",
        "biosequence", 
        "dbxref",
        "dbxref_qualifier_value",
        "location",
        "location_qualifier_value",
        "ontology",
        "reference",
        "seqfeature",
        "seqfeature_dbxref",
        "seqfeature_path",
        "seqfeature_qualifier_value",
        "seqfeature_relationship",
        "taxon",
        "taxon_name",
        "term",
        "term_dbxref",
        "term_path",
        "term_relationship",
        "term_synonym",
    };
    

    private Connection mConnection = null;
    
    
    public BioSQLSequenceDBTest(String name) {
        super(name);
    }

    protected SequenceDB getSequenceDB() throws Exception {
        return new BioSQLSequenceDB(DB_URL, DB_USER, DB_PW, DB_BIODB, true);
    }

    public void setUp() throws Exception {
        mConnection = getConnection();
        loadSchema(mConnection);
        super.setUp();
    }

    public void tearDown() throws Exception {
        dropSchema(mConnection);
        mConnection.close();
        mConnection = null;
        super.tearDown();
    }
    
    
    protected static String readAll(BufferedReader br) throws IOException {
        String line;
        StringBuffer sb = new StringBuffer();
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    protected static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PW);
    }
    
    protected static void loadSchema(Connection connection) throws IOException, SQLException {
        if (CREATE_SQL == null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream(DB_CREATE_RESOURCE)));
            CREATE_SQL = readAll(br);
            br.close();
        }
        Statement st = connection.createStatement();
        st.executeQuery(CREATE_SQL);
        st.close();
    }
    

    protected static void dropSchema(Connection connection) throws IOException, SQLException {
        if (DROP_SQL == null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream("files/drop-biosqldb-hsqldb.sql")));
            DROP_SQL = readAll(br);
            br.close();
        }
        Statement st = connection.createStatement();
        st.executeQuery(DROP_SQL);
        st.close();
    }

    
    public void testSchemaSetup() throws Exception {
        Connection connection = getConnection();

        // See if the expected tables are present
        for (int i = 0; i < TABLES.length; i++) {
            Statement st = connection.createStatement();
            assertNotNull("Couldn't access " + TABLES[i], st.executeQuery("SELECT * FROM " + TABLES[i]));
            st.close();
        }

        // Clear database
        dropSchema(connection);
        for (int i = 0; i < TABLES.length; i++) {
            Statement st = connection.createStatement();
            try {
                st.executeQuery("SELECT * FROM " + TABLES[i]);
                fail(TABLES[i] + " still in database");
            } catch (SQLException se) {
                ; // should get here
            }
            st.close();
        }
    
        // should be able to load again
        loadSchema(connection);
        for (int i = 0; i < TABLES.length; i++) {
            Statement st = connection.createStatement();
            assertNotNull("Couldn't access " + TABLES[i], st.executeQuery("SELECT * FROM " + TABLES[i]));
            st.close();
        }
    }


    public static Test suite() {
        return HAVE_DB ? new TestSuite(BioSQLSequenceDBTest.class) : new TestSuite();
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }    
}
