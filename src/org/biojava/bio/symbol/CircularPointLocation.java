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
 * A single symbol on a CircularSequence.
 * <P>
 * min and max equal the location of the single symbol.
 *
 *
 * @author Mark Schreiber
 * @version 1.0
 * @since 1.1
 */

public class CircularPointLocation extends PointLocation {
  private int point;
  private int length;


  public CircularPointLocation(int point, int length) {
    super(realValue(point, length));
    this.point = realValue(point, length);
    this.length = length;
  }

  /**
   * Overrides PointLocation and returns a CircularPointLocation based on the
   * translation and the assumption that the underlying sequence is circular.
   */
  public Location translate(int dist) {
    return new CircularPointLocation(this.point + dist, length);
  }

  private static int realValue(int val, int length){
    val = ((val-1) % length) + 1;
    if(val < 0) val = length +1 + val;
    return val;
  }
}
