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


package org.biojava.bio.symbol;

import java.util.NoSuchElementException;

import org.biojava.bio.*;


public class DoubleAlphabet implements Alphabet {
  public static final DoubleAlphabet INSTANCE = new DoubleAlphabet();
  public static SymbolList fromArray(double [] dArray) {
    return new DoubleArray(dArray);
  }

  public static DoubleAlphabet getInstance() {
    return INSTANCE;
  }

  public static DoubleSymbol getSymbol(double val) {
    return new DoubleSymbol(val);
  }
 
  public Annotation getAnnotation() {
    return Annotation.EMPTY_ANNOTATION;
  }
  
  public boolean contains(Symbol r) {
    return r instanceof DoubleSymbol;
  }
  
  public void validate(Symbol r) throws IllegalSymbolException {
    if(!contains(r)) {
      throw new IllegalSymbolException(
        "Only symbols of type DoubleAlphabet.DoubleSymbol are valid for this alphabet.\n" +
        "(" + r.getClass() + ") " + r.getName()
      );
    }
  }
  
  public String getName() {
    return "Alphabet of all doubles.";
  }
  
  public SymbolParser getParser(String name) {
    throw new NoSuchElementException("No parsers supported by DoubleAlphabet yet");
  }
  
  private DoubleAlphabet() {
  }
  
  public static class DoubleSymbol implements Symbol {
    private double val;
    
    public Annotation getAnnotation() {
      return Annotation.EMPTY_ANNOTATION;
    }
    
    public String getName() {
      return val + "";
    }
    
    public char getToken() {
      return '#';
    }
    
    public double doubleValue() {
      return val;
    }
    
    protected DoubleSymbol(double val) {
      this.val = val;
    }
  }
  
  private static class DoubleArray extends AbstractSymbolList {
    private final double [] dArray;
    
    public Alphabet alphabet() {
      return INSTANCE;
    }
    
    public Symbol symbolAt(int i) {
      return new DoubleSymbol(dArray[i]);
    }
    
    public int length() {
      return dArray.length;
    }
    
    public DoubleArray(double [] dArray) {
      this.dArray = dArray;
    }
  }
}
