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

import java.util.*;

import org.biojava.bio.symbol.*;
import org.biojava.bio.*;

/**
 * Class used by ProjectedFeatureHolder to wrap Feature objects.
 *
 * @author Thomas Down
 * @since 1.1
 */

class ProjectedFeatureWrapper implements Feature {
    private final Feature feature;
    private final FeatureHolder inParent;
    private final Location newLocation;
    private final int translation;
    private FeatureHolder projectedFeatures;

    public ProjectedFeatureWrapper(Feature f,
				   FeatureHolder tParent,
				   int offset)
    {
	this.feature = f;
	this.inParent = tParent;
	this.translation = offset;
	this.newLocation = f.getLocation().translate(offset);
    }

    public Feature getFeature() {
	return feature;
    }

    public Location getLocation() {
	return newLocation;
    }

    public FeatureHolder getParent() {
	return inParent;
    }

    public Sequence getSequence() {
	FeatureHolder fh = inParent;
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
							   this, translation);
	}
	return projectedFeatures;
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
}
