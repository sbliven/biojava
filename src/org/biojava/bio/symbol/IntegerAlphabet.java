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
import org.biojava.bio.seq.io.*;
import org.biojava.utils.*;

/**
 * An efficient implementation of an Alphabet over the infinite set of integer
 * values.
 * <P>
 * This class can be used to represent lists of integer numbers as a
 * SymbolList with the alphabet IntegerAlphabet. These lists can then be
 * annotated with features, or fed into dynamic-programming algorithms, or
 * processed as per any other SymbolList object.
 * <P>
 * Object identity can not be used to decide if two IntegerResidue objects are
 * the same. You must use the equals method, or compare intValue manually.
 *
 * @author Matthew Pocock
 * @author Mark Schreiber
 */
public class IntegerAlphabet implements Alphabet, Serializable {
  /**
   * The singleton instance of the IntegerAlphabet class.
   */
  private static final IntegerAlphabet INSTANCE = new IntegerAlphabet();

  private Object writeReplace() throws ObjectStreamException {
    try {
      return new StaticMemberPlaceHolder(IntegerAlphabet.class.getField("INSTANCE"));
    } catch (NoSuchFieldException nsfe) {
      throw new NotSerializableException(nsfe.getMessage());
    }
  }

  /**
   * Retrieve a SymbolList view of an array of integers.
   * <P>
   * The returned object is a view onto the underlying array, and does not copy
   * it. Changes made to the original array will alter the symulting SymbolList.
   *
   * @param iArray  the array of integers to view
   * @return a SymbolList over the IntegerAlphabet that represent the values in
   *         iArray
   */
  public static SymbolList fromArray(int [] iArray) {
    return new IntegerArray(iArray);
  }

  /**
   * Retrieve the single IntegerAlphabet instance.
   *
   * @return the singleton IntegerAlphabet instance
   */
  public static IntegerAlphabet getInstance() {
    return INSTANCE;
  }

  /**
   * Retrieve the Symbol for an int.
   *
   * @param val  the int to view
   * @return a IntegerSymbol embodying val
   */
  public IntegerSymbol getSymbol(int val) {
    return new IntegerSymbol(val);
  }

  public Symbol getGapSymbol() {
    return AlphabetManager.getGapSymbol(getAlphabets());
  }

  public Annotation getAnnotation() {
    return Annotation.EMPTY_ANNOTATION;
  }

  public List getAlphabets() {
    return new SingletonList(this);
  }

  public Symbol getSymbol(List symList)
  throws IllegalSymbolException {
    throw new BioError("Unimplemneted method");
  }

  public Symbol getAmbiguity(Set symSet)
  throws IllegalSymbolException {
    throw new BioError("Unimplemneted method");
  }

  public boolean contains(Symbol s) {
    if(s instanceof IntegerSymbol) {
      return true;
    } else {
      return false;
    }
  }

  public void validate(Symbol s) throws IllegalSymbolException {
    if(!contains(s)) {
      throw new IllegalSymbolException(
        "Only symbols of type IntegerAlphabet.IntegerSymbol are valid for this alphabet.\n" +
        "(" + s.getClass() + ") " + s.getName()
      );
    }
  }

  public String getName() {
    return "Alphabet of all integers.";
  }

  /**
   * @param name Currently only "token" is supported.
   * @return an IntegerParser.
   * @author Mark Schreiber 3 May 2001.
   */
  public SymbolParser getParser(String name) {
    if(name.equals("token")){
      return new IntegerParser();
    }else{
      throw new NoSuchElementException(name + " parser not supported by IntegerAlphabet yet");
    }
  }

  public void addChangeListener(ChangeListener cl) {}
  public void addChangeListener(ChangeListener cl, ChangeType ct) {}
  public void removeChangeListener(ChangeListener cl) {}
  public void removeChangeListener(ChangeListener cl, ChangeType ct) {}

  /**
   * A single int value.
   * <P>
   * @author Matthew Pocock
   */
  public static class IntegerSymbol implements AtomicSymbol, Serializable {
    private final int val;
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

    public int intValue() {
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

    protected IntegerSymbol(int val) {
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
  private static class IntegerArray
  extends AbstractSymbolList implements Serializable {
    private final int [] iArray;

    public Alphabet getAlphabet() {
      return INSTANCE;
    }

    public Symbol symbolAt(int i) {
      return new IntegerSymbol(iArray[i-1]);
    }

    public int length() {
      return iArray.length;
    }

    public IntegerArray(int [] iArray) {
      this.iArray = iArray;
    }
  }
}
