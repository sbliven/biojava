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

import java.io.Serializable;
import java.util.NoSuchElementException;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;

/**
 * A filter for accepting or rejecting a feature.
 * <P>
 * This may implement arbitrary rules, or be based upon the feature's
 * annotation, type, location or source.
 * <P>
 * If the filter is to be used in a remote process, it is recognized that it may
 * be serialized and sent over to run remotely, rather than each feature being
 * retrieved localy.
 *
 * @since 1.0
 * @author Matthew Pocock
 * @author Thomas Down
 */
public interface FeatureFilter extends Serializable {
  /**
   * This method determines whether a feature is to be accepted.
   *
   * @param f the Feature to evaluate
   * @return  true if this feature is to be selected in, or false if it is to be ignored
   */
  boolean accept(Feature f);

  /**
   * All features are selected in with this filter.
   */
  static final public FeatureFilter all = new AcceptAllFilter();
  
  /**
   * The class that accepts all features.
   * <P>
   * Use the FeatureFilter.all member.
   *
   * @author Thomas Down
   * @author Matthew Pocock
   * @since 1.0
   */
  public final static class AcceptAllFilter implements OptimizableFilter {
    public boolean accept(Feature f) { return true; }
    public boolean equals(Object o) {
      return o instanceof AcceptAllFilter;
    }
    public boolean isProperSubset(FeatureFilter sup) {
      return sup.equals(this);
    }
    
    public boolean isDisjoint(FeatureFilter filt) {
      return filt instanceof AcceptNoneFilter;
    }
  }

  /**
   * No features are selected in with this filter.
   */
  static final public FeatureFilter none = new AcceptNoneFilter();
  
  /**
   * The class that accepts no features.
   * <P>
   * Use the FeatureFilter.none member.
   *
   * @author Matthew Pocock
   * @since 1.2
   */
  public final static class AcceptNoneFilter implements OptimizableFilter {
    public boolean accept(Feature f) { return false; }
    public boolean equals(Object o) {
      return o instanceof AcceptNoneFilter;
    }
    public boolean isProperSubset(FeatureFilter sup) {
      return true;
    }
    
    public boolean isDisjoint(FeatureFilter filt) {
      return true;
    }
  }
  
  /**
   * Construct one of these to filter features by type.
   *
   * @author Matthew Pocock
   * @since 1.0
   */
  final public static class ByType implements OptimizableFilter {
    private String type;
    
    public String getType() {
      return type;
    }
    
    /**
     * Create a ByType filter that filters in all features with type fields
     * equal to type.
     *
     * @param type  the String to match type fields against
     */
    public ByType(String type) {
      this.type = type;
    }
    
    /**
     * Returns true if the feature has a matching type property.
     */
    public boolean accept(Feature f) {
      return type.equals(f.getType());
    }
    
    public boolean equals(Object o) {
      return
        (o instanceof ByType) &&
        (((ByType) o).getType() == this.getType());
    }
    
    public boolean isProperSubset(FeatureFilter sup) {
      return this.equals(sup) || (sup instanceof AcceptAllFilter);
    }
    
    public boolean isDisjoint(FeatureFilter filt) {
      return (filt instanceof AcceptNoneFilter) || (
        (filt instanceof ByType) &&
        !getType().equals(((ByType) filt).getType())
      );
    }
  }

  /**
   * Construct one of these to filter features by source.
   *
   * @author Matthew Pocock
   * @since 1.0
   */
  public final static class BySource implements OptimizableFilter {
    private String source;
    
    public String getSource() {
      return source;
    }
    
    /**
     * Create a BySource filter that filters in all features which have sources
     * equal to source.
     *
     * @param source  the String to match source fields against
     */
    public BySource(String source) {
      this.source = source;
    }
    
    public boolean accept(Feature f) { return source.equals(f.getSource()); }
    
    public boolean equals(Object o) {
      return
        (o instanceof BySource) &&
        (((BySource) o).getSource() == this.getSource());
    }

    public boolean isProperSubset(FeatureFilter sup) {
      return this.equals(sup) || (sup instanceof AcceptAllFilter);
    }

    public boolean isDisjoint(FeatureFilter filt) {
      return (filt instanceof AcceptNoneFilter) || (
        (filt instanceof BySource) &&
        !getSource().equals(((BySource) filt).getSource())
      );
    }
  }

  /**
   * Filter which accepts only those filters which are an instance
   * of a specific Java class
   *
   * @author Thomas Down
   * @author Matthew Pocock
   * @since 1.1
   */
   
  public final static class ByClass implements OptimizableFilter {
    private Class clazz;
    
    public ByClass(Class clazz) {
      this.clazz = clazz;
    }
    
    public boolean accept(Feature f) {
      return clazz.isInstance(f);
    }
    
    public Class getTestClass() {
      return clazz;
    }
    
    public boolean equals(Object o) {
      return
        (o instanceof ByClass) &&
        (((ByClass) o).getTestClass() == this.getTestClass());
    }
    
    public boolean isProperSubset(FeatureFilter sup) {
      if(sup instanceof ByClass) {
        Class supC = ((ByClass) sup).getTestClass();
        return supC.isAssignableFrom(this.getClass());
      }
      return (sup instanceof AcceptAllFilter);
    }
    
    public boolean isDisjoint(FeatureFilter feat) {
      if(feat instanceof ByClass) {
        Class featC = ((ByClass) feat).getClass();
        return
          ! (featC.isAssignableFrom(getClass())) &&
          ! (getClass().isAssignableFrom(featC));
      }
      return (feat instanceof AcceptNoneFilter);
    }
  }

  /**
   *  A filter that returns all features contained within a location.
   *
   * @author Matthew Pocock
   * @since 1.0
   */
  public final static class ContainedByLocation implements OptimizableFilter {
    private Location loc;

    public Location getLocation() {
      return loc;
    }
    
    /**
     * Creates a filter that returns everything contained within loc.
     *
     * @param loc  the location that will contain the accepted features
     */
    public ContainedByLocation(Location loc) {
      this.loc = loc;
    }
    
    /**
     * Returns true if the feature is within this filter's location.
     */
    public boolean accept(Feature f) {
      return loc.contains(f.getLocation());
    }
    
    public boolean equals(Object o) {
      return
        (o instanceof ContainedByLocation) &&
        (((ContainedByLocation) o).getLocation() == this.getLocation());
    }
    
    public boolean isProperSubset(FeatureFilter sup) {
      if(sup instanceof ContainedByLocation) {
        Location supL = ((ContainedByLocation) sup).getLocation();
        return supL.contains(this.getLocation());
      } else if(sup instanceof OverlapsLocation) {
        Location supL = ((OverlapsLocation) sup).getLocation();
        return supL.contains(this.getLocation());
      }
      return (sup instanceof AcceptAllFilter);
    }
    
    public boolean isDisjoint(FeatureFilter filt) {
      if(filt instanceof ContainedByLocation) {
        Location loc = ((ContainedByLocation) filt).getLocation();
        return !getLocation().overlaps(loc);
      }
      
      return (filt instanceof AcceptNoneFilter);
    }
  }
  
  /**
   *  A filter that returns all features overlapping a location.
   *
   * @author Matthew Pocock
   * @since 1.0
   */
  public final static class OverlapsLocation implements OptimizableFilter {
    private Location loc;
    
    public Location getLocation() {
      return loc;
    }
    
    /**
     * Creates a filter that returns everything overlapping loc.
     *
     * @param loc  the location that will overlap the accepted features
     */
    public OverlapsLocation(Location loc) {
      this.loc = loc;
    }
    
    /**
     * Returns true if the feature overlaps this filter's location.
     */
    public boolean accept(Feature f) {
      return loc.overlaps(f.getLocation());
    }
    
    public boolean equals(Object o) {
      return
        (o instanceof OverlapsLocation) &&
        (((OverlapsLocation) o).getLocation() == this.getLocation());
    }
    
    public boolean isProperSubset(FeatureFilter sup) {
      if(sup instanceof OverlapsLocation) {
        Location supL = ((OverlapsLocation) sup).getLocation();
        return supL.contains(this.getLocation());
      }
      return (sup instanceof AcceptAllFilter);
    }

    public boolean isDisjoint(FeatureFilter filt) {
      return (filt instanceof AcceptNoneFilter);
    }
  }
  
  /**
   *  A filter that returns all features not accepted by a child filter.
   *
   * @author Thomas Down
   * @author Matthew Pocock
   * @since 1.0
   */
  public final static class Not implements OptimizableFilter {
    FeatureFilter child;

    public FeatureFilter getChild() {
      return child;
    }
    
    public Not(FeatureFilter child) {
        this.child = child;
    }

    public boolean accept(Feature f) {
        return !(child.accept(f));
    }
    
    public boolean equals(Object o) {
      return
        (o instanceof Not) &&
        (((Not) o).getChild() == this.getChild());
    }
    
    public boolean isProperSubset(FeatureFilter sup) {
      if(sup instanceof Not) {
        FeatureFilter supC = ((Not) sup).getChild();
        FilterUtils.areProperSubset(supC, this.getChild());
      }
      return (sup instanceof AcceptAllFilter);
    }
    
    public boolean isDisjoint(FeatureFilter filt) {
      return FilterUtils.areProperSubset(filt, getChild());
    }
  }

  /**
   *  A filter that returns all features accepted by both child filter.
   *
   * @author Thomas Down
   * @author Matthew Pocock
   * @since 1.0
   */
  public final static class And implements OptimizableFilter {
    FeatureFilter c1, c2;

    public FeatureFilter getChild1() {
      return c1;
    }
    
    public FeatureFilter getChild2() {
      return c2;
    }
    
    public And(FeatureFilter c1, FeatureFilter c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    public boolean accept(Feature f) {
        return (c1.accept(f) && c2.accept(f));
    }
    
    public boolean equals(Object o) {
      return
        (o instanceof And) &&
        (((And) o).getChild1() == this.getChild1()) &&
        (((And) o).getChild2() == this.getChild2());
    }
    
    public boolean isProperSubset(FeatureFilter sup) {
      return
        FilterUtils.areProperSubset(getChild1(), sup) ||
        FilterUtils.areProperSubset(getChild2(), sup);
    }
    
    public boolean isDisjoint(FeatureFilter filt) {
      return (filt instanceof AcceptNoneFilter) || (
        FilterUtils.areDisjoint(getChild1(), filt) ||
        FilterUtils.areDisjoint(getChild2(), filt)
      );
    }
  }

  /**
   *  A filter that returns all features accepted by the first filter and
   * rejected by the seccond.
   *
   * @author Matthew Pocock
   * @since 1.2
   */
  public final static class AndNot implements FeatureFilter {
    FeatureFilter c1, c2;

    public FeatureFilter getChild1() {
      return c1;
    }
    
    public FeatureFilter getChild2() {
      return c2;
    }
    
    public AndNot(FeatureFilter c1, FeatureFilter c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    public boolean accept(Feature f) {
        return (c1.accept(f) && !c2.accept(f));
    }
    
    public boolean equals(Object o) {
      return
        (o instanceof AndNot) &&
        (((And) o).getChild1() == this.getChild1()) &&
        (((And) o).getChild2() == this.getChild2());
    }
  }

  /**
   *  A filter that returns all features accepted by at least one child filter.
   *
   * @author Thomas Down
   * @author Matthew Pocock
   * @since 1.0
   */
  public final static class Or implements OptimizableFilter {
    FeatureFilter c1, c2;

    public FeatureFilter getChild1() {
      return c1;
    }
    
    public FeatureFilter getChild2() {
      return c2;
    }
    
    public Or(FeatureFilter c1, FeatureFilter c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    public boolean accept(Feature f) {
        return (c1.accept(f) || c2.accept(f));
    }
    
    public boolean equals(Object o) {
      return
        (o instanceof Or) &&
        (((Or) o).getChild1() == this.getChild1()) &&
        (((Or) o).getChild2() == this.getChild2());
    }
    
    public boolean isProperSubset(FeatureFilter sup) {
      return
        FilterUtils.areProperSubset(getChild1(), sup) &&
        FilterUtils.areProperSubset(getChild2(), sup);
    }
    
    public boolean isDisjoint(FeatureFilter filt) {
      return (filt instanceof AcceptNoneFilter) || (
        FilterUtils.areDisjoint(getChild1(), filt) &&
        FilterUtils.areDisjoint(getChild2(), filt)
      );
    }
  }
  
  /**
   * Retrieve features that contain a given annotation with a given value.
   *
   * @author Matthew Pocock
   * @since 1.1
   */
  public final static class ByAnnotation implements OptimizableFilter {
    private Object key;
    private Object value;
    
    /**
     * Make a new ByAnnotation that will accept features with an annotation
     * bundle containing 'value' associated with 'key'.
     *
     * @param key  the Object used as a key in the annotation
     * @param value the Object associated with key in the annotation
     */
    public ByAnnotation(Object key, Object value) {
      this.key = key;
      this.value = value;
    }
    
    public Object getKey() {
      return key;
    }
    
    public Object getValue() {
      return value;
    }
    
    public boolean accept(Feature f) {
      Annotation ann = f.getAnnotation();
      // fixme - Annotation should have a hasProperty method
      try {
        Object v = ann.getProperty(key);
        if(v == null ) {
          if(value == null) {
            return true;
          } else {
            return false;
          }
        } else {
          return v.equals(value);
        }
      } catch (NoSuchElementException nsee) {
        return false;
      }
    }
    
    public boolean equals(Object o) {
      return
        (o instanceof ByAnnotation) &&
        (((ByAnnotation) o).getKey() == this.getKey()) &&
        (((ByAnnotation) o).getValue() == this.getValue());
    }
    
    public boolean isProperSubset(FeatureFilter sup) {
      return this.equals(sup);
    }

    public boolean isDisjoint(FeatureFilter filt) {
      return (filt instanceof AcceptNoneFilter) || (
        (filt instanceof ByAnnotation) && (
          !(getKey().equals(((ByAnnotation) filt).getKey())) ||
          !(getValue().equals(((ByAnnotation) filt).getValue()))
        )
      );
    }
  }
  
  /**
   * Retrieve features that contain a given annotation with any value.
   *
   * @author Matthew Pocock
   * @since 1.1
   */
  public final static class HasAnnotation implements FeatureFilter {
    private Object key;
    
    /**
     * Make a new ByAnnotation that will accept features with an annotation
     * bundle containing any value associated with 'key'.
     *
     * @param key  the Object used as a key in the annotation
     */
    public HasAnnotation(Object key) {
      this.key = key;
    }
    
    public Object getKey() {
      return key;
    }
    
    public boolean accept(Feature f) {
      Annotation ann = f.getAnnotation();
      // fixme - Annotation should have a hasProperty method
      try {
        Object v = ann.getProperty(key);
        return true;
      } catch (NoSuchElementException nsee) {
        return false;
      }
    }
    
    public boolean equals(Object o) {
      return
        (o instanceof HasAnnotation) &&
        (((HasAnnotation) o).getKey() == this.getKey());
    }
    
    public boolean isProperSubset(FeatureFilter sup) {
      return this.equals(sup);
    }

    public boolean isDisjoint(FeatureFilter filt) {
      return (filt instanceof AcceptNoneFilter) || (
        (filt instanceof HasAnnotation) &&
        !(getKey().equals(((HasAnnotation) filt).getKey()))
      );
    }
  }
  
  /**
   * Accept features with a given strandedness.
   *
   * @author Matthew Pocock
   * @since 1.1
   */
  public final static class StrandFilter implements OptimizableFilter {
    private StrandedFeature.Strand strand;
    
    /**
     * Build a new filter that matches all features of a given strand.
     *
     * @param strand the Strand to match
     */
    public StrandFilter(StrandedFeature.Strand strand) {
      this.strand = strand;
    }
    
    /**
     * Retrieve the strand this matches.
     *
     * @return the Strand matched
     */
    public StrandedFeature.Strand getStrand() {
      return strand;
    }
    
    /**
     * Accept the Feature if it is an instance of StrandedFeature and matches
     * the value of getStrand().
     *
     * @param f the Feature to check
     * @return true if the strand matches, or false otherwise
     */
    public boolean accept(Feature f) {
      if(f instanceof StrandedFeature) {
        StrandedFeature sf = (StrandedFeature) f;
        return sf.getStrand() == strand;
      } else {
        return strand == StrandedFeature.UNKNOWN;
      }
    }
    
    public boolean equals(Object o) {
      return
        (o instanceof StrandFilter) &&
        (((StrandFilter) o).getStrand() == this.getStrand());
    }
    
    public boolean isProperSubset(FeatureFilter sup) {
      return this.equals(sup);
    }
    
    public boolean isDisjoint(FeatureFilter filt) {
      return (filt instanceof AcceptNoneFilter) || (
        (filt instanceof StrandFilter) &&
        ((StrandFilter) filt).getStrand() == getStrand()
      );
    }
  }
}
