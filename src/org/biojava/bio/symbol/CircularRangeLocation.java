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

/**
 * Essentially the equivalent of a <code>RangeLocation<\code> but the location
 * may be specified in terms of an index that only makes sense for a
 * <code>CircularSequence<\code> or a <code>CircularView<\code>
 *
 * Note that although methods such as equals() and intersection() have been
 * coded so as to allow interaction with non circular locations, as yet most
 * other locations are not coded to cope with circular locations therefore
 * the methods should be used with caution when using both types of location,
 * which ideally should not happen anyhow.
 *
 * @author Mark Schreiber
 * @version 1.0
 */

import org.biojava.bio.*;
import org.biojava.utils.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.bio.seq.io.*;

import java.util.*;

public class CircularRangeLocation implements Location, java.io.Serializable{
  private int min;
  private int max;
  private int realMin;
  private int realMax;
  private int length;
  private boolean overlap; //indicates if the range overlaps the origin.

  /**
   * Constructs a Circular Location. It makes little sense to use this object
   * except in conjunction with a circular sequence. Note that because the
   * location is circular it is perfectly sensible for the min to be greater
   * than the max!?  Locations may also be specified in terms of negative
   * integers or integers greater than the length of the sequence. These will
   * be internally processed into "real" coordinates.
   *
   * Note that a circular location need not overlap the origin. This is so non
   * overlapping locations can be created using the more flexible integer
   * indices appropriate for a Circular Sequence. It also allows for sensible
   * translation of the object past the "end" or "start" of the underlying
   * Circular Sequence.
   *
   * @param min Any non zero integer, probably best described as the left
   * end of the feature.
   * @param max Any non zero integer, probably best described as the right
   * end of the feature.
   */
  public CircularRangeLocation(int min, int max, int length) {
    this.min = min;
    this.max = max;
    this.length = length;
    realMin = realValue(min);
    realMax = realValue(max);
    if(realMin > realMax){overlap = true;}
    else if (realMin == 1 && realMax == length){
    //Special case where the location includes the entire sequence
      overlap = true;
    }
    else{overlap = false;}
  }

  public Iterator blockIterator() {
    return Collections.singleton(this).iterator();
  }

  /**
   * Determines if p is present within the range of the CircularRangeLocation
   *
   * @param p any non zero integer.
   */
  public boolean contains(int p){
    p = realValue(p);
    if(overlap = true){
      return getMin() >= p ||
             getMax() <= p;
    }
    else{
      return getMin() <= p &&
             getMax() >= p;
    }
  }

  /**
   * Checks to see in this location wholey contains another location.
   *
   * @param l The location that might be contained. l may be circular or linear.
   */
  public boolean contains(Location l) {
    if(overlapsOrigin() && l.getClass().isInstance(this)){// l is circular
      CircularRangeLocation cl = (CircularRangeLocation)l;
      if(cl.overlapsOrigin()){// both this and cl overlap the origin
        return getMin() <= cl.getMin()&&
               getMax() >= cl.getMax();
      }else{ //only this overlaps the orign, cl doesn't
        return getMin() <= cl.getMin()&&
               length >= cl.getMax();
      }
    }else if(!overlapsOrigin() && l.getClass().isInstance(this)){
      CircularRangeLocation cl = (CircularRangeLocation)l;
      if(cl.overlapsOrigin()){//cl overlaps origin this doesn't
        return false; //By definition!
      }else{// neither this nor cl overlap the origin
        return getMin() <= l.getMin() &&
             getMax() >= l.getMax();
      }
    }
    else{// No overlap and l is linear
      return getMin() <= l.getMin() &&
             getMax() >= l.getMax();
    }
  }

  /**
  *Tests for object equality against another location
  *@param l the location to compare against. May be circular or linear.
  */
  public boolean equals(Location l) {
    if(l.getClass().isInstance(this)){
      CircularRangeLocation cl = (CircularRangeLocation)l;
      return getMin() == cl.getMin()&&
             getMax() == cl.getMax()&&
             overlapsOrigin() == cl.overlapsOrigin();
    }else if(overlapsOrigin()){
      return false;
    }else{
      return getMin() == l.getMin() &&
             getMax() == l.getMax() && l.isContiguous();
    }
  }

  /**
   * @return the "real left end" index of the location.
   */
  public int getMin(){return realMin;}
  /**
   * @return the "real right end" index of the location.
   */
  public int getMax(){return realMax;}

  /**
   * A method to return a location object which is the intersection of this
   * object and the location l.
   *
   * @param l may be circular or linear.
   */
  public Location intersection(Location l) {
    if(l.getClass().isInstance(this) && overlapsOrigin()){
      CircularRangeLocation cl = (CircularRangeLocation)l;
      if(cl.overlapsOrigin()){  //both overlap
        int start = Math.max(getMin(), cl.getMin());
        int end = Math.min(getMax(), cl.getMax());
        return new CircularRangeLocation(start, end, length);
      }else{
        // this overlaps, cl doesn't
        int start = Math.max(getMin(),cl.getMin());
        int end = Math.min(length,cl.getMax());
        if(start <= end) return new CircularRangeLocation(start, end, length);
        else return Location.empty;
      }

    }else if(!overlapsOrigin() && l.getClass().isInstance(this)){
      CircularRangeLocation cl = (CircularRangeLocation)l;
      if(cl.overlapsOrigin()){// this doesn't overlap, cl overlaps
        int start = Math.max(getMin(), cl.getMin());
        int end = Math.min(getMax(), length);
        if(start <= end)return new CircularRangeLocation(start, end, length);
        else return Location.empty;
      }else{// neither overlap
        int start = Math.max(getMin(), cl.getMin());
        int end = Math.min(getMax(), cl.getMax());
        if(start <= end) return new CircularRangeLocation(start, end, length);
        else return Location.empty;
      }

    }else{// no overlaps in either
      int start = Math.max(getMin(), l.getMin());
      int end = Math.min(getMax(), l.getMax());
      if(start <= end) return new CircularRangeLocation(start, end, length);
      else return Location.empty;
    }
  }

  public boolean isContiguous() {
    return true;
  }

  public boolean overlaps(Location l){
    if(overlapsOrigin() && l.getClass().isInstance(this)){
      CircularRangeLocation cl = (CircularRangeLocation)l;
      if(cl.overlapsOrigin()){//both overlap the origin
        return true; //They both share at least the common origin overlap.
      }else{// Only this overlaps the origin
        return cl.getMax() > getMax() ||
               cl.getMin() < getMin() ||
               (getMin() == 1 && getMax() == length);//This contains the whole
                                                     //sequence.
      }
    }
    else if(!overlapsOrigin() && l.getClass().isInstance(this)){
      CircularRangeLocation cl = (CircularRangeLocation)l;
      if(cl.overlapsOrigin()){// only cl overlaps the origin
        return getMax() > cl.getMax() ||
               getMin() < cl.getMin() ||
               (cl.getMin() ==1 && cl.getMax() == length);//cl contains the
                                                          //whole sequence.
      }else{// neither overlap the origin.
        return !(getMin() > l.getMax() ||
               getMax() < l.getMin());
      }
    }
    else{// neither overlap the origin.
      return !(getMin() > l.getMax() ||
             getMax() < l.getMin());
    }
  }

  /**
   * Note that calling this method on a non circular sequence will cause
   * all kinds of problems so don't do it.
   */
  public SymbolList symbols(SymbolList seq) {
    return seq.subList(getMin(), getMax());
  }

  /**
   * returns a CircularRangeLocation object created by translating the indices
   * of the location by the specified distance.
   */
  public Location translate(int dist) {
    return new CircularRangeLocation(getMin() + dist, getMax() + dist, length);
  }

  /**
   * Return a Location containing all points in either ranges.
   *
   * NOTE: this will make a CompoundLocation containing this location
   * and another Location that may or may not be Circular. It should be used
   * with care as the behaviour of the CompoundLocation maybe unpredictable.
   *
   * @param l	the Location to union with
   * @return	a Location representing the union
   */
  public Location union(Location l) {
    List al = new ArrayList(2);
    al.add(this);
    al.add(l);

    return new CompoundLocation(al);
  }

  /**
   * returns true if the location overlaps the origin or if the location
   * contains the entire sequence. By default the origin is 1.
   */
  public boolean overlapsOrigin(){
    return overlap;
  }

   public String toString() {
    return "[" + getMin() + ", " + getMax() + "]";
  }

  private int realValue(int val){
    val = ((val-1) % length) + 1;
    if(val < 0) val = length +1 + val;
    return val;
  }
}
