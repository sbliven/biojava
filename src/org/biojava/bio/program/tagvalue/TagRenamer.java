package org.biojava.bio.program.tagvalue;

import java.util.Map;

import org.biojava.utils.ParserException;
import org.biojava.utils.SmallMap;

public class TagRenamer extends TagValueWrapper {
  private Map tags;
  
  public TagRenamer(TagValueListener delegate) {
    super(delegate);
    this.tags = new SmallMap();
  }
  
  public void setNewTag(Object oldTag, Object newTag) {
    tags.put(oldTag, newTag);
  }
  
  public Object getNewTag(Object oldTag) {
    return tags.get(oldTag);
  }
  
  public void tagValue(Object tag, Object value)
  throws ParserException {
    Object newTag = getNewTag(tag);
    if(newTag != null) {
      tag = newTag;
    }
    
    super.tagValue(tag, value);
  }
}

