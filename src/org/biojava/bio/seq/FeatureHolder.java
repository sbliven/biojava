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

import org.biojava.bio.*;
import java.util.*;

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
public interface FeatureHolder {
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
     * @throws UnsupportedOperationException if this FeatureHolder does not
     *                    support addition of new features.  
     */

    public Feature createFeature(Feature.Template ft) throws BioException;

    /**
     * Remove a feature from this FeatureHolder.
     *
     * @throws UnsupportedOperationException if this FeatureHolder
     *                     does not support feature removal.
     */

    public void removeFeature(Feature f);

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
    }
}
