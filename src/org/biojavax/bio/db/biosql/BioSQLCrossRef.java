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
 
 * BioSQLNamespace.java
 
 *
 
 * Created on June 15, 2005, 6:04 PM
 
 */



package org.biojavax.bio.db.biosql;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import java.util.HashMap;
import java.util.Iterator;

import java.util.Map;
import java.util.Set;
import org.biojavax.CrossRef;
import org.biojavax.bio.db.Persistent;
import org.biojavax.bio.db.PersistentBioDB;
import org.biojavax.bio.db.PersistentComparableOntology;
import org.biojavax.bio.db.PersistentComparableTerm;
import org.biojavax.bio.db.PersistentCrossRef;




/**
 *
 * A basic Namespace implemenation.
 *
 *
 *
 * Equality is based on the name of the namespace.
 *
 *
 *
 * @author Mark Schreiber
 *
 */

public class BioSQLCrossRef extends PersistentCrossRef {
    
    
    
    /**
     *
     * Wraps a dbxref in a persistence wrapper.
     * @param db the database this dbxref lives in.
     * @param dbxref the taxon to dbxref.
     */
    
    private BioSQLCrossRef(PersistentBioDB db, CrossRef dbxref) {
        
        super(db, dbxref);
        
    }
    
    
    
    /**
     *
     * Wraps a dbxref in a persistence wrapper.
     * @param db the database this dbxref lives in.
     * @param dbname the dbname of this dbxref.
     * @param accession the accession of this dbxref.
     * @param version the version of this dbxref.
     */
    
    private BioSQLCrossRef(PersistentBioDB db, String dbname, String accession, int version) {
        
        super(db, dbname, accession, version);
        
    }
    
    
    
    /** Singleton map */
    
    private static Map singletons;
    
    /**
     *
     * Singleton constructor.
     * @return the persistent version.
     * @param db the database this dbxref lives in.
     * @param dbxref the dbxref to wrap and make persistent.
     */
    
    public static PersistentCrossRef getInstance(PersistentBioDB db, CrossRef dbxref) {
        
        String key = dbxref.getDbname()+"___"+dbxref.getAccession()+"___"+dbxref.getVersion();
        
        if (dbxref instanceof BioSQLCrossRef) return (BioSQLCrossRef)dbxref;
        
        synchronized(singletons) {
            
            if (singletons==null) singletons = new HashMap();
            
            if (!singletons.containsKey(key)) singletons.put(key,new BioSQLCrossRef(db, dbxref));
            
        }
        
        return (BioSQLCrossRef)singletons.get(key);
        
    }
    
    /**
     *
     * Singleton constructor.
     * @return the persistent version.
     * @param db the database this dbxref lives in.
     * @param dbname the dbname of this dbxref.
     * @param accession the accession of this dbxref.
     * @param version the version of this dbxref.
     */
    
    public static PersistentCrossRef getInstance(PersistentBioDB db, String dbname, String accession, int version) {
        
        String key = dbname+"___"+accession+"___"+version;
        
        synchronized(singletons) {
            
            if (singletons==null) singletons = new HashMap();
            
            if (!singletons.containsKey(key)) singletons.put(key,new BioSQLCrossRef(db, dbname, accession, version));
            
        }
        
        return (BioSQLCrossRef)singletons.get(key);
        
    }
    
    /**
     *
     * Singleton constructor.
     * @return the persistent version.
     * @param uid the uid to load from the database.
     */
    
    public static PersistentCrossRef getInstanceByUID(PersistentBioDB db, int uid) throws SQLException {
        
        // attempt to load from UID, or unique key if UID not found
        
        String sql =
                "select    dbname, accession, version "+
                "from      dbxref "+
                "where     dbxref_id = ? ";
        Connection c = db.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        PersistentCrossRef xr = null;
        try {
            ps = c.prepareStatement(sql);
            ps.setInt(1, uid);
            ps.execute();
            rs = ps.getResultSet();
            if (rs.next()) {
                xr = (PersistentCrossRef)getInstance(db, rs.getString(1), rs.getString(2), rs.getObject(3)==null?Persistent.NULL_INTEGER:rs.getInt(3));
                xr.setUid(uid);
            } else xr= null;
        } catch (SQLException e) {
            throw e;
        } finally {
            if (rs!=null) rs.close();
            if (ps!=null) ps.close();
            db.releaseConnection(c);
        }
        
        return xr;
    }
    
    
    
    /**
     *
     * Stores this object and returns the persistent object stored. If it has no UID,
     *
     * attempt to load it. If found, assign. Else, create it and assign the UID.
     * @return the persisted object.
     * @param vars some extra parameters if required.
     * @throws java.lang.Exception in case of error.
     */
    
    public synchronized Persistent store(Object[] vars) throws Exception {
        
        if (this.getStatus()==Persistent.DELETED) throw new SQLException("Object has been previously deleted");
        
        // vars is ignored here as namespaces don't have ranks or parent objects
        
        if (this.getUid() == Persistent.UID_UNKNOWN) {
            
            // check unique key fields for existence
            
            // if it does, then load the UID from the existing record and assign to this
            
            String sql =
                    "select     dbxref_id " +
                    "from       dbxref " +
                    "where      dbname = ? " +
                    "and        accession = ? " +
                    "and        version = ? ";
            PersistentBioDB db = this.getDB();
            Connection c = db.getConnection();
            PreparedStatement ps = null;
            ResultSet rs = null;
            boolean success = false;
            try {
                ps = c.prepareStatement(sql);
                ps.setString(1, this.getDbname());
                ps.setString(2, this.getAccession());
                if (this.getVersion()==Persistent.NULL_INTEGER) ps.setNull(3,Types.INTEGER);
                else ps.setInt(3,this.getVersion());
                ps.execute();
                rs = ps.getResultSet();
                if (rs.next()) {
                    this.setUid(rs.getInt(1)); // get existing UID
                }
                rs.close();
                ps.close();
                rs = null;
                ps = null;
                if (this.getUid()==Persistent.UID_UNKNOWN) {
                    // create a new object using new UID and unique key fields
                    
                    if (db.autoAllocatedUids()) {
                        // Auto-allocate UID and retrieve afterwards
                        
                        sql =
                                "insert into dbxref " +
                                "   (dbname, accession, version) " +
                                "values " +
                                "   (?,?,?) ";
                        ps = c.prepareStatement(sql);
                        ps.setString(1, this.getDbname());
                        ps.setString(2, this.getAccession());
                        if (this.getVersion()==Persistent.NULL_INTEGER) ps.setNull(3,Types.INTEGER);
                        else ps.setInt(3,this.getVersion());
                        ps.executeUpdate();
                        
                        this.setUid(db.getAutoAllocatedUid(c));
                    } else {
                        // Pre-allocate UID
                        int uid = db.getPreAllocatedUid("dbxref");
                        
                        sql =
                                "insert into dbxref " +
                                "   (dbxref_id, dbname, accession, version) " +
                                "values " +
                                "   (?, ?, ?, ?) ";
                        ps = c.prepareStatement(sql);
                        ps.setInt(1, uid);
                        ps.setString(2, this.getDbname());
                        ps.setString(3, this.getAccession());
                        if (this.getVersion()==Persistent.NULL_INTEGER) ps.setNull(4,Types.INTEGER);
                        else ps.setInt(4,this.getVersion());
                        ps.executeUpdate();
                        
                        this.setUid(uid);
                    }
                }
                success = true;
            } catch (SQLException e) {
                success = false;
                throw e;
            } finally {
                if (rs!=null) rs.close();
                if (ps!=null) ps.close();
                if (!db.respectsTransactions()) {
                    if (success) c.commit();
                    else c.rollback();
                }
                db.releaseConnection(c);
            }
            
            // set MODIFIED status
            
            this.setStatus(Persistent.MODIFIED);
            
        }
        
        if (this.getStatus() == Persistent.MODIFIED) {
            
            // do an update by UID - actually irrelevant as we have no mutable fields
            
            String dsql =
                    "delete from dbxref_qualifier_value " +
                    "where dbxref_id = ? " +
                    "and rank = ? ";
            String isql =
                    "insert into dbxref_qualifier_value " +
                    "   (dbxref_id, term_id, rank, value) " +
                    "values " +
                    "   (?,?,?,?) ";
            PersistentBioDB db = this.getDB();
            Connection c = db.getConnection();
            PreparedStatement dps = null;
            PreparedStatement ips = null;
            boolean success = false;
            try {
                dps = c.prepareStatement(dsql);
                ips = c.prepareStatement(isql);
                
                Set alteredTerms = this.getAlteredTerms();
                
                for (Iterator i = alteredTerms.iterator(); i.hasNext(); ) {
                    int index = ((Integer)i.next()).intValue();
                    PersistentComparableTerm t = (PersistentComparableTerm)this.getTerm(index);
                    ((PersistentComparableOntology)t.getOntology()).store(vars); // make sure the term exists!
                    // delete this position first - it'll get recreated if this is an update
                    dps.setInt(1,this.getUid());
                    dps.setInt(2, index);
                    dps.executeUpdate();
                    if (t!=null) {
                        // create the position (equiv. to both inserts and updates)
                        ips.setInt(1,this.getUid());
                        ips.setInt(2,t.getUid());
                        ips.setInt(3,index);
                        ips.setString(4,this.getTermValue(index));
                    }
                }
                
                success = true;
            } catch (SQLException e) {
                success = false;
                throw e;
            } finally {
                if (dps!=null) dps.close();
                if (ips!=null) ips.close();
                if (!db.respectsTransactions()) {
                    if (success) c.commit();
                    else c.rollback();
                }
                db.releaseConnection(c);
            }
            
            // set UNMODIFIED status
            
            this.setStatus(Persistent.UNMODIFIED);
            
        }
        
        
        
        return this;
        
    }
    
    
    
    /**
     *
     * Removes this object from the underlying database based on its UID.
     * @return True if the object was found and removed, false if not.
     * @param vars some extra parameters if required.
     * @throws java.lang.Exception in case of error.
     */
    
    public synchronized boolean remove(Object[] vars) throws Exception {
        
        if (this.getStatus()==Persistent.DELETED) throw new SQLException("Object has been previously deleted");
        
        if (this.getUid()==Persistent.UID_UNKNOWN) throw new SQLException("UID has not been set");
        
        
        // vars is ignored here as namespaces don't have ranks or parent objects
        
        
        // delete statements go here - taxon_name will recurse so we don't need to do that too.
        
        String sql =
                "delete from dbxref "+
                "where     dbxref_id = ? ";
        PersistentBioDB db = this.getDB();
        Connection c = db.getConnection();
        PreparedStatement ps = null;
        boolean success = false;
        try {
            ps = c.prepareStatement(sql);
            ps.setInt(1, this.getUid());
            ps.executeUpdate();
            success = true;
        } catch (SQLException e) {
            success = false;
            throw e;
        } finally {
            if (ps!=null) ps.close();
            if (!db.respectsTransactions()) {
                if (success) c.commit();
                else c.rollback();
            }
            db.releaseConnection(c);
        }
        
        
        this.setStatus(Persistent.DELETED);
        
        this.setUid(Persistent.UID_UNKNOWN);
        
        return true;
        
    }
    
    
    
    /**
     *
     * Removes this object from the underlying database based on its UID.
     * @return True if the object was found and removed, false if not.
     * @param vars some extra parameters if required.
     * @throws java.lang.Exception in case of error.
     */
    
    public synchronized Persistent load(Object[] vars) throws Exception {
        
        if (this.getStatus()==Persistent.DELETED) throw new SQLException("Object has been previously deleted");
        
        
        
        // vars is ignored here as namespaces don't have ranks or parent objects
        
        
        
        // attempt to load from UID, or unique key if UID not found
        
        // actually we have no fields that aren't already loaded, so just load qualifiers
        
        boolean found = false;
        
        String sql =
                "select    term_id, rank, value " +
                "from      dbxref_qualifier_value "+
                "where     dbxref_id = ? ";
        PersistentBioDB db = this.getDB();
        Connection c = db.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            // Load the terms
            ps.setInt(1,this.getUid());
            ps.execute();
            rs = ps.getResultSet();
            while (rs.next()) {
                PersistentComparableTerm t = BioSQLComparableTerm.getInstanceByUID(db,rs.getInt(1));
                int index = rs.getInt(2);
                String value = rs.getString(3);
                this.setTerm(t,value,index);
            }
            found = true;
        } catch (Exception e) {
            throw e;
        } finally {
            if (rs!=null) rs.close();
            if (ps!=null) ps.close();
            db.releaseConnection(c);
        }
        
        // if found, set status = UNMODIFIED and return this
        
        // else return null
        
        if (found) {
            this.setStatus(Persistent.UNMODIFIED);
            return this;
        } else return null;
        
    }
    
    
    
}

