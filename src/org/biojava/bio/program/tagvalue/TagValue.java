package org.biojava.bio.program.tagvalue;

/**
 * <p>
 * Utility class for representing tag-value pairs for TagValueParser
 * implementors.
 * </p>
 *
 * @author Matthew Pocock
 * @since 1.2
 */
public class TagValue {
  private final Object tag;
  private final Object value;
  private final boolean newTag;
  
  /**
   * Build a new TagValue with a tag, a value and a flag indicating if this is a
   * new example of this tag or a continuation of an old example.
   *
   * @param tag  the tag Object
   * @param value the value Object
   * @param newTag true if startTag events should be thrown even if the tag is
   *        identical to the previously observed tag
   */
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
