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
import org.biojava.bio.seq.io.*;

/**
 * <p>
 * An abstract implementation of <code>Alphabet</code>.
 * </p>
 *
 * <p>
 * This provides the frame-work for maintaining the SymbolParser <-> name
 * mappings and also for the ChangeListeners.
 * </p>
 *
 * <p>
 * This class is for developers to derive from, not for use directly.
 * </p>
 *
 * @author Matthew Pocock
 * @since 1.1
 */
public abstract class AbstractAlphabet implements FiniteAlphabet, Serializable {
  private final Map tokenizationsByName;
  private final Map ambCache;
  private ChangeSupport changeSupport;


  {
    tokenizationsByName = new HashMap();
    ambCache = new HashMap();
  }
  

  /**
   * To prevent duplication of a what should be a
   * single instance of an existing alphabet. This method
   * was written as protected so that subclasses even from 
   * other packages will inherit it. It should only be overridden
   * with care.
   */
  protected Object readResolve() throws ObjectStreamException{
    try{
      return AlphabetManager.alphabetForName(this.getName());
    }catch(NoSuchElementException nse){
      //a custom alphabet has been sent to your VM, register it.
      AlphabetManager.registerAlphabet(this.getName(), this);
      return this;
    }

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
   * <p>
   * Assigns a symbol parser to a String object.
   * </p>
   *
   * <p>
   * Afterwards, the parser can be retrieved using the getParser method.
   * </p>
   */
  public void putTokenization(String name, SymbolTokenization parser) {
    tokenizationsByName.put(name, parser);
  }
  
  public SymbolTokenization getTokenization(String name)
	throws NoSuchElementException, BioException
    {
	SymbolTokenization toke = (SymbolTokenization) tokenizationsByName.get(name);
	if(toke == null) {
	    if(name.equals("name")) {
		if (getAlphabets().size() == 1) {
		    toke = new NameTokenization(this);
		} else {
		    toke = new CrossProductTokenization(this);
		}
		putTokenization(name, toke);
	    } else {
		throw new NoSuchElementException("There is no tokenization '" + name +
						 "' defined in alphabet " + getName());
	    }
	}
	return toke;
    }

  public final Symbol getAmbiguity(Set syms)
      throws IllegalSymbolException 
  {
      if (syms.size() == 0) {
	  return getGapSymbol();
      } else if (syms.size() == 1) {
	  Symbol sym = (Symbol) syms.iterator().next();
	  validate(sym);
	  return sym;
      } else {
        Symbol s = (Symbol) ambCache.get(syms);
        if(s == null) {
          for (Iterator i = syms.iterator(); i.hasNext(); ) {
            validate((Symbol) i.next());
          }
          
          s = AlphabetManager.createSymbol(
					      '*', Annotation.EMPTY_ANNOTATION,
					      syms, this
					      );
          ambCache.put(new HashSet(syms), s);
        }
        return s;
      }
  }
  
  public final Symbol getSymbol(List syms)
  throws IllegalSymbolException {
    if (syms.size() == 1) {
      Symbol s = (Symbol) syms.get(0);
      validate(s);
      return s;
    }

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
      //a.validate(s); // very expensive for requent fetches!
      if(s instanceof AtomicSymbol) {
        atomic++;
      }
    }
    
    if(atomic == syms.size()) {
      return getSymbolImpl(syms);
    } else {
      return AlphabetManager.createSymbol(
        '*', Annotation.EMPTY_ANNOTATION,
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
      FiniteAlphabet matches = (FiniteAlphabet) sym.getMatches();
      if(matches.size() == 0) {
        //System.out.println("Got empty symbol " + sym.getName());
        if(sym.equals(AlphabetManager.getGapSymbol())) {
          //System.out.println("Global gap symbol");
          return true;
        } else if(sym instanceof BasisSymbol) {
          if(((BasisSymbol) sym).getSymbols().size() == getAlphabets().size()) {
            //System.out.println("Basis symbol and the right length");
            return true;
          }
        }
        //System.out.println("Empty symbol and not basis - let's accept it.");
        return true;
      }
      for(Iterator i = matches.iterator(); i.hasNext(); ) {
        AtomicSymbol s = (AtomicSymbol) i.next();
        if(!containsImpl(s)) {
          return false;
        }
      }
      return true;
    }
  }
  
  public final Symbol getGapSymbol() {
    return AlphabetManager.getGapSymbol(getAlphabets());
  }
  
  public final void validate(Symbol sym)
  throws IllegalSymbolException {
    if(!contains(sym)) {
      StringBuffer sb = new StringBuffer("{");
      /*Iterator i = iterator();
      if(i.hasNext()) {
        sb.append(((Symbol) i.next()).getName());
      }
      while(i.hasNext()) {
        sb.append(',');
        sb.append(((Symbol) i.next()).getName());
      }
      sb.append("}");*/
        
      throw new IllegalSymbolException(
        "Symbol " + sym.getName() + " not found in alphabet " + this.getName() +
        " " + sb.toString()
      );
    }
  }
  
  protected abstract boolean containsImpl(AtomicSymbol s);
  
  public boolean equals(Object o) {
    if(o == this) {
      return true;
    }
    
    if(!(o instanceof FiniteAlphabet)) {
      return false;
    }
    
    FiniteAlphabet that = (FiniteAlphabet) o;
    
    if(this.size() != that.size()) {
      return false;
    }
    
    for(Iterator i = that.iterator(); i.hasNext(); ) {
      if(!this.contains((AtomicSymbol) i.next())) {
        return false;
      }
    }
    
    return true;
  }
  
  public void addChangeListener(ChangeListener cl) {
    getChangeSupport(ChangeType.UNKNOWN).addChangeListener(cl);
  }
  public void addChangeListener(ChangeListener cl, ChangeType ct) {
    getChangeSupport(ct).addChangeListener(cl, ct);
  }
  public void removeChangeListener(ChangeListener cl) {
    getChangeSupport(ChangeType.UNKNOWN).removeChangeListener(cl);
  }
  public void removeChangeListener(ChangeListener cl, ChangeType ct) {
    getChangeSupport(ct).removeChangeListener(cl, ct);
  } 
  
  public String toString() {
    return getName();
  }
  
  protected AbstractAlphabet() {}
}

