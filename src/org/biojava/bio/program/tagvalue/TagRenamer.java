package org.biojava.bio.program.tagvalue;

import java.util.Map;

import org.biojava.utils.ParserException;
import org.biojava.utils.SmallMap;

public class TagRenamer extends TagValueWrapper {
  private TagMapper mapper;
  
  public TagRenamer(TagValueListener delegate, TagMapper mapper) {
    super(delegate);
    this.mapper = mapper;
  }
  
  public TagMapper getMapper() {
    return mapper;
  }
  
  public void startTag(Object tag)
  throws ParserException {
    Object newTag = mapper.getNewTag(tag);
    if(newTag != null) {
      tag = newTag;
    }
    
    super.startTag(tag);
  }
}

