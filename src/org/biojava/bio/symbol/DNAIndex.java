package org.biojava.bio.symbol;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;

/**
 * Hand-optimized indexer for DNA ('cos nothing else really counts ;)
 *
 * @author Thomas Down
 */

class DNAIndex implements AlphabetIndex {
    private final static Symbol a = DNATools.a();
    private final static Symbol c = DNATools.c();
    private final static Symbol g = DNATools.g();
    private final static Symbol t = DNATools.t();

    private final static Symbol[] array;

    static {
	array = new Symbol[4];
	array[0] = a;
	array[1] = c;
	array[2] = g;
	array[3] = t;
    }

    public FiniteAlphabet getAlphabet() {
	return DNATools.getDNA();
    }

    public int indexForSymbol(Symbol s) throws IllegalSymbolException {
	if (s == a)
	    return 0;
	else if (s == c) 
	    return 1;
	else if (s == g)
	    return 2;
	else if (s == t)
	    return 3;
	
	throw new IllegalSymbolException("Alphabet DNA does not contain atomic symbol " + s.getName());
    }

    public Symbol symbolForIndex(int i) {
	return array[i];
    }

    public Symbol[] toArray() {
	Symbol[] a = new Symbol[array.length];
	System.arraycopy(array, 0, a, 0, array.length);
	return a;
    }

    public int size() {
	return array.length;
    }
}
