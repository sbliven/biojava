package org.biojava.bio.program.tagvalue;

import org.biojava.utils.ParserException;

public abstract class TagValueWrapper
  implements
    TagValueListener
{
  private TagValueListener delegate;
  
  public TagValueWrapper(TagValueListener delegate) {
    this.delegate = delegate;
  }
  
  public TagValueListener getDelegate() {
    return delegate;
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

