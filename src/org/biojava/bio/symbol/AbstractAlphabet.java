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

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.io.*;

/**
 * An abstract implementation of Alphabet.
 * <P>
 * This provides the frame-work for maintaining the SymbolParser <-> name
 * mappings and also for the ChangeListeners.
 * <P>
 * This class is for developers to derive from, not for use directly.
 *
 * @author Matthew Pocock
 */
public abstract class AbstractAlphabet implements FiniteAlphabet {
  private Map parserByName;
  private ChangeSupport changeSupport;

  {
    parserByName = new HashMap();
  }
  
  protected boolean hasListeners() {
    return changeSupport != null;
  }
  
  protected ChangeSupport getChangeSupport(ChangeType ct) {
    if(changeSupport == null) {
      changeSupport = new ChangeSupport();
    }
    
    return changeSupport;
  }
  
  /**
   * Assigns a symbol parser to a String object.
   * <P>
   * Afterwards, the parser can be retrieved using the getParser method.
   *
   * @param name Name of the string to associate with a parser
   * @param parser The parser to associate your String with
   */
  public void putParser(String name, SymbolParser parser) {
    parserByName.put(name, parser);
  }

  public SymbolParser getParser(String name)
         throws NoSuchElementException {
    SymbolParser parser = (SymbolParser) parserByName.get(name);
    if(parser == null) {
      if(name.equals("token")) {
        parser = new TokenParser(this);
        putParser(name, parser);
      } else if(name.equals("name")) {
        parser = new NameParser(this);
        putParser(name, parser);
      } else {
        throw new NoSuchElementException("There is no parser '" + name +
                                         "' defined in alphabet " + getName());
      }
    }
    return parser;
  }
  
  public final Symbol getAmbiguity(Set syms)
  throws IllegalSymbolException {
    return AlphabetManager.createSymbol(
      '*', "?", Annotation.EMPTY_ANNOTATION,
      syms, this
    );
  }
  
  public final Symbol getSymbol(List syms)
  throws IllegalSymbolException {
    List alphas = getAlphabets();
    
    if(alphas.size() != syms.size()) {
      throw new IllegalSymbolException(
        "Can't retrieve symbol as symbol list is the wrong length " +
        syms.size() + ":" + alphas.size()
      );
    }
    
    Iterator si = syms.iterator();
    Iterator ai = getAlphabets().iterator();
    int atomic = 0;
    while(si.hasNext()) {
      Symbol s = (Symbol) si.next();
      Alphabet a = (Alphabet) ai.next();
      a.validate(s);
      if(s instanceof AtomicSymbol) {
        atomic++;
      }
    }
    
    if(atomic == syms.size()) {
      return getSymbolImpl(syms);
    } else {
      return AlphabetManager.createSymbol(
        '*', "?", Annotation.EMPTY_ANNOTATION,
        syms, this
      );
    }
  }
  
  protected abstract AtomicSymbol getSymbolImpl(List symList)
  throws IllegalSymbolException;
  
  protected abstract void addSymbolImpl(AtomicSymbol s)
  throws IllegalSymbolException, ChangeVetoException;

  public final void addSymbol(Symbol s)
  throws IllegalSymbolException, ChangeVetoException {
    if(s == null) {
      throw new IllegalSymbolException(
        "You can not add null as a symbol to alphabet " + getName()
      );
    }
    
    if(hasListeners()) {
      ChangeSupport cs = getChangeSupport(Alphabet.SYMBOLS);
      synchronized(cs) {
        ChangeEvent ce = new ChangeEvent(this, Alphabet.SYMBOLS, s, null);
        cs.firePreChangeEvent(ce);
        doAddSymbol(s);
        cs.firePostChangeEvent(ce);
      }
    } else {
      doAddSymbol(s);
    }
  }
  
  private void doAddSymbol(Symbol s)
  throws IllegalSymbolException, ChangeVetoException {
    Alphabet sa = s.getMatches();
    if(!(sa instanceof FiniteAlphabet)) {
      throw new IllegalSymbolException(
        "Can't add symbol " + s.getName() +
        " as it matches an infinite number of symbols."
      );
    } else {
      for(Iterator si = ((FiniteAlphabet) sa).iterator(); si.hasNext(); ) {
        addSymbolImpl((AtomicSymbol) si.next());
      }
    }
  }
  
  public final boolean contains(Symbol sym) {
    if(sym instanceof AtomicSymbol) {
      return containsImpl((AtomicSymbol) sym);
    } else {
      if(sym == null) {
        throw new NullPointerException("Symbols can't be null");
      }
      for(Iterator i = ((FiniteAlphabet) sym.getMatches()).iterator(); i.hasNext(); ) {
        AtomicSymbol s = (AtomicSymbol) i.next();
        if(!containsImpl(s)) {
          return false;
        }
      }
      return true;
    }
  }
  
  public final void validate(Symbol sym)
  throws IllegalSymbolException {
    if(!contains(sym)) {
      StringBuffer sb = new StringBuffer("{");
      Iterator i = iterator();
      if(i.hasNext()) {
        sb.append(((Symbol) i.next()).getToken());
      }
      while(i.hasNext()) {
        sb.append(',');
        sb.append(((Symbol) i.next()).getToken());
      }
      sb.append("}");
        
      throw new IllegalSymbolException(
        "Symbol " + sym.getName() + " not found in alphabet " + this.getName() +
        " " + sb.toString()
      );
    }
  }
  
  protected abstract boolean containsImpl(AtomicSymbol s);
  
  public void addChangeListener(ChangeListener cl) {
    getChangeSupport(null).addChangeListener(cl);
  }
  public void addChangeListener(ChangeListener cl, ChangeType ct) {
    getChangeSupport(ct).addChangeListener(cl, ct);
  }
  public void removeChangeListener(ChangeListener cl) {
    getChangeSupport(null).removeChangeListener(cl);
  }
  public void removeChangeListener(ChangeListener cl, ChangeType ct) {
    getChangeSupport(ct).removeChangeListener(cl, ct);
  } 
  
  protected AbstractAlphabet() {}
}

