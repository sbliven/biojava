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
 * BioException.java
 *
 * Coppied from AceError By Thomas Down <td2@sanger.ac.uk>
 */

package org.biojava.bio;

import org.biojava.utils.NestedError;

/**
 * A nestable biological error.
 *
 * @author Matthew Pocock
 */
public class BioError extends NestedError {
  public BioError(String message) {
	  super(message);
  }

  public BioError(Throwable ex) {
    super(ex);
  }

  public BioError(Throwable ex, String message) {
    super(ex, message);
  }
  
  public BioError() {
	  super();
  }
}
