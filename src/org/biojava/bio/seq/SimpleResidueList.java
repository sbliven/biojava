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
 * A no-frills implementation of ResidueList, backed by a java.util.List.
 *
 * @author Matthew Pocock
 */
public class SimpleResidueList implements ResidueList {
  /**
   * The alphabet over which this residue list is taken.
   */
  private Alphabet alphabet;
  
  /**
   * The List of residues that actualy stoors the sequence.
   */
  private List residues;

  public int length() {
    return residues.size();
  }

  public Alphabet alphabet() {
    return alphabet;
  }

  public ResidueList subList(int start, int end) {
    return new SimpleResidueList(alphabet(), residues.subList(start-1, end));
  }

  /**
   * A zero indexed residue list.
   */
  public List toList() {
    return residues;
  }

  /**
   * An iterator over all residues.
   */
  public Iterator iterator() {
    return residues.iterator();
  }

  public void addResidue(Residue res) throws IllegalResidueException {
    alphabet().validate(res);
    residues.add(res);
  }

  public Residue residueAt(int index)
  throws IndexOutOfBoundsException {
    if(index < 1 || index > length()) {
      throw new IndexOutOfBoundsException(
        "Index must be within (1 .. " + length() + "): " + index
      );
    }
    return (Residue) residues.get(index-1);
  }

  public SimpleResidueList(Alphabet alpha) {
    this.alphabet = alpha;
    residues = new ArrayList();
  }

  /**
   * Generates a new SimpleResidueList that shairs the alphabet and residues of
   * rList.
   *
   * @param rList the ResidueList to copy
   */
  public SimpleResidueList(ResidueList rList) {
    this.alphabet = rList.alphabet();
    residues = new ArrayList(rList.toList());
  }

  public String seqString() {
    return subStr(1, length());
  }

  public String subStr(int start, int end) {
    StringBuffer sb = new StringBuffer();
    for(int i = start; i <= end; i++) {
      sb.append( residueAt(i).getSymbol() );
    }
    return sb.toString();
  }

  /**
   * Not safe - doesn't check that rList contains residues only in alpha.
   * Use carefuly.
   *
   * @param alpha the alphabet for this residue list
   * @param rList a list of residues that define the sequence
   */
  public SimpleResidueList(Alphabet alpha, List rList) {
    this.alphabet = alpha;
    this.residues = rList;
  }
}
