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


package org.biojava.bio.seq;

/**
 * The exception to indicate that a residue is not valid within a context.
 * <P>
 * The usual reason for throwing an IllegalResidueException is that you are
 * trying to add a residue to a sequence with an alpabet that does not contain
 * the residue. This is the sequence/alphabet equivalent of a ClassCastException
 * for objects.
 * <P>
 * Frequently, these excepions are actualy generated from Alphabet.validate.
 *
 * @author Matthew Pocock
 */
public class IllegalResidueException extends Exception {
  /**
   * Just make the exception.
   */
  public IllegalResidueException() { super(); }
  
  /**
   * Make the exception with a message.
   */
  public IllegalResidueException(String message) { super(message); }
}
