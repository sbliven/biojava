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
 * A single symbol.
 * <P>
 * min and max equal the location of the single symbol.
 *
 * @author Matthew Pocock
 */
public class PointLocation implements Location, Serializable {
  /**
   * The actual index contained.
   */
  private int point;

  public int getMin()	{ return point; }
  public int getMax()	{ return point; }
  public boolean overlaps(Location l)	{ return l.contains(this); }
  public boolean contains(Location l)	{ return (this.point == l.getMin()) &&
                                                   (this.point == l.getMax()); }
  public boolean contains(int p)	{ return this.point == p; }
  /**
  *Test for equality with another Location object
  *@param l location to compare with
  */
  public boolean equals(Location l)	{ return this.contains(l) && l.contains(this); }
  public Location intersection(Location l) {
    return l.contains(this)
      ? this
      : Location.empty;
  }
  public Location union(Location l)	{
    List locations = new ArrayList();
    locations.add(this);
    locations.add(l);
    CompoundLocation cl = new CompoundLocation(locations);
    return cl;
  }

  public SymbolList symbols(SymbolList s)	{
    final Symbol sym = s.symbolAt(this.point);
    try {
      return new SimpleSymbolList(s.getAlphabet(), new AbstractList() {
        public Object get(int index) throws IndexOutOfBoundsException {
          if(index == 0) {
            return sym;
          }
          throw new IndexOutOfBoundsException("Index " + index + " greater than 0");
        }
        public int size() { return 1; }
      });
    } catch (IllegalSymbolException ex) {
      throw new BioError(ex);
    }
  }

  public boolean isContiguous() {
    return true;
  }

  public Iterator blockIterator() {
    return Collections.singleton(this).iterator();
  }
  
  public Location translate(int dist) {
    return new PointLocation(this.point + dist);
  }
  
  public PointLocation(int point) {
    this.point = point;
  }
}
