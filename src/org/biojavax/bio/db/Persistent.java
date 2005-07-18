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
 * Persistent.java
 *
 * Created on June 14, 2005, 4:21 PM
 */

package org.biojavax.bio.db;
import java.sql.SQLException;


/**
 * Defines an object as persistable. You really must supply a constructor
 * that takes a single parameter which is the base object which the implementing
 * class is able to persist. This constructor then creates a persistent object
 * containing all the values of the wrapped object. The default state for all
 * objects that have been created from scratch is MODIFIED, whereas all that have
 * been loaded are UNMODIFIED. If something is MODIFIED, it becomes UNMODIFIED
 * when it is successfully stored back to the database. Use DELETED to indicate
 * that nothing references this object any more, and it can be safely deleted.
 * However, it won't actually be deleted until the remove() method is called.
 * @author Mark Schreiber
 * @author Richard Holland
 */
public interface Persistent {
    
    // Persistent objects are singletons and must have private constructors and a getInstance()
    // method which returns an instance.
    
    /** Defined for use in databases where integers can be null, which is NOT zero. */
    public static final int NULL_INTEGER = Integer.MIN_VALUE;
    
    /** Defined for use in databases where doubles can be null, which is NOT zero. */
    public static final double NULL_DOUBLE = Double.NaN;
    
    // Don't forget a public constructor that wraps a non-persistent version up for you.
    
    /**
     * The value to use if UID is not set. By default -1.
     */
    public static final int UID_UNKNOWN = -1;
    
    /**
     * The value to use if the object is in its original state (as loaded from the database).
     */
    public static final int UNMODIFIED = 0;
    
    /**
     * The value to use if the object has been modified (since being loaded from the database).
     * This is also the value to use for newly created objects that were not loaded from the db.
     */
    public static final int MODIFIED = 1;
    
    /**
     * The value to use if the object is no longer in use anywhere.
     */
    public static final int DELETED = 2;
    
    /**
     * Getter for property uid. UID is the unique key for the object used in a
     * database. If it has not been set yet, then it should return the constant UID_UNKNOWN.
     * @return Value of property uid.
     */
    public int getUid();
    
    /**
     * Setter for property uid. UID is the unique key for the object used in a
     * database. If it has not been set yet, then it should be the constant UID_UNKNOWN.
     * @param uid Value of property uid.
     */
    public void setUid(int uid);
    
    /**
     * Getter for property status. The status will be one of the ones defined in this interface, ie.
     * UNMODIFIED, MODIFIED, and DELETED.
     * @return Value of property status.
     */
    public int getStatus();
    
    /**
     * Setter for property status. The status will be one of the ones defined in this interface, ie.
     * UNMODIFIED, MODIFIED, and DELETED.
     * @param status Value of property status.
     * @throws IllegalArgumentException if the status is unrecognised.
     */
    public void setStatus(int status) throws IllegalArgumentException;
    
    /**
     * Loads the underlying persistent object based on the current UID, or the other important
     * values if the UID is missing. Will overwrite all data with that found in the db.
     * @return The persistent object for the current UID, or null if the UID was not found.
     * @throws SQLException if the object could not be loaded.
     */
    public Persistent load(Object[] vars) throws SQLException;
    
    /**
     * Stores this object and returns the persistent object stored.
     * @return the persisted object.
     * @throws SQLException if the persistence of the object failed.
     */
    public Persistent store(Object[] vars) throws SQLException;
    
    /**
     * Removes this object from the underlying database based on its UID.
     * @return True if the object was found and removed, false if not.
     * @throws SQLException if removal failed or the UID was missing.
     */
    public boolean remove(Object[] vars) throws SQLException;
    
    /**
     * Returns the database this object is persisting to.
     * @return the db connection.
     */
    public PersistentBioDB getDB();
}
