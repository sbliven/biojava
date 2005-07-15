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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Set;
import javax.sql.DataSource;
import org.biojavax.Namespace;
import org.biojavax.bio.db.*;



/**
 *
 * @author hollandr
 */
public interface BioSQLBioDB extends PersistentBioDB {
    
    /**
     * This method is used to write long strings, ie. CLOBS in Oracle.
     * @param text the string to write
     * @param ps the statement we are writing to
     * @param column the column number to write the string to.
     * @throws SQLException if the write process fails.
     */
    public void writeLongString(String text, PreparedStatement ps, int column) throws SQLException;
    
    /**
     * This method reads long strings, eg. CLOBS in Oracle.
     * @param rs the resultset to read from
     * @param column the column the string lives in
     * @return String the string that has been read.
     * @throws SQLException if the string could not be read.
     */
    public String readLongString(ResultSet rs, int column) throws SQLException;
    
    public class Impl implements BioSQLBioDB {
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
            if (this.conn==null) this.conn = ds.getConnection();
            return this.conn;
        }
        public boolean respectsTransactions() {
            return this.respectsTransactions;
        }
        public Set loadNamespaceNames() throws SQLException {
            // use SQL to locate all the names
            // return them as a set
            return Collections.EMPTY_SET;
        }
        public PersistentNamespace loadNamespace(String name) throws SQLException {
            // name is all we need to identify it uniquely, so no SQL required!
            PersistentNamespace ns = BioSQLNamespace.getInstance(this, name);
            return (PersistentNamespace)ns.load(null);
        }
        public Set loadOntologyNames() {
            // use SQL to locate all the names
            // return them as a set
            return Collections.EMPTY_SET;
        }
        public PersistentComparableOntology loadOntology(String name) throws SQLException {
            // name is all we need to identify it uniquely, so no SQL required!
            // PersistentComparableOntology co = BioSQLComparableOntology.getInstance(this, name);
            // return (PersistentComparableOntology)co.load(null);
            return null;
        }
        public void setNamespace(Namespace ns) {
            this.readns = ns;
        }
        public Set loadSequenceUIDs() throws SQLException {
            // use SQL to locate all the Integer UIDs
            // return them as a set
            return Collections.EMPTY_SET;
        }
        public PersistentBioEntry loadSequenceByUID(int UID) throws SQLException, NullPointerException {
            // use SQL to locate the FIRST set of details, including the UID.
            // construct a SimpleBioEntry object using SimpleBioEntryBuilder
            // wrap it in a BioSQLBioEntry object with the UID set
            return null;
        }
        public PersistentBioEntry loadSequence(String name, String accession, int version) throws SQLException, NullPointerException {
            // use SQL to locate the FIRST set of details, including the UID.
            // construct a SimpleBioEntry object using SimpleBioEntryBuilder
            // wrap it in a BioSQLBioEntry object with the UID set
            return null;
        }
        public Persistent convert(Object o) throws IllegalArgumentException {
            // if-else wrapper
            //if (o instanceof BioEntry) return BioSQLBioEntry.getInstance(this,(BioEntry)o);
            //else if (o instanceof BioEntryFeature) return BioSQLBioEntryFeature.getInstance(this,(BioEntryFeature)o);
            //else if (o instanceof BioEntryRelationship) return BioSQLBioEntryRelationship.getInstance(this,(BioEntryRelationship)o);
            //else if (o instanceof ComparableOntology) return BioSQLComparableOntology.getInstance(this,(ComparableOntology)o);
            //else if (o instanceof ComparableTerm) return BioSQLComparableTerm.getInstance(this,(ComparableTerm)o);
            //else if (o instanceof ComparableTriple) return BioSQLComparableTriple.getInstance(this,(ComparableTriple)o);
            //else if (o instanceof CrossRef) return BioSQLCrossRef.getInstance(this,(CrossRef)o);
            //else if (o instanceof DocumentReference) return BioSQLDocumentReference.getInstance(this,(DocumentReference)o);
            //else if (o instanceof LocatedDocumentReference) return BioSQLLocatedDocumentReference.getInstance(this,(LocatedDocumentReference)o);
            //else if (o instanceof NCBITaxon) return BioSQLNCBITaxon.getInstance(this,(NCBITaxon)o);
            //else if (o instanceof Namespace) return BioSQLNamespace.getInstance(this,(Namespace)o);
            //else throw new IllegalArgumentException("Unable to convert object of type "+o.getClass());
            throw new IllegalArgumentException("Unable to convert object of type "+o.getClass());
        }
        public void writeLongString(String text, PreparedStatement ps, int column) throws SQLException {
            ps.setString(column, text);
        }
        public String readLongString(ResultSet rs, int column) throws SQLException {
            return rs.getString(column);
        }
    }
    
    public class MySQL extends Impl {
        public MySQL(Connection conn) {super(conn);}
        public MySQL(DataSource ds) {super(ds);}
    }
    
    public class Oracle extends Impl {
        public Oracle(Connection conn) {super(conn);}
        public Oracle(DataSource ds) {super(ds);}
        public void writeLongString(String text, PreparedStatement ps, int column) throws SQLException {
            ps.setString(column, text); // Doesn't work on Oracle 9i/10g - FIXME
        }
        public String readLongString(ResultSet rs, int column) throws SQLException {
            return rs.getString(column); // Doesn't work on Oracle 9i/10g - FIXME
        }
    }
    
}
