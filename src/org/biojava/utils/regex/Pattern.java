


package org.biojava.utils.regex;

import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.SymbolList;

public class Pattern
{
    private FiniteAlphabet alfa;
    private java.util.regex.Pattern pattern;

    Pattern(java.util.regex.Pattern pattern, FiniteAlphabet alfa)
    {
        this.pattern = pattern;
        this.alfa = alfa;
    }

    public org.biojava.utils.regex.Matcher matcher(SymbolList sl)
    {
        return new org.biojava.utils.regex.Matcher(this, sl);
    }

    public String patternAsString()
    {
        return pattern.pattern();
    }

    /**
     * returns the java.util.regex.Pattern object that underlies this instance.
     * for package-private use only but made public because of need to use interface.
     */
    public java.util.regex.Pattern getPattern() { return pattern; }

    public FiniteAlphabet getAlphabet() { return alfa; }
}

