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

import org.biojava.bio.*;
import org.biojava.utils.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.bio.seq.io.*;

import java.util.*;

/**
 * Circular view onto an underlying Location instance. If the location overlaps
 * the origin of the sequence the underlying location will be a CompoundLocation
 * Note that in this case isContiguous() will return false. This behaviour is
 * desirable for proper treatment of the location with LocationTools.
 * To find if a location overlaps the origin use the overlapsOrigin() method
 *
 * @author Matthew Pocock
 * @author Mark Schreiber
 * @since 1.2
 */
public class CircularLocation
extends AbstractLocationDecorator {
  private final int length;
  private final boolean overlaps;

  public final int getLength() {
    return length;
  }

  public boolean overlapsOrigin(){
    return overlaps;
  }

  public CircularLocation(Location wrapped, int length) {
    super(wrapped);
    this.length = length;
    this.overlaps = CircularLocationTools.overlapsOrigin(this);
  }

  protected Location decorate(Location loc) {
    return new CircularLocation(loc, getLength());
  }

  public boolean contains(int p) {
    int pp = p % getLength() + (super.getMin() / getLength());

    return getWrapped().contains(pp);
  }


  public Location intersection(Location l) {
    return LocationTools.intersection(this,l);
  }
  public boolean overlaps(Location l) {
    return LocationTools.overlaps(this,l);
  }
  public Location union(Location l) {
    return LocationTools.union(this,l);
  }
  public boolean contains(Location l) {
    return LocationTools.contains(this,l);
  }
  public boolean equals(Object o){
    if((o instanceof Location)==false) return false;
    return LocationTools.areEqual(this, (Location)o);
  }
  public int getMax() {
    if(getWrapped().isContiguous()){
      if(getWrapped().getMin() ==1 && getWrapped().getMax() == length){
        return length;
      }
    }
    if(overlaps){
      int max = 1;
      for(Iterator i = getWrapped().blockIterator();i.hasNext();){
        Location l = ((Location)i.next());
        if(l.getMin() == 1) max = l.getMax();
      }
      return max;
    }else{
      return super.getMax();
    }
  }
  public int getMin() {
    if(getWrapped().isContiguous()){
      if(getWrapped().getMin() ==1 && getWrapped().getMax() == length){
        return 1;
      }
    }
    if(overlaps){
      int min = 1;
      for(Iterator i = getWrapped().blockIterator();i.hasNext();){
        Location l = ((Location)i.next());
        if(l.getMax() == length) min = l.getMin();
      }
      return min;
    }else{
      return super.getMin();
    }
  }
}
