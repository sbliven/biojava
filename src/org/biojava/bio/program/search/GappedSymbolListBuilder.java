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

package org.biojava.bio.program.search;

import java.lang.StringBuffer;
import java.lang.IndexOutOfBoundsException;
import java.util.Arrays;

import org.biojava.bio.seq.io.TokenParser;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.GappedSymbolList;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.symbol.IllegalSymbolException;

/**
 * <code>GappedSymbolListBuilder</code> objects contain a method to
 * create GappedSymbolList objects from sequence strings which contain
 * gap characters. Currently only the standard Biojava gap character
 * ("-") is supported.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @version 1.0
 * @since 1.1
 */
class GappedSymbolListBuilder
{
    /**
     * <code>INCREMENT</code> is the length that the gapIndices array
     * is extended by if it overflows its inital allocation of 128
     * gaps
     */
    private static final int INCREMENT = 128;

    private TokenParser parser;
    private int []      gapIndices;

    SymbolList          ungapped;
    GappedSymbolList    gapped;

    /**
     * Creates a new <code>GappedSymbolListBuilder</code> object.
     *
     * @param alpha a <code>FiniteAlphabet</code> object into which
     * the sequence tokens will be parsed
     */
    GappedSymbolListBuilder(FiniteAlphabet alpha)
    {
	this.parser = new TokenParser(alpha);
    }

    /**
     * <code>makeGappedSymbolList</code>.
     *
     * @param tokeBuffer a <code>StringBuffer</code> object containing
     * the sequence tokens.
     * @return a <code>SymbolList</code> object which consists of a
     * GappedSymbolList with an ungapped underlying sequence and all
     * the gaps added in the gapped view.
     */
    SymbolList makeGappedSymbolList(StringBuffer tokeBuffer)
    {
	gapIndices = new int [128];

	// Record and delete the gaps in reverse order so each
	// operation won't affect the indices of the remaining gaps

	int gapCount = 0;
	for (int i =  tokeBuffer.length() - 1;  i >= 0; i--)
	{
	    if (tokeBuffer.charAt(i) == '-')
	    {
		gapCount++;
		recordGap(gapCount, i);
		tokeBuffer.deleteCharAt(i);
	    }
	}

	// The gap positions were recorded in descending numerical
	// order. This sorts them back to ascending as I think
	// subsequent operations are clearer
	Arrays.sort(gapIndices);

	try
	{
	    ungapped = parser.parse(tokeBuffer.toString());
	    gapped   = new GappedSymbolList(ungapped);

	    for (int i = 0;  i < gapIndices.length; i++)
	    {
		// SymbolLists use biological sequence indexing (from
		// 1, not 0), so add 1 to the position
		if (gapIndices[i] > 0)
		{
		    gapped.addGapInView(gapIndices[i] + 1);
		}
	    }
	}
	catch (IllegalSymbolException ise)
	{
	    ise.printStackTrace();
	}
	catch (IndexOutOfBoundsException iobe)
	{
	    iobe.printStackTrace();
	}
	// System.out.println("Created: " + gapped.seqString());
	return gapped;
    }

    /**
     * The <code>recordGap</code> method records positions of
     * individual gap characters
     *
     * @param gapCount an <code>int</code> value n, indicating that
     * this is the n'th gap.
     * @param gapIndex an <code>int</code> value indicating the
     * position of this gap within the sequence
     */
    private void recordGap(int gapCount, int gapIndex)
    {
	// System.out.println("Recording gap at: " + gapIndex);

	// There is room to record another gap index
	if (gapCount <= gapIndices.length)
	{
	    gapIndices[gapCount] = gapIndex;
	}
	// The array needs to be extended first
	else
	{
	    int [] extGapIndices = new int [gapIndices.length + INCREMENT];
	    System.arraycopy(gapIndices,    0,
			     extGapIndices, 0,
			     gapIndices.length);
	    gapIndices = extGapIndices;

	    gapIndices[gapCount] = gapIndex;
	}
    }
}
