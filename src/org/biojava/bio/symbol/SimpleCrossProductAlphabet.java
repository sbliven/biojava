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
  private final List alphas;
  private final HashMap ourSymbols;
  private char tokenSeed = 'A';

  /**
   * Create a cross-product alphabet over the list of alphabets in 'a'.
   */
  public SimpleCrossProductAlphabet(List a)
  throws IllegalAlphabetException {
    for(Iterator i = a.iterator(); i.hasNext(); ) {
      Alphabet aa = (Alphabet) i.next();
      if(! (aa instanceof FiniteAlphabet) ) {
        throw new IllegalAlphabetException(
          "Can't create a SimpleAlphabetManager over non-fininte alphabet " +
          aa.getName() + " of type " + aa.getClass()
        );
      }
    }
    alphas = Collections.unmodifiableList(a);
    ourSymbols = new HashMap();
    populateSymbols(new ArrayList());
  }
  
  public Iterator iterator() {
    return ourSymbols.values().iterator();
  }
  
  private void populateSymbols(List r) {
    if (r.size() == alphas.size()) {
	    putSymbol(r);
    } else {
	    int indx = r.size();
	    FiniteAlphabet a = (FiniteAlphabet) alphas.get(indx);
	    Iterator i = a.iterator();
	    r.add(i.next());
	    populateSymbols(r);
	    while (i.hasNext()) {
        r.set(indx, i.next());
        populateSymbols(r);
	    }
	    r.remove(indx);
    }
  }

  private void putSymbol(List r) {
    List l = Collections.unmodifiableList(new ArrayList(r));
    Symbol rr = new SimpleCrossProductSymbol(l, tokenSeed++);
    // System.out.println(rr.getName());
    ourSymbols.put(new AlphabetManager.ListWrapper(l), rr);
  }

  public boolean contains(Symbol s) {
    if(s instanceof AmbiguitySymbol) {
      AmbiguitySymbol as = (AmbiguitySymbol) s;
      Iterator i = ((FiniteAlphabet) as.getMatchingAlphabet()).iterator();
      while(i.hasNext()) {
        Symbol sym = (Symbol) i.next();
        if(!this.contains(sym)) {
          return false;
        }
      }
      return true;
    } else {
      return ourSymbols.values().contains(s);
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
    return new SimpleSymbolList(this, new ArrayList(ourSymbols.values()));
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

  private AlphabetManager.ListWrapper gopher =
    new AlphabetManager.ListWrapper();

  public CrossProductSymbol getSymbol(List l)
  throws IllegalSymbolException {
    CrossProductSymbol r;
    synchronized(gopher) {
      gopher.l = l;
      r = (CrossProductSymbol) ourSymbols.get(gopher);
    }
    if (r == null) {
      throw new IllegalSymbolException(
        "Unable to find CrossProduct symbol for " +
        new SimpleCrossProductSymbol(l, '?').getName() + " in alphabet " + getName()
      );
    }
    return r;
  }
}
