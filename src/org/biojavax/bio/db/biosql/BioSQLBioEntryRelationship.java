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
import org.biojavax.bio.BioEntry;
import org.biojavax.bio.BioEntryRelationship;
import org.biojavax.bio.db.Persistent;
import org.biojavax.bio.db.PersistentBioDB;
import org.biojavax.bio.db.PersistentBioEntry;
import org.biojavax.bio.db.PersistentComparableOntology;
import org.biojavax.bio.db.PersistentComparableTerm;
import org.biojavax.bio.db.PersistentBioEntryRelationship;
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

public class BioSQLBioEntryRelationship extends PersistentBioEntryRelationship {
    
    
    
    /**
     *
     * Wraps a dbxref in a persistence wrapper.
     * @param db the database this dbxref lives in.
     * @param dbxref the taxon to dbxref.
     */
    
    private BioSQLBioEntryRelationship(PersistentBioDB db, BioEntryRelationship r) {
        
        super(db, r);
        
    }
    
    
    
    /**
     *
     * Wraps a dbxref in a persistence wrapper.
     * @param db the database this dbxref lives in.
     * @param dbname the dbname of this dbxref.
     * @param accession the accession of this dbxref.
     * @param version the version of this dbxref.
     */
    
    private BioSQLBioEntryRelationship(PersistentBioDB db, BioEntry object, BioEntry subject, ComparableTerm term) {
        
        super(db, object, subject, term);
        
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
    
    public static PersistentBioEntryRelationship getInstance(PersistentBioDB db, BioEntryRelationship r) {
        
        String key = r.getObject().toString()+"___"+r.getSubject().toString()+"___"+r.getTerm().toString();
        
        if (r instanceof BioSQLBioEntryRelationship) return (BioSQLBioEntryRelationship)r;
        
        synchronized(singletons) {
            
            if (singletons==null) singletons = new HashMap();
            
            if (!singletons.containsKey(key)) singletons.put(key,new BioSQLBioEntryRelationship(db, r));
            
        }
        
        return (BioSQLBioEntryRelationship)singletons.get(key);
        
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
    
    public static PersistentBioEntryRelationship getInstance(PersistentBioDB db, BioEntry object, BioEntry subject, ComparableTerm term) {
        
        String key = object.toString()+"___"+subject.toString()+"___"+term.toString();
        
        synchronized(singletons) {
            
            if (singletons==null) singletons = new HashMap();
            
            if (!singletons.containsKey(key)) singletons.put(key,new BioSQLBioEntryRelationship(db, object, subject, term));
            
        }
        
        return (BioSQLBioEntryRelationship)singletons.get(key);
        
    }
    
    /**
     *
     * Singleton constructor.
     * @return the persistent version.
     * @param uid the uid to load from the database.
     */
    
    public static PersistentBioEntryRelationship getInstanceByUID(PersistentBioDB db, int uid) throws SQLException {
        
        // attempt to load from UID, or unique key if UID not found
        
        String sql =
                "select    subject_bioentry_id, term_id, object_bioentry_id "+
                "from      bioentry_relationship "+
                "where     bioentry_relationship_id = ? ";
        Connection c = db.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        PersistentBioEntryRelationship xr = null;
        try {
            ps = c.prepareStatement(sql);
            ps.setInt(1, uid);
            ps.execute();
            rs = ps.getResultSet();
            if (rs.next()) {
                PersistentBioEntry subj = db.loadSequenceByUID(rs.getInt(1));
                PersistentComparableTerm term = BioSQLComparableTerm.getInstanceByUID(db,rs.getInt(2));
                PersistentBioEntry obj = db.loadSequenceByUID(rs.getInt(3));
                xr = (PersistentBioEntryRelationship)getInstance(db, obj, subj, term);
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
        
        PersistentBioEntry subj = (PersistentBioEntry)this.getSubject();
        int subject_id = subj.getUid(); 
        if (subject_id==Persistent.UID_UNKNOWN) {
            subj.store(vars);
            subject_id = subj.getUid();
        }
        PersistentBioEntry obj = (PersistentBioEntry)this.getObject();
        int obj_id = obj.getUid(); 
        if (obj_id==Persistent.UID_UNKNOWN) {
            obj.store(vars);
            obj_id = obj.getUid();
        }
        PersistentComparableTerm t = (PersistentComparableTerm)this.getTerm();
        ((PersistentComparableOntology)t.getOntology()).store(vars); // just in case
        int term_id = t.getUid(); 
        
        if (this.getUid() == Persistent.UID_UNKNOWN) {
            
            // check unique key fields for existence
            
            // if it does, then load the UID from the existing record and assign to this
            
            String sql =
                    "select     bioentry_relationship_id " +
                    "from       bioentry_relationship " +
                    "where      subject_bioentry_id = ? " +
                    "and        term_id = ? " +
                    "and        object_bioentry_id = ? ";
            PersistentBioDB db = this.getDB();
            Connection c = db.getConnection();
            PreparedStatement ps = null;
            ResultSet rs = null;
            boolean success = false;
            try {
                ps = c.prepareStatement(sql);
                ps.setInt(1, subject_id);
                ps.setInt(2, term_id);
                ps.setInt(3, obj_id);
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
                                "insert into bioentry_relationship " +
                                "   (subject_bioentry_id, term_id, object_bioentry_id) " +
                                "values " +
                                "   (?,?,?) ";
                        ps = c.prepareStatement(sql);
                        ps.setInt(1, subject_id);
                        ps.setInt(2, term_id);
                        ps.setInt(3, obj_id);
                        ps.executeUpdate();
                        
                        this.setUid(db.getAutoAllocatedUid(c));
                    } else {
                        // Pre-allocate UID
                        int uid = db.getPreAllocatedUid("bioentry_relationship");
                        
                        sql =
                                "insert into bioentry_relationship " +
                                "   (bioentry_relationship_id, subject_bioentry_id, term_id, object_bioentry_id) " +
                                "values " +
                                "   (?, ?, ?, ?) ";
                        ps = c.prepareStatement(sql);
                        ps.setInt(1, uid);
                        ps.setInt(2, subject_id);
                        ps.setInt(3, term_id);
                        ps.setInt(4, obj_id);
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
                    "update bioentry_relationship " +
                    "set    rank = ? " +
                    "where  bioentry_relationship_id = ? ";
            PersistentBioDB db = this.getDB();
            Connection c = db.getConnection();
            PreparedStatement ps = null;
            boolean success = false;
            try {
                ps = c.prepareStatement(sql);
                
                if (this.getRank()==Persistent.NULL_INTEGER) ps.setNull(1,Types.INTEGER);
                else ps.setInt(1,this.getRank());
                ps.setInt(2,this.getUid());
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
                "delete from bioentry_relationship "+
                "where     bioentry_relationship_id = ? ";
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
                "select    rank " +
                "from      bioentry_relationship "+
                "where     bioentry_relationship_id = ? ";
        PersistentBioDB db = this.getDB();
        Connection c = db.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = c.prepareStatement(sql);
            ps.setInt(1,this.getUid());
            ps.execute();
            rs = ps.getResultSet();
            if (rs.next()) {
                this.setRank(rs.getObject(1)==null?Persistent.NULL_INTEGER:rs.getInt(1));
                found = true;
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

