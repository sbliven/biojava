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
 * Cross product of a list of arbitrary alphabets.  This is the
 * most flexible implementation of CrossProductAlphabet, but it
 * is likely to be possible to produce more efficient implementations
 * for specific tasks.
 * 
 * @author Thomas Down
 * @author Matthew Pocock
 */

class SimpleCrossProductAlphabet
implements FiniteAlphabet, CrossProductAlphabet, Serializable {
  private final CrossProductAlphabet parent;
  private final List alphas;
  private final HashMap ourSymbols;
  private char tokenSeed = 'A';

  /**
   * Create a cross-product alphabet over the list of alphabets in 'a'.
   */
  public SimpleCrossProductAlphabet(List a)
  throws IllegalAlphabetException {
    this(a, null);
  }
  
  public SimpleCrossProductAlphabet(List a, CrossProductAlphabet parent)
  throws IllegalAlphabetException {
    this.parent = parent;
    for(Iterator i = a.iterator(); i.hasNext(); ) {
      Alphabet aa = (Alphabet) i.next();
      if(! (aa instanceof FiniteAlphabet) ) {
        throw new IllegalAlphabetException(
          "Can't create a SimpleAlphabetManager over non-fininte alphabet " +
          aa.getName() + " of type " + aa.getClass()
        );
      }
    }
    alphas = Collections.unmodifiableList(new ArrayList(a));
    ourSymbols = new HashMap();
    populateSymbols(new ArrayList());
  }
  
  public Iterator iterator() {
    return ourSymbols.values().iterator();
  }
  
  private void populateSymbols(List symList) {
    if (symList.size() == alphas.size()) {
	    putSymbol(symList);
    } else {
	    int indx = symList.size();
	    FiniteAlphabet a = (FiniteAlphabet) alphas.get(indx);
	    Iterator i = a.iterator();
      if(i.hasNext()) {
        symList.add(i.next());
        populateSymbols(symList);
        while (i.hasNext()) {
          symList.set(indx, i.next());
          populateSymbols(symList);
        }
        symList.remove(indx);
      }
    }
  }

  private void putSymbol(List r) {
    if(r.size() == 0) {
      return;
    }
    CrossProductSymbol rr;
    if(parent != null) {
      try {
        rr = parent.getSymbol(r);
      } catch (IllegalSymbolException ise) {
        throw new BioError(ise, "Balls up - couldn't fetch symbol from parent");
      }
    } else {
      rr = new AtomicCrossProductSymbol(tokenSeed++, r);
    }
    ourSymbols.put(new ListWrapper(rr.getSymbols()), rr);
  }

  public boolean contains(Symbol s) {
    if(ourSymbols.values().contains(s)) {// have seen it before
      return true;
    } else if(s == null) {
      throw new NullPointerException("Can't use null as a symbol");
    } else if(!(s instanceof AtomicSymbol)) { // ambiguity
      Alphabet sa = s.getMatches();
      if(sa instanceof FiniteAlphabet) {
        Iterator i = ((FiniteAlphabet) sa).iterator();
        while(i.hasNext()) {
          CrossProductSymbol sym = (CrossProductSymbol) i.next();
          if(!this.contains(sym)) {
            return false;
          }
        }
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
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

  public SymbolParser getParser(String name)
  throws NoSuchElementException, BioException {
    if(name == "name") {
      return new CrossProductSymbolNameParser(this);
    }
    throw new NoSuchElementException(
      "No parser for " + name + " is defined for " + getName()
    );
  }

  public SymbolList symbols() {
      try {
	  return new SimpleSymbolList(this, new ArrayList(ourSymbols.values()));
      } catch (IllegalSymbolException ex) {
	  throw new BioError(ex);
      }
  }

  public int size() {
    return ourSymbols.size();
  }

  public void validate(Symbol s) throws IllegalSymbolException {
    if (!contains(s)) {
	    throw new IllegalSymbolException("Alphabet " + getName() + " does not accept " + s.getName());
    }
  }

  public Annotation getAnnotation() {
    return Annotation.EMPTY_ANNOTATION;
  }

  public List getAlphabets() {
    return alphas;
  }

  private ListWrapper gopher =
    new ListWrapper();

  public CrossProductSymbol getSymbol(List l)
  throws IllegalSymbolException {
    CrossProductSymbol cps;
    synchronized(gopher) {
      gopher.setList(l);
      cps = (CrossProductSymbol) ourSymbols.get(gopher);
    }
    if (cps == null) {
      cps = AlphabetManager.getCrossProductSymbol('?', l, this);
      if(this.contains(cps)) {
        return cps;
      } else {
        throw new IllegalSymbolException(
          "Unable to find CrossProduct symbol for " + cps.getClass() +
          cps.getName() + " in alphabet " + getName()
        );
      }
    } else {
      return cps;
    }
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
