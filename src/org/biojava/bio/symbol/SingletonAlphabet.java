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

import java.io.Serializable;
import java.util.*;

import org.biojava.bio.*;

/**
 * A no-frills implementation of a symbol.
 *
 * @author Matthew Pocock
 */
public class SingletonAlphabet implements FiniteAlphabet, Serializable {
  private final AtomicSymbol sym;
  
  public SingletonAlphabet(AtomicSymbol sym) {
    this.sym = sym;
  }
  
  public boolean contains(Symbol s) {
    return
      s == sym ||
      s == AlphabetManager.getGapSymbol();
  }
  
  public void validate(Symbol s)
  throws IllegalSymbolException {
    if(!contains(s)) {
      throw new IllegalSymbolException(
        "The alphabet " + getName() + " does not contain the symbol " + s.getName()
      );
    }
  }
  
  public String getName() {
    return sym.getName() + "-alphabet";
  }
  
  public SymbolParser getParser(String name)
  throws NoSuchElementException, BioException {
    throw new NoSuchElementException(
      "No parsers associated with " + getName() +
      ": " + name
    );
  }
  
  public Iterator iterator() {
    return Collections.singleton(sym).iterator();
  }
  
  public int size() {
    return 1;
  }
  
  public Annotation getAnnotation() {
    return Annotation.EMPTY_ANNOTATION;
  }
  
  public SymbolList symbols() {
    try {
      return new SimpleSymbolList(this, Collections.nCopies(1, sym));
    } catch (IllegalSymbolException ise) {
      throw new BioError(ise, "This is impossible. I must contain me.");
    }
  }
}
