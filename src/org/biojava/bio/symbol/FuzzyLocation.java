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
public class FuzzyLocation implements Location {
  private final Location parent;
  private final boolean fuzzyMin;
  private final boolean fuzzyMax;
  
  /**
   * Create a new FuzzyLocation that decorates 'parent' with a potentialy
   * fuzzy min or max value.
   *
   * @param parent   the Location to decorate
   * @param fuzzyMin true if getMin represents a fuzzy location, false
   *                 otherwise
   * @param fuzzyMax true if getMax represents a fuzzy location, false
   *                 otherwise
   */
  public FuzzyLocation(Location parent, boolean fuzzyMin, boolean fuzzyMax) {
    this.parent = parent;
    this.fuzzyMin = fuzzyMin;
    this.fuzzyMax = fuzzyMax;
  }
  
  /**
   * Retrieve the Location that this decorates.
   *
   * @return the Location instance that stores all of the Loctaion interface
   *         data
   */
  public Location getParent() {
    return parent;
  }
  
  /**
   * Find out if the getMin vaule is fuzzy or not.
   *
   * @return true if getMin should be treated as fuzzy, false otherwise
   */
  public boolean getFuzzyMin() {
    return fuzzyMin;
  }
      
  /**
   * Find out if the getMax vaule is fuzzy or not.
   *
   * @return true if getMax should be treated as fuzzy, false otherwise
   */
  public boolean getFuzzyMax() {
    return fuzzyMax;
  }

  public int getMin() {
    return getParent().getMin();
  }

  public int getMax() {
    return getParent().getMax();
  }

  public boolean overlaps(Location l) {
    return getParent().overlaps(l);
  }

  public boolean contains(Location l) {
    return getParent().contains(l);
  }

  public boolean contains(int p) {
    return getParent().contains(p);
  }
  
  public boolean equals(Object l) {
    return getParent().equals(l);
  }

  public Location intersection(Location l) {
    return getParent().intersection(l);
  }

  public Location union(Location l) {
    return getParent().union(l);
  }

  public SymbolList symbols(SymbolList seq) {
    return getParent().symbols(seq);
  }

  public Location translate(int dist) {
    return getParent().translate(dist);
  }
   
  public boolean isContiguous() {
    return getParent().isContiguous();
  }
  
  public Iterator blockIterator() {
    return getParent().blockIterator();
  }
}
