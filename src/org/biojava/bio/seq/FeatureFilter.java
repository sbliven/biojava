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
 * @author Matthew Pocock
 * @author Thomas Down
 */
public interface FeatureFilter extends Serializable {
  /**
   * This method determines whether a fetaure is to be accepted.
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
   */
  public static class AcceptAllFilter implements FeatureFilter {
    public boolean accept(Feature f) { return true; }
  };

  /**
   * Construct one of these to filter features by type.
   *
   * @author Matthew Pocock
   */
  public static class ByType implements FeatureFilter {
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
  }

  /**
   * Construct one of these to filter features by source.
   *
   * @author Matthew Pocock
   */
  public static class BySource implements FeatureFilter {
    private String source;
    
    public String getSource() {
      return source;
    }
    
    /**
     * Create a BySource filter that filters in all features which have sources
     * equal to source.
     *
     * @param type  the String to match source fields against
     */
    public BySource(String source) {
      this.source = source;
    }
    public boolean accept(Feature f) { return source.equals(f.getSource()); }
  }

    /**
     * Filter which accepts only those filters which are an instance
     * of a specific Java class
     *
     * @author Thomas Down
     * @since 1.1
     */

    public static class ByClass implements FeatureFilter {
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
    }


  /**
   *  A filter that returns all features contained within a location.
   *
   * @author Matthew Pocock
   */
  public static class ContainedByLocation implements FeatureFilter {
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
  }
  
  /**
   *  A filter that returns all features overlapping a location.
   *
   * @author Matthew Pocock
   */
  public static class OverlapsLocation implements FeatureFilter {
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
  }
  
  /**
   *  A filter that returns all features not accepted by a child filter.
   *
   * @author Thomas Down
   * @author Matthew Pocock
   */
  public static class Not implements FeatureFilter {
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
  }

  /**
   *  A filter that returns all features accepted by both child filter.
   *
   * @author Thomas Down
   * @author Matthew Pocock
   */
  public static class And implements FeatureFilter {
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
  }

  /**
   *  A filter that returns all features accepted by at least one child filter.
   *
   * @author Thomas Down
   * @author Matthew Pocock
   */
  public static class Or implements FeatureFilter {
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
  }
}
