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

import java.io.*;
import java.net.*;

/**
 * Simple URLStreamHandler for acedb: URLs, so that we
 * can use URL objects as appropriate.  This should
 * really be somewhere where it isn't user-visible.
 *
 * @author Matthew Pocock
 */

public class Handler extends URLStreamHandler {
  protected URLConnection openConnection(URL url) throws IOException {
    throw new UnknownServiceException("acedb urls can not be opened");
  }
}
