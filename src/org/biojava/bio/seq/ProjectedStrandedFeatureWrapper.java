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
 * Class used by ProjectedFeatureHolder to wrap StrandedFeatures.
 *
 * @author Thomas Down
 * @since 1.1
 */

class ProjectedStrandedFeatureWrapper extends ProjectedFeatureWrapper 
                 implements StrandedFeature 
{
    public ProjectedStrandedFeatureWrapper(StrandedFeature f,
					   FeatureHolder tParent,
					   int offset)
    {
	super(f, tParent, offset);
    }

    public StrandedFeature.Strand getStrand() {
	return ((StrandedFeature) getFeature()).getStrand();
    }
}
