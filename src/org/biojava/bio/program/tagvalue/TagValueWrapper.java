package org.biojava.bio.program.tagvalue;

import org.biojava.utils.ParserException;

public abstract class TagValueWrapper implements TagValueListener {
  private TagValueListener delegate;
  
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
  
  public void tagValue(Object tag, Object value)
  throws ParserException {
    delegate.tagValue(tag, value);
  }
}

