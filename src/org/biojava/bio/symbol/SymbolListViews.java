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
 * Factory methods for constructing useful views onto SymbolLists.
 *
 * @author Thomas Down
 * @since 1.1
 */

public class SymbolListViews {
    /**
     * An n-th order view of another SymbolList.
     * <p>
     * In practice, what this means is that you can view a DNA sequence into an
     * overlapping dinucleotide sequence without having to do any work yourself.
     * </p>
     *
     * @param source The underlying SymbolList to view
     * @param order The window size
     */

    public static SymbolList orderNSymbolList(SymbolList source, int order) 
        throws IllegalAlphabetException
    {
	if (order == 1)
	    return source;

	return new OrderNSymbolList(source, order);
    }

    /**
     * A view of windows onto another SymbolList.
     * <p>
     * In practice, what this means is that you can view a DNA sequence as codons which
     * do not overlap.
     * </p>
     *
     * @param source The underlying SymbolList to view
     * @param wsize The window size.
     * @throws IllegalArgumentException if the symbollist length isn't an integer multiple of wsize.
     */

    public static SymbolList windowedSymbolList(SymbolList source, int wsize)
        throws IllegalArgumentException
    {
	return new WindowedSymbolList(source, wsize);
    }

    /**
     * A reversed view onto a SymbolList.
     *
     * @param symbols the SymbolList to reverse.
     */

    public static SymbolList reverse(SymbolList symbols) {
	return new ReverseSymbolList(symbols);
    }

    /**
     * Provides a 'translated' view of an underlying SymbolList.
     * <p>
     * This method allows you to translate from one alphabet into another, so
     * for example, you could translate from DNA-triplets into amino-acids. You
     * could also translate from DNA-dinucleotide into the 'twist' structural
     * metric, or any other translation that takes your fancy.</p>
     * <p>
     * The actual mapping from source to view Symbol is encapsulated in a
     * TranslationTable object.</p>
     * <p>
     * The translated SymbolList will be the same length as the source, and each
     * Symbol in the view will correspond to a single Symbol in the source.</p>
     *
     * @param symbols a SymbolList to translate.
     * @param table a translation table for mapping symbols.
     */

    public static SymbolList translate(SymbolList symbols,
				TranslationTable table)
        throws IllegalAlphabetException
    {
	return new TranslatedSymbolList(symbols, table);
    }
}
