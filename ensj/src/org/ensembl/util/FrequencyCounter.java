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


/**
 * Convenience class that can be used to count the number
 * of times a key is added.
 * 
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public class FrequencyCounter extends HashMap {
	
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public FrequencyCounter() {
		super();
	}

	
	/**
	 * @param initialCapacity
	 * @param loadFactor
	 */
	public FrequencyCounter(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	
	/**
	 * If object is not currently a key in the map
	 * it is added as a key pointing to a Counter instance
	 * with a value of 1.
	 * @see Counter
	 * @param key key to be added or to have it's Counter
	 * incremented.
	 */
	public void addOrIncrement(Object key) {
		if (containsKey(key))
			((Counter)get(key)).increment();
		else
			put(key, new Counter(1));
	}
	
	/**
	 * Returns the most frequently added key. 
	 * 
	 * If two or
	 * more keys have been added the same (most) number
	 * of times then any of these could be returned.
	 * 
	 * @return most frequent key or null if none in map.
	 */
	public Object getMostFrequent() {
		if (size()==0) return null;
		int max = -1;
		Object r = null;
		for (Iterator iter = keySet().iterator(); iter.hasNext();) {
			Object tmp = iter.next();
			int c = ((Counter)get(tmp)).count;
			if (c>max) {
				max = c;
				r=tmp;
			}
		}
		return r;
	}


	/**
	 * Number of times _o_ has been added to this frequency
	 * counter.
	 * @param o object.
	 * @return frequency that object occured, or 0 if unkown object.
	 */
	public int getCount(Object o) {
		Counter c = (Counter) get(o);
		return (c==null) ? 0 : c.count;
	}
	
	/**
	 * Calls addOrIncrement(Object) for each item in list.
	 * @param list zero or more objects to be added to counter.
	 */
	public void addOrIncrementAll(List list) {
		for (int i = 0, n = list.size(); i < n; i++) 
			addOrIncrement(list.get(i));
			
	}
}
