/*
    Copyright (C) 2001 EBI, GRL

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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import org.ensembl.datamodel.Locatable;

/**
 * Container for locatables that offers a convenient
 * means of retrieving an array of the locatable's internalIDs
 * where the locatables are sorted by genomic location.
 * 
 * Note that the type of Object added to list via the add() methods
 * is not tested but they should be Locatable. The type is not
 * checked when added because the overhead of doing so could be prohibitive.
 * If a non Locatable item is added then an Exception will be thrown when
 * toSortedInternalIDArray() is called.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public class LocatableList extends LinkedList {


  private static final long serialVersionUID = 1L;
	private long[] result = null;

  
  public void add(int index, Object element) {
    result = null;
    super.add(index, element);
    
  }

  /**
   * Adds a locatable instance to the list.
   * @param o a Locatable instance.
   * @see Locatable 
   */
  public boolean add(Object o) {
    result = null;
    return super.add(o);
  }

  /**
   * Adds all the Locatables from the collection into this list.
   * @param c collection of Locatables.
   * @see Locatable 
   */
  public boolean addAll(Collection c) {
    result = null;
    return super.addAll(c);
  }

  /**
   * Adds all the Locatables from the collection into this list.
   * 
   * @param index index at which to insert first element
   * from the specified collection.
   * @param c Collection of Locatables.
   * @return <tt>true</tt> if this list changed as a result of the call.
   * @see Locatable 
   */
  public boolean addAll(int index, Collection c) {
    result = null;
    return super.addAll(index, c);
  }

  /**
   * Adds a locatable instance to the beginning of this list.
   * @param o a Locatable instance.
   * @see Locatable 
   */
  public void addFirst(Object o) {
    result = null;
    super.addFirst(o);
  }

  /**
   * Adds a locatable instance to the end of the list.
   * @param o a Locatable instance.
   * @see Locatable 
   */
  public void addLast(Object o) {
    result = null;
    super.addLast(o);
  }
  /**
   * Sorts locatable's by genomic location and returns
   * an array of the locatable's internalIDs.
   * @return zero or more internalIDs.
   * @throws ClassCastException if a non Locatable item was added
   * to the list.
   */
  public long[] toSortedInternalIDArray() {
    if (result == null) {
      Collections.sort(this);
      result = new long[size()];
      Iterator iter = listIterator();
      for (int i = 0; i < size(); i++)
        result[i] = ((Locatable) iter.next()).getInternalID();
    }
    return result;
  }
  
}
