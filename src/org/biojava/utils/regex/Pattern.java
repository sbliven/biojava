


package org.biojava.utils.regex;

import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.SymbolList;

/**
 * A class analogous to java.util.regex.Pattern but for SymbolLists.
 * @author David Huen
 * @since 1.4
 */
public class Pattern
{
    private FiniteAlphabet alfa;
    private java.util.regex.Pattern pattern;

    Pattern(java.util.regex.Pattern pattern, FiniteAlphabet alfa)
    {
        this.pattern = pattern;
        this.alfa = alfa;
    }

    /**
     * Creates a matcher that will match the given input against this pattern.
     * @param input SymbolList against which match is to be made.
     * @return A new matcher for this pattern.
     */
    public org.biojava.utils.regex.Matcher matcher(SymbolList sl)
    {
        return new org.biojava.utils.regex.Matcher(this, sl);
    }

    /**
     * returns the Pattern to be matched as a String.
     */
    //FIXME: do something about unicode strings and conversion back to something sensible.
    public String patternAsString()
    {
        return pattern.pattern();
    }

    /**
     * returns the java.util.regex.Pattern object that underlies this instance.
     */
    java.util.regex.Pattern getPattern() { return pattern; }

    public FiniteAlphabet getAlphabet() { return alfa; }
}

