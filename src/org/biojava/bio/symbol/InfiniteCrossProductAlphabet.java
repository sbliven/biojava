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

class InfiniteCrossProductAlphabet implements CrossProductAlphabet, Serializable {
  private final List alphas;
  private char tokenSeed = 'A';

  InfiniteCrossProductAlphabet(List alphas) {
    this.alphas = alphas;
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

  public boolean contains(Symbol s) {
    if(! (s instanceof CrossProductSymbol)) {
      return false;
    }
    
    CrossProductSymbol cs = (CrossProductSymbol) s;
    
    List sl = cs.getSymbols();
    if(sl.size() != alphas.size()) {
      return false;
    }
    
    Iterator ai = alphas.iterator();
    Iterator si = sl.iterator();
    
    while(ai.hasNext() && si.hasNext()) {
      Alphabet aa = (Alphabet) ai.next();
      Symbol ss = (Symbol) si.next();
      if(!aa.contains(ss)) {
        return false;
      }
    }
    
    return true;
  }
  
  public void validate(Symbol r) throws IllegalSymbolException {
    if(!this.contains(r)) {
	    throw new IllegalSymbolException(
        "CrossProductAlphabet " + getName() + " does not accept " + r.getName() +
        " as it is not an instance of CrossProductSymbol or " +
        " an AmbiguitySymbol over a subset of symbols in this alphabet."
      );
    }
  }

  public Annotation getAnnotation() {
    return Annotation.EMPTY_ANNOTATION;
  }

  public List getAlphabets() {
    return alphas;
  }
  
  public CrossProductSymbol getSymbol(List rList)
  throws IllegalSymbolException {
    if(rList.size() != alphas.size()) {
      throw new IllegalSymbolException(
        "List of symbols is the wrong length (" + alphas.size() +
        ":" + rList.size() + ")"
      );
    }
    
    Iterator ai = alphas.iterator();
    Iterator ri = rList.iterator();
    
    while(ai.hasNext() && ri.hasNext()) {
      Alphabet aa = (Alphabet) ai.next();
      Symbol rr = (Symbol) ri.next();
      if(!aa.contains(rr)) {
        throw new IllegalSymbolException(
          "CrossProductAlphabet " + getName() + " does not accept " + rList +
          " as symbol " + rr.getName() + " is not a member of the alphabet " +
          aa.getName()
        );
      }
    }
    
    return AlphabetManager.getCrossProductSymbol(tokenSeed++, rList);
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
  
  public void addChangeListener(ChangeListener cl) {}
  public void addChangeListener(ChangeListener cl, ChangeType ct) {}
  public void removeChangeListener(ChangeListener cl) {}
  public void removeChangeListener(ChangeListener cl, ChangeType ct) {} 

}
