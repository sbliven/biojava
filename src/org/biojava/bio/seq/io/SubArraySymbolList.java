package org.biojava.bio.seq.io;

import java.util.*;
import java.io.*;

import org.biojava.bio.symbol.*;

class SubArraySymbolList extends AbstractSymbolList {
    private final Alphabet alpha;
    private final int length;
    private final int offset;
    private final Symbol[] array;

    SubArraySymbolList(Symbol[] array, int length, int offset, Alphabet alpha) {
	this.alpha = alpha;
	this.length = length;
	this.offset = offset;
	this.array = array;

	if (length + offset > array.length)
	    throw new IndexOutOfBoundsException();
    }

    public Alphabet getAlphabet() {
	return alpha;
    }

    public int length() {
	return length;
    }

    public Symbol symbolAt(int pos) {
	if (pos < 1 || pos > length)
	    throw new IndexOutOfBoundsException();
	return array[offset + pos - 1];
    }
}
