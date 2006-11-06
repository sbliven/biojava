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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.ensembl.datamodel.Persistent;

/**
 * Manages a set of persistent objects.
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public class PersistentSet implements Collection, Serializable {

	/**
   * 
   */
  private static final long serialVersionUID = 1L;
  private IDMap map = new IDMap();

	/**
	 * 
	 */
	public PersistentSet() {
		super();
	}

	
	public boolean add(Persistent persistent) {
		return map.put(persistent);
	}
	
	public List toList() {
		return new ArrayList(map.values());
	}


  /**
   * @see java.util.Collection#size()
   */
  public int size() {
    return map.size();
  }


  /**
   * @see java.util.Collection#clear()
   */
  public void clear() {
    map.clear();    
  }


  /**
   * @see java.util.Collection#isEmpty()
   */
  public boolean isEmpty() {
    return map.isEmpty();
  }


  /**
   * @see java.util.Collection#toArray()
   */
  public Object[] toArray() {
    return map.values().toArray(new Object[map.values().size()]);
  }


  /**
   * @see java.util.Collection#add(java.lang.Object)
   */
  public boolean add(Object o) {
    return add((Persistent) o);
  }


  /**
   * @see java.util.Collection#contains(java.lang.Object)
   */
  public boolean contains(Object o) {
    return map.containsValue(o);
  }


  /**
   * @see java.util.Collection#remove(java.lang.Object)
   */
  public boolean remove(Object o) {
    return map.remove(o)!=null;
  }


  /**
   * @see java.util.Collection#addAll(java.util.Collection)
   */
  public boolean addAll(Collection c) {
    for (Iterator iter = c.iterator(); iter.hasNext();) 
      add(iter.next());
    return true;
  }


  /**
   * @see java.util.Collection#containsAll(java.util.Collection)
   */
  public boolean containsAll(Collection c) {
    for (Iterator iter = c.iterator(); iter.hasNext();) 
      if (!contains(iter.next())) return false;
    return true;
  }


  /**
   * @see java.util.Collection#removeAll(java.util.Collection)
   */
  public boolean removeAll(Collection c) {
    return map.values().removeAll(c);
  }


  /**
   * @see java.util.Collection#retainAll(java.util.Collection)
   */
  public boolean retainAll(Collection c) {
    return map.values().retainAll(c);
  }


  /**
   * @see java.util.Collection#iterator()
   */
  public Iterator iterator() {
    return map.values().iterator();
  }


  /**
   * @see java.util.Collection#toArray(java.lang.Object[])
   */
  public Object[] toArray(Object[] a) {
    return map.values().toArray(a);
  }
	
  /**
   * @return list of zero or more comma separated values.
   */
  public String toString() {
    return "["+StringUtil.toString(map.values())+"]";
  }
}
