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

import org.biojava.utils.ChangeVetoException;
import org.biojava.bio.BioException;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.LocationTools;

/**
 * A set of FeatureFilter algebraic operations.
 *
 * @since 1.2
 * @author Matthew Pocock
 * @author Thomas Down
 */

public class FilterUtils {
    /**
     * Determines if the set of features matched by sub can be <code>proven</code> to be a
     * proper subset of the features matched by sup.
     * <p>
     * If the filter sub matches only features that are matched by sup, then it is
     * a proper subset. It is still a proper subset if it does not match every
     * feature in sup, as long as no feature matches sub that is rejected by sup.
     * </p>
     *
     * @param sub the subset filter
     * @param sup the superset filter
     * @return <code>true</code> if <code>sub</code> is a proper subset of <code>sup</code>
     */

    public static boolean areProperSubset(FeatureFilter sub, FeatureFilter sup) {
	if(sub.equals(sup)) {
	    return true;
	}
	
	if (sup instanceof FeatureFilter.AcceptAllFilter) {
	    return true;
	} else if (sub instanceof FeatureFilter.AcceptAllFilter) {
	    return false;
	} else if (sub instanceof FeatureFilter.AcceptNoneFilter) {
	    return false;
	} else if (sup instanceof FeatureFilter.AcceptNoneFilter) {
	    return false;
	} else if (sup instanceof FeatureFilter.And) {
	    FeatureFilter.And and_sup = (FeatureFilter.And) sup;
	    return areProperSubset(sub, and_sup.getChild1()) && areProperSubset(sub, and_sup.getChild2());
	} else if (sub instanceof FeatureFilter.And) {
	    // is this sufficient for complex and/or structures?

	    FeatureFilter.And and_sub = (FeatureFilter.And) sub;
	    return areProperSubset(and_sub.getChild1(), sup) || areProperSubset(and_sub.getChild2(), sup);
	} else if (sub instanceof FeatureFilter.Or) {
	    FeatureFilter.Or or_sub = (FeatureFilter.Or) sub;
	    return areProperSubset(or_sub.getChild1(), sup) && areProperSubset(or_sub.getChild2(), sup);
	} else if (sup instanceof FeatureFilter.Or) {
	    FeatureFilter.Or or_sup = (FeatureFilter.Or) sup;
	    return areProperSubset(sub, or_sup.getChild1()) || areProperSubset(sub, or_sup.getChild2());
	} else if (sup instanceof FeatureFilter.Not) {
	    FeatureFilter not_sup = ((FeatureFilter.Not) sup).getChild();
	    return areDisjoint(sub, not_sup);
	} else if (sub instanceof FeatureFilter.Not) {
	    // How do we prove this one?
	} else if (sub instanceof OptimizableFilter) {
	    return ((OptimizableFilter) sub).isProperSubset(sup);
	}
    
	return false;
    }
  
    /**
     * Determines the two queries can be proven to be disjoint.
     * <p>
     * They are disjoint if there is no element that is matched by both filters
     * - that is, they have an empty intersection.
     * </p>
     *
     * @param a   the first FeatureFilter
     * @param b   the second FeatureFilter
     * @return <code>true</code> if they a proved to be disjoint, <code>false</code> otherwise
     */

    public static boolean areDisjoint(FeatureFilter a, FeatureFilter b) {
	if(a.equals(b)) {
	    return false;
	}
	
	if (a instanceof FeatureFilter.AcceptAllFilter || b instanceof FeatureFilter.AcceptAllFilter) {
	    return false;
	} else if (a instanceof FeatureFilter.AcceptNoneFilter || b instanceof FeatureFilter.AcceptNoneFilter) {
	    return true;
	} if (a instanceof FeatureFilter.And) {
	    FeatureFilter.And and_a = (FeatureFilter.And) a;
	    return areDisjoint(and_a.getChild1(), b) || areDisjoint(and_a.getChild2(), b);
	} else if (b instanceof FeatureFilter.And) {
	    FeatureFilter.And and_b = (FeatureFilter.And) b;
	    return areDisjoint(a, and_b.getChild1()) || areDisjoint(a, and_b.getChild2());
	} else if (a instanceof FeatureFilter.Or) {
	    FeatureFilter.Or or_a = (FeatureFilter.Or) a;
	    return areDisjoint(or_a.getChild1(), b) && areDisjoint(or_a.getChild2(), b);
	} else if (b instanceof FeatureFilter.Or) {
	    FeatureFilter.Or or_b = (FeatureFilter.Or) b;
	    return areDisjoint(a, or_b.getChild1()) && areDisjoint(a, or_b.getChild2());
	} else if (a instanceof FeatureFilter.Not) {
	    FeatureFilter not_a = ((FeatureFilter.Not) a).getChild();
	    return areProperSubset(b, not_a);
	} else if (b instanceof FeatureFilter.Not) {
	    FeatureFilter not_b = ((FeatureFilter.Not) b).getChild();
	    return areProperSubset(a, not_b);
	} else if (a instanceof OptimizableFilter) {
	    return ((OptimizableFilter) a).isDisjoint(b);
	} else if (b instanceof OptimizableFilter) {
	    return ((OptimizableFilter) b).isDisjoint(a);
	}

	// *SIGH* we don't have a proof here...

	return false;
    }
    
    /**
     * Try to determine the minimal location which all features matching a given
     * filter must overlap.
     *
     * @param ff A feature filter
     * @return the minimal location which any features matching <code>ff</code>
     *          must overlap, or <code>null</code> if no proof is possible
     *          (normally indicates that the filter has nothing to do with
     *          location).
     * @since 1.2
     */

    public static Location extractOverlappingLocation(FeatureFilter ff) {
	if (ff instanceof FeatureFilter.OverlapsLocation) {
	    return ((FeatureFilter.OverlapsLocation) ff).getLocation();
	} else if (ff instanceof FeatureFilter.ContainedByLocation) {
	    return ((FeatureFilter.ContainedByLocation) ff).getLocation();
	} else if (ff instanceof FeatureFilter.And) {
	    FeatureFilter.And ffa = (FeatureFilter.And) ff;
	    Location l1 = extractOverlappingLocation(ffa.getChild1());
	    Location l2 = extractOverlappingLocation(ffa.getChild2());

	    if (l1 != null) {
		if (l2 != null) {
		    return l1.intersection(l2);
		} else {
		    return l1;
		}
	    } else {
		if (l2 != null) {
		    return l2;
		} else {
		    return null;
		}
	    }
	} else if (ff instanceof FeatureFilter.Or) {
	    FeatureFilter.Or ffo = (FeatureFilter.Or) ff;
	    Location l1 = extractOverlappingLocation(ffo.getChild1());
	    Location l2 = extractOverlappingLocation(ffo.getChild2());
	    
	    if (l1 != null && l2 != null) {
		return LocationTools.union(l1, l2);
	    }
	}

	return null;
    }
  
  /**
   * Takes a feature filter and returns the reverse-polish representation of the
   * tree.
   * <p>
   * The list is traversed from left to right. Each atomic filter can be
   * evaluated directly to be replaced by a set of features. Each logical
   * operator grabs the required number of result sets from immediately before it
   * in the list and replaces itself and these with the result of it acting
   * upon these sets. In the end, the list should be left with a sing result
   * set which contains all matching features.
   * <p>
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
  private static void reversePolish(List polish, FeatureFilter filt) {
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
      FeatureFilter.Or or = (FeatureFilter.Or) filt;
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
  
  public FeatureHolder evaluate(FeatureFilter filt, FeatureHolder fh)
  throws BioException {
    return evaluatePolish(reversePolish(filt), fh);
  }
  
  public FeatureHolder evaluatePolish(List polish, FeatureHolder fh)
  throws BioException {
    try {
      Stack stack = new Stack();
      
      for (Iterator i = polish.iterator(); i.hasNext(); ) {
        FeatureFilter filt = (FeatureFilter) i.next();
        
        if(filt instanceof FeatureFilter.And) {
          SimpleFeatureHolder result = new SimpleFeatureHolder();
          FeatureHolder b = (FeatureHolder) stack.pop();
          FeatureHolder a = (FeatureHolder) stack.pop();
          
          // largest set in a
          if(a.countFeatures() < b.countFeatures()) {
            FeatureHolder t = a;
            a = b;
            b = t;
          }
          
          for(
            Iterator fi = b.features();
            fi.hasNext();
          ) {
            Feature f = (Feature) fi.next();
            if(a.containsFeature(f)) {
              result.addFeature(f);
            }
          }
          
          stack.push(result);
        } else if(filt instanceof FeatureFilter.AndNot) {
          FeatureHolder b = (FeatureHolder) stack.pop();
          SimpleFeatureHolder a = (SimpleFeatureHolder) stack.peek();
          
          for(
            Iterator fi = b.features();
            fi.hasNext();
          ) {
            Feature f = (Feature) fi.next();
            if(a.containsFeature(f)) {
              a.removeFeature(f);
            }
          }
        } else if(filt instanceof FeatureFilter.Or) {
          FeatureHolder b = (FeatureHolder) stack.pop();
          SimpleFeatureHolder a = (SimpleFeatureHolder) stack.peek();
          
          for(
            Iterator fi = b.features();
            fi.hasNext();
          ) {
            Feature f = (Feature) fi.next();
            if(!a.containsFeature(f)) {
              a.addFeature(f);
            }
          }
        } else if(filt instanceof FeatureFilter.Not) {
          throw new IllegalArgumentException(
                "Can't evaluate Not - transform them into AndNot statements"
          );
        } else if(filt instanceof FilterWrapper) {
          stack.add(fh.filter(((FilterWrapper) filt).getWrapped(), false));
        } else {
          stack.add(fh.filter(filt, false));
        }
      }
      
      return (FeatureHolder) stack.pop();
    } catch (ChangeVetoException cve) {
      throw new BioException(cve, "Couldn't evaluate the query");
    }
  }
  
  private static class FilterWrapper implements FeatureFilter {
    private FeatureFilter wrapped;
    
    public FilterWrapper(FeatureFilter wrapped) {
      this.wrapped = wrapped;
    }
    
    public FeatureFilter getWrapped() {
      return wrapped;
    }
    
    public boolean accept(Feature f) {
      return wrapped.accept(f);
    }
  }
}
