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
