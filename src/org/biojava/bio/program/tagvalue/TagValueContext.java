package org.biojava.bio.program.tagvalue;

public interface TagValueContext {
  void pushParser(TagValueParser subParser, TagValueListener subListener);
}
