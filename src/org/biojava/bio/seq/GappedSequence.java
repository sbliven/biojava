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
import org.biojava.bio.seq.impl.*;
import org.biojava.bio.seq.projection.*;
import org.biojava.utils.*;

import java.util.*;

/**
 * Extension of GappedSymbolList which also projects features
 * into the gapped coordinate system.
 *
 * @author Thomas Down
 * @since 1.3
 */

public class GappedSequence extends GappedSymbolList implements Sequence {
    private Sequence sequence;
    private FeatureHolder features;

    public GappedSequence(Sequence seq) {
	super(seq);
	this.sequence = seq;
    }

    public Annotation getAnnotation() {
	return sequence.getAnnotation();
    }

    public String getName() {
	return sequence.getName();
    }

    public String getURN() {
	return sequence.getURN();
    }
    
    private FeatureHolder getFeatures() {
	if (features == null) {
	    features = makeFeatures();
	}
	return features;
    }
    
    private FeatureHolder makeFeatures() {
	ProjectionContext pc = new ProjectionContext() {
		public FeatureHolder getParent(Feature f) {
		    return GappedSequence.this;
		}

		public Sequence getSequence(Feature f) {
		    return GappedSequence.this;
		}

		public Location getLocation(Feature f) {
		    return locationToGapped(f.getLocation());
		}

		public StrandedFeature.Strand getStrand(StrandedFeature f) {
		    return f.getStrand();
		}

		public Annotation getAnnotation(Feature f) {
		    return f.getAnnotation();
		}

		public FeatureHolder projectChildFeatures(Feature f, FeatureHolder parent) {
		    return FeatureHolder.EMPTY_FEATURE_HOLDER;
		}

		public Feature createFeature(Feature f, Feature.Template templ)
		    throws ChangeVetoException, BioException
		{
		    throw new ChangeVetoException("NO");
		}
		    
		public void removeFeature(Feature f, Feature f2)
		    throws ChangeVetoException
		{
		    throw new ChangeVetoException("NO");
		}
	    } ;

	try {
	    SimpleFeatureHolder results = new SimpleFeatureHolder();
	    for (Iterator i = sequence.features(); i.hasNext(); ) {
		final Feature f = (Feature) i.next();
		results.addFeature(ProjectionEngine.DEFAULT.projectFeature(f, pc));
	    }
		
	    return results;
	} catch (ChangeVetoException cve) {
	    throw new BioError("Assertion failure: can't modify newly created feature holder");
	}
    }

    public Iterator features() {
	return getFeatures().features();
    }

    public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
	return getFeatures().filter(ff, recurse);
    }

    public int countFeatures() {
	return getFeatures().countFeatures();
    }

    public boolean containsFeature(Feature f) {
	return getFeatures().containsFeature(f);
    }
    
    public void removeFeature(Feature f)
        throws ChangeVetoException
    {
	throw new ChangeVetoException("NO");
    }

    public Feature createFeature(Feature.Template templ)
        throws ChangeVetoException, BioException
    {
	throw new ChangeVetoException("NO");
    }
}
