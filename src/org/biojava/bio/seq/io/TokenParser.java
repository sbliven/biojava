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

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;

/**
 * This uses symbol token to parse characters into Symbols.
 *
 * @author Matthew Pocock
 * @author Thomas Down (optimization and StreamParser support)
 */

public class TokenParser implements SymbolParser, Serializable {
    /**
     * The alphabet to parse into.
     */
    private FiniteAlphabet alphabet;
 
    private transient Symbol[] symbolsByChar;
  
    private void generateTable() {
	Set tokenSymbols = new HashSet();
	tokenSymbols.add(alphabet.getGapSymbol());
	for (Iterator si = alphabet.iterator(); si.hasNext(); ) {
	    Symbol sym = (Symbol) si.next();
	    tokenSymbols.add(sym);
	}
	if (alphabet instanceof SimpleAlphabet) {
	    for (Iterator si = ((SimpleAlphabet) alphabet).ambiguities(); si.hasNext(); ) {
		Symbol sym = (Symbol) si.next();
		tokenSymbols.add(sym);
	    }
	}

	int maxIndx = 0;
	for (Iterator i = tokenSymbols.iterator(); i.hasNext(); ) {
	    Symbol sym = (Symbol) i.next();
	    char token = sym.getToken();
	    maxIndx = Math.max(maxIndx, Character.toUpperCase(token));
	    maxIndx = Math.max(maxIndx, Character.toLowerCase(token));
	}
	symbolsByChar = new Symbol[maxIndx + 1];
	for (Iterator i = tokenSymbols.iterator(); i.hasNext(); ) {
	    Symbol sym = (Symbol) i.next();
	    char token = sym.getToken();
	    symbolsByChar[Character.toUpperCase(token)] = sym;
	    symbolsByChar[Character.toLowerCase(token)] = sym;
	}
    }
  
    public Alphabet getAlphabet() {
	return alphabet;
    }

    public SymbolList parse(String seq) throws IllegalSymbolException {
	int len = seq.length();
	if (len < 100) {
	    List rList = new ArrayList(seq.length());
	    for(int i = 0; i < seq.length(); i++) {
		rList.add(parseCharToken(seq.charAt(i)));
	    }
	    return new SimpleSymbolList(getAlphabet(), rList);
	} else {
	    Symbol[] buffer = new Symbol[256];
	    ChunkedSymbolListBuilder builder = new ChunkedSymbolListBuilder();
	    
	    int pos = 0;
	    while (pos < len) {
		int bufPos = 0;
		while (pos < len && bufPos < buffer.length) {
		    buffer[bufPos++] = parseCharToken(seq.charAt(pos++));
		}
		try {
		    builder.addSymbols(alphabet,
				       buffer,
				       0,
				       bufPos);
		} catch (IllegalAlphabetException ex) {
		    throw new BioError(ex);
		}
	    }

	    return builder.makeSymbolList();
	}
    }
  
    Symbol parseCharToken(char token) 
        throws IllegalSymbolException
    {
	if (symbolsByChar == null)
	    generateTable();

	try {
	    Symbol s = symbolsByChar[token];
	    if (s != null)
		return s;
	} catch (IndexOutOfBoundsException ex) {
	}

	throw new IllegalSymbolException(
		       "No symbol for token '" + token +
		       "' found in alphabet " + alphabet.getName()
		       );
    }

    public Symbol parseToken(String token) throws IllegalSymbolException {
	if (token.length() > 1)
	    throw new IllegalSymbolException("All tokens recognized by this parser are single characters.");
	return parseCharToken(token.charAt(0));
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
	installChangeListener();
    }

    private void installChangeListener() {
	alphabet.addChangeListener(new ChangeListener() {
	    public void preChange(ChangeEvent cev) {
	    }

	    public void postChange(ChangeEvent cev) {
		// Flush the cache.

		symbolsByChar = null;
	    }
	} );
    }

    public StreamParser parseStream(SeqIOListener listener) {
	return new TPStreamParser(listener);
    }

    private class TPStreamParser implements StreamParser {
	private SeqIOListener listener;
	private Symbol[] buffer;

	{
	    buffer = new Symbol[256];
	}

	public TPStreamParser(SeqIOListener l) {
	    this.listener = l;
	}

	public void characters(char[] data, int start, int len) 
	    throws IllegalSymbolException
	{
	    int cnt = 0;
	    while (cnt < len) {
		int bcnt = 0;
		while (cnt < len && bcnt < buffer.length) {
		    buffer[bcnt++] = parseCharToken(data[start + (cnt++)]);
		}
		try {
		    listener.addSymbols(getAlphabet(),
					buffer,
					0,
					bcnt);
		} catch (IllegalAlphabetException ex) {
		    throw new BioError(ex, "Assertion failed: can't add symbols.");
		}
	    }
	}

	public void close() {
	}
    }
}

