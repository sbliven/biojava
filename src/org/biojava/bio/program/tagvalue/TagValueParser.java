package org.biojava.bio.program.tagvalue;

import java.io.BufferedReader;
import java.io.IOException;

import org.biojava.utils.ParserException;

/**
 * Format processor for the family of files
 */
public interface TagValueParser {
  public boolean read(BufferedReader br, TagValueListener tvListener)
    throws
      ParserException,
      IOException;
}
