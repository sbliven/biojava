package org.biojava.bio.program.tagvalue;

import java.util.Set;
import org.biojava.utils.SmallSet;
import org.biojava.utils.ParserException;

/**
 * Silently drop all tags except those specified, and pass the rest onto a
 * delegate.
 *
 * @author Matthew Pocock
 * @since 1.2
 */
public class TagDropper
extends TagValueWrapper {
  private Set tags;
  boolean propagate;
  
  /**
   * Create a new TagDropper that will pass on all retained tags and values to
   * tvl. Initially, no tags will be retained.
   *
   * @param tvl  the TagValueListener to inform of all surviving events
   */
  public TagDropper(TagValueListener tvl) {
    super(tvl);
    this.tags = new SmallSet();
  }
  
  /**
   * Add a tag to retain.
   *
   * @param tag  a tag that will be forwarded to the delegate
   */
  public void addTag(Object tag) {
    tags.add(tag);
  }
  
  /**
   * Remove a tag so that it will not be retained.
   *
   * @param tag  a tag that will not be forwarded to the delegate
   */
  public void removeTag(Object tag) {
    tags.remove(tag);
  }
  
  public void startTag(Object tag)
  throws ParserException {
    propagate = tags.contains(tag);
    
    if(propagate) {
      super.startTag(tag);
    }
  }
  
  public void endTag()
  throws ParserException {
    if(propagate) {
      super.endTag();
    }
  }
  
  public void value(TagValueContext ctxt, Object value)
  throws ParserException {
    if(propagate) {
      super.value(ctxt, value);
    }
  }
}

