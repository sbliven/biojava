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

package org.biojava.bio.seq.db.biosql;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.*;

class BioSQLStrandedFeature extends SimpleStrandedFeature {
    private Annotation _annotation;

    BioSQLStrandedFeature(Sequence seq,
			  FeatureHolder parent,
			  StrandedFeature.Template templ)
	throws IllegalArgumentException, IllegalAlphabetException
    {
	super(seq, parent, mungeTemplate(templ));
	_annotation = templ.annotation;
    }

    private static StrandedFeature.Template mungeTemplate(StrandedFeature.Template templ) {
	StrandedFeature.Template sft = new StrandedFeature.Template();
	sft.location = templ.location;
	sft.type = templ.type;
	sft.source = templ.source;
	sft.strand = templ.strand;
	sft.annotation = Annotation.EMPTY_ANNOTATION;
	return sft;
    }

    public Annotation getAnnotation() {
	return _annotation;
    }
} 
