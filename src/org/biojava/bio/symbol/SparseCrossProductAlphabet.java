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
 * Cross product of a list of arbitrary alphabets.  This is a memory efficicent
 * implementation of CrossProductAlphabet that instantiates symbols as they are
 * needed. This is required as alphabets can get prohibatively large very
 * quickly (e.g. align 200 proteins & you need 20^200 Symbols).
 * 
 * @author Matthew Pocock
 */

class SparseCrossProductAlphabet
implements FiniteAlphabet, CrossProductAlphabet, Serializable {
  private final int size;
  private final List alphas;
  private final Map knownSymbols;
  private char tokenSeed = 'A';
  
  SparseCrossProductAlphabet(List alphas) {
    this.alphas = alphas;
    knownSymbols = new HashMap();
    int size = 1;
    for(Iterator i = alphas.iterator(); i.hasNext(); ) {
      FiniteAlphabet a = (FiniteAlphabet) i.next();
      size *= a.size();
    }
    this.size = size;
  }
  
  public SymbolList symbols() {
    throw new UnsupportedOperationException(
      "Can't return a list of the symbols in SparseCrossProductAlphabet " +
      getName()
    );
  }
  
  public String getName() {
    StringBuffer name = new StringBuffer("(");
    for (int i = 0; i < alphas.size(); ++i) {
	    Alphabet a = (Alphabet) alphas.get(i);
	    name.append(a.getName());
	    if (i < alphas.size() - 1) {
        name.append(" x ");
      }
    }
    name.append(")");
    return name.toString();
  }

  public int size() {
    return size;
  }
  
  public boolean contains(Symbol s) {
    if(!(s instanceof AtomicSymbol)) {
      Alphabet sa = s.getMatches();
      if(sa instanceof FiniteAlphabet) {
        Iterator i = ((FiniteAlphabet) sa).iterator();
        while(i.hasNext()) {
          AtomicSymbol sym = (AtomicSymbol) i.next();
          if(!this.contains(sym)) {
            return false;
          }
        }
        return true;
      } else {
        return false;
      }
    }

    return knownSymbols.values().contains(s);
  }

  public void validate(Symbol s)
  throws IllegalSymbolException {
    if(!this.contains(s)) {
      throw new IllegalSymbolException(
        "CrossProductAlphabet " + this.getName() + " does not accept " +
        s.getName() + " as it is not an instance of CrossProductSymbol " +
        " or an AmbiguitySymbol over a subset of this alphabet"
      );
    }
  }
  
  
  public Annotation getAnnotation() {
    return Annotation.EMPTY_ANNOTATION;
  }

  public List getAlphabets() {
    return alphas;
  }
  
  public Iterator iterator() {
    return knownSymbols.values().iterator();
  }
  
  public SymbolParser getParser(String name)
  throws NoSuchElementException, BioException {
    if(name == "name") {
      return new CrossProductSymbolNameParser(this);
    }
    throw new NoSuchElementException(
      "No parser for " + name + " is defined for " + getName()
    );
  }

  private ListWrapper gopher =
    new ListWrapper();

  public CrossProductSymbol getSymbol(List rList)
  throws IllegalSymbolException {
    if(rList.size() != alphas.size()) {
      throw new IllegalSymbolException(
        "List of symbols is the wrong length (" + alphas.size() +
        ":" + rList.size() + ")"
      );
    }
    
    CrossProductSymbol r;
    synchronized(gopher) {
      gopher.setList(rList);
      r = (CrossProductSymbol) knownSymbols.get(gopher);
    }

    if(r == null) {
      for(Iterator i = rList.iterator(), j = alphas.iterator(); i.hasNext(); ) {
        Symbol res = (Symbol) i.next();
        Alphabet alp = (Alphabet) j.next();
        try {
          alp.validate(res);
        } catch (IllegalSymbolException ire) {
          throw new IllegalSymbolException(
            ire,
            "Can't retrieve symbol for " +
            AlphabetManager.getCrossProductSymbol('?', rList) + " in alphabet " +
            getName()
          );
        }
      }
      List l = new ArrayList(rList);
      r = AlphabetManager.getCrossProductSymbol(tokenSeed++, l);
      knownSymbols.put(new ListWrapper(r.getSymbols()), r);
    }
    
    return r;
  }
    
  public void addSymbol(Symbol sym) throws IllegalSymbolException {
    throw new IllegalSymbolException(
      "Can't add symbols to alphabet: " + sym.getName() +
      " in " + getName()
    );
  }
  
  public void removeSymbol(Symbol sym) throws IllegalSymbolException {
    throw new IllegalSymbolException(
      "Can't remove symbols from alphabet: " + sym.getName() +
      " in " + getName()
    );
  }

  public void addChangeListener(ChangeListener cl) {}
  public void addChangeListener(ChangeListener cl, ChangeType ct) {}
  public void removeChangeListener(ChangeListener cl) {}
  public void removeChangeListener(ChangeListener cl, ChangeType ct) {} 
}
