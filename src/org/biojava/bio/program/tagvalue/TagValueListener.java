package org.biojava.bio.program.tagvalue;

import org.biojava.utils.ParserException;

public interface TagValueListener {
  public void startRecord()
  throws ParserException;
  
  public void endRecord()
  throws ParserException;
  
  public void startTag(Object tag)
  throws ParserException;
  
  public void endTag()
  throws ParserException;
  
  public void value(TagValueContext ctxt, Object value)
  throws ParserException;
}
