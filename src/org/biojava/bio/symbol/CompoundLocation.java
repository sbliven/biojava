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

import java.util.*;
import java.io.*;
import org.biojava.bio.*;

/**
 * A comlex location. It is made up from multiple sub-locations and is essential
 * the point-wise union of the child locations.
 * <P>
 * Currently this is implemented very badly. I need a maths person to look over
 * it, and sort stuff out.
 *
 * @author Matthew Pocock
 */
public class CompoundLocation implements Location, Serializable {
  /**
   * The list of child locations in order. Should contain only RangeLocation
   * instances.
   */
  private List locations;

  /**
   * Minimum index contained.
   */
  private int min = Integer.MAX_VALUE;

  /**
   * Maximum index contained.
   */
  private int max = Integer.MIN_VALUE;

  /**
   * Set up the member variables.
   */
  {
    locations = new ArrayList();
  }
  
  /** 
   * Generate a new CompoundLocation from a list of locations.
   * <P>
   * The generated location will contain exactly those points that are within
   * at least one of the loctaions in the list.
   *
   * @param locations a list of Location instances to combine into a single
   *        compound location
   */
  public CompoundLocation(List locations) {
    List working = new ArrayList();
    for(Iterator i = locations.iterator(); i.hasNext(); ) {
      for(Iterator bi = ((Location) i.next()).blockIterator(); bi.hasNext(); ) {
        Location bl = (Location) bi.next();
        if(bl != Location.empty) {
          working.add(bl);
        }
      }
    }
    Collections.sort(this.locations, Location.naturalOrder);
    
    Location last = Location.empty;
    for(int i = 0; i < working.size(); i++) {
      Location cur = (Location) working.get(i);
      if(cur.overlaps(last)) {
        last = last.union(cur);
      } else {
        this.locations.add(last);
        last = cur;
      }
    }
    if(last != Location.empty) {
      this.locations.add(last);
    }
  }
  
  public boolean contains(int p) {
    if(p < min || p > max)
      return false;

    for(Iterator i = locations.iterator(); i.hasNext(); ) {
      if( ((Location) i.next()).contains(p) )
        return true;
    }

    return false;
  }

  // may be broken in the case when l spans more than one of the sub-locations
  public boolean contains(Location l) {
    if(l.getMin() > max || l.getMax() < min)
      return false;

    for(Iterator i = locations.iterator(); i.hasNext(); ) {
      if( ((Location) i.next()).contains(l) )
        return true;
    }

    return false;
  }

  public boolean overlaps(Location l) {
    if(l.getMin() > max || l.getMax() < min)
      return false;

    for(Iterator i = locations.iterator(); i.hasNext(); ) {
      if( ((Location) i.next()).overlaps(l) )
        return true;
    }

    return false;
  }

  public boolean equals(Object o) {
    if(o instanceof Location) {
      return Location.naturalOrder.areEqual(this, (Location) o);
    } else {
      return false;
    }
  }

  public int getMin() {
    return min;
  }

  public int getMax() {
    return max;
  }

  public Location intersection(Location l) {
    List res = new ArrayList();

    for(Iterator i = locations.iterator(); i.hasNext(); ) {
      Location loc = ((Location) i.next()).intersection(l);
      if(loc != Location.empty) {
        res.add(loc);
      }
    }

    if(res.size() != 0)
      return new CompoundLocation(res);
    return Location.empty;
  }

  public Location union(Location l) {
    List res = new ArrayList();

    for(Iterator i = locations.iterator(); i.hasNext(); )
      res.add( ((Location) i.next()).union(l) );

    return new CompoundLocation(res);
  }

  public SymbolList symbols(SymbolList s) {
    List res = new ArrayList();

    for(int p = min; p <= max; p++)
      if(this.contains(p))
        res.add(s.symbolAt(p));

    try {
      return new SimpleSymbolList(s.getAlphabet(), res);
    } catch (IllegalSymbolException ex) {
      throw new BioError(ex);
    }
  }

  public Location translate(int dist) {
    List res = new ArrayList();

    for(Iterator i = locations.iterator(); i.hasNext(); )
      res.add( ((Location) i.next()).translate(dist) );

    return new CompoundLocation(res);
  }

  public boolean isContiguous() {
    return locations.size() <= 1;
  }
  
  public Iterator blockIterator() {
    return locations.iterator();
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(getMin() + ", " + getMax() + " {");
    Iterator i = locations.iterator();
    if(i.hasNext())
      sb.append("(" + i.next() + ")");
    while(i.hasNext())
      sb.append(", (" + i.next() + ")");
    sb.append("}");

    return sb.toString();
  }
}
