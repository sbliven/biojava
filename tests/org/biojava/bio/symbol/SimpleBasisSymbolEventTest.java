package org.biojava.bio.symbol;

import org.biojava.bio.Annotation;
import org.biojava.utils.ChangeListener;
import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeSupport;

import java.util.Collections;

/**
 * @author Matthew Pocock
 */
public class SimpleBasisSymbolEventTest
        extends AbstractSymbolEventTest
{
  protected Symbol createSymbol(Annotation ann)
          throws Exception
  {
    return new SimpleBasisSymbol(Annotation.EMPTY_ANNOTATION, Collections.EMPTY_LIST);
  }
}
