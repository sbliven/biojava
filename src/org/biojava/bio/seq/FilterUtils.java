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
  
  /**
   * Takes a feature filter and returns the reverse-polish representation of the
   * tree.
   * <P>
   * The list is traversed from left to right. Each atomic filter can be
   * evaluated directly to be replaced by a set of features. Each logical
   * operator grabs the required number of result sets from emediately before it
   * in the list and replaces itself and these with the result of it acting
   * upon these sets. In the end, the list should be left with a sing result
   * set which contains all matching features.
   * <P>
   * By definition, the attomic filter operations can be performed in any order.
   *
   * @param filt the FeatureFilter to flatten
   * @return a List of FeatureFilter instances
   */
  public static List reversePolish(FeatureFilter filt) {
    List polish = new ArrayList();
    
    reversePolish(polish, filt);
    
    return polish;
  }
  
  /**
   * Recurse over the tree in filt, adding things to polish as we go.
   */
  private static reversePolish(List polish, filt) {
    if(filt instanceof FeatureFilter.And) {
      FeatureFilter.And and = (FeatureFilter.And) filt;
      FeatureFilter c1 = and.getChild1();
      FeatureFilter c2 = and.getChild2();
      
      reversePolish(polish, c1);
      reversePolish(polish, c2);
    } else if(filt instanceof FeatureFilter.AndNot) {
      FeatureFilter.AndNot andNot = (FeatureFilter.AndNot) filt;
      FeatureFilter c1 = andNot.getChild1();
      FeatureFilter c2 = andNot.getChild2();
      
      reversePolish(polish, c1);
      reversePolish(polish, c2);
    } else if(filt instanceof FeatureFilter.Or) {
      FeatureFilter.Or or = (FeatureFilter.AndNot) filt;
      FeatureFilter c1 = or.getChild1();
      FeatureFilter c2 = or.getChild2();
      
      reversePolish(polish, c1);
      reversePolish(polish, c2);
    } else if(filt instanceof FeatureFilter.Not) {
      FeatureFilter.Not not = (FeatureFilter.Not) filt;
      FeatureFilter c = not.getChild();
      
      reversePolish(polish, c);
    }

    polish.add(filt);
  }
}
