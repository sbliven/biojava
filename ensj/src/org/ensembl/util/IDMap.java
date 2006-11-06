/*
	Copyright (C) 2003 EBI, GRL

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package org.ensembl.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.ensembl.datamodel.Persistent;

/**
 * A fast hash map providing internalID -> Persistent object mapping.
 * 
 * This class is not thread safe.
 */
public class IDMap extends HashMap {


	private static final long serialVersionUID = 1L;


	/**
   * Adds all the 
   * persistent objects returned by the iterator
   * using their internalIDs
   * as the keys.
   * @param iter iterator over Persistent objects to be stored.
   * @throws ClassCastException if anything other
   * Persistent objects are in the list.
   */
  public IDMap(Iterator iter) {
    putAll(iter);
  }

  /**
   * Adds all the 
   * persistent objects in the list using their internalIDs
   * as the keys. 
   * 
   * Implementation uses values.get(i) to access
   * the list items. If it is more efficient to to access
   * the list using an iterator consider using
   * IDMap(Iterator) instead.
   * @param values list of Persistent objects to be stored.
   * @throws ClassCastException if anything other
   * Persistent objects are in the list.
   * @see #IDMap(Iterator)
   */
  public IDMap(List values) {
    putAll(values);
  }

  /**
   * Creates a map conainting all the persistent objects 
   * keyed on their internalIDs. 
   * 
   * @param values Persistent objects to be stored.
   * @throws ClassCastException if anything other
   * Persistent objects are in the list.
   */
  public IDMap(Persistent[] values) {
    putAll(values);
  }
  
  public IDMap() {
    super();
  }

  /**
   * Retrieves the item using get(new Long(key)).
   * @param key key for value in map.
   * @return value if present, otherwise null.
   */
  public Object get(long key) {
    return get(new Long(key));
  }

  /**
   * Stores the object using new Long(key) as
   * it's key.
   * @param key key for value in map.
   * @param value value to be stored.
   */
  public void put(long key, Object value) {
    put(new Long(key), value);
  }

  /**
   * Stores the 
   * persistent object using it's internalID
   * as the key.
   * @param value value to be stored.
   * @return true, value is always added, replaces pervious item with same internalID if
   * necessary.
   */
  public boolean put(Persistent value) {
    put(new Long(value.getInternalID()), value);
    return true;
  }

  /**
   * Stores the 
   * persistent objects using their internalIDs
   * as the keys.
   * @param iter iterator over Persistent objects to be stored.
   * @throws ClassCastException if anything other
   * Persistent objects are in the list.
   */
  public void putAll(Iterator iter) {
    while (iter.hasNext())
      put((Persistent) iter.next());
  }
  
  
  /**
   * Adds all the 
   * persistent objects using their internalIDs
   * as the keys. Uses values.get(i) to access
   * the list items.
   * @param values list of Persistent objects to be stored.
   * @throws ClassCastException if anything other
   * Persistent objects are in the list.
   */
  public void putAll(List values) {
    final int n = values.size();
    for (int i = 0; i < n; i++) {
      put((Persistent) values.get(i));
    }
  }

  /**
   * Adds all the persistent objects using their internalIDs
   * as the keys. 
   * @param values objects to be stored.
   * @throws ClassCastException if anything other
   * Persistent objects are in the list.
   */
  public void putAll(Persistent[] values) {
    final int n = values.length;
    for (int i = 0; i < n; i++) 
      put(values[i]);
    
  }
}
