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
  private Set symbols;
  
  /**
   * A set of well-known ambiguity symbols.
   */
  private Set ambig;
  
  public Iterator iterator() {
    return symbols.iterator();
  }
  
  public Iterator ambiguities() {
    return ambig.iterator();
  }
  
  public String getName() {
    return name;
  }

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
    return new SimpleSymbolList(this, new ArrayList(symbols));
  }

  public boolean contains(Symbol s) {
    if(s == null) {
      return false;
    } else if(symbols.contains(s)) {
      return true;
    } else if(s instanceof AmbiguitySymbol) {
      AmbiguitySymbol as = (AmbiguitySymbol) s;
      Iterator i = ((FiniteAlphabet) as.getMatchingAlphabet()).iterator();
      while(i.hasNext()) {
        Symbol sym = (Symbol) i.next();
        if(!symbols.contains(sym)) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * Adds a symbol to this alphabet.
   * <P>
   * If the symbol is an ambiguity symbol, then each symbol matching it will be
   * added.
   *
   * @param s the Symbol to add
   * @throws IllegalSymbolException if the symbol is null, or if for any reason
   *         it can't be added
   */
  public void addSymbol(Symbol s)
  throws IllegalSymbolException {
    if(s == null) {
      throw new IllegalSymbolException(
        "You can not add null as a symbol to alphabet " + getName()
      );
    }
    if(s instanceof AmbiguitySymbol) {
      AmbiguitySymbol as = (AmbiguitySymbol) s;
      Iterator i = ((FiniteAlphabet) as.getMatchingAlphabet()).iterator();
      while(i.hasNext()) {
        Symbol sym = (Symbol) i.next();
        symbols.add(sym);
      }
    } else {
      symbols.add(s);
    }
  }
  
  /**
   * Add a commonly recognized ambiguiy symbol to this alphabet.
   *
   * @param as the AmbiguitySymbol to add
   * @throws IllegalSymbolException if as contains Symbols not contained within
   *         this alpahbet
   */
  public void addAmbiguity(AmbiguitySymbol as)
  throws IllegalSymbolException {
    validate(as);
    ambig.add(as);
  }
  
  /**
   * Remove a symbol from this alphabet.
   * <P>
   * If the symbol is an ambiguity symbol, then each symbol matching it will be
   * removed.
   *
   * @param s the Symbol to remove
   * @throws IllegalSymbolException if the symbol is null, or if for any reason
   *         it can't be removed
   */
  public void removeSymbol(Symbol s)
  throws IllegalSymbolException {
    validate(s);
    if(s instanceof AmbiguitySymbol) {
      AmbiguitySymbol as = (AmbiguitySymbol) s;
      Iterator i = ((FiniteAlphabet) as.getMatchingAlphabet()).iterator();
      while(i.hasNext()) {
        Symbol sym = (Symbol) i.next();
        symbols.remove(sym);
      }
    } else {
      symbols.remove(s);
    }
  }

  public void validate(Symbol s) throws IllegalSymbolException {
    if(!contains(s)) {
      if(s == null) {
        throw new IllegalSymbolException("NULL is an illegal symbol");
      } else if (s instanceof AmbiguitySymbol) {
        try {
          FiniteAlphabet fa = (FiniteAlphabet) ((AmbiguitySymbol) s).getMatchingAlphabet();
          for(Iterator i = fa.iterator(); i.hasNext(); ) {
            validate((Symbol) i.next());
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
        throw new IllegalSymbolException("Symbol " + s.getName() +
                                          " not found in alphabet " +
                                          getName());
      }
    }
  }
  
  public SimpleAlphabet() {
    symbols	= new HashSet();
    ambig = new HashSet();
  }
  
  public SimpleAlphabet(Set symbols) {
    this.symbols = symbols;
  }
}
