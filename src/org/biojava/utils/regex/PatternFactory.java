



package org.biojava.utils.regex;

import java.util.Iterator;

import org.biojava.bio.BioException;
import org.biojava.bio.symbol.AtomicSymbol;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.AlphabetManager;
import org.biojava.bio.symbol.AbstractAlphabet;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.seq.io.CharacterTokenization;

class PatternFactory
{
    private FiniteAlphabet alfa;
    private SymbolTokenization toke = null;

    PatternFactory(FiniteAlphabet alfa)
    {
        this.alfa = alfa;
        fetchTokenizer();
    }

    private void fetchTokenizer()
    {
        boolean gotCharTokenizer =false;
        try {
            toke = alfa.getTokenization("token");
            if (toke.getTokenType() == SymbolTokenization.CHARACTER)
                gotCharTokenizer = true;
        }
        catch (BioException be) {
        }

        if (!gotCharTokenizer) {
            // make own tokenizer for this turkey
            CharacterTokenization cToke = new CharacterTokenization(alfa, true);

            // go thru' and associate all atomic symbols with a unicode char
            char uniChar = '\uE000';
            for (Iterator symI = alfa.iterator(); symI.hasNext(); ) {
                AtomicSymbol sym = (AtomicSymbol) symI.next();
                cToke.bindSymbol(sym, uniChar);
                uniChar++;
            }

            // add all ambiguity symbol
            cToke.bindSymbol(
                AlphabetManager.getAllAmbiguitySymbol((FiniteAlphabet) alfa),
                '\uF8FF');
            // add terminal gap
            cToke.bindSymbol(Alphabet.EMPTY_ALPHABET.getGapSymbol(), '~');
            // add interstitial gap
            cToke.bindSymbol(alfa.getGapSymbol(), '-');

            // bind alphabet to this tokenization
            ((AbstractAlphabet) alfa).putTokenization("unicode", cToke);
            toke = cToke;
        }
    }

    public org.biojava.utils.regex.Pattern compile(String pattern)
    {
        // validate the pattern is from this alphabet
        // we only accept RE tokens and characters from 
        // the alphabet itself.
        return new org.biojava.utils.regex.Pattern(java.util.regex.Pattern.compile(pattern), alfa);
    }

    public char charValue(Symbol sym)
        throws IllegalSymbolException
    {
        // this class is only used with alphabets that have a character tokenization.
        return toke.tokenizeSymbol(sym).charAt(0);
    }

    public static PatternFactory makeFactory(FiniteAlphabet alfa)
    {
        return new PatternFactory(alfa);
    }
}

