package org.biojava.bio.symbol;

import org.biojava.bio.Annotation;
import org.biojava.bio.seq.DNATools;
import org.biojava.utils.*;

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
    return new SimpleBasisSymbol(ann, new ListTools.Doublet(DNATools.c(), DNATools.n()));
  }
}
