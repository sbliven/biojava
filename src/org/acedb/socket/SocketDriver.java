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


package org.acedb.socket;

import java.net.URL;
import org.acedb.*;

/**
 * @author Thomas Down
 * @author Matthew Pocock
 */

public class SocketDriver implements Driver {
  public boolean accept(URL url) {
    return url.getProtocol().equals("acedb");
  }
  
  public Database connect(URL url) throws AceException {
    return new SocketDatabase(url);
  }
}
