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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.biojava.bio.BioError;
import org.biojava.bio.BioException;
import org.biojava.bio.symbol.AlphabetManager;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;

/**
 * <code>SymbolListCharSequence</code> is a <code>CharSequence</code>
 * implementation which wraps a <code>SymbolList</code>. It is present
 * primarily to support regular expression matching over
 * <code>SymbolList</code>s as it avoids creating a copy.
 *
 * @author Keith James
 * @since 1.3
 */
public class SymbolListCharSequence implements CharSequence
{
    private SymbolList syms;
    private Map alphaTokens;

    /**
     * Creates a new <code>SymbolListCharSequence</code> wrapping a
     * <code>SymbolList</code>.
     *
     * @param syms a <code>SymbolList</code>.
     */
    public SymbolListCharSequence(SymbolList syms)
    {
        FiniteAlphabet alphabet = (FiniteAlphabet) syms.getAlphabet();
        if (! (alphabet instanceof FiniteAlphabet))
            throw new IllegalArgumentException("Only SymbolLists using a FiniteAlphabet are supported by SymbolListCharSequence");

        SymbolTokenization sToke;
        try
        {
            sToke = alphabet.getTokenization("token");
        }
        catch (BioException be)
        {
            throw new BioError(be, "Internal error: failed to get SymbolTokenization for SymbolList alphabet");
        }

        if (sToke.getTokenType() != SymbolTokenization.CHARACTER)
            throw new IllegalArgumentException("Only FiniteAlphabets using a char token are supported by SymbolListCharSequence");

        this.syms = syms;
        alphaTokens = new HashMap(Math.round(alphabet.size() / 0.75f) + 1);

        int sIndex;

        try
        {
            for (Iterator si = AlphabetManager.getAllSymbols(alphabet).iterator(); si.hasNext();)
            {
                Symbol s = (Symbol) si.next();
                char symChar = sToke.tokenizeSymbol(s).charAt(0);
                alphaTokens.put(s, new Character(symChar));
            }
        }
        catch (IllegalSymbolException ise)
        {
            throw new BioError(ise, "Internal error: failed to tokenize a Symbol from an existing SymbolList");
        }
    }

    private SymbolListCharSequence(SymbolList syms, Map alphaTokens)
    {
        this.syms        = syms;
        this.alphaTokens = alphaTokens;
    }

    public char charAt(int index)
    {
        return ((Character) alphaTokens.get(syms.symbolAt(index + 1))).charValue();
    }

    public int length()
    {
        return syms.length();
    }

    public CharSequence subSequence(int start, int end)
    {
        return new SymbolListCharSequence(syms.subList(start + 1, end + 1),
                                          alphaTokens);
    }

    public String toString()
    {
        return syms.seqString();
    }
}
