package org.biojava.bio.program.tagvalue;

import java.util.Map;
import org.biojava.utils.SmallMap;

public class TagMapper {
  private Map tags;
  
  public TagMapper() {
    this.tags = new SmallMap();
  }
  
  public void setNewTag(Object oldTag, Object newTag) {
    tags.put(oldTag, newTag);
  }
  
  public Object getNewTag(Object oldTag) {
    return tags.get(oldTag);
  }
}

