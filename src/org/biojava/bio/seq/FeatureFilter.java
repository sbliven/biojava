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
import org.biojava.bio.seq.homol.SimilarityPairFeature;

/**
 * A filter for accepting or rejecting a feature.
 * <p>
 * This may implement arbitrary rules, or be based upon the feature's
 * annotation, type, location or source.
 * <p>
 * If the filter is to be used in a remote process, it is recognized that it may
 * be serialized and sent over to run remotely, rather than each feature being
 * retrieved locally.
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
   * <p>
   * Use the FeatureFilter.all member.
   *
   * @author Thomas Down
   * @author Matthew Pocock
   * @since 1.0
   */
  public final static class AcceptAllFilter implements FeatureFilter {
    public boolean accept(Feature f) { return true; }

    public boolean equals(Object o) {
      return o instanceof AcceptAllFilter;
    }

    public int hashCode() {
      return 0;
    }

    public boolean isProperSubset(FeatureFilter sup) {
      return sup.equals(this);
    }

    public boolean isDisjoint(FeatureFilter filt) {
      return filt instanceof AcceptNoneFilter;
    }

    public String toString() {
      return "All";
    }
  }

  /**
   * No features are selected in with this filter.
   */
  static final public FeatureFilter none = new AcceptNoneFilter();

  /**
   * The class that accepts no features.
   * <p>
   * Use the FeatureFilter.none member.
   *
   * @author Matthew Pocock
   * @since 1.2
   */
  public final static class AcceptNoneFilter implements FeatureFilter {
    public boolean accept(Feature f) { return false; }

    public boolean equals(Object o) {
      return o instanceof AcceptNoneFilter;
    }

    public int hashCode() {
      return 1;
    }

    public boolean isProperSubset(FeatureFilter sup) {
      return true;
    }

    public boolean isDisjoint(FeatureFilter filt) {
      return true;
    }

       public String toString() {
	  return "None";
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
        (((ByType) o).getType().equals(this.getType()));
    }

    public int hashCode() {
      return getType().hashCode();
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

    public String toString() {
      return "ByType(" + type + ")";
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
        (((BySource) o).getSource().equals(this.getSource()));
    }

    public boolean isProperSubset(FeatureFilter sup) {
      return this.equals(sup) || (sup instanceof AcceptAllFilter);
    }

    public int hashCode() {
      return getSource().hashCode();
    }

    public boolean isDisjoint(FeatureFilter filt) {
      return (filt instanceof AcceptNoneFilter) || (
        (filt instanceof BySource) &&
        !getSource().equals(((BySource) filt).getSource())
      );
    }

      public String toString() {
	  return "BySource(" + source + ")";
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

    public int hashCode() {
      return getTestClass().hashCode();
    }

    public boolean isProperSubset(FeatureFilter sup) {
      if(sup instanceof ByClass) {
        Class supC = ((ByClass) sup).getTestClass();
        return supC.isAssignableFrom(this.getTestClass());
      }
      return (sup instanceof AcceptAllFilter);
    }

    public boolean isDisjoint(FeatureFilter feat) {
      if(feat instanceof ByClass) {
        Class featC = ((ByClass) feat).getTestClass();
        return
          ! (featC.isAssignableFrom(getTestClass())) &&
          ! (getTestClass().isAssignableFrom(featC));
      } else if (feat instanceof ByComponentName) {
	  return !getTestClass().isAssignableFrom(ComponentFeature.class);
      }

      return (feat instanceof AcceptNoneFilter);
    }

    public String toString() {
      return "ByClass(" + clazz.getName() + ")";
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
        (((ContainedByLocation) o).getLocation().equals(this.getLocation()));
    }

    public int hashCode() {
      return getLocation().hashCode();
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
      } else if (filt instanceof OverlapsLocation) {
	  Location filtL = ((OverlapsLocation) filt).getLocation();
	  return !filtL.overlaps(this.getLocation());
      }

      return (filt instanceof AcceptNoneFilter);
    }

    public String toString() {
      return "ContainedBy(" + loc + ")";
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
        (((OverlapsLocation) o).getLocation().equals(this.getLocation()));
    }

    public int hashCode() {
      return getLocation().hashCode();
    }

    public boolean isProperSubset(FeatureFilter sup) {
      if(sup instanceof OverlapsLocation) {
        Location supL = ((OverlapsLocation) sup).getLocation();
        return supL.contains(this.getLocation());
      }
      return (sup instanceof AcceptAllFilter);
    }

    public boolean isDisjoint(FeatureFilter filt) {
	if (filt instanceof ContainedByLocation)  {
	    Location loc = ((ContainedByLocation) filt).getLocation();
	    return !getLocation().overlaps(loc);
	}
	return (filt instanceof AcceptNoneFilter);
    }

    public String toString() {
      return "Overlaps(" + loc + ")";
    }
  }

  /**
   *  A filter that returns all features not accepted by a child filter.
   *
   * @author Thomas Down
   * @author Matthew Pocock
   * @since 1.0
   */
  public final static class Not implements FeatureFilter {
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
        (((Not) o).getChild().equals(this.getChild()));
    }

    public int hashCode() {
      return getChild().hashCode();
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

    public String toString() {
      return "Not(" + child + ")";
    }
  }

  /**
   *  A filter that returns all features accepted by both child filter.
   *
   * @author Thomas Down
   * @author Matthew Pocock
   * @since 1.0
   */
  public final static class And implements FeatureFilter {
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
        (o instanceof And) && ((
          ((And) o).getChild1().equals(this.getChild1()) &&
          ((And) o).getChild2().equals(this.getChild2())
        ) || (
          ((And) o).getChild1().equals(this.getChild2()) &&
          ((And) o).getChild2().equals(this.getChild1())
        ));
    }

    public int hashCode() {
      return getChild1().hashCode() ^ getChild2().hashCode();
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

      public String toString() {
	  return "And(" + c1 + " , " + c2 + ")";
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
        (((AndNot) o).getChild1().equals(this.getChild1())) &&
        (((AndNot) o).getChild2().equals(this.getChild2()));
    }

    public int hashCode() {
      return getChild1().hashCode() ^ getChild2().hashCode();
    }

    public String toString() {
      return "AndNot(" + c1 + " , " + c2 + ")";
     }
  }

  /**
   *  A filter that returns all features accepted by at least one child filter.
   *
   * @author Thomas Down
   * @author Matthew Pocock
   * @since 1.0
   */
  public final static class Or implements FeatureFilter {
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
        (o instanceof Or) && ((
          ((Or) o).getChild1().equals(this.getChild1()) &&
          ((Or) o).getChild2().equals(this.getChild2())
        ) || (
          ((Or) o).getChild1().equals(this.getChild2()) &&
          ((Or) o).getChild2().equals(this.getChild1())
        ));
    }

    public int hashCode() {
      return getChild1().hashCode() ^ getChild2().hashCode();
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

    public String toString() {
      return "Or(" + c1 + " , " + c2 + ")";
    }
  }

  /**
   * Retrieve features that contain a given annotation with a given value.
   *
   * @author Matthew Pocock
   * @author Keith James
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
        if (! ann.containsProperty(key)) {
            return false;
        }
        else
        {
            try {
                Object v = ann.getProperty(key);
                if (v == null) {
                    if (value == null) {
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
    }

    public boolean equals(Object o) {
      return
        (o instanceof ByAnnotation) &&
        (((ByAnnotation) o).getKey().equals(this.getKey())) &&
        (((ByAnnotation) o).getValue().equals(this.getValue()));
    }

    public int hashCode() {
      return getKey().hashCode() ^ getValue().hashCode();
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

    public String toString() {
      return getKey() + " == " + getValue();
    }
  }

  /**
   * Retrieve features that contain a given annotation with any value.
   *
   * @author Matthew Pocock
   * @author Keith James
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

        if (! ann.containsProperty(key)) {
            return false;
        } else {
            try {
                Object v = ann.getProperty(key);
                return true;
            } catch (NoSuchElementException nsee) {
                return false;
            }
        }
    }

    public boolean equals(Object o) {
      return
        (o instanceof HasAnnotation) &&
        (((HasAnnotation) o).getKey().equals(this.getKey()));
    }

    public int hashCode() {
      return getKey().hashCode();
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

    /**
     * Filter by applying a nested <code>FeatureFilter</code> to the
     * parent feature.  Always <code>false</code> if the parent
     * is not a feature (e.g. top-level features, where the
     * parent is a sequence).
     *
     * @author Thomas Down
     * @since 1.2
     */

    public static class ByParent implements OptimizableFilter {
	private FeatureFilter filter;

	public ByParent(FeatureFilter ff) {
	    filter = ff;
	}

	public FeatureFilter getFilter() {
	    return filter;
	}

	public boolean accept(Feature f) {
	    FeatureHolder fh = f.getParent();
	    if (fh instanceof Feature) {
		return filter.accept((Feature) fh);
	    }

	    return false;
	}

	public int hashCode() {
	    return filter.hashCode() + 173;
	}

	public boolean equals(Object o) {
	    if (! (o instanceof FeatureFilter.ByParent)) {
		return false;
	    }

	    FeatureFilter.ByParent ffbp = (FeatureFilter.ByParent) o;
	    return ffbp.getFilter().equals(filter);
	}

	public boolean isProperSubset(FeatureFilter ff) {
	    FeatureFilter ancFilter = null;
	    if (ff instanceof FeatureFilter.ByParent) {
		ancFilter = ((FeatureFilter.ByParent) ff).getFilter();
	    } else if (ff instanceof FeatureFilter.ByAncestor) {
		ancFilter = ((FeatureFilter.ByAncestor) ff).getFilter();
	    }

	    if (ancFilter != null) {
		return FilterUtils.areProperSubset(ancFilter, filter);
	    } else {
		return false;
	    }
	}

	public boolean isDisjoint(FeatureFilter ff) {
	    FeatureFilter ancFilter = null;
	    if (ff instanceof FeatureFilter.ByParent) {
		ancFilter = ((FeatureFilter.ByParent) ff).getFilter();
	    }

	    if (ancFilter != null) {
		return FilterUtils.areDisjoint(ancFilter, filter);
	    } else {
		return false;
	    }
	}
    }

    /**
     * Filter by applying a nested <code>FeatureFilter</code> to all
     * ancestor features.  Returns <code>true</code> if at least one
     * of them matches the filter.  Always <code>false</code> if the
     * parent is not a feature (e.g. top-level features, where the
     * parent is a sequence).
     *
     * @author Thomas Down
     * @since 1.2
     */

    public static class ByAncestor implements OptimizableFilter {
	private FeatureFilter filter;

	public ByAncestor(FeatureFilter ff) {
	    filter = ff;
	}

	public FeatureFilter getFilter() {
	    return filter;
	}

	public boolean accept(Feature f) {
	    do {
		FeatureHolder fh = f.getParent();
		if (fh instanceof Feature) {
		    f = (Feature) fh;
		    if (filter.accept(f)) {
			return true;
		    }
		} else {
		    return false;
		}
	    } while (true);
	}

	public int hashCode() {
	    return filter.hashCode() + 186;
	}

	public boolean equals(Object o) {
	    if (! (o instanceof FeatureFilter.ByAncestor)) {
		return false;
	    }

	    FeatureFilter.ByAncestor ffba = (FeatureFilter.ByAncestor) o;
	    return ffba.getFilter().equals(filter);
	}


	public boolean isProperSubset(FeatureFilter ff) {
	    FeatureFilter ancFilter = null;
	    if (ff instanceof FeatureFilter.ByAncestor) {
		ancFilter = ((FeatureFilter.ByAncestor) ff).getFilter();
	    }

	    if (ancFilter != null) {
		return FilterUtils.areProperSubset(ancFilter, filter);
	    } else {
		return false;
	    }
	}

	public boolean isDisjoint(FeatureFilter ff) {
	    FeatureFilter ancFilter = null;
	    if (ff instanceof FeatureFilter.ByParent) {
		ancFilter = ((FeatureFilter.ByParent) ff).getFilter();
	    } else if (ff instanceof FeatureFilter.ByParent) {
		ancFilter = ((FeatureFilter.ByParent) ff).getFilter();
	    }

	    if (ancFilter != null) {
		return FilterUtils.areDisjoint(ancFilter, filter);
	    } else {
		return false;
	    }
	}
    }

  /**
   * Accept features with a given reading frame.
   *
   * @author Mark Schreiber
   * @since 1.2
   */
  public final static class FrameFilter implements OptimizableFilter {
    private FramedFeature.ReadingFrame frame;

    /**
     * Build a new filter that matches all features of a reading frame.
     *
     * @param frame the ReadingFrame to match
     */
    public FrameFilter(FramedFeature.ReadingFrame frame) {
      this.frame = frame;
    }

    /**
     * Retrieve the reading frame this filter matches.
     */
     public FramedFeature.ReadingFrame getFrame(){
       return frame;
     }

    /**
     * Accept the Feature if it is an instance of FramedFeature and matches
     * the value of getFrame().
     *
     * @param f the Feature to check
     * @return true if the frame matches, or false otherwise
     */
    public boolean accept(Feature f) {
      if(f instanceof FramedFeature) {
        FramedFeature ff = (FramedFeature) f;
        return ff.getReadingFrame() == frame;
      } else {
        return false;
      }
    }

    public boolean equals(Object o) {
      return (o instanceof StrandFilter);
    }

    public boolean isProperSubset(FeatureFilter sup) {
      return this.equals(sup);
    }

    public boolean isDisjoint(FeatureFilter filt) {
      return (filt instanceof AcceptNoneFilter) || (
        (filt instanceof FrameFilter) &&
        ((FrameFilter) filt).getFrame() == getFrame()
      );
    }
  }

    /**
     * <code>ByPairwiseScore</code> is used to filter
     * <code>SimilarityPairFeature</code>s by their score. Features
     * are accepted if their score falls between the filter's minimum
     * and maximum values, inclusive. Features are rejected if they
     * are not <code>SimilarityPairFeature</code>s. The minimum value
     * accepted must be less than the maximum value.
     *
     * @author Keith James
     * @since 1.3
     */
    public static final class ByPairwiseScore implements OptimizableFilter {
        private double minScore;
        private double maxScore;
        private double score;
        private int    hashCode;

        /**
         * Creates a new <code>ByPairwiseScore</code>.
         *
         * @param minScore a <code>double</code>.
         * @param maxScore a <code>double</code>.
         */
        public ByPairwiseScore(double minScore, double maxScore) {
            if (minScore > maxScore)
                throw new IllegalArgumentException("Filter minimum score must be less than maximum score");

            this.minScore = minScore;
            this.maxScore = maxScore;

            hashCode += (minScore == 0.0 ? 0L : Double.doubleToLongBits(minScore));
            hashCode += (maxScore == 0.0 ? 0L : Double.doubleToLongBits(maxScore));
        }

        /**
         * Accept a Feature if it is an instance of
         * SimilarityPairFeature and its score is <= filter's minimum
         * score and >= filter's maximum score.
         *
         * @param f a <code>Feature</code>.
         * @return a <code>boolean</code>.
         */
        public boolean accept(Feature f) {
            if (! (f instanceof SimilarityPairFeature)) {
                return false;
            }

            score = ((SimilarityPairFeature) f).getScore();
            return (score >= minScore &&
                    score <= maxScore);
        }

        /**
         * <code>getMinScore</code> returns the minimum score
         * accepted.
         *
         * @return a <code>double</code>.
         */
        public double getMinScore() {
            return minScore;
        }

        /**
         * <code>getMaxScore</code> returns the maximum score
         * accepted.
         *
         * @return a <code>double</code>.
         */
        public double getMaxScore() {
            return maxScore;
        }

        public boolean equals(Object o) {
            if (o instanceof ByPairwiseScore) {
                ByPairwiseScore psf = (ByPairwiseScore) o;
                if (psf.getMinScore() == minScore &&
                    psf.getMaxScore() == maxScore) {
                    return true;
                }
            }
            return false;
        }

        public int hashCode() {
            return hashCode;
        }

        public boolean isProperSubset(FeatureFilter sup) {
            if (sup instanceof ByPairwiseScore) {
                ByPairwiseScore psf = (ByPairwiseScore) sup;
                return (psf.getMinScore() >= minScore &&
                        psf.getMaxScore() <= maxScore);
            }
            return false;
        }

        public boolean isDisjoint(FeatureFilter filt) {
            if (filt instanceof AcceptNoneFilter)
                return true;

            if (filt instanceof ByPairwiseScore) {
                ByPairwiseScore psf = (ByPairwiseScore) filt;
                return (psf.getMaxScore() < minScore ||
                        psf.getMinScore() > maxScore);
            }
            return false;
        }

        public String toString() {
            return minScore + " >= score <= " + maxScore;
        }
    }

    /**
     * Accepts features which are ComponentFeatures and have a <code>componentSequenceName</code>
     * property of the specified value.
     *
     * @author Thomas Down
     * @since 1.3
     */

    public final static class ByComponentName implements OptimizableFilter {
        private String cname;

        public ByComponentName(String cname) {
            this.cname = cname;
        }

        public boolean accept(Feature f) {
            if (f instanceof ComponentFeature) {
                return cname.equals(((ComponentFeature) f).getComponentSequenceName());
            } else {
                return false;
            }
        }

        public String getComponentName() {
            return cname;
        }

        public boolean equals(Object o) {
            return (o instanceof ByComponentName) && ((ByComponentName) o).getComponentName().equals(cname);
	}

        public int hashCode() {
            return getComponentName().hashCode();
        }

        public boolean isProperSubset(FeatureFilter sup) {
            if (sup instanceof ByComponentName) {
                return equals(sup);
	    } else if (sup instanceof ByClass) {
		return ((ByClass) sup).getTestClass().isAssignableFrom(ComponentFeature.class);
	    } else {
		return (sup instanceof AcceptAllFilter);
	    }
	}


	public boolean isDisjoint(FeatureFilter feat) {
	    if (feat instanceof ByComponentName) {
		return !equals(feat);
	    } else if (feat instanceof ByClass) {
		Class featC = ((ByClass) feat).getTestClass();
		return
		    ! (featC.isAssignableFrom(ComponentFeature.class));
	    } else {
		return (feat instanceof AcceptNoneFilter);
	    }
	}

	public String toString() {
	    return "ByComponentName(" + cname + ")";
	}
    }
}
