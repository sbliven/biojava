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

package org.biojava.bio.seq.projection;

import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

/**
 * Context for projected features.  The projection proxies
 * make callbacks into this context object to do much of
 * the actual work of projection.  Different implementations
 * can be provided to do more complex projection and data
 * integration.
 *
 * @author Thomas Down
 * @since 1.2
 */

public interface ProjectionContext {
    /**
     * Get the parent FeatureHolder into which a feature should be projected
     */

    public FeatureHolder getParent(Feature f);

    /**
     * Get the Sequence which defines the coordinate system for the projected
     * feature.  This should be reachable by one or more getParent operations.
     */

    public Sequence getSequence(Feature f);

    /**
     * Get the location of the projected feature
     */

    public Location getLocation(Feature f);

    /**
     * Get the strand of the projected feature.
     */

    public StrandedFeature.Strand getStrand(StrandedFeature f);

    /**
     * Get the annotation bundle of the projected feature.
     * This will often (but not always) be identical to that
     * of the underlying feature.
     */

    public Annotation getAnnotation(Feature f);

    /**
     * Get the child features of the projected feature.
     */

    public FeatureHolder projectChildFeatures(Feature f, FeatureHolder parent);

    /**
     * Delegate for createFeature
     *
     * @since 1.3
     */

    public Feature createFeature(Feature f, Feature.Template templ)
        throws BioException, ChangeVetoException;

    /**
     * Delegate for removeFeature
     *
     * @since 1.3
     */

    public void removeFeature(Feature f, Feature dyingChild)
        throws ChangeVetoException;
}

