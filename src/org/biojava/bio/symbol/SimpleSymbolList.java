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

import org.biojava.utils.*;

/**
 * Basic implementation of SymbolList.  This
 * is currently backed by a normal Java array.
 *
 * This is a new implementation which no longer uses the
 * Java ArrayLists.  I hope that eventually it can be
 * made immutable, but for now the legacy addSymbol method
 * is implemented.
 *
 * @author Thomas Down
 */

public class SimpleSymbolList extends AbstractSymbolList implements Serializable {
    private static final int INCREMENT = 100;

    private Alphabet alphabet;
    private Symbol[] symbols;
    private int length;

    /**
     * Construct an empty SimpleSymbolList.
     *
     * @param alpha The alphabet of legal symbols in this list.
     */

    public SimpleSymbolList(Alphabet alpha) {
	this.alphabet = alpha;
	this.length = 0;
	this.symbols = new Symbol[INCREMENT];
    }

    /**
     * Construct a SymbolList containing the symbols in the specified list.
     *
     * @param alpha The alphabet of legal symbols for this list.
     * @param rList A Java List of symbols.
     * 
     * @throws IllegalSymbolException if a Symbol is not in the specified alphabet.
     * @throws ClassCastException if rList contains objects which do not implement Symbol.
     */

    public SimpleSymbolList(Alphabet alpha, List rList) 
        throws IllegalSymbolException
    {
      this.alphabet = alpha;
      this.length = rList.size();
      symbols = new Symbol[length];
	
	    int pos = 0;
      for (Iterator i = rList.iterator(); i.hasNext(); ) {
        symbols[pos] = (Symbol) i.next();
        alphabet.validate(symbols[pos]);
        pos++;
      }
    }

    /**
     * Construct a copy of an existing SymbolList.
     *
     * @param The list to copy.
     */

    public SimpleSymbolList(SymbolList sl) {
      this.alphabet = sl.getAlphabet();
      this.length = sl.length();
      symbols = new Symbol[length];
      for (int i = 0; i < length; ++i) {
        symbols[i] = sl.symbolAt(i + 1);
      }
    }

    /**
     * Get the alphabet of this SymbolList.
     */

    public Alphabet getAlphabet() {
      return alphabet;
    }

    /**
     * Get the length of this SymbolList.
     */

    public int length() {
      return length;
    }

    /**
     * Find a symbol at a specified offset in the SymbolList.
     * NB. Speedups possible once this class is immutable.
     * 
     * @param pos Position in biological coordinates (1..length)
     */

    public Symbol symbolAt(int pos) {
      if (pos > length || pos < 1) {
        throw new IndexOutOfBoundsException(
          "Can't access " + pos +
          " as it is not within 1.." + length
        );
      }
      return symbols[pos - 1];
    }

    /**
     * Add a new Symbol to the end of this list.
     * 
     * @param sym Symbol to add
     * @throws IllegalSymbolException if the Symbol is not in this list's alphabet.
     *
     * @deprecated Can we make SimpleSymbolList immutable [Thomas Down]
     */

    public void addSymbol(Symbol sym) 
        throws IllegalSymbolException
    {
	alphabet.validate(sym);

	if (symbols.length <= this.length) {
	    Symbol[] newSymbols = new Symbol[symbols.length + INCREMENT];
	    System.arraycopy(symbols, 0, newSymbols, 0, length);
	    symbols = newSymbols;
	}
	symbols[length++] = sym;
    }
}
