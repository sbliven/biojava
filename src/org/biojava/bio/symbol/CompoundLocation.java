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
 * A complex location. It is made up from multiple sub-locations and is essentially
 * the point-wise union of the child locations.
 * <P>
 * Currently this is implemented very badly. I need a maths person to look over
 * it, and sort stuff out.
 *
 * @author Matthew Pocock
 * @author Thomas Down
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
   * at least one of the Locations in the list.
   *
   * @param locations a list of Location instances to combine into a single
   *        compound location
   */
  public CompoundLocation(List locs) {
      //      System.out.println("\n\n\nInput: " + locs.toString());

    List working = new ArrayList();
    for(Iterator i = locs.iterator(); i.hasNext(); ) {
      for(Iterator bi = ((Location) i.next()).blockIterator(); bi.hasNext(); ) {
        Location bl = (Location) bi.next();
        if(bl != Location.empty) {
          working.add(bl);
        }
      }
    }

    Collections.sort(working, Location.naturalOrder);

    // 
    // NB the following code assumes that all elements in
    // `working' are contiguous blocks, and it may break
    // if this is not true.
    //

    Iterator i = working.iterator();
    Location last = Location.empty;
    if(i.hasNext()) {
      last = (Location) i.next();
    }
    while(i.hasNext()) {
      Location cur = (Location) i.next();
      if (last.overlaps(cur)) {
	    last = new RangeLocation(Math.min(last.getMin(), cur.getMin()),
				     Math.max(last.getMax(), cur.getMax()));
      } else {
	if (last != Location.empty) {
	    this.locations.add(last);
        }
        last = cur;
      }
    }
    if(last != Location.empty) {
      this.locations.add(last);
    }

    if (this.locations.size() != 0) {
	min = ((Location) this.locations.get(0)).getMin();
	max = ((Location) this.locations.get(this.locations.size() - 1)).getMax();
    }

    // System.out.println("Output: " + this.locations.toString());
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
    List res = new ArrayList(this.locations);
    res.add(l);

    return new CompoundLocation(res);
  }

  public SymbolList symbols(SymbolList seq) {
      if (isContiguous())
	  return seq.subList(min, max);

    List res = new ArrayList();
    for (Iterator i = blockIterator(); i.hasNext(); ) {
	Location l = (Location) i.next();
	res.addAll(l.symbols(seq).toList());
    }

    try {
      return new SimpleSymbolList(seq.getAlphabet(), res);
    } catch (IllegalSymbolException ex) {
      throw new BioError(ex);
    }
  }

  public Location translate(int dist) {
      if (dist == 0) {
	  return this;
      }

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
  
  public boolean equals(Object o) {
    if(!(o instanceof Location)) {
      return false;
    }
    
    Location loc = (Location) o;
    if(loc.isContiguous())       { return false; }
    if(loc.getMin() != getMin()) { return false; }
    if(loc.getMax() != getMax()) { return false; }
    
    Iterator thisI = blockIterator();
    Iterator thatI = loc.blockIterator();
    
    while(thisI.hasNext() && thatI.hasNext()) {
      Location thisL = (Location) thisI.next();
      Location thatL = (Location) thatI.next();
      if(!thisL.equals(thatL)) {
        return false;
      }
    }
    
    if(thisI.hasNext() || thatI.hasNext()) {
      return false;
    }
    
    return true;
  }
  
  public int hashCode() {
    return getMin() ^ getMax();
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
