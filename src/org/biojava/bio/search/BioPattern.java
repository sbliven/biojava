package org.biojava.bio.search;

import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.SymbolList;

/**
 *
 *
 * @author Matthew Pocock
 */
public interface BioPattern {
  /**
   * Get a matcher that will use these parameters to search a SymbolList.
   *
   * <p>
   * The resulting BioMatcher is independant of this BioPattern.
   * In particular, calling any mutator methods on this pattern will not affect
   * the matcher.
   * </p>
   *
   * @param symList  the SymbolList to match against
   * @return a BioMatcher that will perform the search
   * @throws IllegalAlphabetException if symList is not over the right alphabet
   */
  BioMatcher matcher(SymbolList symList)
  throws IllegalAlphabetException;
}
