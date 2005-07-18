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
 * PersistentBioDB.java
 *
 * Created on June 15, 2005, 4:16 PM
 */

package org.biojavax.bio.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import org.biojavax.Namespace;


/**
 * This represents a database which can store sequences and ontologies. 
 * It is a good idea not to use lazy-loading here as it is very difficult to track
 * changes and/or transactions over time when lazy-loading is used. It is better
 * to read the whole object at once from the connection, and then write it back
 * later without assuming that the connection will remain open or even be the same.
 * @author Richard Holland
 */
public interface PersistentBioDB {
    
    /**
     * Creates a persistent database connection using the provided connection. This
     * instance will never commit or rollback anything on that connection. You can
     * only set the datasource or connection once. Subsequent attempts to set it will throw
     * exceptions.
     * @param Connection a database connection.
     */
    // public PersistentBioDB(Connection conn);
    
    /**
     * Creates a persistent database connection using the provided datasource. This
     * instance WILL commit or rollback according to the schedule set out by the
     * member Persistent objects. You can only set the datasource or connection once.
     * Subsequent attempts to set it will throw exceptions.
     * @param Connection a database connection.
     */
    // public PersistentBioDB(DataSource ds);
    
    /**
     * Sets a single namespace to use for searching via this connection. When
     * searching, only sequences from this namespace will be returned. 
     * When null, all namespaces are used by default. Does not affect additions,
     * updates, or deletions. If the namespace does not exist in the db, then you'll never
     * get any results back when searching.
     * @param ns the namespace to use.
     */
    public void setNamespace(Namespace ns);
    
    /**
     * Locates and loads the namespace identified by the given name.
     * @param name the name of the namespace to load.
     * @return The namespace, or null if not found.
     * @throws SQLException if the namespace could not be loaded.
     */
    public PersistentNamespace loadNamespace(String name) throws SQLException;
    
    /**
     * Locates and loads all namespace names in the current connection.
     * @return A set of all namespace names available.
     * @throws SQLException if the namespace names could not be loaded.
     */
    public Set loadNamespaceNames() throws SQLException;
    
    /**
     * Loads the sequence from the currently set namespace. If the namespace is not set
     * you will get an exception. If the sequence name was null or was not found, it
     * will return null.
     * @param name the name of the sequecnce to load.
     * @param accession the accession of the sequecnce to load.
     * @param version the version to load.
     * @return Sequence that matches, or null if none.
     * @throws SQLException if the sequence could not be loaded.
     * @throws NullPointerException if no namespace has been set.
     */
    public PersistentBioEntry loadSequence(String name, String accession, int version) throws SQLException, NullPointerException;
    
    /**
     * Loads the sequence with the given UID.
     * @param UID the UID of the sequence to load regardless of namespace.
     * @return Sequence that matches, or null if none.
     * @throws SQLException if the sequence could not be loaded.
     * @throws NullPointerException if no namespace has been set.
     */
    public PersistentBioEntry loadSequenceByUID(int UID) throws SQLException, NullPointerException;
    
    /**
     * Returns a complete set of all UIDs in the current namespace. If there are
     * none then it will return an empty set.
     * @return A set of Integer UIDs.
     * @throws SQLException in case it couldn't find what the UIDs are.
     */
    public Set loadSequenceUIDs() throws SQLException;
    
    /**
     * Loads an ontology by name. If the ontology could not be found it will return
     * null.
     * @param name The name of the ontology to load.
     * @return The ontology, or null if it could not be found.
     * @throws SQLException if the ontology could not be loaded.
     */
    public PersistentComparableOntology loadOntology(String name) throws SQLException;
    
    /**
     * Finds out all current ontology names.
     * @return A set of all ontology names.
     */
    public Set loadOntologyNames();
    
    /**
     * Returns false if this object will make its own commits/rollbacks, true if not.
     * Generally, if you instantiated it with a Connection, it will return true here,
     * but if you instantiated with a DataSource, it will return false.
     * @return true if this object will commit on its own, false if not.
     */
    public boolean respectsTransactions();
    
    /**
     * Converts an object into its persistent equivalent, if appropriate.
     * @param o the comparable object to convert.
     * @return The persistent equivalent.
     * @throws IllegalArgumentException if it could not understand the object.
     */
    public Persistent convert(Object o) throws IllegalArgumentException;
    
    /**
     * Returns the underlying database connection this object is using.
     * @return The connection.
     */
    public Connection getConnection() throws SQLException;
}