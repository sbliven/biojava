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

package org.biojava.bio.seq.distributed;

import java.util.*;
import java.lang.reflect.*;

import org.biojava.utils.*;
import org.biojava.utils.bytecode.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.bio.seq.projection.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.program.das.*;

/**
 * Projection for MetaDAS
 *
 * @author Thomas Down
 * @since 1.2
 */


class DistProjectedFeatureHolder extends AbstractFeatureHolder {
    private final FeatureHolder wrapped;
    private final FeatureHolder parent;
    private final Annotation annotation;
    private FeatureHolder projectedFeatures;
    private boolean cachingProjections = true;
    private ChangeListener underlyingFeaturesChange;
    private PFHContext projectionContext;

    public FeatureHolder projectFeatureHolder(FeatureHolder fh,
					      FeatureHolder parent)
    {
	return new DistProjectedFeatureHolder(fh, parent, annotation);
    }

    /**
     * Construct a new FeatureHolder which projects a set of features
     * into a new coordinate system.  If <code>translation</code> is 0
     * and <code>oppositeStrand</code> is <code>false</code>, the features
     * are simply reparented without any transformation.
     *
     * @param fh The set of features to project.
     * @param parent The FeatureHolder which is to act as parent
     *               for the projected features.
     */

    public DistProjectedFeatureHolder(FeatureHolder fh,
				      FeatureHolder parent,
				      Annotation annotation)
    {
	this.wrapped = fh;
	this.parent = parent;
	this.annotation = annotation;

	this.projectionContext = new PFHContext();

	underlyingFeaturesChange = new ChangeListener() {
	    public void preChange(ChangeEvent e)
		throws ChangeVetoException 
	    {
		if (hasListeners()) {
		    getChangeSupport(FeatureHolder.FEATURES).firePreChangeEvent(new ChangeEvent(this,
								     FeatureHolder.FEATURES,
								     e.getChange(),
								     e.getPrevious(),
								     e));
		}
	    }

	    public void postChange(ChangeEvent e) {
		projectedFeatures = null; // Flush all the cached projections --
		                          // who knows what might have changed.

		if (hasListeners()) {
		    getChangeSupport(FeatureHolder.FEATURES).firePostChangeEvent(new ChangeEvent(this,
								      FeatureHolder.FEATURES,
								      e.getChange(),
								      e.getPrevious(),
								      e));
		}
	    }
	} ;

	wrapped.addChangeListener(underlyingFeaturesChange);
    }

    public boolean isCachingProjections() {
	return cachingProjections;
    }

    /**
     * Determine whether or not the projected features should be cached.
     * This is a temporary optimization, and might go away once feature
     * filtering is more intelligent.
     *
     * @since 1.2
     */

    public void setIsCachingProjections(boolean b) {
	cachingProjections = b;
	projectedFeatures = null;
    }

    protected FeatureHolder getProjectedFeatures() {
	if (projectedFeatures != null) {
	    return projectedFeatures;
	}

	FeatureHolder toProject = wrapped;

	SimpleFeatureHolder sfh = new SimpleFeatureHolder();
	for (Iterator i = toProject.features(); i.hasNext(); ) {
	    Feature f = (Feature) i.next();
	    Feature wf;
	    if (f instanceof ComponentFeature) {
		if (parent instanceof DistributedSequence) {
		    // We currently don't use the projection engine for this case.  Extend
		    // the ProjectionContext interface?
		    
		    ComponentFeature.Template cft = (ComponentFeature.Template) ((ComponentFeature) f).makeTemplate();
		    if (cft.componentSequenceName == null) {
			cft.componentSequenceName = cft.componentSequence.getName();
		    }
		    cft.componentSequence = null;    // We need to go back though the DistDB for the
		                                     // proper component sequence to use here.
		    if (cft.componentSequenceName == null) {
			throw new NullPointerException("Can't get component sequence name");
		    }

		    try {
			wf = new DistComponentFeature((DistributedSequence) parent,
						      cft);
		    } catch (Exception ex) {
			throw new BioRuntimeException(ex, "Error instantiating DistComponentFeature");
		    }
		} else {
		    throw new BioRuntimeException("MetaDAS currently doesn't handle ComponentFeatures which aren't top-level features.  Any idea what these mean?");
		}
	    } else {
		// Everything else goes through a fairly standard projection
		
		wf = ProjectionEngine.DEFAULT.projectFeature(f, projectionContext);
	    }
	    try {
		sfh.addFeature(wf);
	    } catch (ChangeVetoException cve) {
		throw new BioError(
				   cve,
				   "Assertion failure: Should be able to manipulate this FeatureHolder"
				   );
	    }
	}

	if (cachingProjections) {
	    projectedFeatures = sfh;
	}

	return sfh;
    }

    public int countFeatures() {
	return wrapped.countFeatures();
    }

    public Iterator features() {
	return getProjectedFeatures().features();
    }
    
    public boolean containsFeature(Feature f) {
      return getProjectedFeatures().containsFeature(f);
    }

    public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
	return getProjectedFeatures().filter(ff, recurse);
    }

    public FeatureHolder getParent() {
	return parent;
    }

    /**
     * ProjectionContext implementation tied to a given ProjectedFeatureHolder
     */

    private class PFHContext implements ProjectionContext {
	public FeatureHolder getParent(Feature f) {
	    return parent;
	}

	public Sequence getSequence(Feature f) {
	    FeatureHolder fh = parent;
	    while (fh instanceof Feature) {
		fh = ((Feature) fh).getParent();
	    }
	    return (Sequence) fh;
	}

	public Location getLocation(Feature f) {
	    return f.getLocation();
	}

	public StrandedFeature.Strand getStrand(StrandedFeature sf) {
	    return sf.getStrand();
	}

	public Annotation getAnnotation(Feature f) {
	    MergeAnnotation ma = new MergeAnnotation();
	    try {
		ma.addAnnotation(f.getAnnotation());
		if (annotation != null) {
		    ma.addAnnotation(annotation);
		}
	    } catch (ChangeVetoException cve) {
		throw new BioError(cve);
	    }
	    return ma;
	}

	public FeatureHolder projectChildFeatures(Feature f, FeatureHolder parent) {
	    return projectFeatureHolder(f, parent);
	}

	public Feature createFeature(Feature f, Feature.Template templ) 
	    throws ChangeVetoException
	{
	    throw new ChangeVetoException("Can't create features in this projection");
	}

	public void removeFeature(Feature f, Feature f2) 
	    throws ChangeVetoException
	{
	    throw new ChangeVetoException("Can't create features in this projection");
	}
        
    public void addChangeListener(Feature f, ChangeListener cl, ChangeType ct) {}
    public void removeChangeListener(Feature f, ChangeListener cl, ChangeType ct) {}
    }
}
