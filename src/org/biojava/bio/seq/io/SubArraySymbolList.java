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
import org.biojava.bio.symbol.*;

/**
 * Really simple SymbolList which exposes any part of an array of Symbols
 * as a SymbolList.  This exists primarily as a support class for
 * ChunkedSymbolList.
 *
 * @author Thomas Down
 * @since 1.1
 */

class SubArraySymbolList extends AbstractSymbolList {
    private final Alphabet alpha;
    private final int length;
    private final int offset;
    private final Symbol[] array;

    protected void finalize() throws Throwable {
	super.finalize();
	alpha.removeChangeListener(ChangeListener.ALWAYS_VETO, Alphabet.SYMBOLS);
    }

    /**
     * Construct a new SubArraySymbolList.  NOTE: this ct does no
     * validation, and therefore shouldn't be called from user code.
     */

    SubArraySymbolList(Symbol[] array, int length, int offset, Alphabet alpha) {
	this.alpha = alpha;
	this.length = length;
	this.offset = offset;
	this.array = array;

	if (length + offset > array.length)
	    throw new IndexOutOfBoundsException();

	alpha.addChangeListener(ChangeListener.ALWAYS_VETO, Alphabet.SYMBOLS);
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
