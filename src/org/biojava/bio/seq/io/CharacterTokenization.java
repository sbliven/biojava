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
 * Implementation of SymbolTokenization which binds symbols
 * to single unicode characters
 *
 * @author Thomas Down
 * @since 1.2
 */

public class CharacterTokenization implements SymbolTokenization {
    private Alphabet alphabet;
    private Map symbolsToCharacters = new HashMap();
    private Map charactersToSymbols = new HashMap();
    private transient Symbol[] tokenTable;
    private boolean caseSensitive;

    public CharacterTokenization(Alphabet alpha, boolean caseSensitive) {
	alphabet = alpha;
	this.caseSensitive = caseSensitive;
    }

    public Alphabet getAlphabet() {
	return alphabet;
    }

    public TokenType getTokenType() {
	return CHARACTER;
    }

    public Annotation getAnnotation() {
	return Annotation.EMPTY_ANNOTATION;
    }

    public void bindSymbol(Symbol s, char c) {
	Character chr = new Character(c);

	if (!symbolsToCharacters.containsKey(s)) {
	    symbolsToCharacters.put(s, chr);
	}
	charactersToSymbols.put(chr, s);
	tokenTable = null;
    }

    public Symbol parseToken(String token)
        throws IllegalSymbolException
    {
	if (token.length() != 1) {
	    throw new IllegalSymbolException("This Tokenization only accepts single-character tokens");
	}
	return parseTokenChar(token.charAt(0));
    }

    protected Symbol[] getTokenTable() {
	if (tokenTable == null) {
	    int maxChar = 0;
	    for (Iterator i = charactersToSymbols.keySet().iterator(); i.hasNext(); ) {
		Character c = (Character) i.next();
		char cv = c.charValue();
		if (caseSensitive) {
		    maxChar = Math.max(maxChar, cv);
		} else {
		    maxChar = Math.max(maxChar, Character.toUpperCase(cv));
		    maxChar = Math.max(maxChar, Character.toLowerCase(cv));
		}
	    }

	    tokenTable = new Symbol[maxChar + 1];

	    for (Iterator i = charactersToSymbols.entrySet().iterator(); i.hasNext(); ) {
		Map.Entry me = (Map.Entry) i.next();
		Symbol sym = (Symbol) me.getValue();
		Character c = (Character) me.getKey();
		char cv = c.charValue();
		if (caseSensitive) {
		    tokenTable[cv] = sym;
		} else {
		    tokenTable[Character.toUpperCase(cv)] = sym;
		    tokenTable[Character.toLowerCase(cv)] = sym;
		}
	    }
	}

	return tokenTable;
    }

    protected Symbol parseTokenChar(char c)
        throws IllegalSymbolException
    {
	Symbol[] tokenTable = getTokenTable();
	Symbol sym = null;
	if (c < tokenTable.length) {
	    sym = tokenTable[c];
	}
	if (sym == null) {
	    throw new IllegalSymbolException("This tokenization doesn't contain character: " + c);
	}

	return sym;
    }

    public String tokenizeSymbol(Symbol s) throws IllegalSymbolException {
	Character c = (Character) symbolsToCharacters.get(s);
	if (c == null) {
	    throw new IllegalSymbolException("No mapping for symbol " + s.getName());
	}

	return "" + c.charValue();
    }

    public String tokenizeSymbolList(SymbolList sl)
        throws IllegalAlphabetException
    {
	if (sl.getAlphabet() != getAlphabet()) {
	    throw new IllegalAlphabetException("Alphabet " + sl.getAlphabet().getName() + " does not match " + getAlphabet().getName());
	}
	StringBuffer sb = new StringBuffer();
	for (Iterator i = sl.iterator(); i.hasNext(); ) {
	    Symbol sym = (Symbol) i.next();
	    Character c = (Character) symbolsToCharacters.get(sym);
	    if (c == null) {
		throw new IllegalAlphabetException("No mapping for symbol " + sym.getName());
	    }

	    sb.append(c.charValue());
	}

	return sb.toString();
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
		    buffer[bcnt++] = parseTokenChar(data[start + (cnt++)]);
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

    
    public void addChangeListener(ChangeListener cl, ChangeType ct) {}
    public void addChangeListener(ChangeListener cl) {}
    public void removeChangeListener(ChangeListener cl, ChangeType ct) {}
    public void removeChangeListener(ChangeListener cl) {}
}
