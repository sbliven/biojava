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

/**
 * An efficient implementation of an Alphabet over the infinite set of double
 * values.
 * <P>
 * This class can be used to represent lists of floating-point numbers as a
 * SymbolList with the alphabet DoubleAlphabet. These lists can then be
 * annotated with features, or fed into dynamic-programming algorithms, or
 * processed as per any other SymbolList object.
 * <P>
 * Object identity can not be used to decide if two DoubleResidue objects are
 * the same. You must use the equals method, or compare doubleValue manualy.
 *
 * @author Matthew Pocock
 */
public class DoubleAlphabet implements Alphabet {
  /**
   * The singleton instance of the DoubleAlphabet class.
   */
  private static final DoubleAlphabet INSTANCE = new DoubleAlphabet();
  
  /**
   * Retrieve a SymbolList view of an array of doubles.
   * <P>
   * The returned object is a view onto the underlying array, and does not copy
   * it. Changes made to the original array will alter the resulting SymbolList.
   *
   * @param dArray  the arrou of doubles to view
   * @return a SymbolList over the DoubleAlphabet that represent the values in
   *         dArray
   */
  public static SymbolList fromArray(double [] dArray) {
    return new DoubleArray(dArray);
  }

  /**
   * Retrieve the single DoubleAlphabet instance.
   *
   * @return the singleton DoubleAlphabet instance
   */
  public static DoubleAlphabet getInstance() {
    return INSTANCE;
  }

  /**
   * Retrieve the Symbol for a double.
   *
   * @param val  the double to view
   * @return a DoubleSymbol embodying val
   */
  public DoubleSymbol getSymbol(double val) {
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
  
  /**
   * A single double value.
   * <P>
   * @author Matthew Pocock
   */
  public static class DoubleSymbol implements Symbol {
    private final double val;
    
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
  
  /**
   * A light-weight implementation of SymbolList that allows an array to
   * appear to be a SymbolList.
   *
   * @author Matthew Pocock
   */
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
