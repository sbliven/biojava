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

/**
 * A simple implementation of Location that contains all points between
 * getMin and getMax inclusive.
 */
public class RangeLocation implements Location {
  /**
   * The minimum point contained.
   */
  private int min;

  /**
   * The maximum point contained.
   */
  private int max;

  public int getMin() {
    return min;
  }

  public int getMax() {
    return max;
  }

  public boolean overlaps(Location l) {
    return !(getMin() > l.getMax() ||
             getMax() < l.getMin());
  }

  public boolean contains(Location l) {
    return getMin() <= l.getMin() &&
           getMax() >= l.getMax();
  }

  public boolean contains(int p) {
    return getMin() <= p &&
           getMax() >= p;
  }

  public boolean equals(Location l) {
    return getMin() == l.getMin() &&
           getMax() == l.getMax() && l.contains(this);
  }

  public Location intersection(Location l) {
    int start = Math.max(getMin(), l.getMin());
    int end = Math.min(getMax(), l.getMax());

    if(start <= end)
      return new RangeLocation(start, end);
    else
      return Location.empty;
  }

  public Location union(Location l) {
    int start = Math.min(getMin(), l.getMin());
    int end = Math.max(getMax(), l.getMax());

    return new RangeLocation(start, end);
  }

  public ResidueList residues(ResidueList seq) {
    return seq.subList(getMin(), getMax());
  }

  public Location translate(int dist) {
    return new RangeLocation(getMin() + dist, getMax() + dist);
  }
  
  public RangeLocation(int min, int max) {
    this.min = min;
    this.max = max;
  }

  public String toString() {
    return getMin() + ", " + getMax();
  }
}
