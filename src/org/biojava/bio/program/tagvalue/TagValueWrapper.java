package org.biojava.bio.program.tagvalue;

import org.biojava.utils.ParserException;

/**
 * <p>
 * Helper class to wrap one TagValueListener inside another one.
 * </p>
 *
 * <p>
 * Implementations will tend to intercept the tags or values as they stream
 * through and modify them in some manner before forwarding them to the delegate
 * listener. Using classes derived from TagValueWrapper, it is possible to build
 * up complex chains of handlers that process and cohalate information as it
 * strams through.
 * </p>
 *
 * @author Matthew Pocock
 * @since 1.2
 */
public abstract class TagValueWrapper
  implements
    TagValueListener
{
  private TagValueListener delegate;
  
  /**
   * Build a TagValueWrapper that will forward everything to a delegate.
   *
   * @param delegate the TagValueWrapper to forward events to
   */
  public TagValueWrapper(TagValueListener delegate) {
    this.delegate = delegate;
  }
  
  public TagValueListener getDelegate() {
    return delegate;
  }
  
  public void startRecord()
  throws ParserException {
    delegate.startRecord();
  }
  
  public void endRecord()
  throws ParserException {
    delegate.endRecord();
  }
  
  public void startTag(Object tag)
  throws ParserException {
    delegate.startTag(tag);
  }
  
  public void endTag()
  throws ParserException {
    delegate.endTag();
  }
  
  public void value(TagValueContext ctxt, Object value)
  throws ParserException {
    delegate.value(ctxt, value);
  }
}

