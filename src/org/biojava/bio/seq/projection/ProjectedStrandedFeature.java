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

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

/**
 * Class used by ProjectedFeatureHolder to wrap StrandedFeatures.
 *
 * @author Thomas Down
 * @since 1.1
 */

class ProjectedStrandedFeature extends ProjectedFeature 
                 implements StrandedFeature 
{
    public ProjectedStrandedFeature(StrandedFeature f,
				    ProjectionContext ctx)
    {
	super(f, ctx);
    }

    public StrandedFeature.Strand getStrand() {
	return getProjectionContext().getStrand((StrandedFeature) getViewedFeature());
    }

    public Feature.Template makeTemplate() {
	StrandedFeature.Template sft = (StrandedFeature.Template) super.makeTemplate();
	sft.strand = getStrand();
	return sft;
    }

    public String toString() {
        String pm;
        if (getStrand() == POSITIVE) {
          pm = "+";
        } else if (getStrand() == NEGATIVE) {
          pm = "-";
        } else {
          pm = " ";
        }
        return "Feature " + getType() + " " +
            getSource() + " " + getLocation() + " " + pm;
    }

}
