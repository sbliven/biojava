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

import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

/**
 * Class used by ProjectedFeatureHolder to wrap Feature objects.
 *
 * @author Thomas Down
 * @author Matthew Pocock
 * @since 1.1
 */

public class ProjectedFeatureWrapper implements Feature {
    private transient ChangeSupport changeSupport;

    private final Feature feature;
    private final ProjectedFeatureHolder holder;
    private final Location newLocation;
    private FeatureHolder projectedFeatures;

    public ProjectedFeatureWrapper(Feature f,
				   ProjectedFeatureHolder holder)
    {
	this.feature = f;
	this.holder = holder;
	this.newLocation = holder.getProjectedLocation(f.getLocation());
    }

    protected ChangeSupport getChangeSupport() {
	return changeSupport;
    }

    protected void instantiateChangeSupport() {
	if (changeSupport == null)
	    changeSupport = new ChangeSupport();
    }

    public Feature getViewedFeature() {
	return feature;
    }

    public Feature.Template makeTemplate() {
      return getFeature().makeTemplate();
    }
    
    public Feature getFeature() {
	return feature;
    }

    public Location getLocation() {
	return newLocation;
    }

    public FeatureHolder getParent() {
	return holder.getParent();
    }

    public Sequence getSequence() {
	FeatureHolder fh = getParent();
	while (fh instanceof Feature) {
	    fh = ((Feature) fh).getParent();
	}
	return (Sequence) fh;
    }

    public String getType() {
	return feature.getType();
    }

    public String getSource() {
	return feature.getSource();
    }

    public Annotation getAnnotation() {
	return feature.getAnnotation();
    }

    public SymbolList getSymbols() {
	return feature.getSymbols();
    }

    public int countFeatures() {
	return feature.countFeatures();
    }

    protected FeatureHolder getProjectedFeatures() {
	if (projectedFeatures == null) {
	    projectedFeatures = new ProjectedFeatureHolder(feature,
							   this,
							   holder.getTranslation(),
							   holder.isOppositeStrand());
	}
	return projectedFeatures;
    }

    protected ProjectedFeatureHolder getProjectingFeatureHolder() {
	return holder;
    }

    public Iterator features() {
	return getProjectedFeatures().features();
    }

    public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
	return getProjectedFeatures().filter(ff, recurse);
    }

    public Feature createFeature(Feature.Template temp) throws BioException {
	throw new BioException("Can't create subfeatures of projected features");
    }

    public void removeFeature(Feature f) {
	throw new UnsupportedOperationException("Projected features don't have children (yet).");
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
