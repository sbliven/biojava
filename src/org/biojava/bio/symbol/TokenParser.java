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
 * This uses symbol token to parse characters into Symbols.
 *
 * @author Matthew Pocock
 */
public class TokenParser implements SymbolParser, Serializable {
  /**
   * The alphabet to parse into.
   */
  private FiniteAlphabet alphabet;
  /**
   * The map from token to symbol.
   */
  private Map tokenToSymbol;
  
  /**
   * Initialize tokenToSymbol.
   */
  {
    tokenToSymbol = new HashMap();
  }
  
  public Alphabet alphabet() {
    return alphabet;
  }
  
  public SymbolList parse(String seq) throws IllegalSymbolException {
    List rList = new ArrayList(seq.length());
    for(int i = 0; i < seq.length(); i++) {
      rList.add(parseToken(seq.substring(i, i+1)));
    }
    return new SimpleSymbolList(alphabet(), rList);
  }
  
  public Symbol parseToken(String token) throws IllegalSymbolException {
    Symbol res = (Symbol) tokenToSymbol.get(token);
    if(res == null) {
      throw new IllegalSymbolException(
        "No symbol for token '" + token +
        "' found in alphabet " + alphabet.getName()
      );
    }
    return res;
  }
  
  /**
   * Generate a new TokenParser for an alphabet.
   * <P>
   * It will be correct at the time of creation. If the alphabet is edited in
   * any way that affects the symbols contained, this parser becomes invalid.
   *
   * @param alpha the Alphabet to parse tokens into
   */
  public TokenParser(FiniteAlphabet alpha) {
    this.alphabet = alpha;
    for(Iterator i = alpha.iterator(); i.hasNext(); ) {
      Symbol res = (Symbol) i.next();
      char c = res.getToken();
      tokenToSymbol.put(Character.toLowerCase(c) + "", res);
      tokenToSymbol.put(Character.toUpperCase(c) + "", res);
    }
  }
}
