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


package org.acedb;

import java.net.URL;

/**
 * An encapsulation of an ACeDB Database server.  The Database
 * implementation is aware of the transport used to connect to
 * the server, and may hold a pool of connections.
 *
 * @author Matthew Pocock
 * @author Thomas Down
 */

public interface Database {
  /**
   * Performs a search, and returns an AceSet with the results.
   *
   * @param classType an object representing the class to
   *                  search.
   * @param namePattern an object name pattern.  Can contain * wildcards.
   * @returns an AceSet containing 0 or more matches.
   * @throws AceException if the search fails.
   */
  public AceSet select(AceType.ClassType classType, String namePattern)
      throws AceException;
  
  /**
   * Retrieves the model for a class.
   */
    // public ModelNode getModel(AceType.ClassType classType);
  
    /**
     * Return a connection object which allows ACeDB commands to
     * be executed directly on the server.
     */

    public Connection getConnection() throws AceException;

  /**
   * Returns the url to the database.
   */
  public URL toURL();

    /**
     * Get a <em>single</em> object from the database.  This
     * is provided primarily for internal use in ACeDBC drivers.
     * Normal clients should normally use search instead.
     */

    public AceObject getObject(AceType.ClassType classType, String name)
	throws AceException;
}

