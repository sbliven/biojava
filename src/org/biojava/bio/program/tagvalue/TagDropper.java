package org.biojava.bio.program.tagvalue;

import java.util.Set;
import org.biojava.utils.SmallSet;
import org.biojava.utils.ParserException;

public class TagDropper
extends TagValueWrapper {
  private Set tags;
  boolean propogate;
  
  public TagDropper(TagValueListener tvl) {
    super(tvl);
    this.tags = new SmallSet();
  }
  
  public void addTag(Object tag) {
    tags.add(tag);
  }
  
  public void removeTag(Object tag) {
    tags.remove(tag);
  }
  
  public void statTag(Object tag)
  throws ParserException {
    propogate = tags.contains(tag);
    
    if(propogate) {
      super.startTag(tag);
    }
  }
  
  public void endTag()
  throws ParserException {
    if(propogate) {
      super.endTag();
    }
  }
  
  public void value(TagValueContext ctxt, Object value)
  throws ParserException {
    if(propogate) {
      super.value(ctxt, value);
    }
  }
}

