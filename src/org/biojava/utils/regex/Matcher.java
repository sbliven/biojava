

package org.biojava.utils.regex;

import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.seq.io.SymbolListCharSequence;
import org.biojava.utils.regex.Pattern;

public class Matcher
{
    private org.biojava.utils.regex.Pattern pattern;
    private java.util.regex.Matcher matcher;
    private SymbolList sl;

    Matcher(org.biojava.utils.regex.Pattern pattern, SymbolList sl)
    {
        this.pattern = pattern;
        this.sl = sl;

        matcher = pattern.getPattern().matcher(new SymbolListCharSequence(sl));
    }

    public int end() { return matcher.end() + 1; }
    public int end(int group) throws IndexOutOfBoundsException { return matcher.end(group); }
    public boolean find() { return matcher.find(); }
    public boolean find(int start) throws IndexOutOfBoundsException { return matcher.find(start - 1); }
    public SymbolList group()
    {
        return sl.subList(start(), end() - 1);
    }

    public SymbolList group(int group)
        throws IndexOutOfBoundsException
    {
        return sl.subList(start(group), end(group) - 1);
    }

    public int groupCount() { return matcher.groupCount(); }
    public boolean lookingAt() { return matcher.lookingAt(); }
    public boolean matches() { return matcher.matches(); }
    public org.biojava.utils.regex.Pattern pattern()
    {
        return pattern;
    }

    public org.biojava.utils.regex.Matcher reset()
    {
        matcher = matcher.reset();
        return this;
    }

    public org.biojava.utils.regex.Matcher reset(SymbolList sl)
    {
        this.sl = sl;
        matcher = matcher.reset(new SymbolListCharSequence(sl));
        return this;
    }

    public int start() { return matcher.start() + 1; }
    public int start(int group) {return matcher.start(group) + 1; }

}

