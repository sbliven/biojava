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

package org.biojava.bio.seq;

import java.util.*;
import org.biojava.bio.symbol.*;

/**
 * A comlex location. It is made up from multiple sub-locations and is essential
 * the point-wise union of the child locations.
 * <P>
 * Currently this is implemented very badly. I need a maths person to look over
 * it, and sort stuff out.
 *
 * @author Matthew Pocock
 */
public class CompoundLocation implements Location {
  /**
   * The list of child locations in no particular order.
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

  public boolean contains(int p) {
    if(p < min || p > max)
      return false;

    for(Iterator i = locations.iterator(); i.hasNext(); ) {
      if( ((Location) i.next()).contains(p) )
        return true;
    }

    return false;
  }

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

  public boolean equals(Location l) {
    return l == this;
  }

  public int getMin() {
    return min;
  }

  public int getMax() {
    return max;
  }

  public Location intersection(Location l) {
    CompoundLocation res = new CompoundLocation();

    for(Iterator i = locations.iterator(); i.hasNext(); ) {
      Location loc = ((Location) i.next()).intersection(l);
      if(loc != Location.empty)
        res.addLocation(loc);
    }

    if(res.locations.size() != 0)
      return res;
    return Location.empty;
  }

  public Location union(Location l) {
    CompoundLocation res = new CompoundLocation();

    for(Iterator i = locations.iterator(); i.hasNext(); )
      res.addLocation( ((Location) i.next()).union(l) );

    return res;
  }

  public SymbolList symbols(SymbolList s) {
    List res = new ArrayList();

    for(int p = min; p <= max; p++)
      if(this.contains(p))
        res.add(s.symbolAt(p));

    return new SimpleSymbolList(s.alphabet(), res);
  }

  public void addLocation(Location l) {
    locations.add(l);
    min = Math.min(min, l.getMin());
    max = Math.max(max, l.getMax());
  }

  public Location translate(int dist) {
    CompoundLocation res = new CompoundLocation();

    for(Iterator i = locations.iterator(); i.hasNext(); )
      res.addLocation( ((Location) i.next()).translate(dist) );

    return res;
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
