package org.biojava.bio.symbol;

import java.util.*;

/*
 * Implementatoin of AlphabetIndex that stores the symbols in an array and does
 * a linear shearch through the list for a given symbol to find its index.
 *
 * @author Matthew Pocock
 * @since 1.1
 */
class LinearAlphabetIndex implements AlphabetIndex {
  private final FiniteAlphabet alpha;
  private Symbol[] symbols;
  
  public LinearAlphabetIndex(FiniteAlphabet alpha) {
    this.alpha = alpha;
    symbols = new Symbol[alpha.size()];
    
    int i = 0;
    Iterator s = alpha.iterator();
    while(s.hasNext()) {
      symbols[i++] = (Symbol) s.next();
    }  
  }

  public FiniteAlphabet getAlphabet() {
    return alpha;
  }
  
  public Symbol symbolForIndex(int i) throws IndexOutOfBoundsException {
    return symbols[i];
  }
  
  public int indexForSymbol(Symbol s) throws IllegalSymbolException {
    for(int i = 0; i < symbols.length; i++) {
      if(s == symbols[i]) {
        return i;
      }
    }
    getAlphabet().validate(s);
    throw new IllegalSymbolException(
      "Symbol " + s.getName() + " was not an indexed member of the alphabet " +
      getAlphabet().getName()
    );
  }
}
