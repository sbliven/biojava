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

import org.biojava.utils.*;
import org.biojava.bio.*;

/**
 * A simple no-frills implementation of the FiniteAlphabet interface.
 *
 * @author Matthew Pocock
 */
public class SimpleAlphabet
extends AbstractAlphabet
implements Serializable {
  /**
   * The name of this alphabet.
   */
  private String name;
  
  /**
   * The annotation associated with this alphabet.
   */
  private Annotation annotation;
  
  /**
   * A set of the non-ambiguity symbols within the alphabet.
   */
  private final Set symbols;
  
  /**
   * A set of well-known ambiguity symbols.
   */
  private final Map ambigMap;
  private final Set ambig;
  
  /**
   * A list of alphabets that make up this one - a singleton list containing
   * this.
   */
  private List alphabets;
  
  public Iterator iterator() {
    return symbols.iterator();
  }
  
  public String getName() {
    return name;
  }
  
  /**
   * Assign a name to the alphabet
   * @param name the name you wish to give this alphabet
   */
  public void setName(String name) {
    this.name = name;
  }
    
  public Annotation getAnnotation() {
    if(annotation == null)
      annotation = new SimpleAnnotation();
    return annotation;
  }

  public int size() {
    return symbols.size();
  }
  
  public SymbolList symbols() {
    try {
      return new SimpleSymbolList(this, new ArrayList(symbols));
    } catch (IllegalSymbolException ex) {
      throw new BioError(
        ex,
        "There should be no circumstances under which this failed"
      );
    }
  }

  protected boolean containsImpl(AtomicSymbol s) {
    return symbols.contains(s);
  }

  protected void addSymbolImpl(AtomicSymbol s)
  throws IllegalSymbolException, ChangeVetoException {
    symbols.add(s);
  }
  
  /**
   * Add a commonly recognized ambiguiy symbol to this alphabet.
   * <P>
   * This effectively forges an alias between aSym and the symbols in its
   * 'matches' alphabet for all derived parsers.
   *
   * @param as the ambiguity symbol to add
   * @throws IllegalSymbolException if aSym contains an AtomicSymbol not found
   *         within this alphabet
   */
  private void addAmbiguity(Symbol aSym)
  throws IllegalSymbolException {
    validate(aSym);
    if (ambig.contains(aSym))
	throw new IllegalSymbolException("Can't add " + aSym.getName() + " twice");

    Set key = new HashSet();
    for (Iterator i = ((FiniteAlphabet) aSym.getMatches()).iterator(); i.hasNext(); ) {
	key.add(i.next());
    }
    ambig.add(aSym);
    ambigMap.put(key, aSym);
  }

    public Symbol getAmbiguity(Set syms) 
        throws IllegalSymbolException
    {
	Symbol a = (Symbol) ambigMap.get(syms);
	if (a == null) {
	    a = super.getAmbiguity(syms);
	    addAmbiguity(a);
	}
	return a;
    }

  public void removeSymbol(Symbol s)
  throws IllegalSymbolException {
    validate(s);
    if(s instanceof AtomicSymbol) {
      symbols.remove(s);
    } else {
      FiniteAlphabet sa = (FiniteAlphabet) s.getMatches();
      Iterator i = ((FiniteAlphabet) sa).iterator();
      while(i.hasNext()) {
        Symbol sym = (Symbol) i.next();
        symbols.remove(sym);
      }
    }
  }

  public List getAlphabets() {
    if(this.alphabets == null) {
      this.alphabets = new SingletonList(this);
    }
    return this.alphabets;
  }
  
  public AtomicSymbol getSymbolImpl(List symL)
  throws IllegalSymbolException {
    AtomicSymbol s = (AtomicSymbol) symL.get(0);
    return s;
  }
  
  public SimpleAlphabet() {
    this(new HashSet(), null);
  }
  
  public SimpleAlphabet(Set symbols) {
    this(symbols, null);
  }
  
  public SimpleAlphabet(String name) {
    this(new HashSet(), name);
  }

  public SimpleAlphabet(Set symbols, String name) {
    this.symbols = new HashSet();
    this.ambig = new HashSet();
    this.ambigMap = new HashMap();
    this.name = name;
    this.alphabets = null;
    
    // this costs, but I am tracking down a ClassCast exception.
    // roll on parameterised types.
    for(Iterator i = symbols.iterator(); i.hasNext(); ) {
      AtomicSymbol a = (AtomicSymbol) i.next();
      this.symbols.add(a);
    }
  }
}
