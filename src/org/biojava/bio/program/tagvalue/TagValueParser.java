package org.biojava.bio.program.tagvalue;

import org.biojava.utils.ParserException;

/**
 * Format processor for the family of files
 */
public interface TagValueParser {
  public TagValue parse(Object record)
  throws ParserException;
}
