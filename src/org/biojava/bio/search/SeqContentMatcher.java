package org.biojava.bio.search;

import java.util.*;

import org.biojava.utils.AssertionFailure;
import org.biojava.bio.symbol.*;

public class SeqContentMatcher {
  private final AlphabetIndex index;
  private final SymbolList symList;
  private final int[] minCounts;
  private final int[] maxCounts;
  private final int length;

  private int pos;
  private int[] curCounts;

  SeqContentMatcher(
    SymbolList symList,
    AlphabetIndex index,
    int[] minCounts,
    int[] maxCounts,
    int length
  ) {
    this.symList = symList;
    this.index = index;
    this.minCounts = minCounts;
    this.maxCounts = maxCounts;
    this.length = length;

    // prime the pump
    curCounts = new int[minCounts.length];
    pos = 0;
  }

  public boolean find() {
    try {
      // are we doing the 1st find?
      if(pos == 0) {
        // can't find an n-long sub-sequence of a <n-long super-sequence
        if(length > symList.length()) {
          return false;
        }

        // getting the initial counts
        for(int i = 1; i <= length; i++) {
          Symbol s = symList.symbolAt(i);
          if(s instanceof AtomicSymbol) {
            curCounts[index.indexForSymbol(s)]++;
          } else {
            for(
              Iterator si = ((FiniteAlphabet) s.getMatches()).iterator();
              si.hasNext();
            ) {
              AtomicSymbol as = (AtomicSymbol) si.next();
              curCounts[index.indexForSymbol(as)]++;
            }
          }
        }

        pos = 1;

        if(acceptable()) {
          return true;
        }
      }

      // ok - not at the beginning, or the beginning didn't contain a match
      pos++;
      for(
        int ourMax = symList.length() - length + 1;
        pos <= ourMax;
        pos++
      ) {
        Symbol s;
        s = symList.symbolAt(pos - 1);
        if(s instanceof AtomicSymbol) {
          curCounts[index.indexForSymbol(s)]--;
        } else {
          for(
            Iterator i = ((FiniteAlphabet) s.getMatches()).iterator();
            i.hasNext();
          ) {
            AtomicSymbol as = (AtomicSymbol) i.next();
            curCounts[index.indexForSymbol(as)]--;
          }
        }

        s = symList.symbolAt(pos + length - 1);
        if(s instanceof AtomicSymbol) {
          curCounts[index.indexForSymbol(s)]++;
        } else {
          for(
            Iterator i = ((FiniteAlphabet) s.getMatches()).iterator();
            i.hasNext();
          ) {
            AtomicSymbol as = (AtomicSymbol) i.next();
            curCounts[index.indexForSymbol(as)]++;
          }
        }

        if(acceptable()) {
          return true;
        }
      }

      // no match could be found
      return false;
    } catch (IllegalSymbolException ise) {
      throw new AssertionFailure(
          "It should not be possible for the wrong symbols to be here",
          ise );
    }
  }

  private boolean acceptable() {
    for(int i = 0; i < curCounts.length; i++) {
      int c = curCounts[i];
      if(minCounts[i] > c) return false;
      if(maxCounts[i] < c) return false;
    }

    return true;
  }

  public int start() {
    return pos;
  }

  public int end() {
    return pos + length - 1;
  }

  public SymbolList group() {
    return symList.subList(pos, pos + length - 1);
  }
}
