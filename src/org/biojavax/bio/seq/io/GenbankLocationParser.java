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

package org.biojavax.bio.seq.io;
import org.biojava.bio.symbol.Location;
import org.biojavax.bio.seq.RichFeature;
import org.biojavax.bio.seq.RichLocation;
import org.biojavax.bio.seq.SimpleRichLocation;

/**
 *
 * @author Richard Holland
 */
public class GenbankLocationParser {
    // No instances please
    private GenbankLocationParser() {}
    
    public static void parseLocation(RichFeature.Template parentFeature, String locationString) {
        RichLocation location = new SimpleRichLocation(0,0,0);
        //populate parentFeature.location
        //locations have terms - jointype?
        //locations have notes - terms/values - no ranks
        //locations have crossrefs - use this to point to a remote location
        //locations have mins
        //locations have maxes
        //locations have strands
        //locations have ranks - sequential
        parentFeature.location = location;
    }
    
    public static String writeLocation(RichFeature parentFeature) {
        //write out location text
        //use crossrefs to calculate remote location positions
        RichLocation l = (RichLocation)parentFeature.getLocation();
        return l.getMin()+".."+l.getMax();
    }
}
