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

import org.biojavax.bio.db.Persistent;

import org.biojavax.bio.db.PersistentBioDB;
import org.biojavax.bio.db.PersistentNCBITaxon;
import org.biojavax.bio.taxa.NCBITaxon;



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

public class BioSQLNCBITaxon extends PersistentNCBITaxon {
    
    
    
    /**
     *
     * Wraps a taxon in a persistence wrapper.
     * @param db the database this taxon lives in.
     * @param taxon the taxon to wrap.
     */
    
    private BioSQLNCBITaxon(PersistentBioDB db, NCBITaxon taxon) {
        
        super(db, taxon);
        
    }
    
    
    
    /**
     *
     * Wraps a taxon in a persistence wrapper.
     * @param db the database this taxon lives in.
     * @param taxid the taxid of this taxon.
     */
    
    private BioSQLNCBITaxon(PersistentBioDB db, int taxid) {
        
        super(db, taxid);
        
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
    
    public static PersistentNCBITaxon getInstance(PersistentBioDB db, NCBITaxon taxon) {
        
        Integer key = Integer.valueOf(taxon.getNCBITaxID());
        
        if (taxon instanceof BioSQLNCBITaxon) return (BioSQLNCBITaxon)taxon;
        
        synchronized(singletons) {
            
            if (singletons==null) singletons = new HashMap();
            
            if (!singletons.containsKey(key)) singletons.put(key,new BioSQLNCBITaxon(db, taxon));
            
        }
        
        return (BioSQLNCBITaxon)singletons.get(key);
        
    }
    
    /**
     *
     * Singleton constructor.
     * @return the persistent version.
     * @param db the database this taxon lives in.
     * @param taxid the taxid of this taxon.
     */
    
    public static PersistentNCBITaxon getInstance(PersistentBioDB db, int taxid) {
        
        Integer key = Integer.valueOf(taxid);
        
        synchronized(singletons) {
            
            if (singletons==null) singletons = new HashMap();
            
            if (!singletons.containsKey(key)) singletons.put(key,new BioSQLNCBITaxon(db, taxid));
            
        }
        
        return (BioSQLNCBITaxon)singletons.get(key);
        
    }
    
    /**
     *
     * Singleton constructor.
     * @return the persistent version.
     * @param uid the uid to load from the database.
     */
    
    public static PersistentNCBITaxon getInstanceByUID(PersistentBioDB db, int uid) throws SQLException {
        
        // attempt to load from UID, or unique key if UID not found
        
        String sql =
                "select    ncbi_taxon_id "+
                "from      taxon "+
                "where     taxon_id = ? ";
        Connection c = db.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        PersistentNCBITaxon tx = null;
        try {
            ps = c.prepareStatement(sql);
            ps.setInt(1, uid);
            ps.execute();
            rs = ps.getResultSet();
            if (rs.next()) {
                tx = (PersistentNCBITaxon)getInstance(db, rs.getInt(1));
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
        
        // vars is ignored here as namespaces don't have ranks or parent objects
        
        if (this.getUid() == Persistent.UID_UNKNOWN) {
            
            // check unique key fields for existence
            
            // if it does, then load the UID from the existing record and assign to this
            
            String sql =
                    "select     taxon_id " +
                    "from       taxon " +
                    "where      ncbi_taxon_id = ? ";
            PersistentBioDB db = this.getDB();
            Connection c = db.getConnection();
            PreparedStatement ps = null;
            ResultSet rs = null;
            boolean success = false;
            try {
                ps = c.prepareStatement(sql);
                ps.setInt(1, this.getNCBITaxID());
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
                                "insert into taxon" +
                                "   (ncbi_taxon_id) " +
                                "values " +
                                "   (?) ";
                        ps = c.prepareStatement(sql);
                        ps.setInt(1,this.getNCBITaxID());
                        ps.executeUpdate();
                        
                        this.setUid(db.getAutoAllocatedUid(c));
                    } else {
                        // Pre-allocate UID
                        int uid = db.getPreAllocatedUid("taxon");
                        
                        sql =
                                "insert into taxon" +
                                "   (taxon_id, ncbi_taxon_id) " +
                                "values " +
                                "   (?, ?) ";
                        ps = c.prepareStatement(sql);
                        ps.setInt(1, uid);
                        ps.setInt(2,this.getNCBITaxID());
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
                    "update    taxon "+
                    "set       NODE_RANK = ?, "+
                    "          GENETIC_CODE = ?, "+
                    "          MITO_GENETIC_CODE = ?, "+
                    "          LEFT_VALUE = ?, "+
                    "          RIGHT_VALUE = ?, "+
                    "          PARENT_TAXON_ID = ? "+
                    "where     taxon_id = ? ";
            PersistentBioDB db = this.getDB();
            Connection c = db.getConnection();
            PreparedStatement ps = null;
            boolean success = false;
            try {
                ps = c.prepareStatement(sql);
                ps.setString(1, this.getNodeRank());
                if (this.getGeneticCode()==Persistent.NULL_INTEGER) ps.setNull(2, Types.INTEGER);
                else ps.setInt(2, this.getGeneticCode());
                if (this.getMitoGeneticCode()==Persistent.NULL_INTEGER) ps.setNull(3, Types.INTEGER);
                else ps.setInt(3, this.getMitoGeneticCode());
                if (this.getLeftValue()==Persistent.NULL_INTEGER) ps.setNull(4, Types.INTEGER);
                else ps.setInt(4, this.getLeftValue());
                if (this.getRightValue()==Persistent.NULL_INTEGER) ps.setNull(5, Types.INTEGER);
                else ps.setInt(5, this.getRightValue());
                if (this.getParentNCBITaxID()==Persistent.NULL_INTEGER) ps.setNull(6, Types.INTEGER);
                else ps.setInt(6, this.getParentNCBITaxID());
                ps.setInt(7, this.getUid());
                ps.executeUpdate();
                
                // Insert/delete the names
                
                ps.close();
                ps = null;
                sql =
                        "delete from taxon_name " +
                        "where taxon_id = ? " +
                        "and name_class = ? " +
                        "and name = ? ";
                ps = c.prepareStatement(sql);
                ps.setInt(1,this.getUid());
                Map removedNames = this.getRemovedNames();
                for (Iterator i = removedNames.keySet().iterator(); i.hasNext(); ) {
                    String nameClass = (String)i.next();
                    ps.setString(2,nameClass);
                    for (Iterator j = ((Set)removedNames.get(nameClass)).iterator(); j.hasNext(); ) {
                        String name = (String)j.next();
                        ps.setString(3,name);
                        ps.executeUpdate();
                    }
                }
                
                ps.close();
                ps = null;
                sql =
                        "insert into taxon_name " +
                        "   (taxon_id, name_class, name) " +
                        "values " +
                        "   (?,?,?) ";
                ps.setInt(1,this.getUid());
                Map addedNames = this.getAddedNames();
                for (Iterator i = addedNames.keySet().iterator(); i.hasNext(); ) {
                    String nameClass = (String)i.next();
                    ps.setString(2,nameClass);
                    for (Iterator j = ((Set)addedNames.get(nameClass)).iterator(); j.hasNext(); ) {
                        String name = (String)j.next();
                        ps.setString(3,name);
                        ps.executeUpdate();
                    }
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
        
        
        // delete statements go here - taxon_name will recurse so we don't need to do that too.
        
        String sql =
                "delete from taxon "+
                "where     taxon_id = ? ";
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
                "select    NODE_RANK, GENETIC_CODE, MITO_GENETIC_CODE, LEFT_VALUE," +
                "          RIGHT_VALUE, PARENT_TAXON_ID " +
                "from      taxon "+
                "where     taxon_id = ? ";
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
                this.setNodeRank(rs.getString(1));
                this.setGeneticCode(rs.getInt(2));
                this.setMitoGeneticCode(rs.getInt(3));
                this.setLeftValue(rs.getInt(4));
                this.setRightValue(rs.getInt(5));
                this.setParentNCBITaxID(rs.getInt(6));
            } else {
                rs.close();
                ps.close();
                ps = null;
                rs = null;
                sql =
                        "select    taxon_id, NODE_RANK, GENETIC_CODE, MITO_GENETIC_CODE, LEFT_VALUE," +
                        "          RIGHT_VALUE, PARENT_TAXON_ID " +
                        "from      taxon "+
                        "where     ncbi_taxon_id = ? ";
                ps = c.prepareStatement(sql);
                ps.setInt(1, this.getNCBITaxID());
                ps.execute();
                rs = ps.getResultSet();
                if (rs.next()) {
                    found = true;
                    this.setUid(rs.getInt(1)); // set our UID to refer to this record
                    this.setNodeRank(rs.getString(2));
                    this.setGeneticCode(rs.getObject(3)==null?Persistent.NULL_INTEGER:rs.getInt(3));
                    this.setMitoGeneticCode(rs.getObject(4)==null?Persistent.NULL_INTEGER:rs.getInt(4));
                    this.setLeftValue(rs.getObject(5)==null?Persistent.NULL_INTEGER:rs.getInt(5));
                    this.setRightValue(rs.getObject(6)==null?Persistent.NULL_INTEGER:rs.getInt(6));
                    this.setParentNCBITaxID(rs.getObject(7)==null?Persistent.NULL_INTEGER:rs.getInt(7));
                }
            }
            
            // Load the names
            
            rs.close();
            ps.close();
            rs = null;
            ps = null;
            
            sql =
                    "select name_class, name from taxon_name where taxon_id = ? ";
            ps.setInt(1,this.getUid());
            ps.execute();
            rs = ps.getResultSet();
            while (rs.next()) {
                this.addName(rs.getString(1),rs.getString(2));
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

