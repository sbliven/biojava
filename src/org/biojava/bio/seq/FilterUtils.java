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

import org.biojava.bio.AnnotationType;
import org.biojava.bio.AnnotationTools;
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
      // Preconditions
      
      if (sub == null) {
        throw new NullPointerException("Null FeatureFilter: sub");
      }
      if (sup == null) {
        throw new NullPointerException("Null FeatureFilter: sup");
      }
      
      // Body
      
      if(sub.equals(sup)) {
        return true;
      }
      
      if (sup instanceof FeatureFilter.AcceptAllFilter) {
        return true;
      } else if (sub instanceof FeatureFilter.AcceptNoneFilter) {
        return true;
      } else if (sup instanceof FeatureFilter.And) {
        FeatureFilter.And and_sup = (FeatureFilter.And) sup;
        return areProperSubset(sub, and_sup.getChild1()) && areProperSubset(sub, and_sup.getChild2());
      } else if (sub instanceof FeatureFilter.And) {
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
     * Determines is two queries can be proven to be disjoint.
     * <p>
     * They are disjoint if there is no element that is matched by both filters
     * - that is, they have an empty intersection.  Order of arguments to this
     * method is not significant.
     * </p>
     *
     * @param a   the first FeatureFilter
     * @param b   the second FeatureFilter
     * @return <code>true</code> if they are proved to be disjoint, <code>false</code> otherwise
     */

    public static boolean areDisjoint(FeatureFilter a, FeatureFilter b) {
      // Preconditions
      
      if (a == null) {
        throw new NullPointerException("Null FeatureFilter: a");
      }
      if (b == null) {
        throw new NullPointerException("Null FeatureFilter: b");
      }
      
      // Body
      
      if(a.equals(b)) {
        return false;
      }
      
      if (a instanceof FeatureFilter.AcceptAllFilter) {
        return areProperSubset(b, FeatureFilter.none);
      } else if(b instanceof FeatureFilter.AcceptAllFilter) {
        return areProperSubset(a, FeatureFilter.none);
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
    
  public final static FeatureFilter byType(String type) {
    return new FeatureFilter.ByType(type);
  }
  
  public final static FeatureFilter bySource(String source) {
    return new FeatureFilter.BySource(source);
  }
  
  public final static FeatureFilter byClass(Class clazz)
  throws ClassCastException {
    return new FeatureFilter.ByClass(clazz);
  }
  
  public final static FeatureFilter containedByLocation(Location loc) {
    return new FeatureFilter.ContainedByLocation(loc);
  }
  
  public final static FeatureFilter overlapsLocation(Location loc) {
    return new FeatureFilter.OverlapsLocation(loc);
  }
  
  public final static FeatureFilter not(FeatureFilter filter) {
    return new FeatureFilter.Not(filter);
  }
  
  public final static FeatureFilter and(FeatureFilter c1, FeatureFilter c2) {
    return new FeatureFilter.And(c1, c2);
  }
  
  public final static FeatureFilter and(FeatureFilter[] filters) {
    if(filters.length == 0) {
      return none();
    } else if(filters.length == 1) {
      return filters[0];
    } else {
      FeatureFilter f = and(filters[0], filters[1]);
      for(int i = 2; i < filters.length; i++) {
        f = and(f, filters[i]);
      }
      return f;
    }
  }
  
  public final static FeatureFilter or(FeatureFilter c1, FeatureFilter c2) {
    return new FeatureFilter.Or(c1, c2);
  }
  
  public final static FeatureFilter or(FeatureFilter[] filters) {
    if(filters.length == 0) {
      return none();
    } else if(filters.length == 1) {
      return filters[0];
    } else {
      FeatureFilter f = or(filters[0], filters[1]);
      for(int i = 2; i < filters.length; i++) {
        f = and(f, filters[i]);
      }
      return f;
    }
  }
  
  public final static FeatureFilter byAnnotationType(AnnotationType type) {
    return new FeatureFilter.ByAnnotationType(type);
  }
  
  public final static FeatureFilter byAnnotation(Object key, Object value) {
    return new FeatureFilter.ByAnnotation(key, value);
  }
  
  public final static FeatureFilter hasAnnotation(Object key) {
    return new FeatureFilter.HasAnnotation(key);
  }
  
  public final static FeatureFilter byStrand(StrandedFeature.Strand strand) {
    return new FeatureFilter.StrandFilter(strand);
  }
  
  public final static FeatureFilter byParent(FeatureFilter parentFilter) {
    return new FeatureFilter.ByParent(parentFilter);
  }
  
  public final static FeatureFilter byAncestor(FeatureFilter ancestorFilter) {
    return new FeatureFilter.ByAncestor(ancestorFilter);
  }
  
  public final static FeatureFilter byFrame(FramedFeature.ReadingFrame frame) {
    return new FeatureFilter.FrameFilter(frame);
  }
  
  public final static FeatureFilter byPairwiseScore(double minScore, double maxScore) {
    return new FeatureFilter.ByPairwiseScore(minScore, maxScore);
  }
  
  public final static FeatureFilter byComponentName(String compName) {
    return new FeatureFilter.ByComponentName(compName);
  }
  
  public final static FeatureFilter topLevel() {
    return FeatureFilter.top_level;
  }
  
  public final static FeatureFilter all() {
    return FeatureFilter.all;
  }
  
  public final static FeatureFilter none() {
    return FeatureFilter.none;
  }
  
  public final static FeatureFilter optimize(FeatureFilter filter) {
    if(filter instanceof FeatureFilter.And) {
      List filters = new ArrayList();
      expandAnd(filter, filters);
      
      int i = 0;
      int j = 0;
      
      do {
        FeatureFilter aa = (FeatureFilter) filters.get(i);
        FeatureFilter bb = (FeatureFilter) filters.get(j);
        
        if(!(aa instanceof OptimizableFilter)) {
          i++;
          j = i + 1;
        } else if(!(bb instanceof OptimizableFilter)) {
          j++;
          if(j == filters.size()) {
            i++;
            j = i + 1;
          }
        } else {
          OptimizableFilter a = (OptimizableFilter) aa;
          OptimizableFilter b = (OptimizableFilter) bb;

          if(a.isDisjoint(b)) {
            // a n b = E
            return none();
          } else if(a.isProperSubset(b)) {
            // if a < b then a n b = a  
            filters.remove(j);
          } else if(b.isProperSubset(a)) {
            // if a > b then a n b = b
            filters.remove(i);
            j = i + 1;
          } else {
            FeatureFilter intersect = intersection(a, b);
            if(intersect != null) {
              filters.set(i, intersect);
              filters.remove(j);
              j = i + 1;
            } else {
              j++;
              if(j == filters.size()) {
                i++;
                j = i + 1;
              }
            }
          }
        }
      } while(i < filters.size() - 1);
      return and((FeatureFilter[]) filters.toArray(new Feature[] {}));
    } else if(filter instanceof FeatureFilter.Or) {
      List filters = new ArrayList();
      expandOr(filter, filters);
      
      int i = 0;
      int j = 0;
      
      do {
        FeatureFilter aa = (FeatureFilter) filters.get(i);
        FeatureFilter bb = (FeatureFilter) filters.get(j);
        
        if(!(aa instanceof OptimizableFilter)) {
          i++;
          j = i + 1;
        } else if(!(bb instanceof OptimizableFilter)) {
          j++;
          if(j == filters.size()) {
            i++;
            j = i + 1;
          }
        } else {
          OptimizableFilter a = (OptimizableFilter) aa;
          OptimizableFilter b = (OptimizableFilter) bb;
          
          if(a == all() || b == all()) {
            return all();
          } else if(a.isProperSubset(b)) {
            filters.remove(i);
            j = i + 1;
          } else if(b.isProperSubset(a)) {
            filters.remove(j);
          } else {
            FeatureFilter union = union(a, b);
            if(union != null) {
              filters.set(i, union);
              filters.remove(j);
              j = i + 1;
            }
          }
        }
      } while(i < filters.size() - 1);
      return or((FeatureFilter[] ) filters.toArray(new Feature[] {}));
    } else if(filter instanceof FeatureFilter.Not) {
      FeatureFilter.Not not = (FeatureFilter.Not) filter;
      return not(optimize(not.getChild()));
    } else {
      return filter;
    }
  }
  
  private static FeatureFilter intersection(FeatureFilter f1, FeatureFilter f2) {
    if(
      f1 instanceof FeatureFilter.ContainedByLocation &&
      f2 instanceof FeatureFilter.ContainedByLocation
    ) {
      Location loc = LocationTools.intersection(
        ((FeatureFilter.ContainedByLocation) f1).getLocation(),
        ((FeatureFilter.ContainedByLocation) f2).getLocation()
      );
      if(loc == Location.empty) {
        return none();
      } else {
        return containedByLocation(loc);
      }
    } else if(
      f1 instanceof FeatureFilter.OverlapsLocation &&
      f2 instanceof FeatureFilter.OverlapsLocation
    ) {
      // can't do much here
    } else if(
      f1 instanceof FeatureFilter.ByAnnotationType &&
      f2 instanceof FeatureFilter.ByAnnotationType
    ) {
      FeatureFilter.ByAnnotationType f1t = (FeatureFilter.ByAnnotationType) f1;
      FeatureFilter.ByAnnotationType f2t = (FeatureFilter.ByAnnotationType) f2;
      
      AnnotationType intersect = AnnotationTools.intersection(
        f1t.getType(),
        f2t.getType()
      );
      
      if(intersect == AnnotationType.NONE) {
        return none();
      } else {
        return byAnnotationType(intersect);
      }
    } else if(
      f1 instanceof ByHierachy &&
      f2 instanceof ByHierachy
    ) {
      ByHierachy f1h = (ByHierachy) f1;
      ByHierachy f2h = (ByHierachy) f2;
      
      FeatureFilter filt = optimize(and(f1h.getFilter(), f2h.getFilter()));
      if(
        f1h instanceof FeatureFilter.ByParent ||
        f2h instanceof FeatureFilter.ByParent
      ) {
        return byParent(filt);
      } else {
        return byAncestor(filt);
      }
    }
    
    return null;
  }
  
  private static FeatureFilter union(FeatureFilter f1, FeatureFilter f2) {
    if(
      f1 instanceof FeatureFilter.ContainedByLocation &&
      f2 instanceof FeatureFilter.ContainedByLocation
    ) {
      return containedByLocation(LocationTools.union(
        ((FeatureFilter.ContainedByLocation) f1).getLocation(),
        ((FeatureFilter.ContainedByLocation) f2).getLocation()
      ));
    } else if(
      f1 instanceof FeatureFilter.OverlapsLocation &&
      f2 instanceof FeatureFilter.OverlapsLocation
    ) {
      return overlapsLocation(LocationTools.intersection(
        ((FeatureFilter.OverlapsLocation) f1).getLocation(),
        ((FeatureFilter.OverlapsLocation) f2).getLocation()
      ));
    } else if(
      f1 instanceof FeatureFilter.ByAnnotationType &&
      f2 instanceof FeatureFilter.ByAnnotationType
    ) {
      FeatureFilter.ByAnnotationType f1t = (FeatureFilter.ByAnnotationType) f1;
      FeatureFilter.ByAnnotationType f2t = (FeatureFilter.ByAnnotationType) f2;
      
      AnnotationType union = AnnotationTools.union(
        f1t.getType(),
        f2t.getType()
      );
      
      return byAnnotationType(union);
    } else if(
      f1 instanceof ByHierachy &&
      f2 instanceof ByHierachy
    ) {
      ByHierachy f1h = (ByHierachy) f1;
      ByHierachy f2h = (ByHierachy) f2;
      
      FeatureFilter filt = optimize(or(f1h.getFilter(), f2h.getFilter()));
      if(
        f1h instanceof FeatureFilter.ByAncestor ||
        f2h instanceof FeatureFilter.ByAncestor
      ) {
        return byAncestor(filt);
      } else {
        return byParent(filt);
      }
    }
    
    return null;
  }
  
  private static void expandAnd(FeatureFilter filt, List filters) {
    if(filt instanceof FeatureFilter.And) {
      FeatureFilter.And and = (FeatureFilter.And) filt;
      expandAnd(and.getChild1(), filters);
      expandAnd(and.getChild2(), filters);
    } else {
      filters.add(filt);
    }
  }
  
  private static void expandOr(FeatureFilter filt, List filters) {
    if(filt instanceof FeatureFilter.Or) {
      FeatureFilter.Or or = (FeatureFilter.Or) filt;
      expandOr(or.getChild1(), filters);
      expandOr(or.getChild2(), filters);
    } else {
      filters.add(filt);
    }
  }
}
