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

/**
 * A no-frills implementation of SymbolList, backed by a java.util.List.
 *
 * @author Matthew Pocock
 */
public class SimpleSymbolList implements SymbolList, Serializable {
  /**
   * The alphabet over which this symbol list is taken.
   */
  private Alphabet alphabet;
  
  /**
   * The List of symbols that actualy stoors the sequence.
   */
  private List symbols;

  public int length() {
    return symbols.size();
  }

  public Alphabet alphabet() {
    return alphabet;
  }

  public SymbolList subList(int start, int end) {
    return new SimpleSymbolList(alphabet(), symbols.subList(start-1, end));
  }

  /**
   * A zero indexed symbol list.
   */
  public List toList() {
    return symbols;
  }

  /**
   * An iterator over all symbols.
   */
  public Iterator iterator() {
    return symbols.iterator();
  }

  public void addSymbol(Symbol res) throws IllegalSymbolException {
    alphabet().validate(res);
    symbols.add(res);
  }

  public Symbol symbolAt(int index)
  throws IndexOutOfBoundsException {
    if(index < 1 || index > length()) {
      throw new IndexOutOfBoundsException(
        "Index must be within (1 .. " + length() + "): " + index
      );
    }
    return (Symbol) symbols.get(index-1);
  }

  public SimpleSymbolList(Alphabet alpha) {
    this.alphabet = alpha;
    symbols = new ArrayList();
  }

  /**
   * Generates a new SimpleSymbolList that shairs the alphabet and symbols of
   * rList.
   *
   * @param rList the SymbolList to copy
   */
  public SimpleSymbolList(SymbolList rList) {
    this.alphabet = rList.alphabet();
    symbols = new ArrayList(rList.toList());
  }

  public String seqString() {
    return subStr(1, length());
  }

  public String subStr(int start, int end) {
    StringBuffer sb = new StringBuffer();
    for(int i = start; i <= end; i++) {
      sb.append( symbolAt(i).getToken() );
    }
    return sb.toString();
  }

  /**
   * Not safe - doesn't check that rList contains symbols only in alpha.
   * Use carefuly.
   *
   * @param alpha the alphabet for this symbol list
   * @param rList a list of symbols that define the sequence
   */
  public SimpleSymbolList(Alphabet alpha, List rList) {
    this.alphabet = alpha;
    this.symbols = rList;
  }
}
