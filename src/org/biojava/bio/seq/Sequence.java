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

public interface Sequence extends ResidueList, FeatureHolder, Annotatable {
  /**
   * The URN for this sequence. This will be something like
   * <code>urn:sequence/embl:U32766</code> or
   * <code>urn:sequence/fasta:sequences.fasta|hox3</code>.
   *
   * @return the urn as a String
   */
  String getURN();
  
  /**
   * The name of this sequence.
   * <P>
   * The name may contain spaces or odd characters.
   *
   * @return the name as a String
   */
  String getName();
  
  /**
   * Stringify this sequence.
   * <P>
   * It is expected that this will use the residue's symbol or name methods.
   *
   * @return  a string representation of the sequence
   */
  String seqString();
  
  /**
   * Return a region of this sequence as a String.
   * <P>
   * This should use the same rules as seqString.
   *
   * @param start  the first residue to include
   * @param end the last residue to include
   * @return the string representation
   */
  String subStr(int start, int end);
}
