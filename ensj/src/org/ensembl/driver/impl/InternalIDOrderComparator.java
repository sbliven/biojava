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

package org.ensembl.driver.impl;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.ensembl.datamodel.Persistent;


/**
 * Compares Persistable items according to the order of their internal
 * IDs as specified in an array.
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public class InternalIDOrderComparator implements Comparator {

  private Map rankings = new HashMap();

  private int indexOf(Object o) {
    Persistent p =(Persistent) o;
    Long key = new Long(p.getInternalID());
    Integer index = (Integer) rankings.get(key);
    return index.intValue();
  }

  /**
   * Creates a comparator that uses the order of the internal IDs in the array 
   * to determine the order Persistent items that have the same internal IDs.
   * @param internalIDs ordered internalIDs to be used for sorting.
   */
  public InternalIDOrderComparator(long[] internalIDs) {
    for (int i = 0; i < internalIDs.length; i++) {
      rankings.put(new Long(internalIDs[i]), new Integer(i));
    }
  }

  /**
   * Compares the order of the internalIDs according to there indexes in the internalIDs array.
   * @param o1 a Persistent object
   * @param o2 a Persistent object
   * @return the difference in the index position of the two items,
   * <0 if o1.internalID appears before o2.internalID in the array, and >0 if
   * it appears afterwards.
   */
  public int compare(Object o1, Object o2) {
    return indexOf(o1) - indexOf(o2);
  }
}