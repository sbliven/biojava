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
 * A parser that uses a fixed width window of characters to look up the
 * associated symbol.
 * <P>
 * The string will be chunked up into substrings the size of the window, and
 * each substring will be converted into a Symbol object.
 *
 * @author Matthew Pocock
 * @author Thomas Down (StreamParser)
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
    SimpleSymbolList sym = new SimpleSymbolList(alpha);
    for(int i = 0; i < seq.length(); i+= tokenLength) {
      sym.addSymbol(parseToken(seq.substring(i, i+tokenLength)));
    }
    return sym;
  }

  public Symbol parseToken(String token) throws IllegalSymbolException {
    Symbol sym = (Symbol) tokenToSymbol.get(token);
    if(sym == null)
      throw new IllegalSymbolException("No symbol associated with token " + token);
    return sym;
  }

    /**
    *Maps a string (one or more characters) onto a symbol
    *@param token the string to be mapped
    *@param symbol the symbol to be mapped onto
    */
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

    public StreamParser parseStream(SeqIOListener l) {
	return new FWStreamParser(l);
    }

    /**
     * Simple but (hopefully) reliable stream parser for fixed width tokens.
     * This creates strings for each token, so there will be quite a lot of
     * object churn.  But it's only worth optimizing this if it's receiving
     * a lot of use.
     *
     * @author Thomas Down
     */

    private class FWStreamParser implements StreamParser {
	private SeqIOListener listener;
	private char[] leftOver;
	private char leftOverLen;
	private Symbol[] buffer;

	{
	    buffer = new Symbol[256];
	}

	public FWStreamParser(SeqIOListener l) {
	    listener = l;
	    leftOver = new char[tokenLength];
	    leftOverLen = 0;
	}

	public void characters(char[] data, int pos, int len)
	    throws IllegalSymbolException
	{
	    int i = 0;
	    int bi = 0;

	    if (leftOverLen > 0) {
		while (leftOverLen < tokenLength && i < len) {
		    leftOver[leftOverLen++] = data[pos + (i++)];
		}
		if (leftOverLen == tokenLength) {
		    buffer[bi++] = parseToken(new String(leftOver));
		    leftOverLen = 0;
		}
	    }

	    while (len - i >= tokenLength && bi < buffer.length) {
		buffer[bi++] = parseToken(new String(data, pos + i, tokenLength));
		i += tokenLength;
	    } 
	    if (bi > 0) {
		try {
		    listener.addSymbols(getAlphabet(), buffer, 0, bi);
		} catch (IllegalAlphabetException ex) {
		    throw new BioError(ex);
		}
	    }

	    if (len - i >= tokenLength) {
		// More complete tokens -- let's go
		characters(data, pos + i, len - i);
		return;
	    }

	    // If there's a partial token at the end of the block, cache it
	    // away for next time.

	    while (len - i > 0) {
		leftOver[leftOverLen++] = data[pos + (i++)];
	    }
	}

	public void close()
	    throws IllegalSymbolException
	{
	    if (leftOverLen > 0) {
		throw new IllegalSymbolException("FixedWidth stream parser has " + leftOverLen + " orphan characters at end of stream");
	    }
	}
    }
}
