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
import org.biojava.utils.cache.*;
import org.biojava.bio.seq.io.*;

/**
 * <p>
 * An efficient implementation of an Alphabet over the infinite set of double
 * values.
 * </p>
 *
 * <p>
 * This class can be used to represent lists of floating-point numbers as a
 * SymbolList with the alphabet DoubleAlphabet. These lists can then be
 * annotated with features, or fed into dynamic-programming algorithms, or
 * processed as per any other SymbolList object.
 * </p>
 *
 * <p>
 * Object identity should be used to decide if two DoubleResidue objects are
 * the same. DoubleAlpabet ensures that all DoubleAlphabet instances are
 * canonicalized.
 * </p>
 *
 * @author Matthew Pocock
 * @author Thomas Down
 */

public final class DoubleAlphabet
  extends
    Unchangeable
  implements
    Alphabet,
    Serializable
{

  public static DoubleAlphabet INSTANCE;

  private Object writeReplace() throws ObjectStreamException {
    try {
      return new StaticMemberPlaceHolder(DoubleAlphabet.class.getField("INSTANCE"));
    } catch (NoSuchFieldException nsfe) {
      throw new NotSerializableException(nsfe.getMessage());
    }
  }

  /**
   * <p>
   * Retrieve a SymbolList view of an array of doubles.
   * </p>
   *
   * <p>
   * The returned object is a view onto the underlying array, and does not copy
   * it. Changes made to the original array will alter the resulting SymbolList.
   * </p>
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
    if(INSTANCE == null) {
      INSTANCE = new DoubleAlphabet();
    }

    return INSTANCE;
  }

    private List alphabets = null;
    private WeakValueHashMap doubleToSym;

    private DoubleAlphabet() {
	doubleToSym = new WeakValueHashMap();
    }

  /**
   * Retrieve the Symbol for a double.
   *
   * @param val  the double to view
   * @return a DoubleSymbol embodying val
   */
  public DoubleSymbol getSymbol(double val) {
      Double d = new Double(val);
      DoubleSymbol sym = (DoubleSymbol) doubleToSym.get(d);
      if (sym== null) {
	  sym = new DoubleSymbol(val);
	  doubleToSym.put(d, sym);
      }
      return sym;
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

  public List getAlphabets() {
    if(alphabets == null) {
      alphabets = new SingletonList(this);
    }
    return alphabets;
  }

  public Symbol getGapSymbol() {
    return AlphabetManager.getGapSymbol(getAlphabets());
  }

  public Symbol getAmbiguity(Set syms) throws IllegalSymbolException {
    for(Iterator i = syms.iterator(); i.hasNext(); ) {
      Symbol sym = (Symbol) i.next();
      validate(sym);
    }
    throw new BioError("Operation not implemented");
  }

  public Symbol getSymbol(List symList) throws IllegalSymbolException {
    if(symList.size() != 1) {
      throw new IllegalSymbolException(
        "Can't build symbol from list " + symList.size() + " long"
      );
    }

    Symbol s = (Symbol) symList.get(0);
    validate(s);
    return s;
  }

  public String getName() {
    return "Alphabet of all doubles.";
  }

  public SymbolTokenization getTokenization(String name) {
    if(!name.equals("name")) {
    	throw new NoSuchElementException(
	  "No parsers supported by DoubleAlphabet called " + name
	);
    }
    return new DoubleTokenization();
  }

  /**
   * A single double value.
   *
   * @author Matthew Pocock
   */
  public static class DoubleSymbol
    extends
      Unchangeable
    implements
      AtomicSymbol,
      Serializable
  {
    private final double val;
    private final Alphabet matches;

    public Annotation getAnnotation() {
      return Annotation.EMPTY_ANNOTATION;
    }

    public String getName() {
      return val + "";
    }

    /**
     * @return the double value associated with this double symbol
     */
    public double doubleValue() {
      return val;
    }

    public Alphabet getMatches() {
      return matches;
    }

    public List getSymbols() {
      return new SingletonList(this);
    }

    public Set getBases() {
      return Collections.singleton(this);
    }

    protected DoubleSymbol(double val) {
      this.val = val;
      this.matches = new SingletonAlphabet(this);
    }
  }

  /**
   * A light-weight implementation of SymbolList that allows an array to
   * appear to be a SymbolList.
   *
   * @author Matthew Pocock
   */
  private static class DoubleArray
  extends
    AbstractSymbolList
  implements
    Serializable
  {
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
