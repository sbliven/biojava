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
public class SimpleAlphabet extends AbstractAlphabet implements Serializable {
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
  private final Set ambig;
  
  public Iterator iterator() {
    return symbols.iterator();
  }
  
  public Iterator ambiguities() {
    return ambig.iterator();
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

  public boolean contains(Symbol r) {
    if(r == null) {
      return false;
    } else if(symbols.contains(r)) {
      return true;
    } else  {
      Alphabet sa = r.getMatches();
      if(!(sa instanceof FiniteAlphabet)) {
        return false;
      } else {
        Iterator i = ((FiniteAlphabet) r.getMatches()).iterator();
        while(i.hasNext()) {
          Symbol sym = (Symbol) i.next();
          if(!symbols.contains(sym)) {
            return false;
          }
        }
        return true;
      }
    }
  }

  protected void addSymbolImpl(Symbol s)
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
   *         within this alpahbet
   */
  public void addAmbiguity(Symbol aSym)
  throws IllegalSymbolException {
    validate(aSym);
    ambig.add(aSym);
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

  public void validate(Symbol s) throws IllegalSymbolException {
    if(!contains(s)) {
      if(s == null) {
        throw new IllegalSymbolException("NULL is an illegal symbol");
      } else if (s instanceof AtomicSymbol) { 
        throw new IllegalSymbolException("Symbol " + s.getName() +
                                          " not found in alphabet " +
                                          getName());
      } else {
        Alphabet alpha = s.getMatches();
        if(alpha instanceof FiniteAlphabet) {
          try {
            for(Iterator i = ((FiniteAlphabet) alpha).iterator(); i.hasNext(); ) {
              validate((AtomicSymbol) i.next());
            }
          } catch (IllegalSymbolException ise) {
            throw new IllegalSymbolException(
              ise,
              "Ambiguity symbol " + s.getName() +
              " could not be accepted as it matches an invalid symbol."
            );
          }
          throw new BioError(
            "Symbol " + s.getName() + " isn't contained within the alphabet " +
            getName() +
            " but I can't find which of the matching symbols is invalid"
          );
        } else {
          throw new IllegalSymbolException(
            "This alphabet is finite. The symbol " + s.getName() +
            " matches an infinite number of symbols."
          );
        }
      }
    }
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
    this.symbols = symbols;
    this.ambig = new HashSet();
    this.name = name;
  }
}
