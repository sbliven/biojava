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
 * @author Matthew Pocock
 */

class DASComponentFeature
  extends
    Unchangeable
  implements
    ComponentFeature
{
    private final DASSequence parent;

    private FeatureHolder projectedFeatures;

    private final Location location;
    private final StrandedFeature.Strand strand;
    private final String type;
    private final String source;
    private final Annotation annotation;

    private String componentID;

    private DASSequence componentSequence;
    private Location componentLocation;

    private final FeatureFilter membershipFilter;

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
	this.annotation = temp.annotation;

	membershipFilter = new FeatureFilter.ContainedByLocation(this.location);

	if (temp.componentSequence != null) {
	    componentSequence = (DASSequence) temp.componentSequence;
	    componentID = componentSequence.getName();
	} else {
	    componentID = temp.componentSequenceName;
	    if (componentID == null) {
		try {
		    componentID = (String) temp.annotation.getProperty("sequence.id");
		} catch (NoSuchElementException ex) {
		    throw new BioRuntimeException("No sequence.id property");
		}
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

    public boolean isComponentResolvable() {
	return true;
    }

    public String getComponentSequenceName() {
	return componentID;
    }

    public StrandedFeature.Strand getStrand() {
	return strand;
    }

    public Location getLocation() {
	return location;
    }
    
    public void setLocation(Location loc)
    throws ChangeVetoException {
      throw new ChangeVetoException(
        new ChangeEvent(this, LOCATION, loc, this.location),
        "Can't change location as it is immutable"
      );
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

    public void setSource(String source)
    throws ChangeVetoException {
      throw new ChangeVetoException(
        new ChangeEvent(this, TYPE, source, this.source),
        "Can't change source as it is immutable"
      );
    }

    public String getType() {
	return type;
    }

    public void setType(String type)
    throws ChangeVetoException {
      throw new ChangeVetoException(
        new ChangeEvent(this, TYPE, type, this.type),
        "Can't change type as it is immutable"
      );
    }

    public Annotation getAnnotation() {
	return annotation;
    }

    public SymbolList getSymbols() {
	SymbolList syms = componentLocation.symbols(getComponentSequence()); 
//  	if (strand == StrandedFeature.NEGATIVE) {
//  	    try {
//  		syms = DNATools.reverseComplement(syms);
//  	    } catch (IllegalAlphabetException ex) {
//  		throw new BioRuntimeException(ex);
//  	    }
//  	}
	return syms;
    }

    DASSequence getSequenceLazy() {
	return componentSequence;
    }

    public Sequence getComponentSequence() {
	return _getComponentSequence();
    }

    private DASSequence _getComponentSequence() {
	if (componentSequence == null) {
	    try {
		componentSequence = parent.getParentDB()._getSequence(componentID, parent.dataSourceURLs());
	    } catch (Exception ex) {
		throw new BioRuntimeException(ex, "Couldn't create child DAS sequence");
	    }
	}
	return componentSequence;
    }

    public Location getComponentLocation() {
	return componentLocation;
    }

    //  protected FeatureHolder getProjectedFeatures() {
//  	if (projectedFeatures == null) {
//  	    if (strand == StrandedFeature.NEGATIVE) {
//  		int translation = location.getMax() + componentLocation.getMin();
//  		this.projectedFeatures = new ProjectedFeatureHolder(getComponentSequence(), this, translation, true);
//  	    } else  if (strand == StrandedFeature.POSITIVE) {
//  		int translation = location.getMin() - componentLocation.getMin();
//  		this.projectedFeatures = new ProjectedFeatureHolder(getComponentSequence(), this, translation, false);
//  	    } 
//  	}
//  	return projectedFeatures;
//      }

    
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


	    projectedFeatures = new ProjectedFeatureHolder(getComponentSequence(), this, translation, flip);
	}
	return projectedFeatures;
    }

    public int countFeatures() {
	return getComponentSequence().countFeatures();
    }

    public Iterator features() {
        // System.err.println("Going to iterate over DASComponentFeature: " + getComponentSequenceName());
        return getProjectedFeatures().features();
    }
    
    public boolean containsFeature(Feature f) {
      return getProjectedFeatures().containsFeature(f);
    }

    public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
        // System.err.println("Filtering in DASComponentFEature:" + getComponentSequenceName());
	    if (FilterUtils.areDisjoint(ff, membershipFilter)) { 
            // System.err.println("Wheeeee! Disjunction in DASComponentFeature");

            return FeatureHolder.EMPTY_FEATURE_HOLDER;
        }

        return getProjectedFeatures().filter(ff, recurse);
    }
    
    public FeatureHolder filter(FeatureFilter ff) {
	    if (FilterUtils.areDisjoint(ff, membershipFilter)) { 
            return FeatureHolder.EMPTY_FEATURE_HOLDER;
        }

        return getProjectedFeatures().filter(ff);
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
	ComponentFeature.Template temp = new ComponentFeature.Template();
	temp.type = getType();
	temp.source = getSource();
	temp.location = getLocation();
	temp.annotation = getAnnotation();
	temp.strand = getStrand();
	temp.componentLocation = getComponentLocation();
	temp.componentSequenceName = componentID;
	// temp.componentSequence = getComponentSequence();

	return temp;
    }
    
    public FeatureFilter getSchema() {
        return new FeatureFilter.ByParent(new FeatureFilter.ByFeature(this));
    }
}
