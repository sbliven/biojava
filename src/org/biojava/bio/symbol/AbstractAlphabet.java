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
  
  protected abstract void addSymbolImpl(Symbol s)
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
        addSymbolImpl((Symbol) si.next());
      }
    }
  }

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
