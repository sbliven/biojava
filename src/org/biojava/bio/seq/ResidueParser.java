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
 * These objects are responsible for converting strings into Residues and ResidueLists.
 */
public interface ResidueParser {
  /**
   * The alphabet that all residues produced will belong to.
   *
   * @return  the Alphabet
   */
  Alphabet alphabet();
  
  /**
   * Parse an entire string into a ResidueList.
   * <P>
   * The ResidueList produced will have the same Alphabet as this ResidueParser.
   *
   * @param seq the String to parse
   * @return  the ResidueList containing the parsed value of the String
   * @throws  IllegalResidueException if any part of the String can not be parsed
   */
  ResidueList parse(String seq) throws IllegalResidueException;
  
  /**
   * Returns the residue for a single token.
   * <P>
   * The Residue will be a member of the alphabet. If the token is not recognized
   * as mapping to a residue, an exception will be thrown.
   *
   * @param token the token to retrieve a Residue for
   * @return the Residue for that token
   * @throws an IllegalResidueException if there is no Residue for the token
   */
  Residue parseToken(String token) throws IllegalResidueException;
}
