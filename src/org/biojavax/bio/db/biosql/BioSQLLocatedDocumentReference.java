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
 
 * BioSQLLocatedDocumentReference.java
 
 *
 
 * Created on June 15, 2005, 6:04 PM
 
 */



package org.biojavax.bio.db.biosql;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import java.util.HashMap;

import java.util.Map;
import org.biojavax.DocumentReference;

import org.biojavax.LocatedDocumentReference;

import org.biojavax.bio.db.Persistent;

import org.biojavax.bio.db.PersistentBioDB;
import org.biojavax.bio.db.PersistentDocumentReference;

import org.biojavax.bio.db.PersistentLocatedDocumentReference;



/**
 *
 * A basic LocatedDocumentReference implemenation.
 *
 * Note: We don't care about our status or UID as that all depends on our parent.
 * So, we just stay nice and quiet and ignore all that rubbish.
 *
 * Equality is based on the name of the LocatedDocumentReference.
 *
 *
 *
 * @author Mark Schreiber
 *
 */

public class BioSQLLocatedDocumentReference extends PersistentLocatedDocumentReference {
    
    
    
    /**
     *
     * Wraps a LocatedDocumentReference in a persistence wrapper.
     * @param db the database this LocatedDocumentReference lives in.
     * @param LocatedDocumentReference the LocatedDocumentReference to wrap.
     */
    
    private BioSQLLocatedDocumentReference(PersistentBioDB db, LocatedDocumentReference ldr) {
        
        super(db, ldr);
        
    }
    
    
    
    /**
     *
     * Wraps a LocatedDocumentReference in a persistence wrapper.
     * @param db the database this LocatedDocumentReference lives in.
     * @param name the name of this LocatedDocumentReference.
     */
    
    private BioSQLLocatedDocumentReference(PersistentBioDB db, DocumentReference dr, int start, int end) {
        
        super(db, dr, start, end);
        
    }
    
    
    
    /** Singleton map */
    
    private static Map singletons;
    
    /**
     *
     * Singleton constructor.
     * @return the persistent version.
     * @param db the database this LocatedDocumentReference lives in.
     * @param LocatedDocumentReference the LocatedDocumentReference to wrap and make persistent.
     */
    
    public static PersistentLocatedDocumentReference getInstance(PersistentBioDB db, LocatedDocumentReference ldr) {
        
        String key = ldr.getDocumentReference().toString()+"___"+ldr.getStart()+"___"+ldr.getEnd();
        
        if (ldr instanceof BioSQLLocatedDocumentReference) return (BioSQLLocatedDocumentReference)ldr;
        
        synchronized(singletons) {
            
            if (singletons==null) singletons = new HashMap();
            
            if (!singletons.containsKey(key)) singletons.put(key,new BioSQLLocatedDocumentReference(db, ldr));
            
        }
        
        return (BioSQLLocatedDocumentReference)singletons.get(key);
        
    }
    
    /**
     *
     * Singleton constructor.
     * @return the persistent version.
     * @param db the database this LocatedDocumentReference lives in.
     * @param name the name of this LocatedDocumentReference.
     */
    
    public static PersistentLocatedDocumentReference getInstance(PersistentBioDB db, DocumentReference dr, int start, int end) {
        
        String key = dr.toString()+"___"+start+"___"+end;
        
        synchronized(singletons) {
            
            if (singletons==null) singletons = new HashMap();
            
            if (!singletons.containsKey(key)) singletons.put(key,new BioSQLLocatedDocumentReference(db, dr, start, end));
            
        }
        
        return (BioSQLLocatedDocumentReference)singletons.get(key);
        
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
        
        // vars[0,1] == bioentry_id, rank
        
        int bioentry_id = ((Integer)vars[0]).intValue();
        int rank = ((Integer)vars[1]).intValue();
        
        // delete first
        
        String sql =
                "delete from bioentry_reference " +
                "where bioentry_id = ? " +
                "and rank = ? ";
        PersistentBioDB db = this.getDB();
        Connection c = db.getConnection();
        PreparedStatement ps = null;
        boolean success = false;
        try {
            ps = c.prepareStatement(sql);
            ps.setInt(1,bioentry_id);
            ps.setInt(2,rank);
            ps.executeUpdate();
            
            // now (re)create
            
            ps.close();
            ps = null;
            sql =
                    "insert into bioentry_reference " +
                    "   (bioentry_id, rank, reference_id, start_pos, end_pos) " +
                    "values " +
                    "   (?,?,?,?,?) ";
            ps = c.prepareStatement(sql);
            ps.setInt(1,bioentry_id);
            ps.setInt(2,rank);
            PersistentDocumentReference pdr = (PersistentDocumentReference)this.getDocumentReference();
            pdr.store(vars); // make sure our docref exists!
            ps.setInt(3,pdr.getUid());
            if (this.getStart()==Persistent.NULL_INTEGER) ps.setNull(4,Types.INTEGER);
            else ps.setInt(4,this.getStart());
            if (this.getEnd()==Persistent.NULL_INTEGER) ps.setNull(5,Types.INTEGER);
            else ps.setInt(5,this.getEnd());
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
                
        // vars[0,1] == bioentry_id, rank
        
        int bioentry_id = ((Integer)vars[0]).intValue();
        int rank = ((Integer)vars[1]).intValue();
        
        // vars is ignored here as LocatedDocumentReferences don't have ranks or parent objects
                
        String sql =
                "delete from bioentry_reference " +
                "where bioentry_id = ? " +
                "and rank = ? ";
        PersistentBioDB db = this.getDB();
        Connection c = db.getConnection();
        PreparedStatement ps = null;
        boolean success = false;
        try {
            ps = c.prepareStatement(sql);
            ps.setInt(1,bioentry_id);
            ps.setInt(2,rank);
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
        
        // there ain't nothing else to load!
        
        return this;
        
    }
    
    
    
}

