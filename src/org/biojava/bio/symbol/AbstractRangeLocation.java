package org.biojava.bio.symbol;

import java.util.*;
import java.io.*;
import java.lang.reflect.*;

import org.biojava.utils.*;
import org.biojava.bio.*;

public abstract class AbstractRangeLocation extends AbstractLocation {
  public Iterator blockIterator() {
    return Collections.singleton(this).iterator();
  }
  
  public boolean isContiguous() {
    return true;
  }
  
  public SymbolList symbols(SymbolList seq) {
    return seq.subList(getMin(), getMax());
  }
  
  public boolean contains(int p) {
    return p >= getMin() && p <= getMax();
  }
}
