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

package org.biojava.bio.seq.io;

import org.biojava.bio.*;

/**
 * ParseException should be thrown to indicate that there was a problem with
 * parsing sequence information.
 *
 * @author Matthew Pocock
 */
public class ParseException extends BioException {
  public ParseException() {
    super();
  }
  
  public ParseException(String message) {
    super(message);
  }
  
  public ParseException(Throwable nested) {
    super(nested);
  }
  
  public ParseException(Throwable nested, String message) {
    super(nested, message);
  }
}
