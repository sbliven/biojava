package org.biojava.bio.program.tagvalue;

import org.biojava.utils.ParserException;

/**
 * <p>
 * Tokenize single records (lines of text, objects) into a tag and a value.
 * </p>
 *
 * <p>
 * TagValueParser instances may be stateful, that is they may remember
 * previous values of tags or values, and return different TagValue responses
 * accordingly.
 * </p>
 *
 * @author Matthew Pocock
 * @since 1.2
 */
public interface TagValueParser {

    /**
     * <p><code>EMPTY_LINE_EOR</code> is a special value which allows
     * an empty line to be used as a record separator. Normally this
     * is not possible as the empty line will be swallowed by the
     * preceding tag or value. Use this as an argument to the
     * <code>setEndOfRecord</code> method.</p>
     *
     * <p>An empty line is defined as a line which contains nothing
     * between the start and the following system-defined line
     * separator. Therefore lines which contain only whitespace are
     * not considererd to be empty.</p>
     */
    public static final String EMPTY_LINE_EOR = "";

    public TagValue parse(Object record)
        throws ParserException;
}
