package org.biojava.bio.search;

import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.symbol.Symbol;

/**
 *
 *
 * @author Matthew Pocock
 */
class MaxMissmatchMatcher
implements BioMatcher {
  // info
  private final SymbolList pattern;
  private final SymbolList seq;

  // working numbers
  private final int[] matches;
  private int pos;

  MaxMissmatchMatcher(SymbolList seq,
                      SymbolList pattern,
                      int missmatches)
  {
    this.seq = seq;
    this.pattern = pattern;

    // initialize matches
    matches = new int[pattern.length()];
    for(int i = 0; i < matches.length; i++) matches[i] = missmatches;

    pos = 0;
  }

  public boolean find() {
    int length = matches.length;

    if(pos >= seq.length()) {
      return false;
    }

    // haven't started yet - better initialize stuff
    if(pos == 0) {
      if(seq.length() < length) {
        return false;
      }

      for(pos = 1; pos <= length; pos++) {
        Symbol sym = seq.symbolAt(pos);
        for(int i = 0; i < length; i++) {
          int indx = (i + pos) % length + 1;
          if(sym == pattern.symbolAt(indx)) {
            matches[i]++;
          }
        }
      }

      if(matches[matches.length-1] >= length) {
        return true;
      }
    }

    for(pos++; pos <= seq.length(); pos++) {
      Symbol newSym = seq.symbolAt(pos);
      Symbol oldSym = seq.symbolAt(pos - length + 1);

      for(int i = 0; i < length; i++) {
        int indx = (i + pos) % length;
        if(oldSym == pattern.symbolAt(indx)) {
          matches[i]--;
        }
        if(newSym == pattern.symbolAt(indx)) {
          matches[i]++;
        }
      }

      if(matches[pos % length] >= length) {
        return true;
      }
    }

    pos = seq.length() + 1;
    return false;
  }

  public int start() {
    return pos - matches.length + 1;
  }

  public int end() {
    return pos;
  }

  public SymbolList group() {
    return seq.subList(start(), end());
  }
}
