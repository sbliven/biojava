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


package org.biojava.bio.seq;

import java.util.ArrayList;

/**
 * A biological location.
 * <P>
 * The location will contain some residues between getMin and getMax inclusive.
 * It is not required to contain all locations within this range. It is meant
 * to contain getMin or getMax. In the event that an operation would produce an
 * invalid or nonsensical range, empty should be returned.
 */
public interface Location {
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
   * it is also in the other. This is equivalent to a.contains(b) && b.contains(a).
   *
   * @param l	the Location to check
   * @return	true if this equals l, otherwise false
   */
  boolean equals(Location l);

  /**
   * Returns a Location that contains all points common to both ranges.
   * 
   * @param l	the Location to intersect with
   * @return	a Location containing all points common to both, or
   *              the empty range if there are no such points
   */
  Location intersection(Location l);
  /**
   * Return a Loctaion containing all points in either ranges.
   *
   * @param l	the Location to union with
   * @return	a Location representing the union
   */
  Location union(Location l);

  /**
   * Return the residues in a sequence that fall within this range.
   *
   * @param seq	the ResidueList to process
   * @return	the ResidueList containing the residues in seq in this range
   */
  ResidueList residues(ResidueList seq);

  /**
   * Create a location that is a translation of this location.
   *
   * @param dist  the distance to translate (to the right)
   */
  Location translate(int dist);
   
  /**
   * The empty range.
   * <P>
   * This object contains nothing. Its minimum value is Integer.MAX_VALUE.
   * Its maximum value is Integer.MIN_VALUE. It overlaps nothing. It is
   * equal to nothing. Intersection results in the empty range. Union
   * results in the argument range. Residues returns an empty array.
   * <P>
   * Every day, in every way, emty becomes more and more booring.
   */
  static final Location empty = new EmptyLocation();
  
  static final class EmptyLocation implements Location {
    public int getMin() { return Integer.MAX_VALUE; }
    public int getMax() { return Integer.MIN_VALUE; }
    public boolean overlaps(Location l) { return false; }
    public boolean contains(Location l) { return false; }
    public boolean contains(int p) { return false; }
    public boolean equals(Location l) { return false; }
    public Location intersection(Location l) { return empty; }
    public Location union(Location l) { return l; }
    public ResidueList residues(ResidueList seq) {
      return new SimpleResidueList(seq.alphabet(), new ArrayList());
    }
    public Location translate(int dist) { return this; }
  }
}
