package org.biojava.bio.program.tagvalue;

/**
 * Communication interface between Parser and a TagValueListener that allows
 * listeners to request that a parser/listener pair be pushed onto the stack to
 * handle the current tag.
 *
 * @author Mathew Pocock
 * @since 1.2
 */
public interface TagValueContext {
  /**
   * <p>
   * Push a parser and listener pair onto the parser stack.
   * </p>
   *
   * <p>
   * This will result in the parser using subParser to process all values of the
   * current tag.
   * </p>
   */
  void pushParser(TagValueParser subParser, TagValueListener subListener);
}
