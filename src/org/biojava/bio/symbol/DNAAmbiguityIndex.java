package org.biojava.bio.symbol;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import java.util.*;

/**
 * Hand-optimized ambiguity indexer for DNA ('cos nothing else really counts ;)
 *
 * @author Thomas Down
 */

class DNAAmbiguityIndex implements AlphabetIndex {
    private final static Symbol a = DNATools.a();
    private final static Symbol c = DNATools.c();
    private final static Symbol g = DNATools.g();
    private final static Symbol t = DNATools.t();

    private final static Symbol[] array;

    static {
	array = new Symbol[16];
	for (int i = 1; i < 16; ++i) {
	    Set s = new HashSet();
	    if ((i & 1) != 0)
		s.add(a);
	    if ((i & 2) != 0)
		s.add(c);
	    if ((i & 4) != 0)
		s.add(g);
	    if ((i & 8) != 0)
		s.add(c);

	    try {
		array[i] = AlphabetManager.getAmbiguitySymbol(s);
	    } catch (IllegalSymbolException ex) {
		throw new BioError(ex);
	    }
	}
    }

    public FiniteAlphabet getAlphabet() {
	return DNATools.getDNA();
    }

    public int indexForSymbol(Symbol s) throws IllegalSymbolException {
	if (s == a)
	    return 1;
	else if (s == c) 
	    return 2;
	else if (s == g)
	    return 4;
	else if (s == t)
	    return 8;
	
	int indx = 0;
	for (Iterator i = ((FiniteAlphabet) s.getMatches()).iterator(); i.hasNext(); ) {
	    Symbol as = (Symbol) i.next();
	    if (as == a)
		indx |= 1;
	    else if (as == c)
		indx |= 2;
	    else if (as == g)
		indx |= 4;
	    else if (as == t)
		indx |= 8;
	}

	if (indx == 0)
	    throw new IllegalSymbolException("Alphabet DNA does not contain atomic symbol " + s.getName());

	return indx;
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
