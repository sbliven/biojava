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
import org.biojava.bio.symbol.*;

/**
 * Class used by ProjectedFeatureHolder to wrap ComponentFeatures.
 *
 * @author Thomas Down
 * @author Matthew Pocock
 * @since 1.1
 */

class ProjectedComponentFeatureWrapper extends ProjectedStrandedFeatureWrapper
{
    public ProjectedComponentFeatureWrapper(ComponentFeature f,
					    FeatureHolder tParent,
					    int offset)
    {
	super(f, tParent, offset);
    }

    public Sequence getComponentSequence() {
	return ((ComponentFeature) getFeature()).getComponentSequence();
    }

    public Location getComponentLocation() {
	return ((ComponentFeature) getFeature()).getComponentLocation();
    }
}
