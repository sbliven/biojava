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
     * Return a connection object which allows ACeDB commands to
     * be executed directly on the server.
     */

    public Connection getConnection() throws AceException;

  /**
   * Returns the url to the database.
   */
  public AceURL toURL();

    /**
     * Return objects from the database.
     */

    public AceSet fetch(AceURL url) throws AceException;
}

