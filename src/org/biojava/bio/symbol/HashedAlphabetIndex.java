package org.biojava.bio.symbol;

import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;

/**
 * Uses Arrays.binarySearch to retrieve indecies for symbols. To save on CPU,
 * an array of symbol hash codes is searched, avoiding the need to multipuly
 * calculate the hash codes of the alphabet symbols.
 *
 * @author Matthew Pocock
 * @since 1.1
 */ 
public class HashedAlphabetIndex implements AlphabetIndex {
  private static final Comparator cmp = new HashComparator();
  
  private final FiniteAlphabet alpha;
  private final Symbol[] symbols;
  private final int[] hashes;
  
  public FiniteAlphabet getAlphabet() {
    return alpha;
  }
  
  public int indexForSymbol(Symbol s)
  throws IllegalSymbolException {
    int indx = Arrays.binarySearch(hashes, s.hashCode());
    if(indx < 0) {
      getAlphabet().validate(s);
      throw new IllegalSymbolException(
        "Symbol " + s.getName() + " is not an AttomicSymbol instance"
      );
    }
    return indx;
  }
  
  public Symbol symbolForIndex(int i) throws IndexOutOfBoundsException {
    return symbols[i];
  }
  
  public HashedAlphabetIndex(FiniteAlphabet alpha) {
    alpha.addChangeListener(ChangeListener.ALWAYS_VETO, Alphabet.SYMBOLS);
    this.alpha = alpha;
    symbols = new Symbol[alpha.size()];
    hashes = new int[alpha.size()];
    
    int i = 0;
    Iterator s = alpha.iterator();
    while(s.hasNext()) {
      symbols[i++] = (Symbol) s.next();
    }
    Arrays.sort(symbols, cmp);
    
    for(i = 0; i < symbols.length; i++) {
      hashes[i] = symbols[i].hashCode();
    }
  }
  
  private static class HashComparator implements Comparator {
    public boolean equals(Object o) {
      return o instanceof HashComparator;
    }
    
    public int compare(Object a, Object b) {
      return a.hashCode() - b.hashCode();
    }
  }
}
