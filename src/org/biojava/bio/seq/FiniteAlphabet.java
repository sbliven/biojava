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

import java.util.NoSuchElementException;

/**
 * An alphabet over a finite set of Residues.
 * <P>
 * This interface makes the distinction between an alphabet over a finite (and
 * possibly small) number of alphabets and an Alphabet over an infinite
 * (or extreemely large) set of tokens. Within a FiniteAlphabet, the == operator
 * should be sufficient to decide upon equality.
 *
 * @author Matthew Pocock
 */
public interface FiniteAlphabet extends Alphabet {
  
  /**
   * The number of residues in the alphabet.
   *
   * @return the size of the alphabet
   */
  int size();
  
  /**
   * A list of residues that make up this alphabet.
   * <P>
   * Subsequent calls to this method are not required to return either the same
   * residue list, or even a residue list with the residues in the same order.
   *
   * @return  a ResidueList containing one Residue for each Residue in this alphabet
   */
  ResidueList residues();
  /**
   * A realy useful static alphabet that is always empty.
   */
  static final FiniteAlphabet EMPTY_ALPHABET = new EmptyAlphabet();
  
  /**
   * The class that implements EmptyAlphabet and is empty.
   */
  public class EmptyAlphabet
  extends Alphabet.EmptyAlphabet implements FiniteAlphabet {
    public int size() {
      return 0;
    }
    
    public ResidueList residues() {
      return ResidueList.EMPTY_LIST;
    }
  }  
}
