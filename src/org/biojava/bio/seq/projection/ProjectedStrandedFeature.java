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

import org.biojava.bio.BioError;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeVetoException;

/**
 * Internal class used by ProjectedFeatureHolder to wrap StrandedFeatures.
 *
 * @author Thomas Down
 * @author Matthew Pocock
 * @since 1.1
 */

public class ProjectedStrandedFeature
  extends ProjectedFeature 
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

    public void setStrand(Strand strand) throws ChangeVetoException {
        throw new ChangeVetoException(new ChangeEvent(
           this, STRAND, getStrand(), strand));

        // fixme: strand should get reverse-projected through the context
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

    public SymbolList getSymbols() {
	SymbolList symList = super.getSymbols();
	if(getStrand() == NEGATIVE) {
	    try {
		symList = DNATools.reverseComplement(symList);
	    } catch (IllegalAlphabetException iae) {
		throw new BioError(
				   iae,
				   "Could not retrieve symbols for feature as " +
				   "the alphabet can not be complemented."
				   );
	    }
	}
	return symList;
    }
}
