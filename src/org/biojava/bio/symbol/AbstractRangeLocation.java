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

package org.biojava.bio.symbol;

import java.io.*;
import java.util.*;

/**
 * A simple implementation of Location that contains all points between
 * getMin and getMax inclusive.
 * <P>
 * This will in practice be the most commonly used pure-java implementation.
 *
 * @author Matthew Pocock
 */
public abstract class AbstractRangeLocation implements Location {
  public boolean overlaps(Location l) {
    return !(getMin() > l.getMax() ||
             getMax() < l.getMin());
  }

  public boolean contains(Location l) {
    return getMin() <= l.getMin() &&
           getMax() >= l.getMax();
  }

  public boolean contains(int p) {
    return getMin() <= p &&
           getMax() >= p;
  }
    
    
  /**
  *Tests for object equality against another location
  *@param l the location to compare against
  */  
  public boolean equals(Location l) {
    return getMin() == l.getMin() &&
           getMax() == l.getMax() && l.isContiguous();
  }

  public Location intersection(Location l) {
    int start = Math.max(getMin(), l.getMin());
    int end = Math.min(getMax(), l.getMax());

    if(start <= end)
      return new RangeLocation(start, end);
    else
      return Location.empty;
  }

  public Location union(Location l) {
    List al = new ArrayList(2);
    al.add(this);
    al.add(l);

    return new CompoundLocation(al);
  }

  public SymbolList symbols(SymbolList seq) {
    return seq.subList(getMin(), getMax());
  }

  public Location translate(int dist) {
      if (dist == 0)
	  return this;

      return new RangeLocation(getMin() + dist, getMax() + dist);
  }
  
  public boolean isContiguous() {
    return true;
  }

  public Iterator blockIterator() {
    return Collections.singleton(this).iterator();
  }

  public String toString() {
    return "[" + getMin() + ", " + getMax() + "]";
  }
}
