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

import java.sql.*;
import java.util.*;

import org.biojava.utils.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;

/**
 * Sequence keyed off a BioSQL biosequence.
 *
 * @author Thomas Down
 * @since 1.3
 */

class BioSQLComponentFeature implements ComponentFeature {
    private BioSQLSequenceDB        seqDB;
    private Sequence                parent;
    private Sequence                componentSequence = null;
    private SymbolList              contigedSymbols = null;
    private Location                location;
    private Location                componentLocation;
    private String                  componentName;
    private FeatureHolder           projectedFeatures;
    private StrandedFeature.Strand  strand;
    private int                     assemblyFragmentID;
    private String                  type;
    private String                  source;
    private boolean                 triedResolve = false;
    
    BioSQLComponentFeature(BioSQLSequenceDB seqDB,
			   Sequence parent,
			   ComponentFeature.Template temp,
			   int assemblyFragmentID)
    {
	this.seqDB = seqDB;
	this.parent = parent;
	this.location = temp.location;
	this.componentLocation = temp.componentLocation;
	this.componentName = temp.componentSequenceName;
	this.type = temp.type;
	this.source = temp.source;
	this.strand = temp.strand;
    }

    public Sequence getSequence() {
	return parent;
    }

    public FeatureHolder getParent() {
	return parent;
    }

    public Strand getStrand() {
	return strand;
    }

    public SymbolList getSymbols() {
	if (contigedSymbols == null) {
	    try {
		contigedSymbols = getComponentSequence().subList(componentLocation.getMin(), componentLocation.getMax());
	    } catch (Exception ex) {
		throw new BioError(ex);
	    }
	}

	return contigedSymbols;
    }

    public Location getLocation() {
	return location;
    }

    public Location getComponentLocation() {
	return componentLocation;
    }

    public boolean isComponentResolvable() {
	return (getComponentSequence() != null);
    }

    public String getComponentSequenceName() {
	return componentName;
    }

    public Sequence getComponentSequence() {
	if (!triedResolve) {
	    try {
		componentSequence = seqDB.getSequence(componentName);
	    } catch (IllegalIDException ex) {
		// Didn't exist.  Don't worry.
	    } catch (BioException ex) {
		throw new BioRuntimeException(ex, "Error fetching component sequence");
	    }
	}
	return componentSequence;
    }

    public String getType() {
	return type;
    }

    public String getSource() {
	return source;
    }

    public Feature.Template makeTemplate() {
	throw new BioError("FIXME");
    }

    public Annotation getAnnotation() {
	return Annotation.EMPTY_ANNOTATION;
    }

    public int countFeatures() {
	return getProjectedFeatures().countFeatures();
    }

    public boolean containsFeature(Feature f) {
	return getProjectedFeatures().containsFeature(f);
    }

    public Feature createFeature(Feature.Template temp)
        throws ChangeVetoException
    {
	throw new ChangeVetoException("Can't create features on components -- edit the underlying sequence instead");
    }

    public void removeFeature(Feature f)
        throws ChangeVetoException
    {
	throw new ChangeVetoException("Can't remove features from components -- edit the underlying sequence instead");
    }

    public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
	if (FilterUtils.areDisjoint(ff, new FeatureFilter.ByParent(new FeatureFilter.ByClass(ComponentFeature.class)))) {
	    // System.err.println("*** Disjunction descending through components");
	    return FeatureHolder.EMPTY_FEATURE_HOLDER;
	} else {
	    return getProjectedFeatures().filter(ff, recurse);
	}
    }

    public Iterator features() {
	return getProjectedFeatures().features();
    }

    protected FeatureHolder getProjectedFeatures() {
	if (projectedFeatures == null) {
	    int translation;
	    boolean flip;
	    if (strand == StrandedFeature.NEGATIVE) {
		translation = location.getMax() + componentLocation.getMin();
		flip = true;
	    } else  if (strand == StrandedFeature.POSITIVE) {
		translation = location.getMin() - componentLocation.getMin();
		flip = false;
	    } else {
		throw new BioError("No strand -- erk!");
	    }

	    FeatureHolder child = getComponentSequence();
	    if (child != null) {
		projectedFeatures = new ProjectedFeatureHolder(getComponentSequence(),
							       this,
							       translation,
							       flip);
	    } else {
		projectedFeatures = FeatureHolder.EMPTY_FEATURE_HOLDER;
	    }
	} 

	return projectedFeatures;
    }
    

    // 
    // Changeable stuff (which we're not, yet)
    //

    public void addChangeListener(ChangeListener cl) {}
    public void addChangeListener(ChangeListener cl, ChangeType ct) {}
    public void removeChangeListener(ChangeListener cl) {}
    public void removeChangeListener(ChangeListener cl, ChangeType ct) {}
}
