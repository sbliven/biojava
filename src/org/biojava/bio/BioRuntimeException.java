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
package org.biojava.bio;

import org.biojava.utils.NestedRuntimeException;

/**
 * A nestable biological exception.
 *
 * <p>
 * In BioJava, checked exceptions are generally preferred to RuntimeExceptions,
 * but RuntimeExceptions can be used as a fall-back if you are implementing
 * an interface which doesn't support checked exceptions.  If you do this,
 * please document this clearly in the implementing class.
 * </p>
 *
 * @author Matthew Pocock
 * @author Thomas Down
 */

public class BioRuntimeException extends NestedRuntimeException {
  public BioRuntimeException(String message) {
	  super(message);
  }

  public BioRuntimeException(Throwable ex) {
    super(ex);
  }

  public BioRuntimeException(Throwable ex, String message) {
    super(ex, message);
  }
  
  public BioRuntimeException() {
	  super();
  }
}
