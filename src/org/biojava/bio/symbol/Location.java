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
import java.lang.reflect.*;

import org.biojava.utils.*;
import org.biojava.bio.*;

/**
 * A biological location.
 * <P>
 * The location will contain some symbols between getMin and getMax inclusive.
 * It is not required to contain all locations within this range. It is meant
 * to contain getMin or getMax. In the event that an operation would produce an
 * invalid or nonsensical range, empty should be returned.
 * </p>
 *
 * <p>
 * Location objects are <strong>always</strong> immutable.
 * </p>
 *
 * @author Matthew Pocock
 * @author Thomas Down
 */
public interface Location {
  /**
   * Create a new instance of Location with all of the same decorators as this
   * instance but with the data stored in <code>loc</code>.
   * <P>
   * The default behavior is to return <loc>loc</loc> unchanged. If the class is
   * a location decorator then it should instantiate an instance of the same
   * type that decorates <code>loc</code>.
   *
   * @param loc  the Location to use as template
   * @return a Location instance based on loc with the same decorators as the
   *         current instance
   */
  Location newInstance(Location loc);
  
  /**
   * Checks the decorator chain for an instance of <class>decoratorClass</class>
   * and return it if found.
   * <P>
   * The default behavior is to return null. If the current object is a
   * decorator and is an instance of <class>decoratorClass</class> it should
   * return itself. Otherwise, the decorator should chain this method onto the
   * instance it wraps.
   *
   * @param decoratorClass  the Class to check
   * @return a Location if an instance of this class is present in the decorator
   *         chain and null otherwise.
   */
  Location getDecorator(Class decoratorClass);
  /**
   * The minimum position contained.
   *
   * @return	the minimum position contained
   */
  int getMin();
  /**
   * The maximum position contained.
   *
   * @return	the maximum position contained
   */
  int getMax();

  /**
   * Checks if these two locations overlap, using this locations's
   * concept of overlaping.
   * <P>
   * Abstractly, two locations overlap if they both contain any point.
   *
   * @param l	the Location to check
   * @return	true if they overlap, otherwise false
   * @deprecated use LocationTools.areOverlapping(Location a, Location b)
   */
  boolean overlaps(Location l);
  /**
   * Checks if this location contains the other.
   * <P>
   * Abstractly, a location contains another if every point in the
   * other location is contained within this one.
   *
   * @param l	the Location to check
   * @return	true if this contains l, otherwise false
   * @deprecated use LocationTools.isContainedBy(Location a, Location b)
   */
  boolean contains(Location l);
  /**
   * Checks if this location contains a point.
   *
   * @param p	the point to check
   * @return	true if this contains p, otherwise false
   */
  boolean contains(int p);
  
  /**
   * Checks if this location is equivalent to the other.
   * <P>
   * Abstractly, a location is equal to another if for every point in one
   * it is also in the other. This is equivalent to
   * a.contains(b) && b.contains(a). You should call LocationTools.areEqual
   * after casting l to Location.
   *
   * @param l	the Object to check
   * @return	true if this equals l, otherwise false
   */
  boolean equals(Object l);

  /**
   * Returns a Location that contains all points common to both ranges.
   * 
   * @param l	the Location to intersect with
   * @return	a Location containing all points common to both, or
   *              the empty range if there are no such points
   * @deprecated use LocationTools.intersection(Location a, Location b)
   */
  Location intersection(Location l);
  /**
   * Return a Location containing all points in either ranges.
   *
   * @param l	the Location to union with
   * @return	a Location representing the union
   * @deprecated use LocationTools.union(Location a, Location b)
   */
  Location union(Location l);

  /**
   * Return the symbols in a sequence that fall within this range.
   *
   * @param seq	the SymbolList to process
   * @return	the SymbolList containing the symbols in seq in this range
   * @deprecated this was a mistake which has not been necisary since the
   *             introduction of blockIterator()
   */
  SymbolList symbols(SymbolList seq);

  /**
   * Create a location that is a translation of this location.
   *
   * @param dist  the distance to translate (to the right)
   */
  Location translate(int dist);
   
  /**
   * Determine if a Location is contiguous.
   *
   * @return <code>true</code> if and only if this Location
   *         contains every point from <code>min</code> to
   *         <code>max</code> inclusive.
   */
  boolean isContiguous();
  
  /**
   * Return an Iterator over the set of maximal contiguous sub-locations.
   * <P>
   * Given any location, it can be considered to contain zero or more
   * maximal contiguous blocks of width 1 or greater. The empty location is
   * composed from nothing. A contiguous location is composed from itself.
   * A non-contiguous location is composed from contiguous blocks seperated by
   * gaps.
   * <P>
   * This method should return an Iterator over these maximally contiguous blocks
   * starting with the left-most block, and finishing at the right-most block.
   *
   * @return an Iterator over Location objects that are the maximally contiguous
   *         set of locations contained within this location
   */
  Iterator blockIterator();

  /**
   * The empty range.
   * <P>
   * This object contains nothing. Its minimum value is Integer.MAX_VALUE.
   * Its maximum value is Integer.MIN_VALUE. It overlaps nothing. It is
   * equal to nothing. Intersection results in the empty range. Union
   * results in the argument range. Symbols returns an empty array.
   * <P>
   * Every day, in every way, empty becomes more and more boring.
   */
  static final Location empty = new EmptyLocation();
  
  static final LocationComparator naturalOrder = new LocationComparator();
  
  /**
   * The implementation of Location that contains no positions at all.
   *
   * @author Matthew Pocock
   */
  static final class EmptyLocation implements Location, Serializable {
    public Location getDecorator(Class decoratorClass) {
      if(decoratorClass.isInstance(this)) {
        return this;
      } else {
        return null;
      }
    }
    public Location newInstance(Location loc) { return loc; }
    public int getMin() { return Integer.MAX_VALUE; }
    public int getMax() { return Integer.MIN_VALUE; }
    public boolean overlaps(Location l) { return false; }
    public boolean contains(Location l) { return false; }
    public boolean contains(int p) { return false; }
    public boolean equals(Object o) {
      if(o instanceof Location) {
        if(o instanceof EmptyLocation) {
          return true;
        } else {
          return Location.naturalOrder.areEqual(this, (Location) o);
        }
      } else {
        return false;
      }
    }
    public Location intersection(Location l) { return empty; }
    public Location union(Location l) { return l; }
    public SymbolList symbols(SymbolList seq) {
      try {
        return new SimpleSymbolList(seq.getAlphabet(), new ArrayList());
      } catch (IllegalSymbolException ex) {
        throw new BioError(ex);
      }
    }
    public Location translate(int dist) { return this; }
    public boolean isContiguous() { return true; }
    public Iterator blockIterator() { return Collections.EMPTY_SET.iterator(); }
    private Object writeReplace() throws ObjectStreamException {
      try {
        return new StaticMemberPlaceHolder(Location.class.getField("empty"));
      } catch (NoSuchFieldException nsfe) {
        throw new NotSerializableException(nsfe.getMessage());
      }
    }
    public String toString() {
      return "{}";
    }
  }
  
  static final class LocationComparator implements Comparator, Serializable {
    public int compare(Object o1, Object o2) {
      int d = 0;
        
      Location l1 = (Location) o1;
      Location l2 = (Location) o2;
        
      Iterator i1 = l1.blockIterator();
      Iterator i2 = l2.blockIterator();

      while(i1.hasNext() && i2.hasNext()) {
        Location li1 = (Location) i1.next();
        Location li2 = (Location) i2.next();
          
        d = li1.getMin() - li2.getMin();
        if(d != 0) {
          return d;
        }
        d = li1.getMax() - li2.getMax();
        if(d != 0) {
          return d;
        }
      }
      if(i2.hasNext()) {
        return 1;
      } else if(i1.hasNext()) {
        return -1;
      }
        
      return 0;
    }
    
    public boolean equals(Object obj) {
      return obj == this;
    }
    
    /**
    *Test whether two locations are equal or not
    */
    public boolean areEqual(Location l1, Location l2) {
      Iterator i1 = l1.blockIterator();
      Iterator i2 = l2.blockIterator();

      while(i1.hasNext() && i2.hasNext()) {
        if(! i1.next().equals(i2.next()) ) {
          return false;
        }
      }
      
      if(!i1.hasNext() && !i2.hasNext()) {
        return false;
      }
      
      return true;
    }
    
    private Object writeReplace() throws ObjectStreamException {
      try {
        return new StaticMemberPlaceHolder(Location.class.getField("naturalOrder"));
      } catch (NoSuchFieldException nsfe) {
        throw new NotSerializableException(nsfe.getMessage());
      }
    }
  }
}
