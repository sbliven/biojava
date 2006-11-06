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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.ensembl.datamodel.Persistent;

/**
 * Set for storing unique longs.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 */
public class LongSet extends HashSet {

  private static final long serialVersionUID = 1L;

  protected long[] arrayCache = null;

  private boolean sorted = false;

  /**
   * Create an empty set.
   */
  public LongSet() {
  }

  /**
   * Creates a set of Longs from _s_.
   * 
   * @param s
   *          collection of zero or more Longs.
   */
  public LongSet(Collection s) {
    this(s, Long.class);
  }

  /**
   * Creates a set of Longs from various collections.
   * 
   * If c==Persistent.class then each item in _s_ is added to the set as 
   * new Long(persistent.getInternalID()).
   * 
   * @param s collection of zero or more Longs or zero or more Persistents.
   * @param c type of data in _s_. Currently must be Long or Persistent.
   * @see Long
   * @see Persistent
   */
  public LongSet(Collection s, Class c) {
    if (c == Persistent.class)
      for (Iterator iter = s.iterator(); iter.hasNext();)
        add(((Persistent) iter.next()).getInternalID());
    else if (c == Long.class)
      for (Iterator iter = s.iterator(); iter.hasNext();)
        add((Long) iter.next());
  }

  /**
   * Creates a set of Longs ids.
   * 
   * @param longs zero or more longs.
   */
  public LongSet(long[] longs) {
    
    addAll(longs);
  }

  public void addAll(long[] ls) {
    for (int i = 0; i < ls.length; i++)
      add(ls[i]);
  }

  public boolean add(long l) {
    return add(new Long(l));
  }

  public boolean add(Long l) {
    arrayCache = null;
    return super.add(l);
  }

  /**
   * This method throws an exception as it should not be used. Use add(long) or
   * add(Long) instead.
   * 
   * @see #add(long)
   * @see #add(Long)
   * @throws IllegalArgumentException
   */
  public boolean add(Object l) {
    throw new IllegalArgumentException("Can not add this type of object: " + l);
  }

  public long[] to_longArray() {
    return to_longArray(false);
  }
  
  public long[] to_longArray(boolean ascendingSort) {
    
    if (arrayCache == null) {
      sorted =false;
      final int size = size();
      arrayCache = new long[size];
      Iterator iter = iterator();
      for (int i = 0; i < size; i++)
        arrayCache[i] = ((Long) iter.next()).longValue();
      
    }
    
    if (ascendingSort && !sorted) {
      Arrays.sort(arrayCache);
      sorted = true;
    }
    
    return arrayCache;
  }

  /**
   * Creates an Array from a collection of Longs.
   * @param c collection of Longs.
   * @return array of longs corresponding to Longs in collection.
   */
  public static long[] to_longArray(Collection c) {
    int size = 0;
    for(Iterator iter = c.iterator(); iter.hasNext(); iter.next())
      size++;
    long[] result = new long[size];
    Iterator iter = c.iterator();
    for (int i = 0; i < size; i++)
      result[i] = ((Long) iter.next()).longValue();
    return result;
  }
  
  public boolean contains(long l) {
    return contains(new Long(l));
  }

}
