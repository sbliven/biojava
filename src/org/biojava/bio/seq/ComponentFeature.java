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

import org.biojava.bio.symbol.*;
import org.biojava.bio.*;

/**
 * Feature which represents a component in an assembly (contig).
 * This implies that a portion (possibly all) of the 
 * associated componentSequence is included in this feature's
 * parent sequence.
 *
 * <p>
 * There are important invariants which apply to all ComponentFeatures.
 * The Location returned by getLocation() must
 * contain the same number of unique point locations as that
 * returned by getComponentLocation().
 * </p>
 *
 * @author Thomas Down
 * @since 1.1
 */

public interface ComponentFeature extends StrandedFeature {
    /**
     * Get the sequence object which provides a component of this
     * feature's parent sequence.
     *
     * @return A sequence.
     */

    public Sequence getComponentSequence();

    /**
     * Return a location which identifies a portion of the component
     * sequence which is to be included in the assembly.
     *
     * @return A location within the component sequence.
     */

    public Location getComponentLocation();

    /**
     * Template for constructing a new ComponentFeature.
     */

    public static class Template extends StrandedFeature.Template {
	public Sequence componentSequence;
	public Location componentLocation;
    }
}
