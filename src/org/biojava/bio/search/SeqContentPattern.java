package org.biojava.bio.search;

import org.biojava.bio.symbol.*;

public class SeqContentPattern {
  private final AlphabetIndex index;
  private final int[] minCounts;
  private final int[] maxCounts;
  private int length;

  public SeqContentPattern(FiniteAlphabet alpha) {
    index = AlphabetManager.getAlphabetIndex(alpha);
    this.minCounts = new int[alpha.size()];
    this.maxCounts = new int[alpha.size()];

    for(int i = 0; i < minCounts.length; i++) {
      minCounts[i] = 0;
      maxCounts[i] = -1;
    }
  }

  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }

  public void setMinCounts(AtomicSymbol as, int count)
  throws IllegalSymbolException {
    minCounts[index.indexForSymbol(as)] = count;
  }

  public int getMinCounts(AtomicSymbol as)
  throws IllegalSymbolException {
    return minCounts[index.indexForSymbol(as)];
  }

  public void setMaxCounts(AtomicSymbol as, int count)
  throws IllegalSymbolException {
    maxCounts[index.indexForSymbol(as)] = count;
  }

  public int getMaxCounts(AtomicSymbol as)
  throws IllegalSymbolException {
    int c = maxCounts[index.indexForSymbol(as)];
    if(c == -1) {
      return length;
    } else {
      return c;
    }
  }

  public SeqContentMatcher matcher(SymbolList symList)
  throws IllegalAlphabetException {
    if(symList.getAlphabet() != index.getAlphabet()) {
      throw new IllegalAlphabetException(
        "Attempted to search symbol list over " + symList.getAlphabet() +
        " but the search parameters only accept " + index.getAlphabet() );
    }

    int[] minCounts = new int[this.minCounts.length];
    int[] maxCounts = new int[this.maxCounts.length];
    for(int i = 0; i < minCounts.length; i++) {
      minCounts[i] = this.minCounts[i];
      
      int c = this.maxCounts[i];
      maxCounts[i] = (c == -1) ? length : c;
    }

    return new SeqContentMatcher(
      symList,
      index,
      minCounts,
      maxCounts,
      length );
  }
}

