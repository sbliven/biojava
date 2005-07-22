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

import java.util.Map;
import org.biojavax.DocumentReference;

import org.biojavax.bio.db.Persistent;

import org.biojavax.bio.db.PersistentBioDB;
import org.biojavax.bio.db.PersistentCrossRef;
import org.biojavax.bio.db.PersistentDocumentReference;




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

public class BioSQLDocumentReference extends PersistentDocumentReference {
    
    
    
    /**
     *
     * Wraps a DocumentReference in a persistence wrapper.
     * @param db the database this DocumentReference lives in.
     * @param docref the DocumentReference to wrap.
     */
    
    private BioSQLDocumentReference(PersistentBioDB db, DocumentReference docref) {
        
        super(db, docref);
        
    }
    
    
    
    /**
     *
     * Wraps a DocumentReference in a persistence wrapper.
     * @param db the database this DocumentReference lives in.
     * @param authors the authors of this DocumentReference.
     * @param location the location of this DocumentReference.
     */
    
    private BioSQLDocumentReference(PersistentBioDB db, String authors, String location) {
        
        super(db, authors, location);
        
    }
    
    
    
    /** Singleton map */
    
    private static Map singletons;
    
    /**
     *
     * Singleton constructor.
     * @return the persistent version.
     * @param db the database this DocumentReference lives in.
     * @param DocumentReference the DocumentReference to wrap and make persistent.
     */
    
    public static PersistentDocumentReference getInstance(PersistentBioDB db, DocumentReference docref) {
        
        String key = docref.getAuthors()+"___"+docref.getLocation();
        
        if (docref instanceof BioSQLDocumentReference) return (BioSQLDocumentReference)docref;
        
        synchronized(singletons) {
            
            if (singletons==null) singletons = new HashMap();
            
            if (!singletons.containsKey(key)) singletons.put(key,new BioSQLDocumentReference(db, docref));
            
        }
        
        return (BioSQLDocumentReference)singletons.get(key);
        
    }
    
    /**
     *
     * Singleton constructor.
     * @return the persistent version.
     * @param db the database this DocumentReference lives in.
     * @param authors the authors of this DocumentReference.
     * @param location the location of this DocumentReference.
     */
    
    public static PersistentDocumentReference getInstance(PersistentBioDB db, String authors, String location) {
        
        String key = authors+"___"+location;
        
        synchronized(singletons) {
            
            if (singletons==null) singletons = new HashMap();
            
            if (!singletons.containsKey(key)) singletons.put(key,new BioSQLDocumentReference(db, authors, location));
            
        }
        
        return (BioSQLDocumentReference)singletons.get(key);
        
    }
    
    
    /**
     *
     * Singleton constructor.
     * @return the persistent version.
     * @param uid the uid to load from the database.
     */
    
    public static PersistentDocumentReference getInstanceByUID(PersistentBioDB db, int uid) throws SQLException {
        
        // attempt to load from UID, or unique key if UID not found
        
        String sql =
                "select    authors, location "+
                "from      reference "+
                "where     reference_id = ? ";
        Connection c = db.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        PersistentDocumentReference dr = null;
        try {
            ps = c.prepareStatement(sql);
            ps.setInt(1, uid);
            ps.execute();
            rs = ps.getResultSet();
            if (rs.next()) {
                dr = (PersistentDocumentReference)getInstance(db, rs.getString(1), rs.getString(2));
                dr.setUid(uid);
            } else dr = null;
        } catch (SQLException e) {
            throw e;
        } finally {
            if (rs!=null) rs.close();
            if (ps!=null) ps.close();
            db.releaseConnection(c);
        }
        
        return dr;
        
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
                    "select     reference_id " +
                    "from       reference " +
                    "where      authors = ? " +
                    "and        location = ? ";
            PersistentBioDB db = this.getDB();
            Connection c = db.getConnection();
            PreparedStatement ps = null;
            ResultSet rs = null;
            boolean success = false;
            try {
                ps = c.prepareStatement(sql);
                ps.setString(1,this.getAuthors());
                ps.setString(2,this.getLocation());
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
                                "insert into reference" +
                                "   (authors,location) " +
                                "values " +
                                "   (?,?) ";
                        ps = c.prepareStatement(sql);
                        ps.setString(1,this.getAuthors());
                        ps.setString(2,this.getLocation());
                        ps.executeUpdate();
                        
                        this.setUid(db.getAutoAllocatedUid(c));
                    } else {
                        // Pre-allocate UID
                        int uid = db.getPreAllocatedUid("reference");
                        
                        sql =
                                "insert into reference" +
                                "   (reference_id, authors, location) " +
                                "values " +
                                "   (?, ?, ?) ";
                        ps = c.prepareStatement(sql);
                        ps.setInt(1, uid);
                        ps.setString(2,this.getAuthors());
                        ps.setString(3,this.getLocation());
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
            
            // do an update by UID
            
            String sql =
                    "update    reference "+
                    "set       title = ?, "+
                    "          crc = ?, "+
                    "          dbxref_id = ? "+
                    "where     reference_id = ? ";
            PersistentBioDB db = this.getDB();
            PersistentCrossRef xref = (PersistentCrossRef)this.getCrossref();
            if (this.getCrossref()!=null) {
                xref.store(vars); // just in case
            }
            Connection c = db.getConnection();
            PreparedStatement ps = null;
            boolean success = false;
            try {
                ps = c.prepareStatement(sql);
                ps.setString(1, this.getTitle());
                ps.setString(2, this.getCRC());
                if (xref==null) ps.setNull(3,Types.INTEGER);
                else ps.setInt(3, xref.getUid());
                ps.setInt(4, this.getUid());
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
        
        
        // delete statements go here
        
        String sql =
                "delete from reference "+
                "where     reference_id = ? ";
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
        
        boolean found = false;
        
        String sql =
                "select    title, crc, dbxref_id "+
                "from      reference "+
                "where     reference_id = ? ";
        PersistentBioDB db = this.getDB();
        Connection c = db.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = c.prepareStatement(sql);
            ps.setInt(1, this.getUid());
            ps.execute();
            rs = ps.getResultSet();
            if (rs.next()) {
                found = true;
                this.setTitle(rs.getString(1));
                this.setCRC(rs.getString(2));
                int xref_id = rs.getObject(3)==null?Persistent.UID_UNKNOWN:rs.getInt(3);
                if (xref_id!=Persistent.UID_UNKNOWN) this.setCrossref(BioSQLCrossRef.getInstanceByUID(db,xref_id));
                else this.setCrossref(null);
            } else {
                rs.close();
                ps.close();
                ps = null;
                rs = null;
                sql =
                        "select    reference_id, title, crc, dbxref_id " +
                        "from      reference "+
                        "where     authors = ? " +
                        "and       location = ? ";
                ps = c.prepareStatement(sql);
                ps.setString(1,this.getAuthors());
                ps.setString(2,this.getLocation());
                ps.execute();
                rs = ps.getResultSet();
                if (rs.next()) {
                    found = true;
                    this.setUid(rs.getInt(1)); // set our UID to refer to this record
                    this.setTitle(rs.getString(2));
                    this.setCRC(rs.getString(3));
                    int xref_id = rs.getObject(4)==null?Persistent.UID_UNKNOWN:rs.getInt(4);
                    if (xref_id!=Persistent.UID_UNKNOWN) this.setCrossref(BioSQLCrossRef.getInstanceByUID(db,xref_id));
                    else this.setCrossref(null);
                }
            }
            
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

