package org.biojava.bio;

import org.biojava.bio.symbol.*;

/**
 * A constraint on the number of values a property can have.
 *
 * @author Matthew Pocock
 * @since 1.3
 */
public final class CardinalityConstraint {
  public static final Location NONE
    = Location.empty;
  public static final Location ZERO
    = new RangeLocation(0, 0);
  public static final Location ZERO_OR_ONE
    = new RangeLocation(0, 1);
  public static final Location ANY
    = new RangeLocation(0, Integer.MAX_VALUE);
  public static final Location ONE
    = new RangeLocation(1, 1);
  public static final Location ONE_OR_MORE
    = new RangeLocation(1, Integer.MAX_VALUE);
  
  private CardinalityConstraint() {}
}
