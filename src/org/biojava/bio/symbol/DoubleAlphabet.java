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

import java.util.*;
import java.io.*;
import java.lang.reflect.*;

import org.biojava.bio.*;
import org.biojava.utils.*;

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
 * the same. You must use the equals method, or compare doubleValue manually.
 *
 * @author Matthew Pocock
 */
public class DoubleAlphabet implements Alphabet, Serializable {
  /**
   * The singleton instance of the DoubleAlphabet class.
   */
  private static final DoubleAlphabet INSTANCE = new DoubleAlphabet();
  private static final SymbolParser PARSER = new SymbolParser() {
    public Alphabet getAlphabet() {
      return INSTANCE;
    }
    
    public Symbol parseToken(String token) {
      return INSTANCE.getSymbol(Double.parseDouble(token));
    }
    
    public SymbolList parse(String seq) {
      throw new BioError("Pants - haven't implemented this yet");
    }
  };
  
  private Object writeReplace() throws ObjectStreamException {
    try {
      return new StaticMemberPlaceHolder(DoubleAlphabet.class.getField("INSTANCE"));
    } catch (NoSuchFieldException nsfe) {
      throw new NotSerializableException(nsfe.getMessage());
    }
  }
  
  /**
   * Retrieve a SymbolList view of an array of doubles.
   * <P>
   * The returned object is a view onto the underlying array, and does not copy
   * it. Changes made to the original array will alter the resulting SymbolList.
   *
   * @param dArray  the array of doubles to view
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
  
  public boolean contains(Symbol s) {
    if(s instanceof DoubleSymbol) {
      return true;
    } else {
      return false;
    }
  }
  
  public void validate(Symbol s) throws IllegalSymbolException {
    if(!contains(s)) {
      throw new IllegalSymbolException(
        "Only symbols of type DoubleAlphabet.DoubleSymbol are valid for this alphabet.\n" +
        "(" + s.getClass() + ") " + s.getName()
      );
    }
  }
  
  public String getName() {
    return "Alphabet of all doubles.";
  }
  
  public SymbolParser getParser(String name) {
    if(!name.equals("name")) {
    	throw new NoSuchElementException(
	  "No parsers supported by DoubleAlphabet called " + name
	);
    }
    return PARSER;    
  }

  public void addChangeListener(ChangeListener cl) {}
  public void addChangeListener(ChangeListener cl, ChangeType ct) {}
  public void removeChangeListener(ChangeListener cl) {}
  public void removeChangeListener(ChangeListener cl, ChangeType ct) {} 
  
  /**
   * A single double value.
   * <P>
   * @author Matthew Pocock
   */
  public static class DoubleSymbol implements AtomicSymbol, Serializable {
    private final double val;
    private final Alphabet matches;
    
    public Annotation getAnnotation() {
      return Annotation.EMPTY_ANNOTATION;
    }
    
    public String getName() {
      return val + "";
    }
    
    public char getToken() {
      return '#';
    }
    
    /**
    *@return the double value associated with this double symbol
    */
    
    public double doubleValue() {
      return val;
    }
    
    public Alphabet getMatches() {
      return matches;
    }
    
    protected DoubleSymbol(double val) {
      this.val = val;
      this.matches = new SingletonAlphabet(this);
    }

    public void addChangeListener(ChangeListener cl) {}
    public void addChangeListener(ChangeListener cl, ChangeType ct) {}
    public void removeChangeListener(ChangeListener cl) {}
    public void removeChangeListener(ChangeListener cl, ChangeType ct) {} 
  }
  
  /**
   * A light-weight implementation of SymbolList that allows an array to
   * appear to be a SymbolList.
   *
   * @author Matthew Pocock
   */
  private static class DoubleArray
  extends AbstractSymbolList implements Serializable {
    private final double [] dArray;
    
    public Alphabet getAlphabet() {
      return INSTANCE;
    }
    
    public Symbol symbolAt(int i) {
      return new DoubleSymbol(dArray[i-1]);
    }
    
    public int length() {
      return dArray.length;
    }
    
    public DoubleArray(double [] dArray) {
      this.dArray = dArray;
    }
  }
}
