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

import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

/**
 * Class used by ProjectionEngine to wrap Feature objects.
 *
 * @author Thomas Down
 * @author Matthew Pocock
 * @since 1.1
 */

class ProjectedFeature implements Feature, Projection {
    private transient ChangeSupport changeSupport;

    private final Feature feature;
    private final ProjectionContext context;
    private Location newLocation;
    private FeatureHolder projectedFeatures;

    public ProjectedFeature(Feature f,
			    ProjectionContext ctx)
    {
	this.feature = f;
	this.context = ctx;
	// this.newLocation = holder.getProjectedLocation(f.getLocation());
    }

    protected ChangeSupport getChangeSupport() {
	return changeSupport;
    }

    protected void instantiateChangeSupport() {
	if (changeSupport == null) {
	    changeSupport = new ChangeSupport();
	}
    }

    public Feature getViewedFeature() {
	return feature;
    }

    public ProjectionContext getProjectionContext() {
	return context;
    }

    public Feature.Template makeTemplate() {
	Feature.Template ft = getViewedFeature().makeTemplate();
	ft.location = getLocation();
	ft.annotation = getAnnotation();
	return ft;
    }

    public Location getLocation() {
	if (newLocation == null) {
	    newLocation = context.getLocation(feature);
	}
	return newLocation;
    }

    public FeatureHolder getParent() {
	return context.getParent(feature);
    }

    public Sequence getSequence() {
//  	FeatureHolder fh = getParent();
//  	while (fh instanceof Feature) {
//  	    fh = ((Feature) fh).getParent();
//  	}
	//  	return (Sequence) fh;
	return context.getSequence(feature);
    }

    public String getType() {
	return feature.getType();
    }

    public String getSource() {
	return feature.getSource();
    }

    public Annotation getAnnotation() {
	return context.getAnnotation(feature);
    }

    public SymbolList getSymbols() {
	return feature.getSymbols();
    }

    public int countFeatures() {
	return feature.countFeatures();
    }
    
    public boolean containsFeature(Feature f) {
	if(countFeatures() > 0) {
	    return getProjectedFeatures().containsFeature(f);
	} else {
	    return false;
	}
    }

    protected FeatureHolder getProjectedFeatures() {
	if (projectedFeatures == null) {
	    projectedFeatures = context.projectChildFeatures(feature, this);
	}
	return projectedFeatures;
    }

    public Iterator features() {
	return getProjectedFeatures().features();
    }

    public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
	FeatureFilter membershipFilter = new FeatureFilter.ContainedByLocation(getLocation());
	if (FilterUtils.areDisjoint(ff, membershipFilter)) { 
	    // System.err.println("Wheeeee! Disjunction in ProjectedFeatureWrapper");

	    return FeatureHolder.EMPTY_FEATURE_HOLDER;
	}

	return getProjectedFeatures().filter(ff, recurse);
    }

    public Feature createFeature(Feature.Template temp)
        throws ChangeVetoException
    {
	throw new ChangeVetoException("Can't create subfeatures of projected features (yet).");
    }

    public void removeFeature(Feature f) 
        throws ChangeVetoException
    {
	throw new ChangeVetoException("Can't alter projected features (yet).");
    }
        
    public void addChangeListener(ChangeListener cl) {
	instantiateChangeSupport();
	getChangeSupport().addChangeListener(cl);
    }

    public void addChangeListener(ChangeListener cl, ChangeType ct) {
	instantiateChangeSupport();
	getChangeSupport().addChangeListener(cl, ct);
    }

    public void removeChangeListener(ChangeListener cl) {
	ChangeSupport cs = getChangeSupport();
	if (cs != null)
	    cs.removeChangeListener(cl);
    }

    public void removeChangeListener(ChangeListener cl, ChangeType ct) {
	ChangeSupport cs = getChangeSupport();
	if (cs != null)
	    cs.removeChangeListener(cl, ct);
    }
}
