package org.biojava.bio.program.tagvalue;

import java.util.Map;

import org.biojava.utils.ParserException;
import org.biojava.utils.SmallMap;

/**
 * Pushes a new parser and listener depending on the tag.
 *
 * @author Matthew Pocock
 */
public class TagDelegator
  extends
    TagValueWrapper
{
  private Map parsers;
  private Map listeners;
  
  private TagValueParser parser;
  private TagValueListener listener;
  
  public TagDelegator(TagValueListener delegate) {
    super(delegate);
    parsers = new SmallMap();
    listeners = new SmallMap();
  }
  
  public void startTag(Object tag)
  throws ParserException {
    parser = (TagValueParser) parsers.get(tag);
    listener = (TagValueListener) listeners.get(tag);
    
    super.startTag(tag);
  }
  
  public void value(TagValueContext tvc, Object value)
  throws ParserException {
    if(parser != null) {
      tvc.pushParser(parser, listener);
    } else {
      super.value(tvc, value);
    }
  }
  
  public void setParserListener(
    Object tag,
    TagValueParser parser,
    TagValueListener listener
  ) {
    parsers.put(tag, parser);
    listeners.put(tag, listener);
  }
}
