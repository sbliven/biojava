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
 * Simple implementation of SymbolTokenization which uses
 * the `name' field of the symbols.  This class works
 * with any FiniteAlphabet, and doesn't need any extra
 * data to be provided.
 *
 * @author Thomas Down
 * @since 1.2
 */

public class NameTokenization implements SymbolTokenization {
    private FiniteAlphabet alphabet;
    private transient Map nameToSymbol = null;

    public NameTokenization(FiniteAlphabet fab) {
	this.alphabet = fab;
    }

    public Alphabet getAlphabet() {
	return alphabet;
    }

    public TokenType getTokenType() {
	return SEPARATED;
    }

    public Annotation getAnnotation() {
	return Annotation.EMPTY_ANNOTATION;
    }

    protected Map getNameToSymbol() {
        if (nameToSymbol == null) {
	    nameToSymbol = new HashMap();
	    for (Iterator i = alphabet.iterator(); i.hasNext(); ) {
		Symbol sym = (Symbol) i.next();
		nameToSymbol.put(sym.getName(), sym);
	    }
	    nameToSymbol.put("gap", getAlphabet().getGapSymbol());
	}

	return nameToSymbol;
    }

    public Symbol parseToken(String token)
        throws IllegalSymbolException
    {
	Symbol sym = (Symbol) getNameToSymbol().get(token);
	if (sym == null) {
	    char c = token.charAt(0);
	    if (c == '[') {
		throw new IllegalSymbolException("We need to handle ambiguity");
	    }
	    throw new IllegalSymbolException("Token `" + token + "' does not appear as a named symbol in alphabet `" + getAlphabet().getName() + "'");
	}
	return sym;
    }

    public String tokenizeSymbol(Symbol s) throws IllegalSymbolException {
	getAlphabet().validate(s);
	return s.getName();
    }

    public String tokenizeSymbolList(SymbolList sl)
        throws IllegalAlphabetException
    {
	if (sl.getAlphabet() != getAlphabet()) {
	    throw new IllegalAlphabetException("Alphabet " + sl.getAlphabet().getName() + " does not match " + getAlphabet().getName());
	}
	StringBuffer sb = new StringBuffer();
	Iterator i = sl.iterator();
	while (i.hasNext()) {
	    Symbol sym = (Symbol) i.next();
	    sb.append(sym.getName());
	    if (i.hasNext()) {
		sb.append(' ');
	    }
	}
	return sb.toString();
    }

    public StreamParser parseStream(SeqIOListener siol) {
	return new NameStreamParser(siol);
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
	    String str = sb.toString();
	    StringTokenizer toke = new StringTokenizer(str, " \n\r");
	    List sl = new ArrayList();
	    while (toke.hasMoreTokens()) {
		Symbol sym = parseToken(toke.nextToken());
		sl.add(sym);
	    }
	}
    }

    public void addChangeListener(ChangeListener cl, ChangeType ct) {}
    public void addChangeListener(ChangeListener cl) {}
    public void removeChangeListener(ChangeListener cl, ChangeType ct) {}
    public void removeChangeListener(ChangeListener cl) {}
}
