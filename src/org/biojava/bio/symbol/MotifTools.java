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

import java.util.Iterator;

import org.biojava.bio.BioError;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;

/**
 * <code>MotifTools</code> contains utility methods for sequence
 * motifs.
 *
 * @author Keith James
 */
public class MotifTools
{
    /**
     * <code>createRegex</code> creates a regular expression which
     * matches the <code>SymbolList</code>. At the moment ambiguous
     * <code>Symbol</code>s are simply expanded into character
     * classes. For example the nucleotide sequence "CTNNG" is
     * expanded to "CT[ACGT][ACGT]G" rather than "CT[ACTG]{2}G". This
     * will be addressed in the future.
     *
     * @param motif a <code>SymbolList</code>.
     *
     * @return a <code>String</code> regular expression.
     */
    public static String createRegex(SymbolList motif)
    {
        StringBuffer sb = new StringBuffer();

        try
        {
            SymbolTokenization sToke = motif.getAlphabet().getTokenization("token");

            for (int i = 1; i <= motif.length(); i++)
            {
                Symbol sym = motif.symbolAt(i);
                FiniteAlphabet ambiAlpha = (FiniteAlphabet) sym.getMatches();
                int ambiSize = ambiAlpha.size();
                if (ambiSize > 1)
                    sb.append("[");

                for (Iterator ai = ambiAlpha.iterator(); ai.hasNext();)
                {
                    Symbol ambiSym = (Symbol) ai.next();
                    sb.append(sToke.tokenizeSymbol(ambiSym));
                }

                if (ambiSize > 1)
                    sb.append("]");
            }
        }
        catch (IllegalSymbolException ise)
        {
            throw new BioError(ise, "Internal error: failed to tokenize a Symbol from an existing SymbolList");
        }
        catch (BioException be)
        {
            throw new BioError(be, "Internal error: failed to get SymbolTokenization for SymbolList alphabet");
        }

        return sb.substring(0);
    }
}
