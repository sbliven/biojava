package org.biojava.bio.program.tagvalue;

import org.biojava.utils.ParserException;

public interface TagValueListener {
  public void startRecord()
  throws ParserException;
  
  public void endRecord()
  throws ParserException;
  
  public void tagValue(Object tag, Object value)
  throws ParserException;
}
