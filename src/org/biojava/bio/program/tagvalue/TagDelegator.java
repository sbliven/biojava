package org.biojava.bio.program.tagvalue;

import java.util.Map;

import org.biojava.utils.ParserException;
import org.biojava.utils.SmallMap;

/**
 * <p>
 * Pushes a new parser and listener depending on the tag.
 * </p>
 *
 * <p>
 * setParserListener() is used to associate a tag with a TagValueParser and
 * TagValueListener. When this tag is encountered, the pair will be pushed onto
 * the parser processing stack and will gain control of the stream untill that
 * tag has ended.
 * The delegator is constructed with a default TagValueListener that will be
 * informed of all events for which there are no explicit delegate pairs
 * registered.
 * </p>
 *
 * @author Matthew Pocock
 * @since 1.2
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
