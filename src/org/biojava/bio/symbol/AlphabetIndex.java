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

/**
 * Map between Symbols and index numbers.
 *
 * @author Thomas Down
 * @since 1.1
 */

public interface AlphabetIndex {
    public FiniteAlphabet getAlphabet();

    public int indexForSymbol(Symbol s) throws IllegalSymbolException;
    public Symbol symbolForIndex(int i) throws IndexOutOfBoundsException;
    public Symbol[] toArray();
    public int size();
}
