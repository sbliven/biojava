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
     * <code>BLANK_LINE_EOR</code> is a special value which allows an
     * empty line to be used as a record separator. Normally this is
     * not possible as the empty line will be swallowed by the
     * preceding tag or value. Use this as an argument to the
     * <code>setEndOfRecord</code> method.
     */
    public static final String BLANK_LINE_EOR = "";

    public TagValue parse(Object record)
        throws ParserException;
}
