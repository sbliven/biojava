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

import org.biojava.bio.symbol.Location;

/**
 * A utils class for FeatureFilter algebraic operations.
 *
 * @since 1.2
 * @author Matthew Pocock
 */
public class FilterUtils {
  /**
   * Returns wether the set of features matched by sub can be proven to be a
   * propper subset of the features matched by sup.
   * <P>
   * If the filter sub matches only features that are matched by sup, then it is
   * a propper subset. It is still a propper subset if it does not match every
   * feature in sup, as long as no feature matches sub that is rejected by sup.
   *
   * @param sub  the subset filter
   * @param sup  the superset filter
   * @return boolean true if sub is a proper subset of sup
   */
  public static boolean areProperSubset(FeatureFilter sub, FeatureFilter sup) {
    if(sub.equals(sup)) {
      return true;
    }
    
    if(sub instanceof OptimizableFilter) {
      return ((OptimizableFilter) sub).isProperSubset(sup);
    }
    
    return false;
  }
  
  /**
   * Returns wether the two queries can be proven to be disjoint.
   * <P>
   * They are disjoint if there is no element that is matched by both filters
   * - that is, they have an empty intersection.
   *
   * @param a   the first FeatureFilter
   * @param b   the second FeatureFilter
   * @return true if they a proved to be disjoint, false otherwise
   */
  public static boolean areDisjoint(FeatureFilter a, FeatureFilter b) {
    if(a.equals(b)) {
      return false;
    }
    
    if(a instanceof OptimizableFilter) {
      return ((OptimizableFilter) a).isDisjoint(b);
    }
    
    return false;
  }
}
