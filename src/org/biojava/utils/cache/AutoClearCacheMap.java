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

package org.biojava.utils.cache;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Iterator;

/**
 * A Map that maintains weak references to objects.
 * It also periodically clears expired references
 * to prevent them cluttering up the Map.
 *
 * @since 1.3
 * @author David Huen
 */

public class AutoClearCacheMap implements CacheMap {
  private Map map = new HashMap();
  private Timer janitor;

  private int clearInterval;
  private int maxKeysToClear;

  private class Janitor extends TimerTask
  {
      public void run()
      {
          // determine number of keys to clear
          clearKeys();
      }
  }

  /**
   * @param clearInterval frequency with which the key clearance is to be done.
   * @param maxKeysToClear maximum number of keys to clear with each attempt.
   */
  public AutoClearCacheMap(int clearInterval, int maxKeysToClear)
  {
      this.map = new HashMap();
      this.maxKeysToClear = maxKeysToClear;

      map = new HashMap();
      janitor = new Timer(true);

      janitor.schedule(new Janitor(), 1000*clearInterval, 1000*clearInterval);
  }

  public synchronized void put(Object key, Object value) {
      map.put(key, new WeakReference(value));
  }
  
  public Object get(Object key) {
      return get(key, false);
  }

  private synchronized Object get(Object key, boolean clear)
  {
      Reference ref = (Reference) map.get(key);
      if (ref != null) {
          return ref.get();
      } else {
          if (clear) {
              // remove mapping when weak references are cleared
              map.remove(key);
          }
          return null;
      }
  }

  private synchronized void clearKeys()
  {
      int clearedKeys = 0;
      for (Iterator keysI = map.keySet().iterator();
          keysI.hasNext() && (clearedKeys <= maxKeysToClear);
          ) {
          // get key
          Object key = keysI.next();

          // keep count of keys removed
          if (get(key, true) == null) clearedKeys++;
      }
  }

  public void remove(Object key) {
    map.remove(key);
  }

  public void finalize() {
      if (janitor != null) janitor.cancel();
      janitor = null;
  }
}
