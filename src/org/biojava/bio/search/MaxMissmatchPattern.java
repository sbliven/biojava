package org.biojava.bio.search;

import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.symbol.IllegalAlphabetException;

/**
 *
 *
 * @author Matthew Pocock
 */
public class MaxMissmatchPattern
implements BioPattern {
  private int missmatches;
  private SymbolList pattern;

  public MaxMissmatchPattern() {}

  public MaxMissmatchPattern(SymbolList pattern, int missmatches) {
    this.pattern = pattern;
    this.missmatches = missmatches;
  }

  public int getMissmatches() {
    return missmatches;
  }

  public void setMissmatches(int missmatches) {
    this.missmatches = missmatches;
  }

  public SymbolList getPattern() {
    return pattern;
  }

  public void setPattern(SymbolList pattern) {
    this.pattern = pattern;
  }

  public BioMatcher matcher(SymbolList symList)
          throws IllegalAlphabetException {
    return new MaxMissmatchMatcher(symList, pattern, missmatches);
  }
}
