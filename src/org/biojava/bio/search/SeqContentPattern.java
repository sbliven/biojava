package org.biojava.bio.search;

import org.biojava.bio.symbol.*;

/**
 * A pattern that can be used to find regions with given sequence content.
 *
 * <p>
 * Regular expressions can be used to find sequence patterns. However, some
 * things can't be easily expressed as a regular expression. For example,
 * a region of length 10 that contains at least 8 Gs and up to two Ts and no
 * other symbols. A SeqContentPattern can be built that does represent this.
 * <p>
 *
 * <code><pre>
 * SeqContentPattern scp = new SeqContentPattern(DNATools.getDNA());
 * scp.setLength(10);
 * scp.setMinCounts(DNATools.g(), 8);
 * scp.setMaxCounts(DNATools.t(), 2);
 * scp.setMaxCounts(DNATools.c(), 0);
 * scp.setMaxCounts(DNATools.a(), 0);
 * </pre></code>
 *
 * <p>
 * The minimum counts default to 0, and the maximum counts default to the
 * length. If you have not manually set the maximum count for a symbol, it will
 * continue to adjust while you change the length. Once you have set it, it will
 * not vary, even if you do set the length. To re-set a maximum count to track
 * the length, set it to -1.
 * </p>
 *
 * <p>
 * All regions of the defined length for which all constraints are satisfied
 * will potentialy be found. At the moment we have not defined what will
 * happen for multiple regions that overlap all of which satisfy the
 * constraints.
 * </p>
 *
 * @author Matthew Pocock
 * @since 1.4
 */
public class SeqContentPattern {
  private final AlphabetIndex index;
  private final int[] minCounts;
  private final int[] maxCounts;
  private int length;

  /**
   * Create a new SeqContentPattern over an alphabet.
   *
   * @param alpha  the FiniteAlphabet for this pattern
   */
  public SeqContentPattern(FiniteAlphabet alpha) {
    index = AlphabetManager.getAlphabetIndex(alpha);
    this.minCounts = new int[alpha.size()];
    this.maxCounts = new int[alpha.size()];

    for(int i = 0; i < minCounts.length; i++) {
      minCounts[i] = 0;
      maxCounts[i] = -1;
    }
  }

  /**
   * Get the current length.
   *
   * @return the length
   */
  public int getLength() {
    return length;
  }

  /**
   * Set the pattern length.
   *
   * @param length  the new length
   */
  public void setLength(int length) {
    this.length = length;
  }

  /**
   * Set the minimum counts required for a symbol.
   *
   * @param as  the AtomicSymbol to check
   * @param count  the minimum number of counts it must have
   * @throws IllegalSymbolException  if as is not known in this alphabet
   */
  public void setMinCounts(AtomicSymbol as, int count)
  throws IllegalSymbolException {
    minCounts[index.indexForSymbol(as)] = count;
  }

  /**
   * Get the minimum counts required for a symbol.
   *
   * @param as  the AtomicSymbol to check
   * @return the minimum number of counts it must have
   * @throws IllegalSymbolException  if as is not known in this alphabet
   */
  public int getMinCounts(AtomicSymbol as)
  throws IllegalSymbolException {
    return minCounts[index.indexForSymbol(as)];
  }

  /**
   * Set the maximum counts required for a symbol.
   * Use -1 to reset it to track the length.
   *
   * @param as  the AtomicSymbol to check
   * @param count  the maximum number of counts it must have
   * @throws IllegalSymbolException  if as is not known in this alphabet
   */
  public void setMaxCounts(AtomicSymbol as, int count)
  throws IllegalSymbolException {
    maxCounts[index.indexForSymbol(as)] = count;
  }

  /**
   * Get the maximum counts required for a symbol.
   *
   * @param as  the AtomicSymbol to check
   * @return the maximum number of counts it must have
   * @throws IllegalSymbolException  if as is not known in this alphabet
   */
  public int getMaxCounts(AtomicSymbol as)
  throws IllegalSymbolException {
    int c = maxCounts[index.indexForSymbol(as)];
    if(c == -1) {
      return length;
    } else {
      return c;
    }
  }

  /**
   * Get a matcher that will use these parameters to search a SymbolList.
   *
   * <p>
   * The resulting SeqContentMatcher is independant of this SeqContentPattern.
   * In particular, calling any mutator methods on this pattern will not affect
   * the matcher.
   * </p>
   *
   * @param symList  the SymbolList to match against
   * @return a SeqContentMatcher that will perform the search
   * @throws IllegalAlphabetException if symList is not over the right alphabet
   */
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

