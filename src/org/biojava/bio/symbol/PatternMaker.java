
package org.biojava.bio.symbol;

import org.biojava.bio.BioException;
import org.biojava.utils.ParserException;
import org.biojava.bio.seq.io.SymbolTokenization;

public class PatternMaker
{
    private class Range
    {
        private int min = 1;
        private int max = 1;
        private int getMin() { return min; }
        private int getMax() { return max; }
        private void setMin(int min) { this.min = min; }
        private void setMax(int max) { this.max = max; }
    }

    private static class Tokenizer
    {
        private String packedTxt;
        private int ptr = 0;

        final static int EOL = -1;
        final static int SYMBOL_TOKEN = 0;
        final static int NUMERIC = 1;
        final static int LEFT_BRACE = 2;
        final static int RIGHT_BRACE = 3;
        final static int COMMA = 4;
        final static int LEFT_BRACKET = 5;
        final static int RIGHT_BRACKET = 6;
        final static int UNKNOWN = 999;

        private Tokenizer(String target)
        {
            packedTxt = pack(target);
        }

        private char getToken()
            throws IndexOutOfBoundsException
        {
            if (hasNext())
                 return packedTxt.charAt(ptr++);
            else
                throw new IndexOutOfBoundsException("text length: " + packedTxt.length() + " index: " + ptr);
        }

        private int nextTokenType()
        {
            if (!hasNext()) return EOL;

            char nextCh = packedTxt.charAt(ptr);

            // symbol tokens assumed to be alphas.
            if (Character.isLetter(nextCh))
                return SYMBOL_TOKEN;

            // now check for specific chars
            if (nextCh == '{')
                return LEFT_BRACE;
            if (nextCh == '}')
                return RIGHT_BRACE;
            if (nextCh == ',')
                return COMMA;
            if (nextCh == '(')
                return LEFT_BRACKET;
            if (nextCh == ')')
                return RIGHT_BRACKET;
            return UNKNOWN;
        }

        private boolean hasNext()
        {
            return ptr < packedTxt.length();
        }

        /**
         * produces a version of the String with whitespace removed.
         */
        private String pack(String source)
        {
            StringBuffer packedString = new StringBuffer();
    
            for (int i=0; i < source.length(); i++) {
                char currCh;
                if (!Character.isWhitespace(currCh = source.charAt(i))) {
                    packedString.append(currCh);
                }
            }
    
            return packedString.toString();
        }
    }

    private Tokenizer toke;
    private PatternSearch.Pattern pattern;
    private SymbolTokenization symToke;
    private FiniteAlphabet alfa;

    public PatternSearch.Pattern parsePattern(String patternTxt, FiniteAlphabet alfa)
        throws BioException, IllegalSymbolException, IllegalAlphabetException, ParserException
    {
        toke = new Tokenizer(patternTxt);
        pattern = new PatternSearch.Pattern(patternTxt, alfa);
        symToke = alfa.getTokenization("token");
        this.alfa = alfa;

        // check for empty pattern
        if (!toke.hasNext()) return null;

        while (toke.hasNext()) {
            int tokenType = toke.nextTokenType();

            switch (tokenType) {
                case Tokenizer.SYMBOL_TOKEN:
                    Symbol sym = symToke.parseToken(Character.toString(toke.getToken()));
                    if (toke.getToken() == Tokenizer.LEFT_BRACE) {
                        Range range = getIterations();
                        PatternSearch.Pattern thisP = new PatternSearch.Pattern(symToke.tokenizeSymbol(sym), alfa);
                        thisP.addSymbol(sym);
                        thisP.setMin(range.getMin());
                        thisP.setMax(range.getMax());
                        pattern.addPattern(thisP);
                    }
                    else {
                        pattern.addSymbol(sym);
                    }
                    break;
                case Tokenizer.LEFT_BRACKET:
                    PatternSearch.Pattern thisP = parsePattern();
                    if (toke.getToken() == Tokenizer.LEFT_BRACE) {
                        Range range = getIterations();
                        thisP.setMin(range.getMin());
                        thisP.setMax(range.getMax());
                    }
                    pattern.addPattern(thisP);
                    break;
                default:
                    throw new ParserException(toke.getToken() + " is not valid at this point.");
            }
        }

        return pattern;
    }

    private Range getIterations()
        throws ParserException
    {
        Range range = new Range();

        // consume the left brace
        toke.getToken();

        // there can either be one or two numbers
        boolean onSecondArg = false;
        while (toke.hasNext()) {
            int tokenType = toke.nextTokenType();
            StringBuffer numString = new StringBuffer();

            switch (tokenType) {
                case Tokenizer.NUMERIC:
                    numString.append(toke.getToken());
                    break;
                case Tokenizer.COMMA:
                    toke.getToken();
                    if (!onSecondArg) {
                        range.setMin(Integer.parseInt(numString.toString()));
                        numString = new StringBuffer();
                        onSecondArg = true;
                    }
                    else {
                        throw new ParserException("only two arguments permitted.");
                    }
                    break;
                case Tokenizer.RIGHT_BRACE:
                    toke.getToken();
                    if (onSecondArg) {
                        range.setMax(Integer.parseInt(numString.toString()));
                    }
                    else {
                        range.setMin(Integer.parseInt(numString.toString()));
                        range.setMax(range.getMin());
                    }
                    return range;
                default:
                    throw new ParserException(toke.getToken() + " is not valid at this point.");
            }
        }

        throw new ParserException("unexpected error.");
    }

    private PatternSearch.Pattern parsePattern()
        throws IllegalSymbolException, IllegalAlphabetException, ParserException
    {
        // consume left bracket
        toke.getToken();

        PatternSearch.Pattern pattern = new PatternSearch.Pattern("", alfa);
        boolean hasContent = false;

        while (toke.hasNext()) {
            int tokenType = toke.nextTokenType();

            switch (tokenType) {
                case Tokenizer.SYMBOL_TOKEN:
                    Symbol sym = symToke.parseToken(Character.toString(toke.getToken()));
                    if (toke.getToken() == Tokenizer.LEFT_BRACE) {
                        Range range = getIterations();
                        PatternSearch.Pattern thisP = new PatternSearch.Pattern("", alfa);
                        thisP.addSymbol(sym);
                        thisP.setMin(range.getMin());
                        thisP.setMax(range.getMax());
                        pattern.addPattern(thisP);
                    }
                    else {
                        pattern.addSymbol(sym);
                    }
                    break;
                case Tokenizer.LEFT_BRACKET:
                    PatternSearch.Pattern thisP = parsePattern();
                    if (toke.getToken() == Tokenizer.LEFT_BRACE) {
                        Range range = getIterations();
                        thisP.setMin(range.getMin());
                        thisP.setMax(range.getMax());
                    }
                    pattern.addPattern(thisP);
                    break;
                case Tokenizer.RIGHT_BRACKET:
                    toke.getToken();
                    if (hasContent)
                        return pattern;
                    else    
                        throw new ParserException("empty pattern!");

                default:
                    throw new ParserException(toke.getToken() + " is not valid at this point.");
            }
        }

        throw new ParserException("unexpected error.");
    }
}

