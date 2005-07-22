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

import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.HashMap;

import java.util.Map;

import org.biojavax.Namespace;

import org.biojavax.bio.db.Persistent;

import org.biojavax.bio.db.PersistentBioDB;

import org.biojavax.bio.db.PersistentNamespace;



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

public class BioSQLNamespace extends PersistentNamespace {
    
    
    
    /**
     *
     * Wraps a namespace in a persistence wrapper.
     * @param db the database this namespace lives in.
     * @param namespace the namespace to wrap.
     */
    
    private BioSQLNamespace(PersistentBioDB db, Namespace namespace) {
        
        super(db, namespace);
        
    }
    
    
    
    /**
     *
     * Wraps a namespace in a persistence wrapper.
     * @param db the database this namespace lives in.
     * @param name the name of this namespace.
     */
    
    private BioSQLNamespace(PersistentBioDB db, String name) {
        
        super(db, name);
        
    }
    
    
    
    /** Singleton map */
    
    private static Map singletons;
    
    /**
     *
     * Singleton constructor.
     * @return the persistent version.
     * @param db the database this namespace lives in.
     * @param namespace the namespace to wrap and make persistent.
     */
    
    public static PersistentNamespace getInstance(PersistentBioDB db, Namespace namespace) {
        
        String key = namespace.getName();
        
        if (namespace instanceof BioSQLNamespace) return (BioSQLNamespace)namespace;
        
        synchronized(singletons) {
            
            if (singletons==null) singletons = new HashMap();
            
            if (!singletons.containsKey(key)) singletons.put(key,new BioSQLNamespace(db, namespace));
            
        }
        
        return (BioSQLNamespace)singletons.get(key);
        
    }
    
    /**
     *
     * Singleton constructor.
     * @return the persistent version.
     * @param db the database this namespace lives in.
     * @param name the name of this namespace.
     */
    
    public static PersistentNamespace getInstance(PersistentBioDB db, String name) {
        
        String key = name;
        
        synchronized(singletons) {
            
            if (singletons==null) singletons = new HashMap();
            
            if (!singletons.containsKey(key)) singletons.put(key,new BioSQLNamespace(db, name));
            
        }
        
        return (BioSQLNamespace)singletons.get(key);
        
    }
    
    /**
     *
     * Singleton constructor.
     * @return the persistent version.
     * @param uid the uid to load from the database.
     */
    
    public static PersistentNamespace getInstanceByUID(PersistentBioDB db, int uid) throws SQLException {
        
        // attempt to load from UID, or unique key if UID not found
        
        String sql =
                "select    name "+
                "from      biodatabase "+
                "where     biodatabase_id = ? ";
        Connection c = db.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        PersistentNamespace ns = null;
        try {
            ps = c.prepareStatement(sql);
            ps.setInt(1, uid);
            ps.execute();
            rs = ps.getResultSet();
            if (rs.next()) {
                ns = (PersistentNamespace)getInstance(db, rs.getString(1));
                ns.setUid(uid);
            } else ns = null;
        } catch (SQLException e) {
            throw e;
        } finally {
            if (rs!=null) rs.close();
            if (ps!=null) ps.close();
            db.releaseConnection(c);
        }
                
        return ns;           
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
                    "select     biodatabase_id " +
                    "from       biodatabase " +
                    "where      name = ? ";
            PersistentBioDB db = this.getDB();
            Connection c = db.getConnection();
            PreparedStatement ps = null;
            ResultSet rs = null;
            boolean success = false;
            try {
                ps = c.prepareStatement(sql);
                ps.setString(1, this.getName());
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
                                "insert into biodatabase" +
                                "   (name) " +
                                "values " +
                                "   (?) ";
                        ps = c.prepareStatement(sql);
                        ps.setString(1,this.getName());
                        ps.executeUpdate();
                        
                        this.setUid(db.getAutoAllocatedUid(c));                     
                    } else {
                        // Pre-allocate UID
                        int uid = db.getPreAllocatedUid("biodatabase");
                        
                        sql = 
                                "insert into biodatabase" +
                                "   (biodatabase_id, name) " +
                                "values " +
                                "   (?, ?) ";
                        ps = c.prepareStatement(sql);
                        ps.setInt(1, uid);
                        ps.setString(2,this.getName());
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
                    "update    biodatabase "+
                    "set       authority = ?, "+
                    "          description = ?, "+
                    "          acronym = ?, "+
                    "          uri = ? "+
                    "where     biodatabase_id = ? ";
            PersistentBioDB db = this.getDB();
            Connection c = db.getConnection();
            PreparedStatement ps = null;
            boolean success = false;
            try {
                ps = c.prepareStatement(sql);
                ps.setString(1, this.getAuthority());
                ps.setString(2, this.getDescription());
                ps.setString(3, this.getAcronym());
                ps.setString(4, this.getURI()==null?null:this.getURI().toString());
                ps.setInt(5, this.getUid());
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
                "delete from biodatabase "+
                "where     biodatabase_id = ? ";
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
                "select    authority, description, acronym, uri " +
                "from      biodatabase "+
                "where     biodatabase_id = ? ";
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
                this.setAuthority(rs.getString(1));
                this.setDescription(rs.getString(2));
                this.setAcronym(rs.getString(3));
                this.setURI(new URI(rs.getString(4)));
            } else {
                rs.close();
                ps.close();
                ps = null;
                rs = null;
                sql =
                        "select    biodatabase_id, authority, description, acronym, uri " +
                        "from      biodatabase "+
                        "where     name = ? ";
                ps = c.prepareStatement(sql);
                ps.setString(1, this.getName());
                ps.execute();
                rs = ps.getResultSet();
                if (rs.next()) {
                    found = true;
                    this.setUid(rs.getInt(1)); // set our UID to refer to this record
                    this.setAuthority(rs.getString(2));
                    this.setDescription(rs.getString(3));
                    this.setAcronym(rs.getString(4));
                    this.setURI(rs.getString(5)==null?null:new URI(rs.getString(5)));
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

