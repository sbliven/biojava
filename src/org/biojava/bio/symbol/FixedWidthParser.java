/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */


package org.biojava.bio.symbol;

import java.util.*;
import java.io.*;

/**
 * A parser that uses a fixed with window of characters to look up the
 * associated symbol.
 * <P>
 * The string will be chunked up into substrings the size of the window, and
 * each substring will be converted into a Symbol object.
 *
 * @author Matthew Pocock
 */
public class FixedWidthParser implements SymbolParser, Serializable {
  /**
   * The alphabet for this parser.
   */
  private Alphabet alpha;
  
  /**
   * The length of each token.
   */
  private int tokenLength;
  
  /**
   * Map from token to Symbol.
   */
  private Map tokenToSymbol;

  /**
   * Initialize tokenToSymbol.
   */
  {
    tokenToSymbol = new HashMap();
  }

  public Alphabet getAlphabet() {
    return alpha;
  }

  public SymbolList parse(String seq) throws IllegalSymbolException {
    SimpleSymbolList res = new SimpleSymbolList(alpha);
    for(int i = 0; i < seq.length(); i+= tokenLength) {
      res.addSymbol(parseToken(seq.substring(i, i+tokenLength)));
    }
    return res;
  }

  public Symbol parseToken(String token) throws IllegalSymbolException {
    Symbol res = (Symbol) tokenToSymbol.get(token);
    if(res == null)
      throw new IllegalSymbolException("No symbol associated with token " + token);
    return res;
  }

  public void addTokenMap(String token, Symbol symbol)
         throws IllegalSymbolException, IllegalArgumentException {
    getAlphabet().validate(symbol);
    if(token.length() != tokenLength)
      throw new IllegalArgumentException("token '" + token +
                                         "' must be of length " + tokenLength);
    tokenToSymbol.put(token, symbol);
  }

  public FixedWidthParser(Alphabet alpha, int tokenLength) {
    this.alpha = alpha;
    this.tokenLength = tokenLength;
  }
}
