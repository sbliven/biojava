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

package org.biojava.bio.seq.impl;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

/**
 * Class used by ProjectedFeatureHolder to wrap StrandedFeatures.
 *
 * @author Thomas Down
 * @since 1.1
 */

public class ProjectedStrandedFeatureWrapper extends ProjectedFeatureWrapper 
                 implements StrandedFeature 
{
    public ProjectedStrandedFeatureWrapper(StrandedFeature f,
					   ProjectedFeatureHolder holder)
    {
	super(f, holder);
    }

    public StrandedFeature.Strand getStrand() {
	if (getProjectingFeatureHolder().isOppositeStrand()) {
	    StrandedFeature.Strand s = ((StrandedFeature) getFeature()).getStrand();
	    if (s == StrandedFeature.POSITIVE) {
		return StrandedFeature.NEGATIVE;
	    } else if (s == StrandedFeature.NEGATIVE) {
		return StrandedFeature.POSITIVE;
	    } else {
		return StrandedFeature.UNKNOWN;
	    }
	} else {
	    return ((StrandedFeature) getFeature()).getStrand();
	}
    }
}
