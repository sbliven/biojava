package org.biojava.bio.symbol;

import org.biojava.bio.Annotation;

import java.util.Collections;

/**
 * @author Matthew Pocock
 */
public class SimpleAtomicSymbolEventTest
        extends AbstractSymbolEventTest
{
  protected Symbol createSymbol(Annotation ann)
          throws Exception
  {
    return new SimpleAtomicSymbol(Annotation.EMPTY_ANNOTATION, Collections.EMPTY_LIST);
  }
}
