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
 * A driver which provides a transport for connecting
 * to an ACeDB server.
 *
 * @author Matthew Pocock
 * @author Thomas Down
 */

public interface Driver {
  /**
   * Returns whether this driver can handle this url type.
   */
  boolean accept(URL url);
  
  /**
   * Return a database that is connected to this url.
   */
  Database connect(URL url) throws AceException;

  /**
   * Return a database that is connected to this url.
   */
  Database connect(URL url, String user, String passwd) throws AceException;
}
