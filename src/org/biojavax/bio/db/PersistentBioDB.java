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
import java.sql.ResultSet;

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

     * Locates and loads all namespace names in the current connection.

     * @return A set of all namespace names available.

     * @throws SQLException if the namespace names could not be loaded.

     */

    public Set loadNamespaceNames() throws SQLException;
 

    /**

     * Returns a complete set of all UIDs in the current namespace. If there are

     * none then it will return an empty set.

     * @return A set of Integer UIDs.

     * @throws SQLException in case it couldn't find what the UIDs are.

     */

    public Set loadSequenceUIDs() throws SQLException;
   

    /**
     * Finds out all current ontology names.
     * @return A set of all ontology names.
     * @throws java.sql.SQLException in case of failure.
     */

    public Set loadOntologyNames() throws SQLException;

    

    /**

     * Returns false if this object will make its own commits/rollbacks, true if not.

     * Generally, if you instantiated it with a Connection, it will return true here,

     * but if you instantiated with a DataSource, it will return false.

     * @return true if this object will commit on its own, false if not.

     */

    public boolean respectsTransactions();

    public boolean autoAllocatedUids();
    
    public int getAutoAllocatedUid(Connection c) throws SQLException;
    
    public int getPreAllocatedUid(String table) throws SQLException;
    

    /**
     * Converts an object into its persistent equivalent, if appropriate.
     * @return The persistent equivalent.
     * @param o the object to convert.
     * @throws IllegalArgumentException if it could not understand the object.
     */

    public Persistent convert(Object o) throws IllegalArgumentException;

    

    /**
     * Returns the underlying database connection this object is using.
     * @return The connection.
     * @throws java.sql.SQLException in case of failure.
     */

    public Connection getConnection() throws SQLException;        

    /**
     * Closes the underlying database connection this object is using, if it
     * came from a DataSource. If it didn't, it does nothing.
     * @throws java.sql.SQLException in case of failure.
     */   
    public void releaseConnection(Connection conn) throws SQLException;

    public boolean writebackLongStrings();    
    public String readLongString(ResultSet rs, int column) throws SQLException;
    public void writeLongString(ResultSet rs, int column, String value) throws SQLException;
}