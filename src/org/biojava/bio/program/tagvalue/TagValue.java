package org.biojava.bio.program.tagvalue;

public class TagValue {
  private final Object tag;
  private final Object value;
  private final boolean newTag;
  
  public TagValue(Object tag, Object value, boolean newTag) {
    this.tag = tag;
    this.value = value;
    this.newTag = newTag;
  }
  
  public Object getTag() {
    return this.tag;
  }
  
  public Object getValue() {
    return this.value;
  }
  
  public boolean isNewTag() {
    return newTag;
  }
}
