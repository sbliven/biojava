package org.biojava.bio.program.tagvalue;

import java.util.Map;

import org.biojava.utils.ParserException;
import org.biojava.utils.SmallMap;

/**
 * <p>
 * Rename tags using a TagMapper.
 * </p>
 *
 * <p> 
 * This will rename tags as they stream into this listener using a TagMapper.
 * Once renamed, the events will be forwarded onto a delegate TagValueListener
 * for further processing.
 * </p>
 *
 * @author Matthew Pocock
 * @since 1.2
 */
public class TagRenamer extends TagValueWrapper {
  private TagMapper mapper;
  
  /**
   * Build a new TagRenamer with a delegate and mapper.
   *
   * @param delegate TagValueListener to pass mapped events onto
   * @param mapper TagMapper used to rename tags
   */
  public TagRenamer(TagValueListener delegate, TagMapper mapper) {
    super(delegate);
    this.mapper = mapper;
  }
  
  /**
   * Retrieve the mapper used to rename tags
   *
   * @return the current mapper
   */
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

