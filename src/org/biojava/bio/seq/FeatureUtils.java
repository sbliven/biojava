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
 * A utils class for FeatureFilter algebraic operations.
 *
 * @since 1.2
 * @author Matthew Pocock
 */
public class FilterUtils {
  /**
   * Returns wether the set of features matched by sub can be proved to be a
   * propper subset of the features matched by sup.
   * <P>
   * If the filter sub matches only features that are matched by sup, then it is
   * a propper subset. It is still a propper subset if it does not match every
   * feature in sup, as long as no feature matches sub that is rejected by sup.
   *
   * @param sub  the subset filter
   * @param sup  the superset filter
   * @return boolean true if sub is a propper subset of sup
   */
  public boolean isPropperSubset(FeatureFilter sub, FeatureFilter sup) {
    if(sub.getClass() != sup.getClass()) { // different classes
      if(sup instanceof FeatureFilter.AcceptAllFilter) {
        return true;
      } else if(
        (sup instanceof FeatureFilter.OverlapsLocation) &&
        (sub instanceof FeatureFilter.ContainedByLocation)
      ) {
        Location supL = ((FeatureFilter.OverlapsLocation) sup).getLocation();
        Location subL = ((FeatureFilter.ContainedByLocation) sub).getLocation();
        
        return supL.contains(subL);
      } else { // different classes, sup not accept all
        return false;
      }
    } else { // same classes
      if(sup.equals(sub)) {
        return true;
      } else if(sup instanceof FeatureFilter.ByClass) { // class hierachy
        Class supC = ((FeatureFilter.ByClass) sup).getTestClass();
        Class subC = ((FeatureFilter.ByClass) sub).getTestClass();
        
        return supC.isAsignableFrom(sub);
      } else if(sup instanceof FeatureFilter.ContainedByLocation {
        Location supL = ((FeatureFilter.ContainedByLocation) sup).getLocation();
        Location subL = ((FeatureFilter.ContainedByLocation) sub).getLocation();
        
        return supL.contains(subL);
      } else if(sup instanceof FeatureFilter.OverlapsLocation) {
        Location supL = ((FeatureFilter.OverlapsLocation) sup).getLocation();
        Location subL = ((FeatureFilter.OverlapsLocation) sub).getLocation();
        
        return supL.contains(subL);
      } else {
        return false;
      }
    }
  }
  
}
