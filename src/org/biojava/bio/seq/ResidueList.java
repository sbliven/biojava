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

import java.util.*;

/**
 * A sequence of residues that belong to an alphabet.
 * <P>
 * This uses biological coordinates (1 to length).
 *
 * @author Matthew Pocock
 */
public interface ResidueList {
  /**
   * The alphabet that this ResidueList is over.
   * <P>
   * Every residue within this ResidueList is a member of this alphabet.
   * <code>alphabet.contains(residue) == true</code>
   * for each residue that is within this sequence.
   *
   * @return  the alphabet
   */
  Alphabet alphabet();
  
  /**
   * The number of residues in this ResidueList.
   *
   * @return  the length
   */
  int length();

  /**
   * Return the residue at index, counting from 1.
   *
   * @param index the ofset into this ResidueList
   * @return  the Residue at that index
   * @throws IndexOutOfBoundsException if index is less than 1, or greater than
   *                                   the length of the residue list
   */
  Residue residueAt(int index) throws IndexOutOfBoundsException;
  
  /**
   * Returns a List of residues.
   * <p>
   * This is an imutable list of residues. Do not edit it.
   *
   * @return  a List of Residues
   */
  List toList();
  
  /**
   * An Iterator over all Residues in this ResidueList.
   * <p>
   * This is an ordered iterator over the Residues. It cannot be used
   * to edit the underlying residues.
   *
   * @return  an iterator
   */
  Iterator iterator();
  
  /**
   * Return a new ResidueList for the residues start to end inclusive.
   * <P>
   * The resulting ResidueList will count from 1 to (end-start + 1) inclusive, and
   * refer to the residues start to end of the original sequence.
   *
   * @param start the first residue of the new ResidueList
   * @param end the last residue (inclusive) of the new ResidueList
   */
  ResidueList subList(int start, int end) throws IndexOutOfBoundsException;
    
  /**
   * Stringify this residue list.
   * <P>
   * It is expected that this will use the residue's symbol to render each
   * residue. It should be parsable back into a ResidueList using the default
   * symbol parser for this alphabet.
   *
   * @return  a string representation of the residue list
   */
  String seqString();
  
  /**
   * Return a region of this residue list as a String.
   * <P>
   * This should use the same rules as seqString.
   *
   * @param start  the first residue to include
   * @param end the last residue to include
   * @return the string representation
   * @throws IndexOutOfBoundsException if either start or end are not within the
   *         sequence
   */
  String subStr(int start, int end) throws IndexOutOfBoundsException;
  
  /**
   * A usefull object that represents an empty residue list, to avoid returning
   * null.
   *
   * @author Matthew Pocock
   */
  static final ResidueList EMPTY_LIST = new EmptyResidueList();
  
  /**
   * The empty immutable implementation.
   */
  class EmptyResidueList implements ResidueList {
    public Alphabet alphabet() {
      return Alphabet.EMPTY_ALPHABET;
    }
    
    public int length() {
      return 0;
    }
    
    public Residue residueAt(int index) throws IndexOutOfBoundsException {
      throw new IndexOutOfBoundsException("Attempted to retrieve residue from empty list at " + index);
    }
    
    public List toList() {
      return Collections.EMPTY_LIST;
    }
    
    public Iterator iterator() {
      return Collections.EMPTY_LIST.iterator();
    }
    
    public ResidueList subList(int start, int end) throws IndexOutOfBoundsException {
      Collections.EMPTY_LIST.subList(start-1, end);
      return ResidueList.EMPTY_LIST;
    }
    
    public String seqString() {
      return "";
    }
    
    public String subStr(int start, int end) throws IndexOutOfBoundsException {
      throw new IndexOutOfBoundsException(
        "You can not retrieve part of an empty residue list"
      );
    }
  }
}
