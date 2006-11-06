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

package org.ensembl.util.mapper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Graham McVicker
 * @author Arne Stabenau
 * A convenience class to store Ranges of coordinates.
 * The ranges are stored in a confusing way. Ensembl requires all coordinates
 * to be inclusive. Inside this registry, all KEYS for range.start are actually 
 * range.start-1 to allow for ranges of length 1. All VALUES which are ranges
 * keep being inclusive coordinates. All comparisons of integers have to be
 * carfully looked at, wether they mean keys or ranges that could potentially
 * be returned.	
 * */
public class RangeRegistry {

	private HashMap registry;

	public RangeRegistry() {
		registry = new HashMap();
	}

	public List checkAndRegister(String id, int start, int end)
		throws IllegalArgumentException {
		return checkAndRegister(id, start, end, start, end);
	}

/**
 * 
 * @param id A possible Sequence id
 * @param start (start,end) area is check if registered
 * @param end
 * @param rstart (rstart, rend) area is registerd if start end is not
 * @param rend
 * @return a List of Range objects with intervalls that havnt been
 * registered yet (inside rstart, rend). It returns null if start,end
 * have been registered already
 * @throws Exception
 */
	public List checkAndRegister(
		String id,
		int start,
		int end,
		int rstart,
		int rend)
		throws IllegalArgumentException {

		if (start > end || rstart > rend) {
			throw new IllegalArgumentException("Start has to be smaller than end");
		}

		if (rstart > start || rend < end) {
			throw new IllegalArgumentException("rstart rend has to enclose start end");
		}

		LinkedList gaps;

		TreeMap ranges = (TreeMap) registry.get(id);

		if (ranges == null) {
			ranges = new TreeMap();
			Range range = new Range(rstart, rend);
			ranges.put(new Integer(rstart-1), range);
			ranges.put(new Integer(rend), range);
			registry.put( id, ranges );
			gaps = new LinkedList();
			gaps.add(range);
			return gaps;
		}

		// check if start end is covered
		try {
			SortedMap sm = ranges.tailMap(new Integer(start));
			Object firstKey = sm.firstKey();
			Range testRange = (Range) sm.get(firstKey);
			if (testRange.start <= start && testRange.end >= end) {
				return null;
			}
		} catch (NoSuchElementException e) {
			// no Range covered start
		}

		SortedMap subMap =
			ranges.subMap(new Integer(rstart - 1), new Integer(rend + 1));
		Range firstRange, lastRange, range;

		if( subMap.isEmpty()) {
			// nothing overlaps (rstart-1, rend+1)
			range = new Range(rstart, rend);
			ranges.put(new Integer(rstart-1), range);
			ranges.put(new Integer(rend), range);
			gaps = new LinkedList();
			gaps.add(range);
			return gaps;
		}
					
		// we could have missed start or end points at the start or end
		// of the intervall. Complete the subMap

		int newStart, newEnd;
		Integer firstKey, lastKey;
		
		firstKey = (Integer) subMap.firstKey();
		lastKey = (Integer) subMap.lastKey();
		firstRange = (Range) subMap.get( firstKey );
		lastRange = (Range) subMap.get( lastKey );
		subMap = ranges.subMap( new Integer(firstRange.start-1), new Integer( lastRange.end+1));
		newStart = firstRange.start < rstart? firstRange.start : rstart;
		newEnd = lastRange.end > rend? lastRange.end : rend;

		// now all relevant Ranges are completely in SubMap
		Iterator keyIterator = subMap.keySet().iterator();
		boolean startFlag = true;
		int currentStart = rstart;
		gaps = new LinkedList();
		
		while( keyIterator.hasNext()) {
			int val = ((Integer)keyIterator.next()).intValue();
			if( startFlag && val >= currentStart ) {
				range = new Range( currentStart, val);
				gaps.add(range);		
			}
			if( !startFlag && val < rend ) {
				currentStart = val+1;
			}
			startFlag = !startFlag;
		}

		if( currentStart <= rend ) {
			range = new Range( currentStart, rend );
			gaps.add( range );
		}
		// we can remove all ranges that overlapped (rstart, rend) 
		// and replace with (newStart, newEnd)
		
		subMap.clear();
		range = new Range( newStart, newEnd );
		ranges.put( new Integer( newStart-1), range );
		ranges.put( new Integer( newEnd ), range );
		
		return gaps;
	}
  
  /**
   * This function checks how much of the given range is registered in
   * this registry without changing the registry.
   */
  public int overlapSize( String id, int start, int end ) {
    int result = 0;
    
    int currentStart = start;
    
    SortedMap ranges = (SortedMap) registry.get( id );
    SortedMap subMap = ranges.subMap( new Integer( start-1 ), new Integer( end+1 ));
    Iterator i = subMap.entrySet().iterator();
    boolean rangeOpen = false;
    while( i.hasNext()) {
      Map.Entry e = (Map.Entry) i.next();
      int key = ((Integer)e.getKey()).intValue();
      Range r = (Range) e.getValue();                
      
      if( key+1 == r.start ) {
        currentStart = r.start;
        rangeOpen = true;  
      } else {
        result += ( r.end - currentStart + 1 );
        rangeOpen = false;
      }
      
    }
    if( rangeOpen ) {
      result += ( end - currentStart + 1 );
    }
   
    return result;  
  }


	public void flush() {
		registry.clear();
	}
}
