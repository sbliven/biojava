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

/*
 * BioSQLBioDB.java
 *
 * Created on July 11, 2005, 4:49 PM
 */

package org.biojavax.bio.db.biosql;
import java.lang.reflect.Method;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import javax.sql.DataSource;
import org.biojava.bio.BioRuntimeException;
import org.biojavax.CrossRef;
import org.biojavax.DocumentReference;
import org.biojavax.LocatedDocumentReference;
import org.biojavax.Namespace;
import org.biojavax.bio.BioEntryRelationship;
import org.biojavax.bio.db.Persistent;
import org.biojavax.bio.db.PersistentBioDB;
import org.biojavax.bio.db.PersistentBioEntry;
import org.biojavax.bio.db.PersistentComparableOntology;
import org.biojavax.bio.db.PersistentNamespace;
import org.biojavax.bio.taxa.NCBITaxon;
import org.biojavax.ontology.ComparableOntology;
import org.biojavax.ontology.ComparableTerm;
import org.biojavax.ontology.ComparableTriple;



/**
 * Represents a BioSQL schema based database.
 * @author Richard Holland
 * @author Len Trigg (from re-used parts of DBHelper)
 * @author Eric Haugen (from re-used parts of DBHelper)
 */
public interface BioSQLBioDB extends PersistentBioDB {
    
    /**
     * Constructs new BioSQLBioDB database instances, based on the connection
     * given. ie. if you give it an Oracle JDBC connection or DataSource, it
     * will return an OracleBioSQLBioDB instance, etc.
     */
    public static class Factory {
        /**
         * Given a connection, return a BioSQLBioDB instance.
         * @param conn a database connection.
         * @return an object talking to that database.
         */
        public static BioSQLBioDB getInstance(Connection conn) {
            try {
                String dbType = conn.getMetaData().getURL();
                if (dbType.startsWith("jdbc:")) {
                    dbType = dbType.substring(5);
                }
                if (!Character.isLetter(dbType.charAt(0))) {
                    throw new IllegalArgumentException("URL must start with a letter: " + dbType);
                }
                int colon = dbType.indexOf(':');
                if (colon > 0) {
                    String protocol = dbType.substring(0, colon);
                    if (protocol.indexOf("mysql") >= 0) {
                        // Accept any string containing `mysql', to cope with Caucho driver
                        return new MySQLBioSQLBioDB(conn);
                        //} else if (protocol.equals("postgresql")) {
                        //    return new PostgreSQLBioSQLBioDB(conn);
                    } else if (protocol.equals("oracle")) {
                        return new OracleBioSQLBioDB(conn);
                        //} else if (protocol.equals("hsqldb")) {
                        //    return new HypersonicBioSQLBioDB(conn);
                    }
                }
            } catch (SQLException se) {
                throw new RuntimeException("Failed to connect to database.",se);
            }
            throw new IllegalArgumentException("Type of database to connect to is unknown.");
        }
        /**
         * Given a datasource, return a BioSQLBioDB instance.
         * @param ds a database datasource.
         * @return an object talking to that database.
         */
        public static BioSQLBioDB getInstance(DataSource ds) {
            try {
                String dbType = ds.getConnection().getMetaData().getURL();
                if (dbType.startsWith("jdbc:")) {
                    dbType = dbType.substring(5);
                }
                if (!Character.isLetter(dbType.charAt(0))) {
                    throw new IllegalArgumentException("URL must start with a letter: " + dbType);
                }
                int colon = dbType.indexOf(':');
                if (colon > 0) {
                    String protocol = dbType.substring(0, colon);
                    if (protocol.indexOf("mysql") >= 0) {
                        // Accept any string containing `mysql', to cope with Caucho driver
                        return new MySQLBioSQLBioDB(ds);
                        //} else if (protocol.equals("postgresql")) {
                        //    return new PostgreSQLBioSQLBioDB(ds);
                    } else if (protocol.equals("oracle")) {
                        return new OracleBioSQLBioDB(ds);
                        //} else if (protocol.equals("hsqldb")) {
                        //    return new HypersonicBioSQLBioDB(ds);
                    }
                }
            } catch (SQLException se) {
                throw new RuntimeException("Failed to connect to database.",se);
            }
            throw new IllegalArgumentException("Type of database to connect to is unknown.");
        }
    }
    
    /**
     * A reference generic implementation that knows how to talk to BioSQL.
     */
    public abstract class Impl implements BioSQLBioDB {
        private Connection conn;
        private DataSource ds;
        private boolean respectsTransactions;
        private Namespace readns;
        private Set namespaces;
        protected Impl(Connection conn) {
            this.conn = conn;
            this.respectsTransactions = true;
            this.readns = null;
        }
        protected Impl(DataSource ds) {
            this.ds = ds;
            this.respectsTransactions = false;
            this.readns = null;
        }
        public Connection getConnection() throws SQLException {
            return ds.getConnection();
        }
        public void releaseConnection(Connection conn) throws SQLException {
            if (this.ds!=null) conn.close();
        }
        public boolean respectsTransactions() {
            return this.respectsTransactions;
        }
        public abstract boolean autoAllocatedUids();
        public abstract int getAutoAllocatedUid(Connection c) throws SQLException;
        public abstract int getPreAllocatedUid(String table) throws SQLException;
        public Set loadNamespaceNames() throws SQLException {
            // use SQL to locate all the names
            Set names = new HashSet();
            String sql =
                    "select    name " +
                    "from      biodatabase ";
            Connection c = this.getConnection();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = c.prepareStatement(sql);
                ps.execute();
                rs = ps.getResultSet();
                while (rs.next()) names.add(rs.getString(1));
            } catch (SQLException e) {
                throw e;
            } finally {
                if (rs!=null) rs.close();
                if (ps!=null) ps.close();
                this.releaseConnection(c);
            }
            // return them as a set
            return names;
        }
        public PersistentNamespace loadNamespace(String name) throws Exception {
            // name is all we need to identify it uniquely, so no SQL required!
            PersistentNamespace ns = BioSQLNamespace.getInstance(this, name);
            return (PersistentNamespace)ns.load(null);
        }
        public Set loadOntologyNames() throws SQLException {
            // use SQL to locate all the names
            Set names = new HashSet();
            String sql =
                    "select    name " +
                    "from      ontology ";
            Connection c = this.getConnection();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = c.prepareStatement(sql);
                ps.execute();
                rs = ps.getResultSet();
                while (rs.next()) names.add(rs.getString(1));
            } catch (SQLException e) {
                throw e;
            } finally {
                if (rs!=null) rs.close();
                if (ps!=null) ps.close();
                this.releaseConnection(c);
            }
            // return them as a set
            return names;
        }
        public PersistentComparableOntology loadOntology(String name) throws Exception {
            // name is all we need to identify it uniquely, so no SQL required!
            PersistentComparableOntology co = BioSQLComparableOntology.getInstance(this, name);
            return (PersistentComparableOntology)co.load(null);
        }
        public void setNamespace(Namespace ns) {
            this.readns = ns;
        }
        public Set loadSequenceUIDs() throws SQLException {
            PersistentNamespace pns = null;
            if (this.readns!=null) pns = (PersistentNamespace)this.convert(this.readns);
            // use SQL to locate all the Integer UIDs in the current namespace
            Set suids = new HashSet();
            String sql =
                    "select    bioentry_id " +
                    "from      bioentry ";
            if (pns!=null) sql = sql + "where biodatabase_id = ? ";
            Connection c = this.getConnection();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = c.prepareStatement(sql);
                if (this.readns!=null) ps.setInt(1,pns.getUid());
                ps.execute();
                rs = ps.getResultSet();
                while (rs.next()) suids.add(Integer.valueOf(rs.getInt(1)));
            } catch (SQLException e) {
                throw e;
            } finally {
                if (rs!=null) rs.close();
                if (ps!=null) ps.close();
                this.releaseConnection(c);
            }
            // return them as a set
            return suids;
        }
        public PersistentBioEntry loadSequenceByUID(int UID) throws SQLException, NullPointerException {
            // use SQL to locate the set of details, including the UID, in the current namespace
            // construct a SimpleBioEntry object using SimpleBioEntryBuilder
            // wrap it in a BioSQLBioEntry object with the UID set
            return null;
        }
        public PersistentBioEntry loadSequence(String name, String accession, int version) throws SQLException, NullPointerException {
            // use SQL to locate the FIRST set of details, including the UID, in the current namespace
            // construct a SimpleBioEntry object using SimpleBioEntryBuilder
            // wrap it in a BioSQLBioEntry object with the UID set
            return null;
        }
        public Persistent convert(Object o) throws IllegalArgumentException {
            // if-else wrapper
            if (o==null) return null;
            //else if (o instanceof BioEntry) return BioSQLBioEntry.getInstance(this,(BioEntry)o);
            //else if (o instanceof BioEntryFeature) return BioSQLBioEntryFeature.getInstance(this,(BioEntryFeature)o);
            else if (o instanceof BioEntryRelationship) return BioSQLBioEntryRelationship.getInstance(this,(BioEntryRelationship)o);
            else if (o instanceof ComparableOntology) return BioSQLComparableOntology.getInstance(this,(ComparableOntology)o);
            else if (o instanceof ComparableTerm) return BioSQLComparableTerm.getInstance(this,(ComparableTerm)o);
            else if (o instanceof ComparableTriple) return BioSQLComparableTriple.getInstance(this,(ComparableTriple)o);
            else if (o instanceof CrossRef) return BioSQLCrossRef.getInstance(this,(CrossRef)o);
            else if (o instanceof DocumentReference) return BioSQLDocumentReference.getInstance(this,(DocumentReference)o);
            else if (o instanceof LocatedDocumentReference) return BioSQLLocatedDocumentReference.getInstance(this,(LocatedDocumentReference)o);
            else if (o instanceof NCBITaxon) return BioSQLNCBITaxon.getInstance(this,(NCBITaxon)o);
            else if (o instanceof Namespace) return BioSQLNamespace.getInstance(this,(Namespace)o);
            else throw new IllegalArgumentException("Unable to convert object of type "+o.getClass());
        }
    }
    
    public class MySQLBioSQLBioDB extends Impl {
        protected MySQLBioSQLBioDB(Connection conn) {super(conn);}
        protected MySQLBioSQLBioDB(DataSource ds) {super(ds);}
        
        public boolean autoAllocatedUids() { return true; }
        public int getAutoAllocatedUid(Connection c) throws SQLException {
            int uid = Persistent.UID_UNKNOWN;
            String sql =
                    "select last_insert_id()";
            PreparedStatement ps = c.prepareStatement(sql);
            ps.execute();
            ResultSet rs = ps.getResultSet();
            if (rs.next()) {
                uid = rs.getInt(1);
            } else throw new SQLException("Failed to read result from sequence");
            return uid;
        }
        public int getPreAllocatedUid(String table) throws SQLException {
            throw new RuntimeException("Not implemented.");
        }
    }
    
    public class OracleBioSQLBioDB extends Impl {
        protected OracleBioSQLBioDB(Connection conn) {super(conn);}
        protected OracleBioSQLBioDB(DataSource ds) {super(ds);}
        
        public boolean autoAllocatedUids() { return false; }
        public int getAutoAllocatedUid(Connection c) throws SQLException {
            throw new RuntimeException("Not implemented.");
        }
        public int getPreAllocatedUid(String table) throws SQLException {
            int uid = Persistent.UID_UNKNOWN;
            String sql =
                    "select "+table+"_pk_seq.nextval from dual";
            PersistentBioDB db = this;
            Connection c = db.getConnection();
            PreparedStatement ps = null;
            ResultSet rs = null;
            boolean success = false;
            try {
                ps = c.prepareStatement(sql);
                ps.execute();
                rs = ps.getResultSet();
                if (rs.next()) {
                    uid = rs.getInt(1);
                } else throw new SQLException("Failed to read result from sequence");
            } catch (SQLException e) {
                throw e;
            } finally {
                if (rs!=null) rs.close();
                if (ps!=null) ps.close();
                db.releaseConnection(c);
            }
            return uid;
        }
        
        /*
         * Use this to retrieve a CLOB value.
         * @param rs the ResultSet to retrieve the CLOB from.
         * @param column the number of the column in the ResultSet that the CLOB lives in.
         * @return String value of the CLOB.
         */
        public String readClob(ResultSet rs, int column) {
            try {
                Clob seqclob = rs.getClob(column);
                StringBuffer buf = new StringBuffer();
                int bufSize = 1024;
                long start = 1L;
                long remain = seqclob.length();
                while (remain>0L) {
                    if (bufSize>remain) bufSize=(int)remain;
                    buf.append(seqclob.getSubString(start,bufSize));
                    start+=bufSize;
                    remain-=bufSize;
                }
                return buf.toString().trim();
            } catch (Exception ex) {
                throw new BioRuntimeException(ex);
            }
        }
        /*
         * Use this to set a CLOB value. OJDBC version 9i must be on the ClassPath.
         * @param rs the ResultSet to retrieve the CLOB from.
         * @param column the number of the column in the ResultSet that the CLOB lives in.
         * @param the value to set to the CLOB.
         */
        public void writeClob(ResultSet rs, int column, String value) {
            try {
                // Can't use oracle.sql.CLOB directly as we'd need it at compile time otherwise.
                Class clob = Class.forName("oracle.sql.CLOB");
                Method putString = clob.getDeclaredMethod("putString",new Class[]{long.class,String.class});
                // Only get here if we have some data to write.
                if (value==null) value=""; // To stop null pointer exceptions. End result is the same.
                putString.invoke(rs.getClob(column), new Object[]{new Long(1L),value});
            } catch (Exception ex) {
                throw new BioRuntimeException(ex);
            }
        }
    }
    
}
