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
 * A single residue.
 * <P>
 * This is the atomic unit of a ResidueList, or a sequence. It allows
 * for fine-train fly-weighting, so that there can be one instance
 * of each residue that is referenced multiple times.
 * <P>
 * Residues are considered unique if they are seperate objects, regardless
 * of any state information. A particular implementation may override this
 * concept so that different Residue objects are considered to be examples
 * of the same type of residue.
 *
 * @author Matthew Pocock
 */
public interface Residue extends Annotatable {
  /**
   * The symbol for the residue.
   *
   * @return  the symbol
   */
  char getSymbol();

  /**
   * The long name for the residue.
   *
   * @return  the long name
   */
  String getName();
}
