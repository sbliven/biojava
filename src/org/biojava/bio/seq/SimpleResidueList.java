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
 * A list of residues, starting at index 1
 */
public class SimpleResidueList implements ResidueList {
  private Alphabet alphabet;
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

  public Residue residueAt(int index) {
    return (Residue) residues.get(index-1);
  }

  public SimpleResidueList(Alphabet alpha) {
    this.alphabet = alpha;
    residues = new ArrayList();
  }

  public SimpleResidueList(ResidueList rList) {
    this.alphabet = rList.alphabet();
    residues = new ArrayList(rList.toList());
  }

  /**
   * Not safe - doesn't check rList contains residues in alpha
   */
  public SimpleResidueList(Alphabet alpha, List rList) {
    this.alphabet = alpha;
    this.residues = rList;
  }
}
