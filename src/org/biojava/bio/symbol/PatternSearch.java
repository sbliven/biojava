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
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.BioException;
import org.biojava.utils.ChangeVetoException;
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

    private static class StatePool
    {
        private List pool = new ArrayList();

        private MatchState allocate()
        {
            int currSize;
            if ((currSize = pool.size()) == 0) {
                return new MatchState();
            }

            MatchState state = (MatchState) pool.remove(currSize -1);
            state.patternPos = 0;
            state.symListPos = 1;
            return state;
        }

        private MatchState clone(MatchState state)
        {
            MatchState newState = allocate();
            newState.patternPos = state.patternPos;
            newState.symListPos = state.symListPos;

            return newState;
        }

        private void release(MatchState state)
        {
            pool.add(state);
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
        StatePool pool = new StatePool();
        MatchState state = pool.allocate();
        state.symListPos = pos;

        return match(pool, p, sl, state);
    }

    /**
     * Annotates the sequence with Features marking where matches to
     * the specified sequence were encountered.
     * @param p Pattern to match.
     * @param seq Sequence to find match in.
     * @param ft Feature.Template to be used in creating the Feature.
     */
    public static boolean match(Pattern p, Sequence seq, Feature.Template ft)
        throws BioException, IllegalAlphabetException, ChangeVetoException
    {
        return match(p, seq, ft, new RangeLocation(1, seq.length()));
    }

    /**
     * Annotates the sequence with Features marking where matches to
     * the specified sequence were encountered.
     * @param p Pattern to match.
     * @param seq Sequence to find match in.
     * @param ft Feature.Template to be used in creating the Feature.
     * @param range The part of the sequence in which the search is to be conducted.
     *              Note that patterns that start within the range but extend
     *              beyond it will be discarded.
     */
    public static boolean match(Pattern p, Sequence seq, Feature.Template ft, RangeLocation range)
        throws BioException, IllegalAlphabetException, ChangeVetoException, IndexOutOfBoundsException
    {
        boolean hasHit = false;

        // check that the alphabets are compatible
        if (p.getAlphabet() != seq.getAlphabet())
            throw new IllegalAlphabetException("alphabets are not compatible.");

        // check range
        if (!LocationTools.contains(new RangeLocation(1, seq.length()), range))
            throw new IndexOutOfBoundsException(range + " is not valid for this sequence.");

        // mark out all hits with a Location object
        StatePool pool = new StatePool();
        MatchState state = pool.allocate();
        for (int i = range.getMin(); i <= range.getMax(); i++) {
            state.symListPos = i;
            state.resetPattern();
            if (match(pool, p, seq, state)) {
                // add an annotation to mark the site
                Location loc = new RangeLocation(i, state.symListPos - 1);

                // confirm that it is in the interval before adding this location.
                if (LocationTools.contains(range, loc)) {
                    ft.location = loc;
                    seq.createFeature(ft);
                    hasHit = true;
                }
            }
        }

        return hasHit;
    }

    /**
     * Compute the extent from matching the Pattern to the specified position.
     * @param p Pattern to match.
     * @param sl SymbolList to find match in.
     * @param pos position to look for match at.
     * @return RangeLocation if a match was achieved, null if not.
     */
    public static RangeLocation getMatchRange(Pattern p, SymbolList sl, int pos)
    {
        StatePool pool = new StatePool();
        MatchState state = pool.allocate();
        state.symListPos = pos;

        if (match(pool, p, sl, state)) {
            return new RangeLocation(pos, state.symListPos - 1);
        }
        else
            return null;
    }

    private static boolean match(StatePool pool, Pattern p, SymbolList sl, MatchState state)
    {
        //System.out.println("match called with " + p.getLabel() + " " + state.patternPos + " " + state.symListPos);
        // record own state
        MatchState myState = pool.clone(state);

        // we have switched pattern so we need to rewind the pattern pointer.
        myState.resetPattern();

        // do the required number of prematches (min -1)
        for (int i = 1; i < p.getMin(); i++) {
            //System.out.println("aligning required iteration " + i + " of pattern.");
            if (!matchOnce(pool, p, sl, myState)) {
                pool.release(myState);
                return false;
            }

            // go back to start of pattern again
            myState.resetPattern();
        }

        // we now attempt to find a single case of a successful
        // match for each legal number of repeats.
        for (int i = p.getMin(); i <= p.getMax(); i++) {
            //System.out.print("aligning iteration " + i + " of pattern: ");
            if (matchOnce(pool, p, sl, myState)) {
                //System.out.println("found match!");
                // extend matched sequence to all we have matched.
                state.symListPos = myState.symListPos;
                // state.patternPos was already incremented prior
                // to calling this method.
                pool.release(myState);
                return true;
            }
            //System.out.println("failed!");
        }  

        pool.release(myState);
        return false;
    }

    /**
     * matches the pattern once to the SymbolList starting at the
     * positions specified within the MatchState object for SymbolList
     * and Pattern.
     */
    private static boolean matchOnce(StatePool pool, Pattern p, SymbolList sl, MatchState state)
    {
        //System.out.println("matchOnce called with " + p.getLabel() + " " + state.patternPos + " " + state.symListPos);
        // record own state
        int symListPos = state.symListPos;
        int patternPos = state.patternPos;
//        MatchState myState = (MatchState) state.clone();

        // match all symbols till the first Pattern.
        // ambigs on sl result in immediate failure.
        List pList = p.getPatternList();

        Object currMatchElement = null; // only to suppress compilation error.
        boolean matchNotFinished;
        final int pListSize = pList.size();
        final int slLength = sl.length();
        while ((matchNotFinished = (patternPos < pListSize))
            && !((currMatchElement = pList.get(patternPos)) instanceof Pattern)
            ) {
            //System.out.println("in symbol matching loop.");
            // no more symbols for matching, pattern fails!
            if (symListPos > slLength)
                return false;

            // matching symbols
            //System.out.println("matching " + ((Symbol) currMatchElement).getName() + " " + sl.symbolAt(myState.symListPos).getName());
            if (!matchSymbols((Symbol) currMatchElement, sl.symbolAt(symListPos)))
                return false;

            symListPos++;
            patternPos++;
        }
        //System.out.println("left initial symbols loop. " + myState.symListPos + " " + myState.patternPos);
        // all available symbols matched.  Success!
        if (!matchNotFinished) {
            ////System.out.println("no further matching to be done.");
            state.patternPos = patternPos;
            state.symListPos = symListPos;
            return true;
        }

        // finished all Symbol matches, do we have a Pattern to match?
        if (currMatchElement instanceof Pattern) {
            MatchState myState = pool.allocate();
            myState.patternPos = patternPos;
            myState.symListPos = symListPos;

            if (!matchExtend(pool, p, sl, myState)) {
                pool.release(myState);
                return false;
            }

            // update the MatchState accordingly
            state.patternPos = myState.patternPos;
            state.symListPos = myState.symListPos;
            pool.release(myState);
            return true;
        }
        else {
            state.patternPos = patternPos;
            state.symListPos = symListPos;
            return true;
        }
    }

    /**
     * extends a match that begins with a Pattern.
     */
    private static boolean matchExtend(StatePool pool, Pattern p, SymbolList sl, MatchState state)
    {
        //System.out.println("matchExtend called with " + p.getLabel() + " " + state.patternPos + " " + state.symListPos);
        // save the state
        MatchState globalState = pool.clone(state);
        MatchState patternState = pool.clone(state);
        patternState.resetPattern();

        // extend the match beginning with the initial Pattern
        // do the required number of prematches (min -1)
        Pattern thisP = (Pattern) p.getPatternList().get(state.patternPos);

        for (int i = 1; i < thisP.getMin(); i++) {
            ////System.out.println("matchExtend: prematch iteration " + i + " of pattern.");            
            if (!matchOnce(pool, thisP, sl, patternState)) {
                pool.release(globalState);
                pool.release(patternState);
                return false;
            }

            // go back to start of pattern again
            patternState.resetPattern();
        }        

        // do required terminal matches
        for (int i = thisP.getMin(); i <= thisP.getMin(); i++) {
            ////System.out.println("matchExtend: match iteration " + i + " of pattern.");
            if (!matchOnce(pool, thisP, sl, patternState)) {
                pool.release(globalState);
                pool.release(patternState);
                return false;
            }

            // we have now got a required match for initial pattern done.
            // if the subsequent extension fails, I may need to add
            // another match for initial pattern.

            // advance global match past initial pattern.
            MatchState tryState = pool.clone(globalState);
            tryState.patternPos++;
            tryState.symListPos = patternState.symListPos;
            ////System.out.println("extending match. " + globalState.patternPos + " " + globalState.symListPos);
            if (matchOnce(pool, p, sl, tryState)) {
                // successful match of rest of pattern!
                state.symListPos = tryState.symListPos;
                pool.release(globalState);
                pool.release(patternState);
                pool.release(tryState);
                return true;
            }
            
            // go back to start of pattern again
            patternState.resetPattern();
            pool.release(tryState);
        }

        pool.release(globalState);
        pool.release(patternState);
        return false;
    }

    private static boolean matchSymbols(Symbol source, Symbol target)
    {
        // an ambiguous target symbol leads to an immediate mismatch
        if (!(target instanceof AtomicSymbol))
            return false;

        // is the source ambiguous?
        if (source instanceof AtomicSymbol) {
            return (source == target);
        }
        else {
            return source.getMatches().contains(target);
        }

    }
}

