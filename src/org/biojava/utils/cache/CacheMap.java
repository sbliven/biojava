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

/**
 * Interface for managing caches of objects fetchable by key.
 * <P>
 * The map may chose to remove a mapping, for example to free memory, or if the
 * data has become too old to be usefull.
 *
 * @author Matthew Pocock
 * @since 1.1
 */

public interface CacheMap {
  /**
   * Associate a value with a key. The association may be broken at any time.
   *
   * @param key the key Object
   * @param value the Object to associate with the key
   */
  public void put(Object key, Object value);
  
  /**
   * Retrieve the Object associated with the key, or null if either no value has
   * been associated or if the key's value has been cleared by the cache.
   *
   * @param key the key Object
   * @return the Object currently associated with the key
   */
  public Object get(Object key);
  
  public void remove(Object o);
}
