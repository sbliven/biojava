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

/**
 * Title:        CircularLocationTools<p>
 * Description:  A class to hide the messiness of CircularLocs from LocationTools<p>
 * Copyright:    Copyright (c) 2001<p>
 * @author Mark Schreiber
 * @author Greg Cox
 * @version 1.0
 *
 * <b>WARNING</b> All binary operations on CircularLocations are currently assumed to
 * be performed on Locations from the same sequence, or sequences of the same
 * length. To enforce this binary operations will currently only accept circular
 * locations of the same length.
 */

final class CircularLocationTools {
  /**
   * translates coordinates from circular into linear
   *
   * @param val The circular coordinate
   * @param length The lenght of the circular molecule
   * @return the transformed coordinate
   */
  private static int realValue(int val, int length){
    val = ((val-1)%length)+1;
    if(val<0)val = length+1 + val;
    return val;
  }

  /**
   * Makes a circular location, called by LocationTools
   */
  protected static CircularLocation makeCircLoc(int min, int max, int seqLength){
    if(min == 0|| max == 0 || seqLength == 0){
        throw new IllegalArgumentException(
             "Must use a non zero integer a a parameter"
        );
      }

      boolean overlap = false;
      int rmin = realValue(min, seqLength);
      int rmax = realValue(max, seqLength);

      if(rmin > rmax){
        overlap = true;
        rmax+= seqLength;
      }

      if(!overlap){
        RangeLocation loc = new RangeLocation(rmin,rmax);
        return new CircularLocation(loc,seqLength);
      }else{
        RangeLocation locB = new RangeLocation(rmin, seqLength);
        RangeLocation locA = new RangeLocation(1,rmax - seqLength);
        Location compound = LocationTools.union(locA,locB);
        return new CircularLocation(compound,seqLength);
      }
  }

  /**
   * Tests a location to see if it overlaps the origin of the circular
   * molecule to which it belongs. The origin is taken to be position
   * one.
   * @returns true if it does overlap or if it contains the entire sequence
   */
  protected static boolean overlapsOrigin(CircularLocation loc){
    if(loc.getWrapped() instanceof CompoundLocation){
      boolean min = false;
      boolean max = false;
      for(Iterator i = loc.blockIterator(); i.hasNext();){
        Location l = (Location)(i.next());
        if(l.getMax() == loc.getLength()) max = true;
        if(l.getMin() == 1 ) min = true;
      }
      if(min && max){
        return true;
      }else{
        return false;
      }
    }
    else if(loc.getMin() ==1 && loc.getMax() == loc.getLength())return true;
    else return false;
  }


  /**
   * Tests if the specified location is an instance of CircularLocation.
   */
  protected static boolean isCircular(Location loc){
	boolean toReturn = false;
    try
    {
	    if(loc.getDecorator(Class.forName("org.biojava.bio.symbol.CircularLocation")) != null)
	    {
	    	toReturn = true;
	    }
	}
	catch(Exception e)
	{
		throw new org.biojava.bio.BioError("class org.biojava.bio.symbol.BetweenLocation could not be loaded");
	}
    return toReturn;
  }


  public static boolean safeOperation(Location locA, Location locB){
    if(isCircular(locA) && isCircular(locB)){
      if (((CircularLocation)locA).getLength() ==
            ((CircularLocation)locB).getLength()) {
        return true;
      }
      else {
        return false;
      }

    }else{
      return false;
    }
  }
}