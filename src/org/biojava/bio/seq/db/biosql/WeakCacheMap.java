/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */

package org.biojava.bio.seq.db.biosql;

import org.biojava.utils.cache.*;

import java.util.*;
import java.lang.ref.*;

/**
 * A cache which retains weak references to objects
 *
 * @since 1.3
 */

public class WeakCacheMap implements CacheMap {
  private Map map = new HashMap();
  
  public void put(Object key, Object value) {
      map.put(key, new WeakReference(value));
  }
  
  public Object get(Object key) {
      Reference ref = (Reference) map.get(key);
      if (ref != null) {
	  return ref.get();
      } else {
	  return null;
      }
  }
  
  public void remove(Object key) {
    map.remove(key);
  }
}
