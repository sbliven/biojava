package org.biojava.bio.program.tagvalue;

import org.biojava.utils.ParserException;

/**
 * <p>
 * Tokenize single records (lines of text, objects) into a tag and a value.
 * </p>
 *
 * <p>
 * TagValueParser instances may be statefull, that is they may remember
 * previous values of tags or values, and return different TagValue responses
 * accordingly.
 * </p>
 *
 * @author Matthew Pocock
 * @since 1.2
 */
public interface TagValueParser {
  public TagValue parse(Object record)
  throws ParserException;
}
