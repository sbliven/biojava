package org.biojava.bio.search;

import org.biojava.bio.symbol.SymbolList;

/**
 * Interface for things that perform matches.
 *
 * <p>
 * These will almost always be produced by a factory method on a BioPattern
 * object.
 * </p>
 *
 * @author Matthew Pocock
 */
public interface BioMatcher {
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
  boolean find();

  /**
   * Get the first symbol index that matches the pattern.
   *
   * @return the start of the current match
   * @throws java.lang.IllegalStateException if there is no current match
   */
  int start();

  /**
   * Get the last symbol index that matches the pattern.
   *
   * @return the end of the current match
   * @throws java.lang.IllegalStateException if there is no current match
   */
  int end();

  /**
   * Get the matching region as a SymbolList.
   *
   * @return the matching symbols
   * @throws java.lang.IllegalStateException if there is no current match
   */
  SymbolList group();

}
