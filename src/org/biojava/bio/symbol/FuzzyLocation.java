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
 * A 'fuzzy' location a-la Embl fuzzy locations.
 * <P>
 * Fuzzy locations have propreties that indicate that they may start before min
 * and end after max. However, this in no way affects how they interact with
 * other locations. In order that any location implementation can be treated
 * as fuzzy, FuzzyLocation is a simple decorator arround an underlying Location
 * implementation.
 * </p>
 *
 * @author Matthew Pocock
 * @author Thomas Down
 */
public class FuzzyLocation
extends AbstractRangeLocation
implements Serializable {
    /**
     * Always use the `inner' values.
     */

    public final static RangeResolver RESOLVE_INNER;

    /**
     * Use the `outer' values, unless they are unbounded in which case the
     * `inner' values are used.
     */

    public final static RangeResolver RESOLVE_OUTER;

    /**
     * Use the arithmetic mean of the `inner' and `outer' values, unless the
     * outer value is unbounded.
     */

    public final static RangeResolver RESOLVE_AVERAGE;

    static {
	RESOLVE_INNER = new InnerRangeResolver();
	RESOLVE_OUTER = new OuterRangeResolver();
	RESOLVE_AVERAGE = new AverageRangeResolver();
    }

    private int outerMin;
    private int innerMin;
    private int innerMax;
    private int outerMax;
    private RangeResolver resolver;
  
  /**
   * Create a new FuzzyLocation that decorates 'parent' with a potentially
   * fuzzy min or max value.
   *
   * @param outerMin the lower bound on the location's min value.  Integer.MIN_VALUE indicates
   *                 unbounded.
   * @param outerMax the upper bound on the location's max value.  Integer.MAX_VALUE indicates
   *                 unbounded.
   * @param innerMin the upper bound on the location's min value.
   * @param innerMax the lower bound on the location's max value.
   * @param resolver a RangeResolver object which defines the policy used to calculate
   *                 the location's min and max properties.
   */

  public FuzzyLocation(
    int outerMin, int outerMax,
    int innerMin, int innerMax,
    RangeResolver resolver
  ) {
    this.outerMin = outerMin;
    this.outerMax = outerMax;
    this.innerMin = innerMin;
    this.innerMax = innerMax;
    this.resolver = resolver;
  }
  
  public Location translate(int dist) {
    return new FuzzyLocation(
      outerMin + dist,
      outerMax + dist, 
      innerMin + dist,
      innerMax + dist,
      resolver
    );
  }
  
  /**
   * Retrieve the Location that this decorates.
   *
   * @return the Location instance that stores all of the Location interface
   *         data
   */

  public RangeResolver getResolver() {
    return resolver;
  }

  public int getOuterMin() {
    return outerMin;
  }
  

  public int getOuterMax() {
    return outerMax;
  }
  
  public int getInnerMin() {
    return innerMin;
  }
  

  public int getInnerMax() {
    return innerMax;
  }

  public int getMin() {
    return resolver.resolveMin(this);
  }

  public int getMax() {
    return resolver.resolveMax(this);
  }
  
  public boolean hasBoundedMin() {
    return outerMin != Integer.MIN_VALUE;
  }
  
  public boolean hasBoundedMax() {
    return outerMax != Integer.MAX_VALUE;
  }
  
    public String toString()
    {
	return "["
	    + (hasBoundedMin() ? Integer.toString(getMin()) : "<" + Integer.toString(getMin()))
	    + ", "
	    + (hasBoundedMax() ? Integer.toString(getMax()) : ">" + Integer.toString(getMax()))
	    + "]";
    }

  public static interface RangeResolver {
    public int resolveMin(FuzzyLocation loc);
    public int resolveMax(FuzzyLocation loc);
  }

    private static class InnerRangeResolver implements RangeResolver {
	public int resolveMin(FuzzyLocation loc) {
	    return loc.getInnerMin();
	}

	public int resolveMax(FuzzyLocation loc) {
	    return loc.getInnerMax();
	}
    }

    private static class OuterRangeResolver implements RangeResolver {
	public int resolveMin(FuzzyLocation loc) {
	    if (loc.hasBoundedMin()) {
		return loc.getOuterMin();
	    } else {
		return loc.getInnerMin();
	    }
	}

	public int resolveMax(FuzzyLocation loc) {
	    if (loc.hasBoundedMax()) {
		return loc.getOuterMax();
	    } else {
		return loc.getInnerMax();
	    }
	}
    }

    private static class AverageRangeResolver implements RangeResolver {
	public int resolveMin(FuzzyLocation loc) {
	    if (loc.hasBoundedMin()) {
		return loc.getOuterMin() + loc.getInnerMin() / 2;
	    } else {
		return loc.getInnerMin();
	    }
	}

	public int resolveMax(FuzzyLocation loc) {
	    if (loc.hasBoundedMax()) {
		return loc.getOuterMax() + loc.getInnerMax() / 2;
	    } else {
		return loc.getInnerMax();
	    }
	}
    }
}
