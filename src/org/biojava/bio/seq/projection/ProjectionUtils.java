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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.LocationTools;
import org.biojava.bio.symbol.PointLocation;
import org.biojava.bio.symbol.RangeLocation;

/**
 * @author Thomas Down
 */
public class ProjectionUtils {
    public static Location transformLocation(Location oldLoc, int translate, boolean oppositeStrand) {
        if (oppositeStrand) {
            if (oldLoc.isContiguous()) {
                if (oldLoc instanceof PointLocation){
                    return new PointLocation(translate - oldLoc.getMin());
                } else {
                    return new RangeLocation(translate - oldLoc.getMax(),
    	                                     translate - oldLoc.getMin());
                }
            } else {
                Location compound = Location.empty;
                List locList = new ArrayList();
                for (Iterator i = oldLoc.blockIterator(); i.hasNext(); ) {
                    Location oldBlock = (Location) i.next();
                    locList.add(new RangeLocation(translate - oldBlock.getMax(),
                    		      			translate - oldBlock.getMin()));
                }
                compound = LocationTools.union(locList);
                return compound;
            }
        } else {
            return oldLoc.translate(translate);
        }
    }
    
    public static StrandedFeature.Strand flipStrand(StrandedFeature.Strand s) {
            if (s == StrandedFeature.POSITIVE) {
                return StrandedFeature.NEGATIVE;
            } else if (s == StrandedFeature.NEGATIVE) {
                return StrandedFeature.POSITIVE;
            } else {
                return StrandedFeature.UNKNOWN;
            }
    }
}
