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


package org.biojava.bio.seq.io;

import java.util.*;
import java.io.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;

/**
 * This uses Symbol names to parse characters into symbols. 
 *
 * <p>
 * <strong>FIXME:</strong> This class currently has some performance
 * issues (especially the StreamParser), and needs a fairly fundamental
 * rethink.
 * </p>
 *
 * @author Matthew Pocock
 * @author Thomas Down
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
      List rList = new ArrayList();
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
      
      rList.add(parseToken(names[chosen]));
      seq = seq.substring(names[chosen].length());
    }
    return new SimpleSymbolList(getAlphabet(), rList);
  }
  
  public Symbol parseToken(String token) throws IllegalSymbolException {
    Symbol sym = (Symbol) nameToSymbol.get(token.toLowerCase());
    if(sym == null) {
      throw new IllegalSymbolException(
        "No symbol for token '" + token +
         "' found in name parser for " + getAlphabet().getName()
       );
    }
    return sym;
  }
  
  public NameParser(FiniteAlphabet alpha) {
    this.alphabet = alpha;
    for(Iterator i = alpha.iterator(); i.hasNext(); ) {
      Symbol sym = (Symbol) i.next();
      nameToSymbol.put(sym.getName().toLowerCase(), sym);
    }
  }
  
  public NameParser(Map nameToSymbol) {
    this.nameToSymbol = nameToSymbol;
  }

    public StreamParser parseStream(SeqIOListener l) {
	throw new BioError("[FIXME] not implemented");
    }

    private class NameStreamParser implements StreamParser {
	SeqIOListener listener;
	StringBuffer sb = new StringBuffer();

	NameStreamParser(SeqIOListener l) {
	    listener = l;
	}

	public void characters(char[] data, int start, int len) {
	    sb.append(data, start, len);
	}

	public void close()
	    throws IllegalSymbolException
	{
	    SymbolList sl = parse(sb.toString());
	    sb = null;
	    Symbol[] symbols = new Symbol[sl.length()];
	    for (int i = 0; i < sl.length(); ++i)
		symbols[i] = sl.symbolAt(i + 1);

	    try {
		listener.addSymbols(getAlphabet(), symbols, 0, symbols.length);
	    } catch (IllegalAlphabetException ex) {
		throw new BioError(ex);
	    }
	}
    }

}
