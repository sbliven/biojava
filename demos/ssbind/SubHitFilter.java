package ssbind;

import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.search.*;

/**
 * Abstract base class for things that will allow or disallow whole sub hit
 * messages through to the next handler.
 *
 * @author Matthew Pocock
 */
public abstract class SubHitFilter
extends SearchContentFilter {
  private List properties = new ArrayList();
  private boolean acceptEntry;
  
  public SubHitFilter(SearchContentHandler delegate) {
    super(delegate);
  }
  
  public void addSubHitProperty(Object key, Object value) {
    acceptEntry &= accept(key, value);
    properties.add(new Object[] {key, value});
  }
  
  public void startSubHit() {
    acceptEntry = true;
  }
  
  public void endSubHit() {
    if(acceptEntry == true) {
      super.startSubHit();

      for(Iterator i = properties.iterator(); i.hasNext(); ) {
        Object[] kv = (Object[]) i.next();
        super.addSubHitProperty(kv[0], kv[1]);
      }

      super.endSubHit();
    }
    
    properties.clear();
  }
  
  protected abstract boolean accept(Object key, Object val);
}
