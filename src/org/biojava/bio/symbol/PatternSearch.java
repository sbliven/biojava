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

import java.util.List;
import java.util.ArrayList;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.BioException;

/**
 * Class to perform arbitrary regex-like searches on
 * any FiniteAlphabet.
 * <p>
 * The API is still in flux.
 *
 * @author David Huen
 * @since 1.4
 */

public class PatternSearch
{

    private static class MatchState
        implements Cloneable
    {

        private int patternPos = 0;
        private int symListPos = 1;

        public Object clone()
        {
            try {
                return super.clone();
            }
            catch (CloneNotSupportedException cne) {
                ////System.err.println("unexpected exception!");
                cne.printStackTrace();
            }
            return null;
        }

        private void resetPattern()
        {
            patternPos = 0;
        }
    }

    /**
     * Class to describe a regex-like pattern.  The pattern
     * is a list of other patterns or Symbols in the target
     * FiniteAlphabet.  These are added to the list with
     * addSymbol and addPattern calls.  The pattern can be
     * reiterated a number of times (deafult is once).
     */
    public static class Pattern
    {
        private List patternList;
        private FiniteAlphabet alfa;
        private int min = 1;
        private int max = 1;
        private String label;
        private SymbolTokenization symToke = null;

        /**
         * @param label A String describing the Pattern.
         * @param alfa The FiniteAlphabet the Pattern is defined over.
         */
        public Pattern(String label, FiniteAlphabet alfa)
        {
            this.alfa = alfa;
            this.label = label;
            patternList = new ArrayList();
        }

        /**
         * Add a Symbol to the end of the Pattern.
         */
        public void addSymbol(Symbol sym)
            throws IllegalAlphabetException
        {
            if (!alfa.contains(sym))
                throw new IllegalAlphabetException(sym.getName() + " is not in Alphabet " + alfa.getName());

            patternList.add(sym);
        }

        /**
         * Add a Pattern to the end of the Pattern.
         */
        public void addPattern(Pattern pattern)
            throws IllegalAlphabetException
        {
            if (alfa != pattern.getAlphabet())
                throw new IllegalAlphabetException(pattern.getAlphabet().getName() + " is not compatible with Alphabet " + alfa.getName());

            patternList.add(pattern);
        }

        public Alphabet getAlphabet() { return alfa; }
        public int getMin() { return min; }
        public int getMax() { return max; }
        public String getLabel() { return label; }

        /**
         * Minimum number of times the Pattern is to be matched.
         */
        public void setMin(int min)
            throws IllegalArgumentException
        {
            if (min < 0)
                throw new IllegalArgumentException("number of repeats must be non-negative.");
            else
                this.min = min;
        }

        /**
         * Maximum number of times the Pattern is to be matched.
         */
        public void setMax(int max)
        {
            if (max < 0)
                throw new IllegalArgumentException("number of repeats must be non-negative.");
            else
            this.max = max;
        }

        private List getPatternList()
        {
            return patternList;
        }

        public String toString()
        {
            try {
                return toString(Pattern.this);
            }
            catch (Exception e) {
                e.printStackTrace();
                return "";
            }     
        }

        private String stringify()
            throws BioException, IllegalSymbolException
        {
            if (symToke == null) symToke = alfa.getTokenization("token");

            StringBuffer s = new StringBuffer();

            for (int i=0; i < patternList.size(); i++) {
                Object currElem = patternList.get(i);
                if (currElem instanceof Symbol) {
                    //System.out.println("symbol is " + ((Symbol) currElem).getName());
                    s.append(symToke.tokenizeSymbol((Symbol) currElem));
                }
                else if (currElem instanceof Pattern) {
                    s.append(toString((Pattern) currElem));
                }
            }

            return s.toString();
        }

        private String toString(Pattern p)
            throws BioException, IllegalSymbolException
        {
            StringBuffer s = new StringBuffer();

            boolean hasCount = (p.getMin() != 1) || (p.getMax() != 1);
            boolean needParen = hasCount || (p.getPatternList().size() > 1);
            if (needParen) s.append('(');
            s.append(p.stringify());
            if (needParen) s.append(')');
            if (hasCount) {
                s.append('{');
                s.append(Integer.toString(p.getMin()));
                s.append(',');     
                s.append(Integer.toString(p.getMax()));
                s.append('}');
            }

            return s.toString();
        }
    }

    /**
     * Attempts to match a pattern starting at specified position in SymbolList.
     * @param p Pattern to match.
     * @param sl SymbolList to find match in.
     * @param pos position to look for match at.
     */
    public static boolean match(Pattern p, SymbolList sl, int pos)
    {
        MatchState state = new MatchState();
        state.symListPos = pos;

        return match(p, sl, state);
    }

    private static boolean match(Pattern p, SymbolList sl, MatchState state)
    {
        //System.out.println("match called with " + p.getLabel() + " " + state.patternPos + " " + state.symListPos);
        // record own state
        MatchState myState = (MatchState) state.clone();
        // we have switched pattern so we need to rewind the pattern pointer.
        myState.resetPattern();

        // do the required number of prematches (min -1)
        for (int i = 1; i < p.getMin(); i++) {
            //System.out.println("aligning required iteration " + i + " of pattern.");
            if (!matchOnce(p, sl, myState))
                return false;

            // go back to start of pattern again
            myState.resetPattern();
        }

        // we now attempt to find a single case of a successful
        // match for each legal number of repeats.
        for (int i = p.getMin(); i <= p.getMax(); i++) {
            //System.out.print("aligning iteration " + i + " of pattern: ");
            if (matchOnce(p, sl, myState)) {
                //System.out.println("found match!");
                // extend matched sequence to all we have matched.
                state.symListPos = myState.symListPos;
                // state.patternPos was already incremented prior
                // to calling this method.
                return true;
            }
            //System.out.println("failed!");
        }  

        return false;
    }

    /**
     * matches the pattern once to the SymbolList starting at the
     * positions specified within the MatchState object for SymbolList
     * and Pattern.
     */
    private static boolean matchOnce(Pattern p, SymbolList sl, MatchState state)
    {
        //System.out.println("matchOnce called with " + p.getLabel() + " " + state.patternPos + " " + state.symListPos);
        // record own state
        MatchState myState = (MatchState) state.clone();

        // match all symbols till the first Pattern.
        // ambigs on sl result in immediate failure.
        List pList = p.getPatternList();

        Object currMatchElement = null; // only to suppress compilation error.
        boolean matchNotFinished;
        while ((matchNotFinished = (myState.patternPos < pList.size()))
            && !((currMatchElement = pList.get(myState.patternPos)) instanceof Pattern)
            ) {
            //System.out.println("in symbol matching loop.");
            // no more symbols for matching, pattern fails!
            if (myState.symListPos > sl.length())
                return false;

            // matching symbols
            //System.out.println("matching " + ((Symbol) currMatchElement).getName() + " " + sl.symbolAt(myState.symListPos).getName());
            if (!matchSymbols((Symbol) currMatchElement, sl.symbolAt(myState.symListPos)))
                return false;

            myState.symListPos++;
            myState.patternPos++;
        }
        ////System.out.println("left initial symbols loop. " + myState.symListPos + " " + myState.patternPos);
        // all available symbols matched.  Success!
        if (!matchNotFinished) {
            ////System.out.println("no further matching to be done.");
            state.patternPos = myState.patternPos;
            state.symListPos = myState.symListPos;
            return true;
        }

        // finished all Symbol matches, do we have a Pattern to match?
        if (currMatchElement instanceof Pattern) {
            if (!matchExtend(p, sl, myState))
                return false;
        }

        // update the MatchState accordingly
        state.patternPos = myState.patternPos;
        state.symListPos = myState.symListPos;
        return true;
    }

    /**
     * extends a match that begins with a Pattern.
     */
    private static boolean matchExtend(Pattern p, SymbolList sl, MatchState state)
    {
        //System.out.println("matchExtend called with " + p.getLabel() + " " + state.patternPos + " " + state.symListPos);
        // save the state
        MatchState globalState = (MatchState) state.clone();
        MatchState patternState = (MatchState) state.clone();
        patternState.resetPattern();

        // extend the match beginning with the initial Pattern
        // do the required number of prematches (min -1)
        Pattern thisP = (Pattern) p.patternList.get(state.patternPos);

        for (int i = 1; i < thisP.getMin(); i++) {
            ////System.out.println("matchExtend: prematch iteration " + i + " of pattern.");            
            if (!matchOnce(thisP, sl, patternState))
                return false;

            // go back to start of pattern again
            patternState.resetPattern();
        }        

        // do required terminal matches
        for (int i = thisP.getMin(); i <= thisP.getMin(); i++) {
            ////System.out.println("matchExtend: match iteration " + i + " of pattern.");
            if (!matchOnce(thisP, sl, patternState))
                return false;

            // we have now got a required match for initial pattern done.
            // if the subsequent extension fails, I may need to add
            // another match for initial pattern.

            // advance global match past initial pattern.
            MatchState tryState = (MatchState) globalState.clone();
            tryState.patternPos++;
            tryState.symListPos = patternState.symListPos;
            ////System.out.println("extending match. " + globalState.patternPos + " " + globalState.symListPos);
            if (matchOnce(p, sl, tryState)) {
                // successful match of rest of pattern!
                state.symListPos = tryState.symListPos;
                return true;
            }
            
            // go back to start of pattern again
            patternState.resetPattern();
        }

        return false;
    }

    private static boolean matchSymbols(Symbol source, Symbol target)
    {
        // an ambiguous target symbol leads to an immediate mismatch
        if (!(target instanceof AtomicSymbol))
            return false;

        // is the source ambiguous?
        if (target instanceof AtomicSymbol) {
            return (source == target);
        }
        else {
            return source.getMatches().contains(target);
        }

    }
}

