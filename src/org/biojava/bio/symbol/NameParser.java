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
 * This uses Symbol names to parse characters into symbols.
 *
 * @author Matthew Pocock
 */
public class NameParser implements SymbolParser, Serializable {
  /**
   * The alphabet to parse names to.
   */
  private FiniteAlphabet alphabet;
  
  /**
   * The map of name to symbols.
   */
  private Map nameToSymbol;
  
  /**
   * Initialize the nameToSymbol map.
   */
  {
    nameToSymbol = new HashMap();
  }
  
  public Alphabet getAlphabet() {
    return alphabet;
  }
  
  public SymbolList parse(String seq) throws IllegalSymbolException {
    SimpleSymbolList rList = new SimpleSymbolList(getAlphabet());
    String [] names = (String []) nameToSymbol.keySet().toArray(new String[0]);
    while(seq.length() > 0) {
      int chosen = -1;
      for(int n = 0; (chosen != -1) && (n < names.length); n++) {
        if(seq.startsWith(names[n])) {
          chosen = n;
          break;
        }
      }
      
      if(chosen == -1) {
        if(seq.length() > 10)
          seq = seq.substring(0, 10);
        throw new IllegalSymbolException("Unable to find symbol name matching from " + seq);
      }
      
      rList.addSymbol(parseToken(names[chosen]));
      seq = seq.substring(names[chosen].length());
    }
    return rList;
  }
  
  public Symbol parseToken(String token) throws IllegalSymbolException {
    Symbol res = (Symbol) nameToSymbol.get(token.toLowerCase());
    if(res == null) {
      throw new IllegalSymbolException(
        "No symbol for token '" + token +
         "' found in name parser for " + getAlphabet().getName()
       );
    }
    return res;
  }
  
  public NameParser(FiniteAlphabet alpha) {
    this.alphabet = alpha;
    for(Iterator i = alpha.iterator(); i.hasNext(); ) {
      Symbol res = (Symbol) i.next();
      nameToSymbol.put(res.getName().toLowerCase(), res);
    }
  }
  
  public NameParser(Map nameToSymbol) {
    this.nameToSymbol = nameToSymbol;
  }
}
