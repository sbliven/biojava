package org.biojava.bio.symbol;

import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;

/*
 * Implementatoin of AlphabetIndex that stores the symbols in an array and does
 * a linear shearch through the list for a given symbol to find its index.
 *
 * @author Matthew Pocock
 * @since 1.1
 */
class LinearAlphabetIndex extends AbstractChangeable implements AlphabetIndex {
  private /*final*/ FiniteAlphabet alpha;
  private Symbol[] symbols;

  // hack for bug in compaq 1.2?
  protected ChangeSupport getChangeSupport(ChangeType ct) {
    return super.getChangeSupport(ct);
  }

  public LinearAlphabetIndex(FiniteAlphabet alpha) {
    // lock the alphabet
    alpha.addChangeListener(ChangeListener.ALWAYS_VETO, Alphabet.SYMBOLS);
    
    this.alpha = alpha;
    
    this.symbols = buildIndex(alpha);
    
    alpha.addChangeListener(
      new IndexRebuilder(),
      Alphabet.SYMBOLS
    );
    
    this.addChangeListener(
      new ChangeAdapter() {
        public void postChange(ChangeEvent ce) {
          symbols = (Symbol[] ) ce.getChange();
        }
      },
      AlphabetIndex.INDEX
    );
    
    // unlock the alphabet
    alpha.removeChangeListener(ChangeListener.ALWAYS_VETO, Alphabet.SYMBOLS);
  }
  
  public LinearAlphabetIndex(Symbol[] syms)
  throws BioException {
    Set si = new HashSet();
    Symbol[] symbols = new Symbol[syms.length];
    for(int i = 0; i < syms.length; i++) {
      Symbol s = syms[i];
      symbols[i] = s; 
      si.add(s);
    }
    
    this.alpha = new SimpleAlphabet(si);
    alpha.addChangeListener(ChangeListener.ALWAYS_VETO, Alphabet.SYMBOLS);
    this.symbols = symbols;
  }
  
  private Symbol[] buildIndex(FiniteAlphabet alpha) {
    Symbol[] symbols = new Symbol[alpha.size()];
    
    int i = 0;
    Iterator s = alpha.iterator();
    while(s.hasNext()) {
      symbols[i++] = (Symbol) s.next();
    }
    
    return symbols;
  }

  public FiniteAlphabet getAlphabet() {
    return alpha;
  }
  
  public Symbol symbolForIndex(int i) throws IndexOutOfBoundsException {
    try {
      return symbols[i];
    } catch (IndexOutOfBoundsException e) {
      throw new IndexOutOfBoundsException("Can't find symbol for index " + i);
    }
  }
  
  public int indexForSymbol(Symbol s) throws IllegalSymbolException {
    for(int i = 0; i < symbols.length; i++) {
      if(s == symbols[i]) {
        return i;
      }
    }
    getAlphabet().validate(s);
    if(s instanceof AtomicSymbol) {
      throw new BioError(
        "Assertion Failure: " +
        "Symbol " + s.getName() + " was not an indexed member of the alphabet " +
        getAlphabet().getName() + " despite being in the alphabet."
      );
    } else {
      throw new IllegalSymbolException("Symbol must be atomic to be indexed.");
    }
  }
  
  protected class IndexRebuilder extends ChangeForwarder {
    public IndexRebuilder() {
      super(
	    LinearAlphabetIndex.this,
		LinearAlphabetIndex.this.getChangeSupport(AlphabetIndex.INDEX)
      );
    }
    
    public ChangeEvent generateEvent(ChangeEvent ce)
    throws ChangeVetoException {
      if(ce.getType() != Alphabet.SYMBOLS) {
        return null;
      }

      /*      
      Object change = ce.getChange();
      Object previous = ce.getPrevious();
      
      if( (change == null) || (previous != null) ) {
        throw new ChangeVetoException(
          ce,
          "Can not update index as either a symbol is being removed, " +
          "or the alphabet has substantialy changed"
        );
      }
      
      if(! (change instanceof AtomicSymbol) ) {
        throw new ChangeVetoException(
          ce,
          "Can not update index as the symbol being added is not atomic"
        );
      }
      */
      
      return new ChangeEvent(
        getSource(), AlphabetIndex.INDEX,
        // fixme: buildIndex should be called using the proposed new alphabet
        LinearAlphabetIndex.this.buildIndex((FiniteAlphabet) ce.getSource()),
        symbols,
        ce
      );
    }
  }
}
