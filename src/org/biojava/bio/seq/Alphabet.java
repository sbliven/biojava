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
 * The set of Residues which can be concatinated together to make a ResidueList.
 * <P>
 * 
 */
public interface Alphabet extends Annotatable {
  /**
   * Get the name of the alphabet.
   *
   * @return  the name as a string.
   */
  String getName();
  
  /**
   * The number of residues in the alphabet.
   *
   * @return the size of the alphabet
   */
  int size();
  
  /**
   * A list of residues that make up this alphabet.
   *
   * @return  a ResidueList containing one Residue for each Residue in this alphabet
   */
  ResidueList residues();

  /**
   * Returns wether or not this Alphabet contains the residue.
   *
   * @param r the Residue to check
   * @return  boolean true if the Alphabet contains the residue and false otherwise
   */
  boolean contains(Residue r);

  /**
   * Throws a precanned IllegalResidueException if the residue is not contained
   * within this Alphabet.
   *
   * @param r the Residue to validate
   * @throws  IllegalResidueException if r is not contained in this alphabet
   */
  void validate(Residue r) throws IllegalResidueException;
  
  /**
   * Get a parser by name.
   * <P>
   * The parser returned is guaranteed to return Residues and ResidueLists that
   * conform to this alphabet.
   * Every alphabet should have a ResidueParser under the name 'symbol' that
   * uses the residue symbol characters to translate a string into a ResidueList. Likewise,
   * there should be a ResidueParser under the name 'name' that uses residue names to
   * identify residues.
   *
   * @param name  the name of the parser
   * @return  a parser for that name
   * @exception NoSuchElementException if the name is unknown
   */
  ResidueParser getParser(String name) throws NoSuchElementException;
  
  static final Alphabet EMPTY_ALPHABET = new EmptyAlphabet();
  
  public class EmptyAlphabet implements Alphabet {
    public String getName() {
      return "Empty Alphabet";
    }
    
    public Annotation getAnnotation() {
      return Annotation.EMPTY_ANNOTATION;
    }
    
    public int size() {
      return 0;
    }
    
    public ResidueList residues() {
      return ResidueList.EMPTY_LIST;
    }
    
    public boolean contains(Residue r) {
      return false;
    }
    
    public void validate(Residue res) throws IllegalResidueException {
      throw new IllegalResidueException(
        "The empty alphabet does not contain residue " + res.getName());
    }
    
    public ResidueParser getParser(String name) throws NoSuchElementException {
      throw new NoSuchElementException("There is no parser for the empty alphabet. Attempted to retrieve " + name);
    }
  }  
}
