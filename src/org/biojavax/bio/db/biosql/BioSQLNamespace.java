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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.biojavax.Namespace;
import org.biojavax.bio.db.Persistent;
import org.biojavax.bio.db.PersistentBioDB;
import org.biojavax.bio.db.PersistentNamespace;

/**
 * A basic Namespace implemenation.
 *
 * Equality is based on the name of the namespace.
 *
 * @author Mark Schreiber
 */
public class BioSQLNamespace extends PersistentNamespace {
    
    /**
     * Wraps a namespace in a persistence wrapper.
     * @param namespace the namespace to wrap.
     */
    private BioSQLNamespace(PersistentBioDB db, Namespace namespace) {
        super(db, namespace);
    }
    
    /**
     * Wraps a namespace in a persistence wrapper.
     * @param namespace the namespace to wrap.
     */
    private BioSQLNamespace(PersistentBioDB db, String name) {
        super(db, name);
    }
    
    /** Singleton map */
    private static Map singletons;    
    /**
     * Singleton constructor.
     * @param namespace the namespace to wrap and make persistent.
     * @return the persistent version.
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
     * Singleton constructor.
     * @param name the namespace to wrap and make persistent.
     * @return the persistent version.
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
     * Stores this object and returns the persistent object stored. If it has no UID,
     * attempt to load it. If found, assign. Else, create it and assign the UID.
     * @return the persisted object.
     * @throws SQLException if the persistence of the object failed.
     */
    public synchronized Persistent store(Object[] vars) throws SQLException {
        if (this.getStatus()==Persistent.DELETED) throw new SQLException("Object has been previously deleted");
        
        // vars is ignored here as namespaces don't have ranks or parent objects
        
        if (this.getUid() == Persistent.UID_UNKNOWN) {
            // check unique key fields for existence
            // if it does, then load the UID from the existing record and assign to this
            // if it does not, create a new object using UID and unique key fields
            // set UID and MODIFIED status
        } 
        if (this.getStatus() == Persistent.MODIFIED) {
            // do an update by UID
            // set UNMODIFIED status
            this.setStatus(Persistent.UNMODIFIED);
        }
        
        return this;
    }
    
    /**
     * Removes this object from the underlying database based on its UID.
     * @return True if the object was found and removed, false if not.
     * @throws SQLException if removal failed or the UID was missing.
     */
    public synchronized boolean remove(Object[] vars) throws SQLException {
        if (this.getStatus()==Persistent.DELETED) throw new SQLException("Object has been previously deleted");
        
        // vars is ignored here as namespaces don't have ranks or parent objects
        
        // delete statements go here - delete by unique key if UID not found.
        
        this.setStatus(Persistent.DELETED);
        this.setUid(Persistent.UID_UNKNOWN);
        return true;
    }
    
    /**
     * Loads the underlying persistent object based on the current UID, or the other important
     * values if the UID is missing. Will overwrite all data with that found in the db.
     * @return The persistent object for the current UID, or null if the UID was not found.
     * @throws SQLException if the object could not be loaded.
     */
    public synchronized Persistent load(Object[] vars) throws SQLException {
        if (this.getStatus()==Persistent.DELETED) throw new SQLException("Object has been previously deleted");
        
        // vars is ignored here as namespaces don't have ranks or parent objects       
        
        // attempt to load from unique key if UID not found
        // if found, set uid = XYZ and status = UNMODIFIED and return this
        // else return null
        
        return null;
    }
    
}
