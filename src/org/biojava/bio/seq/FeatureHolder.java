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

import org.biojava.utils.*;
import org.biojava.bio.*;

/**
 * The interface for objects that contain features.
 * <P>
 * Feature holders abstract the containment of a feature from the objects
 * that implements both the real container or the features. FeatureHolders are
 * like sets of features.
 * </p>
 *
 * @author Matthew Pocock
 * @author Thomas Down
 */
public interface FeatureHolder extends Changeable {
  /**
   * Signals that features have been added or removed directly within this
   * FeatureHolder.
   */
  public static final ChangeType FEATURES = new ChangeType(
    "Features have been added or removed",
    "org.biojava.bio.seq.FeatureHolder",
    "FEATURES"
  );
  
    /**
     * Count how many features are contained.
     *
     * @return  a positive integer or zero, equal to the number of features
     *          contained
     */
    int countFeatures();
    
    /**
     * Iterate over the features in no well defined order.
     *
     * @return  an Iterator
     */
    Iterator features();

    /**
     * Return a new FeatureHolder that contains all of the children of this one
     * that passed the filter fc.
     *
     * @param fc  the FeatureFilter to apply
     * @param recurse true if all features-of-features should be scanned, and a
     *                single flat collection of features returned, or false if
     *                just immediate children should be filtered.
     */
    FeatureHolder filter(FeatureFilter fc, boolean recurse);
  
    /**
     * Create a new Feature, and add it to this FeatureHolder.  This
     * method will generally only work on Sequences, and on some
     * Features which have been attached to Sequences.
     *
     * @throws BioException if something went wrong during creating the feature
     * @throws ChangeVetoException if this FeatureHolder does not support
     *         creation of new features, or if the change was vetoed  
     */
    public Feature createFeature(Feature.Template ft)
    throws BioException, ChangeVetoException;

    /**
     * Remove a feature from this FeatureHolder.
     *
     * @throws ChangeVetoException if this FeatureHolder does not support
     *         feature removal or if the change was vetoed
     */
    public void removeFeature(Feature f)
    throws ChangeVetoException;
    
    /**
     * Check if the feature is present in this holder.
     *
     * @since 1.2
     * @param f the Feature to check
     * @return true if f is in this set
     */
    public boolean containsFeature(Feature f);

    public static final FeatureHolder EMPTY_FEATURE_HOLDER =
	new EmptyFeatureHolder();
    
    final class EmptyFeatureHolder implements FeatureHolder {
	public int countFeatures() {
	    return 0;
	}
    
	public Iterator features() {
	    return Collections.EMPTY_SET.iterator();
	}
    
	public FeatureHolder filter(FeatureFilter fc, boolean recurse) {
	    return this;
	}

	public Feature createFeature(Feature.Template f) {
	    throw new UnsupportedOperationException();
	}
	
	public void removeFeature(Feature f) {
	    throw new UnsupportedOperationException();
	}
        
        public boolean containsFeature(Feature f) {
          return false;
        }
    
    public void addChangeListener(ChangeListener cl) {}
    public void addChangeListener(ChangeListener cl, ChangeType ct) {}
    public void removeChangeListener(ChangeListener cl) {}
    public void removeChangeListener(ChangeListener cl, ChangeType ct) {}
    }
}
