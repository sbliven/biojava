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
import org.biojava.ontology.Ontology;

import org.biojavax.bio.db.Persistent;

import org.biojavax.bio.db.PersistentBioDB;
import org.biojavax.bio.db.PersistentComparableOntology;
import org.biojavax.bio.db.PersistentComparableTerm;
import org.biojavax.ontology.ComparableTerm;



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

public class BioSQLComparableTerm extends PersistentComparableTerm {
    
    
    
    /**
     *
     * Wraps a taxon in a persistence wrapper.
     * @param db the database this taxon lives in.
     * @param taxon the taxon to wrap.
     */
    
    private BioSQLComparableTerm(PersistentBioDB db, ComparableTerm term) {
        
        super(db, term);
        
    }
    
    
    
    /**
     *
     * Wraps a taxon in a persistence wrapper.
     * @param db the database this taxon lives in.
     * @param taxid the taxid of this taxon.
     */
    
    private BioSQLComparableTerm(PersistentBioDB db, Ontology o, String name, String description, Object[] synonyms) {
        
        super(db, o, name, description, synonyms);
        
    }
    
    
    
    /** Singleton map */
    
    private static Map singletons;
    
    /**
     *
     * Singleton constructor.
     * @return the persistent version.
     * @param db the database this taxon lives in.
     * @param taxon the taxon to wrap and make persistent.
     */
    
    public static PersistentComparableTerm getInstance(PersistentBioDB db, ComparableTerm term) {
        
        String key = term.getOntology().toString()+"___"+term.getName();
        
        if (term instanceof BioSQLComparableTerm) return (BioSQLComparableTerm)term;
        
        synchronized(singletons) {
            
            if (singletons==null) singletons = new HashMap();
            
            if (!singletons.containsKey(key)) singletons.put(key,new BioSQLComparableTerm(db, term));
            
        }
        
        return (BioSQLComparableTerm)singletons.get(key);
        
    }
    
    /**
     *
     * Singleton constructor.
     * @return the persistent version.
     * @param db the database this taxon lives in.
     * @param taxid the taxid of this taxon.
     */
    
    public static PersistentComparableTerm getInstance(PersistentBioDB db, Ontology o, String name, String description, Object[] synonyms) {
        
        String key = o.toString()+"___"+name;
        
        synchronized(singletons) {
            
            if (singletons==null) singletons = new HashMap();
            
            if (!singletons.containsKey(key)) singletons.put(key,new BioSQLComparableTerm(db, o, name, description, synonyms));
            
        }
        
        return (BioSQLComparableTerm)singletons.get(key);
        
    }
    
    /**
     *
     * Singleton constructor.
     * @return the persistent version.
     * @param uid the uid to load from the database.
     */
    
    public static PersistentComparableTerm getInstanceByUID(PersistentBioDB db, int uid) throws SQLException {
        
        // attempt to load from UID, or unique key if UID not found
        
        String sql =
                "select    ontology_id, name, definition "+
                "from      term "+
                "where     term_id = ? ";
        Connection c = db.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        PersistentComparableTerm tx = null;
        try {
            ps = c.prepareStatement(sql);
            ps.setInt(1, uid);
            ps.execute();
            rs = ps.getResultSet();
            if (rs.next()) {
                PersistentComparableOntology o = BioSQLComparableOntology.getInstanceByUID(db,rs.getInt(1));
                tx = (PersistentComparableTerm)getInstance(db, o, rs.getString(2), rs.getString(3), null);
                tx.setUid(uid);
            } else tx = null;
        } catch (SQLException e) {
            throw e;
        } finally {
            if (rs!=null) rs.close();
            if (ps!=null) ps.close();
            db.releaseConnection(c);
        }
        
        return tx;
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
        
        // vars is not used
        
        PersistentComparableOntology o = (PersistentComparableOntology)this.getOntology();
        int ontology_id = o.getUid(); 
        if (ontology_id==Persistent.UID_UNKNOWN) {
            o.store(vars);
            ontology_id = o.getUid();
        }
        
        if (this.getUid() == Persistent.UID_UNKNOWN) {
            
            // check unique key fields for existence
            
            // if it does, then load the UID from the existing record and assign to this
            
            String sql =
                    "select     term_id " +
                    "from       term " +
                    "where      ontology_id = ? " +
                    "and        name = ?";
            PersistentBioDB db = this.getDB();
            Connection c = db.getConnection();
            PreparedStatement ps = null;
            ResultSet rs = null;
            boolean success = false;
            try {
                ps = c.prepareStatement(sql);
                ps.setInt(1, ontology_id);
                ps.setString(2, this.getName());
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
                                "insert into term " +
                                "   (ontology_id, name) " +
                                "values " +
                                "   (?,?) ";
                        ps = c.prepareStatement(sql);
                        ps.setInt(1,ontology_id);
                        ps.setString(2, this.getName());
                        ps.executeUpdate();
                        
                        this.setUid(db.getAutoAllocatedUid(c));
                    } else {
                        // Pre-allocate UID
                        int uid = db.getPreAllocatedUid("term");
                        
                        sql =
                                "insert into term " +
                                "   (term_id, ontology_id, name) " +
                                "values " +
                                "   (?, ?, ?) ";
                        ps = c.prepareStatement(sql);
                        ps.setInt(1, uid);
                        ps.setInt(2, ontology_id);
                        ps.setString(3, this.getName());
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
                    "update    term "+
                    "set       definition = ?, " +
                    "          identifier = ?, " +
                    "          is_obsolete = ?  "+
                    "where     term_id = ? ";
            PersistentBioDB db = this.getDB();
            Connection c = db.getConnection();
            PreparedStatement ps = null;
            boolean success = false;
            try {
                ps = c.prepareStatement(sql);
                ps.setString(1, this.getDescription());
                ps.setString(2, this.getIdentifier());
                ps.setString(3, this.getObsolete()?"Y":null);
                ps.setInt(4, this.getUid());
                ps.executeUpdate();
                
                // Insert/delete the synonyms
                
                ps.close();
                ps = null;
                sql =
                        "delete from term_synonym " +
                        "where term_id = ? " +
                        "and name = ? ";
                ps = c.prepareStatement(sql);
                ps.setInt(1,this.getUid());
                Set removedSynonyms = this.getRemovedSynonyms();
                for (Iterator i = removedSynonyms.iterator(); i.hasNext(); ) {
                    String name = (String)i.next();
                    ps.setString(2,name);
                    ps.executeUpdate();
                }
                
                ps.close();
                ps = null;
                sql =
                        "insert into term_synonym " +
                        "   (term_id, name) " +
                        "values " +
                        "   (?,?) ";
                ps.setInt(1,this.getUid());
                Set addedSynonyms = this.getAddedSynonyms();
                for (Iterator i = addedSynonyms.iterator(); i.hasNext(); ) {
                    String name = (String)i.next();
                    ps.setString(2,name);
                    ps.executeUpdate();
                }
                
                // Done.
                
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
        
        
        // delete statements go here - term_synonym etc. will recurse so we don't need to do that too.
        
        String sql =
                "delete from term "+
                "where     term_id = ? ";
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
        
        
        
        // vars is not used
        
        PersistentComparableOntology o = (PersistentComparableOntology)this.getOntology();
        int ontology_id = o.getUid(); // assuming we will always be called from PersistentComparableOntology.load()
        
        
        
        // attempt to load from UID, or unique key if UID not found
        
        boolean found = false;
        
        String sql =
                "select    identifier, is_obsolete " +
                "from      term "+
                "where     term_id = ? ";
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
                this.setIdentifier(rs.getString(1));
                this.setObsolete("Y".equals(rs.getString(2))?true:false);
            } else {
                rs.close();
                ps.close();
                ps = null;
                rs = null;
                sql =
                        "select    term_id, identifier, is_obsolete " +
                        "from      term "+
                        "where     ontology_id = ? " +
                        "and       name = ? ";
                ps = c.prepareStatement(sql);
                ps.setInt(1, ontology_id);
                ps.setString(2, this.getName());
                ps.execute();
                rs = ps.getResultSet();
                if (rs.next()) {
                    found = true;
                    this.setUid(rs.getInt(1)); // set our UID to refer to this record
                    this.setIdentifier(rs.getString(2));
                    this.setObsolete("Y".equals(rs.getString(3))?true:false);
                }
            }
            
            // Load the synonyms
            
            rs.close();
            ps.close();
            rs = null;
            ps = null;
            
            sql =
                    "select name from term_synonym where term_id = ? ";
            ps.setInt(1,this.getUid());
            ps.execute();
            rs = ps.getResultSet();
            while (rs.next()) {
                this.addSynonym(rs.getString(1));
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

