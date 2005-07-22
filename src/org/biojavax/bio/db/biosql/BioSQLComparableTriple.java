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
import org.biojava.ontology.Term;

import org.biojavax.bio.db.Persistent;

import org.biojavax.bio.db.PersistentBioDB;
import org.biojavax.bio.db.PersistentComparableOntology;
import org.biojavax.bio.db.PersistentComparableTerm;
import org.biojavax.bio.db.PersistentComparableTriple;
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

public class BioSQLComparableTriple extends PersistentComparableTriple {
    
    
    
    /**
     *
     * Wraps a taxon in a persistence wrapper.
     * @param db the database this taxon lives in.
     * @param taxon the taxon to wrap.
     */
    
    private BioSQLComparableTriple(PersistentBioDB db, ComparableTriple triple) {
        
        super(db, triple);
        
    }
    
    
    
    /**
     *
     * Wraps a taxon in a persistence wrapper.
     * @param db the database this taxon lives in.
     * @param taxid the taxid of this taxon.
     */
    
    private BioSQLComparableTriple(PersistentBioDB db, Ontology onto, Term subject, Term object, Term predicate) {
        
        super(db, onto, subject, object, predicate);
        
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
    
    public static PersistentComparableTriple getInstance(PersistentBioDB db, ComparableTriple triple) {
        
        String key = triple.getOntology().toString()+"___"+triple.getSubject().toString()+
                "___"+triple.getObject().toString()+"___"+triple.getPredicate().toString();
        
        if (triple instanceof BioSQLComparableTriple) return (BioSQLComparableTriple)triple;
        
        synchronized(singletons) {
            
            if (singletons==null) singletons = new HashMap();
            
            if (!singletons.containsKey(key)) singletons.put(key,new BioSQLComparableTriple(db, triple));
            
        }
        
        return (BioSQLComparableTriple)singletons.get(key);
        
    }
    
    /**
     *
     * Singleton constructor.
     * @return the persistent version.
     * @param db the database this taxon lives in.
     * @param taxid the taxid of this taxon.
     */
    
    public static PersistentComparableTriple getInstance(PersistentBioDB db, Ontology onto, Term subject, Term object, Term predicate) {
        
        String key = onto.toString()+"___"+subject.toString()+"___"+object.toString()+"___"+predicate.toString();
        
        synchronized(singletons) {
            
            if (singletons==null) singletons = new HashMap();
            
            if (!singletons.containsKey(key)) singletons.put(key,new BioSQLComparableTriple(db, onto, subject, object, predicate));
            
        }
        
        return (BioSQLComparableTriple)singletons.get(key);
        
    }
    
    /**
     *
     * Singleton constructor.
     * @return the persistent version.
     * @param uid the uid to load from the database.
     */
    
    public static PersistentComparableTriple getInstanceByUID(PersistentBioDB db, int uid) throws SQLException {
        
        // attempt to load from UID, or unique key if UID not found
        
        String sql =
                "select    ontology_id, subject_term_id, object_term_id, predicate_term_id "+
                "from      term_relationship "+
                "where     term_relationship_id = ? ";
        Connection c = db.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        PersistentComparableTriple tx = null;
        try {
            ps = c.prepareStatement(sql);
            ps.setInt(1, uid);
            ps.execute();
            rs = ps.getResultSet();
            if (rs.next()) {
                PersistentComparableOntology o = BioSQLComparableOntology.getInstanceByUID(db,rs.getInt(1));
                PersistentComparableTerm subj = BioSQLComparableTerm.getInstanceByUID(db,rs.getInt(2));
                PersistentComparableTerm obj = BioSQLComparableTerm.getInstanceByUID(db,rs.getInt(3));
                PersistentComparableTerm pred = BioSQLComparableTerm.getInstanceByUID(db,rs.getInt(4));
                tx = (PersistentComparableTriple)getInstance(db, o, subj, obj, pred);
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
        PersistentComparableTerm subj = (PersistentComparableTerm)this.getSubject();
        subj.store(vars);
        PersistentComparableTerm obj = (PersistentComparableTerm)this.getObject();
        obj.store(vars);
        PersistentComparableTerm pred = (PersistentComparableTerm)this.getPredicate();
        pred.store(vars);
        int subject_id = subj.getUid();
        int object_id = obj.getUid();
        int predicate_id = pred.getUid();
        
        if (this.getUid() == Persistent.UID_UNKNOWN) {
            
            // check unique key fields for existence
            
            // if it does, then load the UID from the existing record and assign to this
            
            String sql =
                    "select     term_relationship_id " +
                    "from       term_relationship " +
                    "where      ontology_id = ? " +
                    "and        subject_term_id = ? " +
                    "and        object_term_id = ? " +
                    "and        predicate_term_id = ? ";
            PersistentBioDB db = this.getDB();
            Connection c = db.getConnection();
            PreparedStatement ps = null;
            ResultSet rs = null;
            boolean success = false;
            try {
                ps = c.prepareStatement(sql);
                ps.setInt(1, ontology_id);
                ps.setInt(2, subject_id);
                ps.setInt(3, object_id);
                ps.setInt(4, predicate_id);
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
                                "insert into term_relationship " +
                                "   (ontology_id, subject_term_id, object_term_id, predicate_term_id) " +
                                "values " +
                                "   (?,?,?,?) ";
                        ps = c.prepareStatement(sql);
                        ps.setInt(1,ontology_id);
                        ps.setInt(2,subject_id);
                        ps.setInt(3,object_id);
                        ps.setInt(4,predicate_id);
                        ps.executeUpdate();
                        
                        this.setUid(db.getAutoAllocatedUid(c));
                    } else {
                        // Pre-allocate UID
                        int uid = db.getPreAllocatedUid("term_relationship");
                        
                        sql =
                                "insert into term_relationship " +
                                "   (term_relationship_id, ontology_id, subject_term_id, object_term_id, predicate_term_id) " +
                                "values " +
                                "   (?, ?, ?, ?, ?) ";
                        ps = c.prepareStatement(sql);
                        ps.setInt(1, uid);
                        ps.setInt(2,ontology_id);
                        ps.setInt(3,subject_id);
                        ps.setInt(4,object_id);
                        ps.setInt(5,predicate_id);
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
            
            // nothing to update on the triple itself
            
            PersistentBioDB db = this.getDB();
            Connection c = db.getConnection();
            PreparedStatement ps = null;
            boolean success = false;
            try {
                
                // Insert/delete the descriptors
                
                String sql =
                        "delete from term_relationship_term " +
                        "where term_relationship_id = ? " +
                        "and term_id = ? ";
                ps = c.prepareStatement(sql);
                ps.setInt(1,this.getUid());
                Set removedDescriptors = this.getRemovedDescriptors();
                for (Iterator i = removedDescriptors.iterator(); i.hasNext(); ) {
                    PersistentComparableTerm t = (PersistentComparableTerm)i.next();
                    ps.setInt(2,t.getUid());
                    ps.executeUpdate();
                }
                
                ps.close();
                ps = null;
                sql =
                        "insert into term_relationship_term " +
                        "   (term_relationship_id, term_id) " +
                        "values " +
                        "   (?,?) ";
                ps.setInt(1,this.getUid());
                Set addedDescriptors = this.getAddedDescriptors();
                for (Iterator i = addedDescriptors.iterator(); i.hasNext(); ) {
                    PersistentComparableTerm t = (PersistentComparableTerm)i.next();
                    ps.setInt(2,t.getUid());
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
        
        
        // delete statements go here - descriptors etc. will recurse so we don't need to do that too.
        
        String sql =
                "delete from term_relationship "+
                "where     term_relationship_id = ? ";
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
        
        
        
        // nothing to load!
        
        PersistentBioDB db = this.getDB();
        Connection c = db.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            // Load the descriptors
            String sql =
                    "select term_id from term_relationship_term where term_relationship_id = ? ";
            ps.setInt(1,this.getUid());
            ps.execute();
            rs = ps.getResultSet();
            while (rs.next()) {
                PersistentComparableTerm t = BioSQLComparableTerm.getInstanceByUID(db, rs.getInt(1));
                this.addDescriptor(t);
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
                    
        this.setStatus(Persistent.UNMODIFIED);
        return this;
        
    }
    
    
    
}

