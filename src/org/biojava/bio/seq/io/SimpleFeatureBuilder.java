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


package org.biojava.bio.seq.io;

import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

/**
 * Simple FeatureBuilder implementation.
 *
 * @author Thomas Down
 * @author Matthew Pocock
 */

public class SimpleFeatureBuilder implements FeatureBuilder, Serializable {
    public Feature.Template buildFeatureTemplate(String type,
						 Location loc,
						 StrandedFeature.Strand strandHint,
						 Map attrs) {
	StrandedFeature.Template t = new StrandedFeature.Template();
	t.annotation = new SimpleAnnotation();
	for (Iterator i = attrs.entrySet().iterator(); i.hasNext(); ) {
	    Map.Entry e = (Map.Entry) i.next();
	    t.annotation.setProperty(e.getKey(), e.getValue());
	}

	t.location = loc;
	t.type = type;
	t.source = "Imported file";
	t.strand = strandHint;

	return t;
    }
}
