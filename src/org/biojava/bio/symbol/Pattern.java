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
//import org.biojava.bio.seq.Sequence;
//import org.biojava.bio.seq.Feature;
import org.biojava.bio.BioException;
//import org.biojava.utils.ChangeVetoException;

/**
 * Class to describe a regex-like pattern.  The pattern
 * is a list of other patterns or Symbols in the target
 * FiniteAlphabet.  These are added to the list with
 * addSymbol and addPattern calls.  The pattern can be
 * reiterated a number of times (deafult is once).
 * <p>
 * It is also possible for the Pattern to contain variant
 * Patterns.  These variants are started with beginNextVariant().
 *
 * @author David Huen
 * @since 1.4
 */
public class Pattern
{
    private List [] patternList;
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
        patternList.add(new ArrayList());
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
     * Add a Pattern to the end of the current Pattern.
     */
    public void addPattern(Pattern pattern)
        throws IllegalAlphabetException
    {
        if (alfa != pattern.getAlphabet())
            throw new IllegalAlphabetException(pattern.getAlphabet().getName() + " is not compatible with Alphabet " + alfa.getName());

        patternList.add(pattern);
    }

    /**
     * Start a variant Pattern within this Pattern.
     */
    public void beginNextVariant()
    {

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

    /**
     * Set the label for this pattern.
     */
    public void setLabel(String label)
    {
        this.label = label;
    }

    List getPatternList()
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

