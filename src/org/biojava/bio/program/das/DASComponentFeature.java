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

package org.biojava.bio.program.das;

import java.util.*;

import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.*;
import org.biojava.utils.*;

/**
 * Component feature mapping a DAS landmark sequence onto its parent.
 *
 * @author Thomas Down
 */

class DASComponentFeature implements ComponentFeature {
    private final DASSequence parent;

    private FeatureHolder projectedFeatures;

    private final Location location;
    private final StrandedFeature.Strand strand;
    private final String type;
    private final String source;

    private final String componentID;

    private Sequence componentSequence;
    private Location componentLocation;

    public DASComponentFeature(DASSequence parent,
			       ComponentFeature.Template temp)
        throws BioException
    {
	if (locationContent(temp.location) != locationContent(temp.componentLocation))
  	{
  	    throw new BioException("Component and container locations must contain an equal number of symbols.");
  	}

  	if (!temp.location.isContiguous() || !temp.componentLocation.isContiguous()) {
  	    throw new BioException("Can only include contiguous segments in an assembly");
  	}

	this.parent = parent;
	
	this.location = temp.location;
	this.componentLocation = temp.componentLocation;
	this.strand = temp.strand;
	this.type = temp.type;
	this.source = temp.source;

	if (temp.componentSequence != null) {
	    componentSequence = temp.componentSequence;
	    componentID = componentSequence.getName();
	} else {
	    try {
		componentID = (String) temp.annotation.getProperty("sequence.id");
	    } catch (NoSuchElementException ex) {
		throw new BioError("No sequence.id property");
	    }
	}

	if (strand != StrandedFeature.NEGATIVE && strand != StrandedFeature.POSITIVE) {
	    throw new BioException("Strand must be specified when creating a ComponentFeature");
	}
    }

    private int locationContent(Location l) {
	if (l.isContiguous())
	    return l.getMax() - l.getMin() + 1;
	int content = 0;
	for (Iterator i = l.blockIterator(); i.hasNext(); ) {
	    Location sl = (Location) i.next();
	    content += (sl.getMax() - sl.getMin() + 1);
	}
	return content;
    }

    public StrandedFeature.Strand getStrand() {
	return strand;
    }

    public Location getLocation() {
	return location;
    }

    public FeatureHolder getParent() {
	return parent;
    }

    public Sequence getSequence() {
	return parent;
    }

    public String getSource() {
	return source;
    }

    public String getType() {
	return type;
    }

    public Annotation getAnnotation() {
	return Annotation.EMPTY_ANNOTATION;
    }

    public SymbolList getSymbols() {
	SymbolList syms = componentLocation.symbols(getComponentSequence()); 
	if (strand == StrandedFeature.NEGATIVE) {
	    try {
		syms = DNATools.reverseComplement(syms);
	    } catch (IllegalAlphabetException ex) {
		throw new BioError(ex);
	    }
	}
	return syms;
    }

    public Sequence getComponentSequence() {
	if (componentSequence == null) {
	    try {
		componentSequence = new DASSequence(parent.getParentDB(), parent.getDataSourceURL(), componentID, parent.getName());
	    } catch (Exception ex) {
		throw new BioError(ex, "Couldn't create child DAS sequence");
	    }
	}
	return componentSequence;
    }

    public Location getComponentLocation() {
	return componentLocation;
    }

    protected FeatureHolder getProjectedFeatures() {
	if (projectedFeatures == null) {
	    if (strand == StrandedFeature.NEGATIVE) {
		int translation = location.getMax() + componentLocation.getMin();
		this.projectedFeatures = new ProjectedFeatureHolder(getComponentSequence(), this, translation, true);
	    } else  if (strand == StrandedFeature.POSITIVE) {
		int translation = location.getMin() - componentLocation.getMin();
		this.projectedFeatures = new ProjectedFeatureHolder(getComponentSequence(), this, translation, false);
	    } 
	}
	return projectedFeatures;
    }

    public int countFeatures() {
	return getComponentSequence().countFeatures();
    }

    public Iterator features() {
	return getProjectedFeatures().features();
    }

    public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
	Location l = null;

	if (ff instanceof FeatureFilter.ContainedByLocation) {
	    l = ((FeatureFilter.ContainedByLocation) ff).getLocation();
	} else if (ff instanceof FeatureFilter.OverlapsLocation) {
	    l = ((FeatureFilter.OverlapsLocation) ff).getLocation();
	}

	if (l != null && !l.overlaps(getLocation())) {
	    // None of our children are of interest...
	    return FeatureHolder.EMPTY_FEATURE_HOLDER;
	}

	return getProjectedFeatures().filter(ff, recurse);
    }

    public Feature createFeature(Feature.Template temp)
        throws BioException
    {
	throw new BioException("Can't create features in a ComponentFeature (yet?)");
    }

    public void removeFeature(Feature f)
    {
	throw new UnsupportedOperationException("Can't remove features from a ComponentFeature.");
    }

    public Feature.Template makeTemplate() {
	throw new BioError("FIXME");
    }

    // 
    // Changeable stuff (which we're not)
    //

    public void addChangeListener(ChangeListener cl) {}
    public void addChangeListener(ChangeListener cl, ChangeType ct) {}
    public void removeChangeListener(ChangeListener cl) {}
    public void removeChangeListener(ChangeListener cl, ChangeType ct) {}
}
