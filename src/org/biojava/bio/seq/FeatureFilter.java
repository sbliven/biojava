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
  static class AcceptAllFilter implements FeatureFilter {
    public boolean accept(Feature f) { return true; }
  };

  /**
   * Construct one of these to filter features by type.
   *
   * @author Matthew Pocock
   */
  static class ByType implements FeatureFilter {
    private String type;
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
    public boolean accept(Feature f) { return f.getType().equals(type); }
  }

  /**
   * Construct one of these to filter features by source.
   *
   * @author Matthew Pocock
   */
  static class BySource implements FeatureFilter {
    private String source;
    /**
     * Create a BySource filter that filters in all features which have sources
     * equal to source.
     *
     * @param type  the String to match source fields against
     */
    public BySource(String source) {
      this.source = source;
    }
    public boolean accept(Feature f) { return f.getSource().equals(source); }
  }

  /**
   *  A filter that returns all features contained within a location.
   *
   * @author Matthew Pocock
   */
  static class ContainedByLocation implements FeatureFilter {
    private Location loc;

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
  static class OverlapsLocation implements FeatureFilter {
    private Location loc;
    
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
}
