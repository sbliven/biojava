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
 */
public class FuzzyLocation
extends AbstractRangeLocation
implements Serializable {
  private int outerMin;
  private int innerMin;
  private int innerMax;
  private int outerMax;
  private RangeResolver resolver;
  
  /**
   * Create a new FuzzyLocation that decorates 'parent' with a potentialy
   * fuzzy min or max value.
   *
   * @param fuzzyMin true if getMin represents a fuzzy location, false
   *                 otherwise
   * @param fuzzyMax true if getMax represents a fuzzy location, false
   *                 otherwise
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
  
  /**
   * Retrieve the Location that this decorates.
   *
   * @return the Location instance that stores all of the Loctaion interface
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
  
  public static interface RangeResolver {
    public int resolveMin(FuzzyLocation loc);
    public int resolveMax(FuzzyLocation loc);
  }
}
