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

import org.biojava.bio.seq.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import org.biojava.bio.*;
import org.biojava.utils.*;
import org.biojava.bio.seq.homol.*;

/**
 * Wrap up default sets of Feature implementations.
 *
 * @author Thomas Down
 * @author Greg Cox
 * @author Keith James
 * @since 1.1
 */

public class FeatureImpl {
    /**
     * Default implementation of FeatureRealizer, which wraps simple
     * implementations of Feature and StrandedFeature.  This is the
     * default FeatureRealizer used by SimpleSequence and ViewSequence,
     * and may also be used by others.  When building new FeatureRealizers,
     * you may wish to use this as a `fallback' realizer, and benefit from
     * the Feature and StrandedFeature implementations.
     */

    public final static FeatureRealizer DEFAULT;

    static {
	SimpleFeatureRealizer d  = new SimpleFeatureRealizer() {
	    public Object writeReplace() {
		try {
		    return new StaticMemberPlaceHolder(SimpleFeatureRealizer.class.getField("DEFAULT"));
		} catch (NoSuchFieldException ex) {
		    throw new BioError(ex);
		}
	    }
	} ;

	try {
	    d.addImplementation(Feature.Template.class,
				SimpleFeature.class);
	    d.addImplementation(StrandedFeature.Template.class,
				SimpleStrandedFeature.class);
	    d.addImplementation(HomologyFeature.Template.class,
				SimpleHomologyFeature.class);
            d.addImplementation(SimilarityPairFeature.Template.class,
				SimpleSimilarityPairFeature.class);
            d.addImplementation(RemoteFeature.Template.class,
				SimpleRemoteFeature.class);
            d.addImplementation(FramedFeature.Template.class,
                                SimpleFramedFeature.class);
	} catch (BioException ex) {
	    throw new BioError(ex, "Couldn't initialize default FeatureRealizer");
	}

	DEFAULT = d;
    }
}
