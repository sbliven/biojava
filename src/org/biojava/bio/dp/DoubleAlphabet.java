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


package org.biojava.bio.dp;

import java.util.NoSuchElementException;

import org.biojava.bio.seq.*;

public class DoubleAlphabet implements Alphabet {
  public static final DoubleAlphabet INSTANCE = new DoubleAlphabet();
  public static ResidueList fromArray(double [] dArray) {
    return new DoubleArray(dArray);
  }

  public static DoubleAlphabet getInstance() {
    return INSTANCE;
  }

  public static DoubleResidue getResidue(double val) {
    return new DoubleResidue(val);
  }
 
  public Annotation getAnnotation() {
    return Annotation.EMPTY_ANNOTATION;
  }
  
  public boolean contains(Residue r) {
    return r instanceof DoubleResidue;
  }
  
  public void validate(Residue r) throws IllegalResidueException {
    if(!contains(r)) {
      throw new IllegalResidueException(
        "Only residues of type DoubleAlphabet.DoubleResidue are valid for this alphabet.\n" +
        "(" + r.getClass() + ") " + r.getName()
      );
    }
  }
  
  public String getName() {
    return "Alphabet of all doubles.";
  }
  
  public ResidueParser getParser(String name) {
    throw new NoSuchElementException("No parsers supported by DoubleAlphabet yet");
  }
  
  public ResidueList residues() {
    return ResidueList.EMPTY_LIST;
  }
  
  public int size() {
    return Integer.MAX_VALUE;
  }
  
  private DoubleAlphabet() {
  }
  
  public static class DoubleResidue implements Residue {
    private double val;
    
    public Annotation getAnnotation() {
      return Annotation.EMPTY_ANNOTATION;
    }
    
    public String getName() {
      return val + "";
    }
    
    public char getSymbol() {
      return '#';
    }
    
    public double doubleValue() {
      return val;
    }
    
    protected DoubleResidue(double val) {
      this.val = val;
    }
  }
  
  private static class DoubleArray extends AbstractResidueList {
    private final double [] dArray;
    
    public Alphabet alphabet() {
      return INSTANCE;
    }
    
    public Residue residueAt(int i) {
      return new DoubleResidue(dArray[i]);
    }
    
    public int length() {
      return dArray.length;
    }
    
    public DoubleArray(double [] dArray) {
      this.dArray = dArray;
    }
  }
}
