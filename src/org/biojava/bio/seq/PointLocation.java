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

import java.util.*;

/**
 * A single residue.
 */
public class PointLocation implements Location {
  /**
   * The actual index contained.
   */
  private int point;

  public int getMin()	{ return point; }
  public int getMax()	{ return point; }
  public boolean overlaps(Location l)	{ return l.contains(this); }
  public boolean contains(Location l)	{ return (this.point == l.getMin()) &&
                                                   (this.point == l.getMax()); }
  public boolean contains(int p)	{ return this.point == p; }
  public boolean equals(Location l)	{ return this.contains(l) && l.contains(this); }
  public Location intersection(Location l) {
    return l.contains(this)
      ? this
      : Location.empty;
  }
  public Location union(Location l)	{
    CompoundLocation cl = new CompoundLocation();
    cl.addLocation(this);
    cl.addLocation(l);
    return cl;
  }

  public ResidueList residues(ResidueList s)	{
    final Residue res = s.residueAt(this.point);
    return new SimpleResidueList(s.alphabet(), new AbstractList() {
      public Object get(int index) throws IndexOutOfBoundsException {
        if(index == 0)
          return res;
        throw new IndexOutOfBoundsException("Index " + index + " greater than 0");
      }
      public int size() { return 1; }
    });
  }

  public Location translate(int dist) {
    return new PointLocation(this.point + dist);
  }
  
  public PointLocation(int point) {
    this.point = point;
  }
}
