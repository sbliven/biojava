package org.biojava.bio.search;

import java.util.*;

import org.biojava.utils.AssertionFailure;
import org.biojava.bio.symbol.*;

/**
 * Matcher class that pairs with SeqContentPattern.
 *
 * <p>
 * This matcher is responsible for searching through a symbol list for regions
 * that match a pattern. It is implemented in a non-thread-safe way. Either
 * have multiple independant matchers, or synchronize externaly.
 * </p>
 *
 * <p>
 * Instances of this class are obtained from SeqContentPattern.matcher(SymbolList) and it can not be instantiated directly.
 * </p>
 *
 * @author Matthew Pocock
 * @since 1.4
 * @see SeqContentPattern
 */
public class SeqContentMatcher {
  // constants
  private final AlphabetIndex index;
  private final SymbolList symList;
  private final int[] minCounts;
  private final int[] maxCounts;
  private final int length;

  // state
  private int pos;
  private int[] curCounts;

  // package-private constructor - should only be invoked by SeqContentPattern
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

  /**
   * Attempt to find the next match.
   *
   * <p>
   * If the pattern can be found, then this will return true. If it could not,
   * then it will return false. This is convenient within for or while loops.
   * </p>
   *
   * <p>
   * Each time this is called, the next match will be found. The start() and
   * end() values will increase each time, regardless of wether you called any
   * other methods.
   * </p>
   *
   * @return true if there is another match
   */
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
      pos = symList.length() + 1;
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

  /**
   * Get the first symbol index that matches the pattern.
   * 
   * @return the start of the current match
   * @throws IllegalStateException if there is no current match
   */
  public int start() {
    if(pos == 0) {
      throw new IllegalStateException("Can't call start() before find()");
    }

    if(pos > symList.length()) {
      throw new IllegalStateException("Can't call start() after find() has returned false");
    }

    return pos;
  }

  /**
   * Get the last symbol index that matches the pattern.
   *
   * @return the end of the current match
   * @throws IllegalStateException if there is no current match
   */
  public int end() {
    if(pos == 0) { 
      throw new IllegalStateException("Can't call end() before find()"); 
    } 

    if(pos > symList.length()) {
      throw new IllegalStateException("Can't call end() after find() has returned false");
    }

    return pos + length - 1;
  }

  /**
   * Get the matching region as a SymbolList.
   *
   * @return the matching symbols
   * @throws IllegalStateException if there is no current match
   */
  public SymbolList group() {
    if(pos == 0) { 
      throw new IllegalStateException("Can't call group() before find()"); 
    } 

    if(pos > symList.length()) {
      throw new IllegalStateException("Can't call group() after find() has returned false");
    }

    return symList.subList(pos, pos + length - 1);
  }
}
