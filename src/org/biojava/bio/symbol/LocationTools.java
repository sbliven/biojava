package org.biojava.bio.symbol;

import java.util.*;

/**
 * Repository for binary operators on Location instances.
 * @author Matthew Pocock
 * @author Greg Cox
 * @author Thomas Down
 * @since 1.2
 */
final public class LocationTools {
  /**
   * Return the union of two locations.
   * <P>
   * The union will be a Location instance that contains every index contained
   * by either locA or locB.
   *
   * @param locA  the first Location
   * @param locB  the second Location
   * @return a Location that is the union of locA and locB
   */
  public static Location union(Location locA, Location locB) {
  	if(isDecorated(locA) || isDecorated(locB))
  	{
  		handleDecorations();
  	}

    if(
      locA.isContiguous() &&
      locB.isContiguous() &&
      locA.overlaps(locB)
    ) {
      // the simple case
      return buildLoc(
        Math.min(locA.getMin(), locB.getMin()),
        Math.max(locA.getMax(), locB.getMax())
      );
    } else {
      // either may be compound. They may not overlap. We must build the
      // complete list of blocks, merge overlapping blocks and then create the
      // apropreate implementation of Location for the resulting list.

      // list of all blocks
      List locList = new ArrayList();

      // add all blocks in locA
      for(Iterator i = locA.blockIterator(); i.hasNext(); ) {
        locList.add(i.next());
      }

      // add all blocks in locB
      for(Iterator i = locB.blockIterator(); i.hasNext(); ) {
        locList.add(i.next());
      }

      return _union(locList);
    }
  }

  /**
   * Return the intersection of two locations.
   * <P>
   * The intersection will be a Location instance that contains every index
   * contained by both locA and locB.
   *
   * @param locA  the first Location
   * @param locB  the second Location
   * @return a Location that is the intersection of locA and locB
   */
  public static Location intersection(Location locA, Location locB) {

    if(isDecorated(locA) || isDecorated(locB))
    {
	handleDecorations();
    }
    if(locA.isContiguous() && locB.isContiguous()) {
      // handle easy case of solid locations
      if(locA.overlaps(locB)) {
        int min = Math.max(locA.getMin(), locB.getMin());
        int max = Math.min(locA.getMax(), locB.getMax());
        return buildLoc(min, max);
      } else {
        return Location.empty;
      }
    } else {

      // One or other of the locations is compound. Build a list of all
      // locations created by finding intersection of all pairwise combinations
      // of blocks in locA and locB. Ignore all Location.empty. Create the
      // appropriate Location instance.
      List locList = new ArrayList();
      Iterator aI = locA.blockIterator();
      while(aI.hasNext()) {
        Location a = (Location) aI.next();
        Iterator bI = locB.blockIterator();
        while(bI.hasNext()) {
          Location b = (Location) bI.next();
          Location bIn = LocationTools.intersection(a, b);
          if(bIn != Location.empty) {
            locList.add(bIn);
          }
        }
      }

      return buildLoc(locList);
    }
  }

  /**
   * Returns whether the two locations overlap or not.
   * <P>
   * Two locations overlap if they contain at least one index in common.
   *
   * @param locA  the first Location
   * @param locB  the second Location
   * @param return true if they overlap, false otherwise
   */
  public static boolean overlaps(Location locA, Location locB) {
  	if(isDecorated(locA) || isDecorated(locB))
  	{
  		handleDecorations();
  	}
    if(locA.isContiguous() && locB.isContiguous()) {
      // if they are both solid, return whether the extents overlap
      return !(
        (locA.getMax() < locB.getMin()) ||
        (locA.getMin() > locB.getMax())
      );
    } else {
      // one or the other is compound. Return true if any of the regions of one
      // location overlap with any of the regions of the other.
      for(Iterator aI = locA.blockIterator(); aI.hasNext(); ) {
        Location a = (Location) aI.next();
        for(Iterator bI = locB.blockIterator(); bI.hasNext(); ) {
          Location b = (Location) bI.next();
          if(LocationTools.overlaps(a, b)) {
            return true;
          }
        }
      }

      // could find no overlapping regions - return false.
      return false;
    }
  }

  public static boolean contains(Location locA, Location locB) {
  	if(isDecorated(locA) || isDecorated(locB))
  	{
  		handleDecorations();
  	}
    if(locA.isContiguous() && locB.isContiguous()) {
      // both solid - check the extents
      return
        (locA.getMin() <= locB.getMin()) &&
        (locA.getMax() >= locB.getMax());
    } else {
      // chech that every block in locB is contained by a block in locA.
     OUTER:
      for(Iterator bI = locB.blockIterator(); bI.hasNext(); ) {
        Location b = (Location) bI.next();
        for(Iterator aI = locA.blockIterator(); aI.hasNext(); ) {
          Location a = (Location) aI.next();
          if(LocationTools.contains(a, b)) {
            continue OUTER;
          }
        }
        // this block is not contained therefore b is not contained within a.
        return false;
      }

      return true;
    }
  }

  /**
   * Return whether two locations are equal.
   * <P>
   * They are equal if both a contains b and b contains a. Equivalently, they
   * are equal if for every point p, locA.contains(p) == locB.contains(p).
   *
   * @param locA the first Location
   * @param locB the second Location
   * @return true if they are equivalent, false otherwise
   */
  public static boolean areEqual(Location locA, Location locB) {
  	if(isDecorated(locA) || isDecorated(locB))
  	{
  		handleDecorations();
  	}
    // simple check - if one is broken and the other isn't, they aren't equal.
    if(locA.isContiguous() != locB.isContiguous()) {
      return false;
    }

    // both contiguous if one is - check extent only
    if(locA.isContiguous()) {
      return
        (locA.getMin() == locB.getMin()) &&
        (locA.getMax() == locB.getMax());
    }

    // ok - both compound. The blocks returned from blockIterator should each be
    // equivalent.
    Iterator i1 = locA.blockIterator();
    Iterator i2 = locB.blockIterator();

    // while there are more pairs to check...
    while(i1.hasNext() && i2.hasNext()) {
      // check that this pair is equivalent
      Location l1 = (Location) i1.next();
      Location l2 = (Location) i2.next();

      if(
        (l1.getMin() != l2.getMin()) ||
        (l1.getMax() != l2.getMax())
      ) {
        // not equivalent blocks so not equal
        return false;
      }
    }

    // One of the locations had more blocks than the other
    if(i1.hasNext() || i2.hasNext()) {
      return false;
    }

    // Same number of blocks, all equivalent. Must be equal.
    return true;
  }

  /**
   * Create a Location instance from the list of contiguous locations in
   * locList.
   * <P>
   * If the list is empty then Location.empty will be produced. If it is just
   * one element long, then this will be returned as-is. If there are multiple
   * locations then they will be sorted and then used to construct a
   * CompoundLocation.
   *
   * @param locList a List<Location>, where each element is a contiguous location.
   * @return a new Location instance
   */
  static Location buildLoc(List locList) {
    Collections.sort(locList, Location.naturalOrder);

    if(locList.size() == 0) {
      return Location.empty;
    } else if(locList.size() == 1) {
      return (Location) locList.get(0);
    } else {
      return new CompoundLocation(locList);
    }
  }

    /**
     * The n-way union of a Collection of locations.  Returns a Location
     * which covers every point covered by at least one of the locations
     * in <code>locs</code>
     *
     * @param locs A collection of locations.
     * @return A union location
     * @throws ClassCastException if the collection contains non-Location objects.
     */

    public static Location union(Collection locs) {
	List locList = new ArrayList();
	for (Iterator li = locs.iterator(); li.hasNext(); ) {
	    Location loc = (Location) li.next();
	    for (Iterator bi = loc.blockIterator(); bi.hasNext(); ) {
		locList.add(bi.next());
	    }
	}

	return _union(locList);
    }


    private static Location _union(List locList) {
      // sort these blocks
      Collections.sort(locList, Location.naturalOrder);

      // merge into this list...
      List joinList = new ArrayList();

      // start iterating over sorted list.
      // last is used as loop variable. We must be careful about zero lengthed
      // lists and also careful to merge overlaps before adding to joinList.
      Iterator i = locList.iterator();
      Location last = Location.empty;

      // prime last
      if(i.hasNext()) {
        last = (Location) i.next();
      }

      // merge or add last with next location
      while(i.hasNext()) {
        Location cur = (Location) i.next();
        if(last.overlaps(cur)) {
          int min = Math.min(last.getMin(), cur.getMin());
          int max = Math.max(last.getMax(), cur.getMax());
          last = buildLoc(min, max);
        } else {
          joinList.add(last);
          last = cur;
        }
      }

      // handle the end of the loop
      if(last == Location.empty) {
        return Location.empty;
      } else {
        joinList.add(last);
      }

      // now make the apropreate Location instance
      return buildLoc(joinList);
    }

  /**
   * Return a contiguous Location from min to max.
   * <P>
   * If min == max then a PointLocation will be made, otherwise, a RangeLocation
   * will be returned.
   *
   * @param min  the Location min value
   * @param max  the Location max value
   * @return a new Location from min to max
   */
  protected static Location buildLoc(int min, int max) {
    if(min == max) {
      return new PointLocation(min);
    } else {
      return new RangeLocation(min, max);
    }
  }

  /**
   * Checks if the location has a decorator.
   *
   * @todo Currently this method walks through circular and between
   * decorators.  This is crude and ugly.
   * @param theLocation The location to test for decorators
   * @return True if the location has a decorator and false otherwise
   */
  protected static boolean isDecorated(Location theLocation)
  {
  	// If you know a cleaner way to do this, please change it and drop me a line
  	// gcox@netgenics.com
	boolean hasCircular = theLocation.getDecorator(CircularLocation.class) != null;
	boolean hasBetween = theLocation.getDecorator(BetweenLocation.class) != null;

  	return(hasCircular || hasBetween);
  }

  /**
   * Short answer: We don't.  This method logs a message and returns the empty
   * location
   *
   * @todo Handle decorations.
   */
  protected static void handleDecorations()
  {
	throw new ClassCastException("Decorated locations are not handled in this version");
  }
}
