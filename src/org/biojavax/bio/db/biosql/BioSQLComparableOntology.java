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

import java.util.HashMap;
import java.util.Iterator;

import java.util.Map;
import java.util.Set;
import org.biojavax.bio.db.Persistent;
import org.biojavax.bio.db.PersistentBioDB;
import org.biojavax.bio.db.PersistentComparableTerm;
import org.biojavax.bio.db.PersistentComparableOntology;
import org.biojavax.bio.db.PersistentComparableTriple;
import org.biojavax.ontology.ComparableOntology;
import org.biojavax.ontology.ComparableTriple;




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

public class BioSQLComparableOntology extends PersistentComparableOntology {
    
    
    
    /**
     *
     * Wraps a dbxref in a persistence wrapper.
     * @param db the database this dbxref lives in.
     * @param dbxref the taxon to dbxref.
     */
    
    private BioSQLComparableOntology(PersistentBioDB db, ComparableOntology o) {
        
        super(db, o);
        
    }
    
    
    
    /**
     *
     * Wraps a dbxref in a persistence wrapper.
     * @param db the database this dbxref lives in.
     * @param dbname the dbname of this dbxref.
     * @param accession the accession of this dbxref.
     * @param version the version of this dbxref.
     */
    
    private BioSQLComparableOntology(PersistentBioDB db, String name) {
        
        super(db, name);
        
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
    
    public static PersistentComparableOntology getInstance(PersistentBioDB db, ComparableOntology o) {
        
        String key = o.getName();
        
        if (o instanceof BioSQLComparableOntology) return (BioSQLComparableOntology)o;
        
        synchronized(singletons) {
            
            if (singletons==null) singletons = new HashMap();
            
            if (!singletons.containsKey(key)) singletons.put(key,new BioSQLComparableOntology(db, o));
            
        }
        
        return (BioSQLComparableOntology)singletons.get(key);
        
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
    
    public static PersistentComparableOntology getInstance(PersistentBioDB db, String name) {
        
        String key = name;
        
        synchronized(singletons) {
            
            if (singletons==null) singletons = new HashMap();
            
            if (!singletons.containsKey(key)) singletons.put(key,new BioSQLComparableOntology(db, name));
            
        }
        
        return (BioSQLComparableOntology)singletons.get(key);
        
    }
    
    /**
     *
     * Singleton constructor.
     * @return the persistent version.
     * @param uid the uid to load from the database.
     */
    
    public static PersistentComparableOntology getInstanceByUID(PersistentBioDB db, int uid) throws SQLException {
        
        // attempt to load from UID, or unique key if UID not found
        
        String sql =
                "select    name "+
                "from      ontology "+
                "where     ontology_id = ? ";
        Connection c = db.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        PersistentComparableOntology xr = null;
        try {
            ps = c.prepareStatement(sql);
            ps.setInt(1, uid);
            ps.execute();
            rs = ps.getResultSet();
            if (rs.next()) {
                xr = (PersistentComparableOntology)getInstance(db, rs.getString(1));
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
                    "select     ontology_id " +
                    "from       ontology " +
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
                                "insert into ontology " +
                                "   (name) " +
                                "values " +
                                "   (?) ";
                        ps = c.prepareStatement(sql);
                        ps.setString(1, this.getName());
                        ps.executeUpdate();
                        
                        this.setUid(db.getAutoAllocatedUid(c));
                    } else {
                        // Pre-allocate UID
                        int uid = db.getPreAllocatedUid("dbxref");
                        
                        sql =
                                "insert into ontology " +
                                "   (ontology_id, name) " +
                                "values " +
                                "   (?, ?) ";
                        ps = c.prepareStatement(sql);
                        ps.setInt(1, uid);
                        ps.setString(2, this.getName());
                        ps.executeUpdate();
                        this.setUid(uid);
                    }
                }
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
                    "update ontology " +
                    "set    definition = ? " +
                    "where  ontology_id = ? ";
            PersistentBioDB db = this.getDB();
            Connection c = db.getConnection();
            PreparedStatement ps = null;
            boolean success = false;
            try {
                ps = c.prepareStatement(sql);
                
                ps.setString(1,this.getDescription());
                ps.setInt(2, this.getUid());
                ps.execute();
                
                // update terms+triples here
                
                Set addedThings = this.getAddedThings();
                // do a store() on each one
                for (Iterator i = addedThings.iterator(); i.hasNext(); ) ((Persistent)i.next()).store(vars);
                
                Set removedThings = this.getRemovedThings();
                // do a delete() on each one
                for (Iterator i = removedThings.iterator(); i.hasNext(); ) ((Persistent)i.next()).remove(vars);
                
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
        
        
        // delete statements go here - terms etc. will recurse so we don't need to do that too.
        
        String sql =
                "delete from ontology "+
                "where     ontology_id = ? ";
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
                "select    definition " +
                "from      ontology "+
                "where     ontology_id = ? ";
        PersistentBioDB db = this.getDB();
        Connection c = db.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            // Load the terms
            ps.setInt(1,this.getUid());
            ps.execute();
            rs = ps.getResultSet();
            if (rs.next()) {
                this.setDescription(rs.getString(1));
                found = true;
            }
            
            // load terms+triples here
            rs.close();
            rs=null;
            ps.close();
            ps=null;
            sql =
                    "select name, definition " +
                    "from   term " +
                    "where  ontology_id = ? ";
            ps = c.prepareStatement(sql);
            ps.setInt(1, this.getUid());
            ps.execute();
            rs = ps.getResultSet();
            while (rs.next()) {
                this.createTerm(rs.getString(1), rs.getString(2));
            }
            
            rs.close();
            rs=null;
            ps.close();
            ps=null;
            sql =
                    "select subject_term_id, object_term_id, predicate_term_id " +
                    "from   term_relationship " +
                    "where  ontology_id = ? ";
            ps = c.prepareStatement(sql);
            ps.setInt(1, this.getUid());
            ps.execute();
            rs = ps.getResultSet();
            while (rs.next()) {
                PersistentComparableTerm subj = BioSQLComparableTerm.getInstanceByUID(db,rs.getInt(1));
                PersistentComparableTerm obj = BioSQLComparableTerm.getInstanceByUID(db,rs.getInt(2));
                PersistentComparableTerm pred = BioSQLComparableTerm.getInstanceByUID(db,rs.getInt(3));
                this.createTriple(subj,obj,pred,null,null);
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

