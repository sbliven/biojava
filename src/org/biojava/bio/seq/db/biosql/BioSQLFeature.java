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

import java.util.*;
import java.sql.*;

import org.biojava.utils.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.*;

class BioSQLFeature implements Feature, RealizingFeatureHolder {
    private Annotation _annotation;
    private int id;

    // Feature stuff

    private String type;
    private String source;
    private Location location;

    // Relationship to sequences

    private int parentID = -1;
    private final BioSQLSequenceI sequence;

    // Children

    private SimpleFeatureHolder childFeatures;

    BioSQLFeature(Sequence seq,
		  Feature.Template templ)
	throws IllegalArgumentException, IllegalAlphabetException
    {
	this.type = templ.type;
	this.source = templ.source;
	this.location = templ.location;

	this.sequence = (BioSQLSequenceI) seq;

	_annotation = templ.annotation;
    }

    BioSQLFeature(Sequence seq,
		  FeatureHolder parent,
		  Feature.Template templ)
	throws IllegalArgumentException, IllegalAlphabetException
    {
	this(seq, templ);
	if (parent instanceof BioSQLFeature) {
	    parentID = ((BioSQLFeature) parent)._getInternalID();
	} else {
	    parentID = -1;
	}
    }

    public void hintChildFree() {
	if (childFeatures == null) {
	    childFeatures = new SimpleFeatureHolder();
	}
    }

    public void setParentID(int i) {
	this.parentID = i;
    }

    public void setType(String s)
        throws ChangeVetoException
    {
	BioSQLChangeHub hub = sequence.getSequenceDB().getChangeHub();
	ChangeEvent cev = new ChangeEvent(this, Feature.TYPE, getType(), s);
	synchronized (hub) {
	    hub.fireFeaturePreChange(cev);
	    try {
		((BioSQLSequenceI) getSequence()).getSequenceDB().getFeaturesSQL().setFeatureType(id, s);
	    } catch (SQLException ex) {
		throw new BioRuntimeException(ex, "Error updating feature in database");
	    }
	    this.type = s;
	    hub.fireFeaturePostChange(cev);
	}
    }

    public String getType() {
	return type;
    }

    public void setSource(String s)
        throws ChangeVetoException
    {
	BioSQLChangeHub hub = sequence.getSequenceDB().getChangeHub();
	ChangeEvent cev = new ChangeEvent(this, Feature.SOURCE, getSource(), s);
	synchronized (hub) {
	    hub.fireFeaturePreChange(cev);
	    try {
		((BioSQLSequenceI) getSequence()).getSequenceDB().getFeaturesSQL().setFeatureSource(id, s);
	    } catch (SQLException ex) {
		throw new BioRuntimeException(ex, "Error updating feature in database");
	    }
	    this.source = s;
	    hub.fireFeaturePostChange(cev);
	}
    }

    public String getSource() {
	return source;
    }

    public void setLocation(Location l)
        throws ChangeVetoException
    {
	BioSQLChangeHub hub = sequence.getSequenceDB().getChangeHub();
	ChangeEvent cev = new ChangeEvent(this, Feature.LOCATION, getLocation(), l);
	synchronized (hub) {
	    hub.fireFeaturePreChange(cev);
	    try {
		((BioSQLSequenceI) getSequence()).getSequenceDB().getFeaturesSQL().setFeatureLocation(id, l, StrandedFeature.UNKNOWN);
	    } catch (SQLException ex) {
		throw new BioRuntimeException(ex, "Error updating feature in database");
	    }
	    this.location = l;
	    hub.fireFeaturePostChange(cev);
	}
    }

    public Location getLocation() {
	return location;
    }

    public FeatureHolder getParent() {
	if (parentID == -1) {
	    return sequence;
	} else {
	    return sequence.getSequenceDB().getFeatureByID(parentID);
	}
    }

    public Sequence getSequence() {
	return sequence;
    }

    public void _setAnnotation(Annotation a) {
	_annotation = a;
    }

    public Feature realizeFeature(FeatureHolder fh, Feature.Template templ)
        throws BioException
    {
	try {
	    RealizingFeatureHolder rfh = (RealizingFeatureHolder) getParent();
	    return rfh.realizeFeature(fh, templ);
	} catch (ClassCastException ex) {
	    throw new BioException("Couldn't propagate feature creation request.");
	}
    }

    public Feature createFeature(Feature.Template templ)
        throws BioException, ChangeVetoException
    {
	Feature f = realizeFeature(this, templ);
	
	BioSQLChangeHub hub = sequence.getSequenceDB().getChangeHub();
	ChangeEvent cev = new ChangeEvent(this, FeatureHolder.FEATURES, f, null);
	synchronized (hub) {
	    hub.fireFeaturePreChange(cev);
	    getFeatures().addFeature(f);
	    ((BioSQLSequenceI) getSequence()).persistFeature(f, id);
	    hub.fireFeaturePostChange(cev);
	}
	return f;
    }

    public void removeFeature(Feature f)
        throws ChangeVetoException
    {
	BioSQLChangeHub hub = sequence.getSequenceDB().getChangeHub();
	ChangeEvent cev = new ChangeEvent(this, FeatureHolder.FEATURES, null, f);
	synchronized (hub) {
	    hub.fireFeaturePreChange(cev);
	    getFeatures().removeFeature(f);
	    ((BioSQLSequenceI) getSequence()).getSequenceDB().getFeaturesSQL().removeFeature((BioSQLFeature) f);
	    hub.fireFeaturePostChange(cev);
	}
    }

    public Annotation getAnnotation() {
	return _annotation;
    }

    public void _setInternalID(int i) {
	this.id = i;
    }

    public int _getInternalID() {
	return id;
    }

    public synchronized void _addFeature(Feature f) 
        throws ChangeVetoException
    {
	if (childFeatures == null) {
	    childFeatures = new SimpleFeatureHolder();
	}
	childFeatures.addFeature(f);
    }

    protected void fillTemplate(Feature.Template template) {
	template.source = source;
	template.type = type;
	template.location = location;
	template.annotation = _annotation;
    }

    public Feature.Template makeTemplate() {
	Feature.Template template = new Feature.Template();
	fillTemplate(template);
	return template;
    }

    public Iterator features() {
	return getFeatures().features();
    }

    public int countFeatures() {
	return getFeatures().countFeatures();
    }

    public boolean containsFeature(Feature f) {
	return getFeatures().containsFeature(f);
    }

    public FeatureFilter getSchema() {
        return new FeatureFilter.ByParent(new FeatureFilter.ByFeature(this));
    }
    
    public FeatureHolder filter(FeatureFilter ff) {
        FeatureFilter childFilter = new FeatureFilter.And(new FeatureFilter.ContainedByLocation(getLocation()),
                                                          new FeatureFilter.Not(FeatureFilter.top_level));
                                                          
        if (FilterUtils.areDisjoint(ff, childFilter)) {
            return FeatureHolder.EMPTY_FEATURE_HOLDER;
        } else {
            return getFeatures().filter(ff);
        }
    }
    
    public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
        FeatureFilter childFilter = new FeatureFilter.ContainedByLocation(getLocation());
        if (FilterUtils.areDisjoint(ff, childFilter)) {
            return FeatureHolder.EMPTY_FEATURE_HOLDER;
        } else {
            return getFeatures().filter(ff, recurse);
        }
    }

    private class FeatureReceiver extends BioSQLFeatureReceiver {
	FeatureReceiver() {
	    super(sequence);
	}

	protected void deliverTopLevelFeature(Feature f)
	    throws ParseException, ChangeVetoException
	{
	    childFeatures.addFeature(f);
	}
    }

    private synchronized SimpleFeatureHolder getFeatures() {
	if (childFeatures == null) {
	    try {
		BioSQLSequenceI seqi = (BioSQLSequenceI) sequence;
		childFeatures = new SimpleFeatureHolder();
		FeaturesSQL adaptor = seqi.getSequenceDB().getFeaturesSQL();
		adaptor.retrieveFeatures(seqi.getBioEntryID(),
					 new FeatureReceiver(),
					 null,
					 id,
					 -1);
	    } catch (SQLException ex) {
		throw new BioRuntimeException(ex, "SQL error while reading features");
	    } catch (BioException ex) {
		throw new BioRuntimeException(ex);
	    } 
	}

	return childFeatures;
    }

    public SymbolList getSymbols() {
	return getLocation().symbols(getSequence());
    }

    public int hashCode() {
	return makeTemplate().hashCode();
    }

//      public boolean equals(Object o) {
//  	if (! (o instanceof Feature)) {
//  	    return false;
//  	}

//  	Feature fo = (Feature) o;
//  	if (! fo.getSequence().equals(getSequence())) 
//  	    return false;
    
//  	return makeTemplate().equals(fo.makeTemplate());
//      }
    
    public void addChangeListener(ChangeListener cl) {
	addChangeListener(cl, ChangeType.UNKNOWN);
    }
    
    public void addChangeListener(ChangeListener cl, ChangeType ct) {
	sequence.getSequenceDB().getChangeHub().addFeatureListener(id, cl, ct);
    }

    public void removeChangeListener(ChangeListener cl) {
	removeChangeListener(cl, ChangeType.UNKNOWN);
    }

    public void removeChangeListener(ChangeListener cl, ChangeType ct) {
	sequence.getSequenceDB().getChangeHub().removeFeatureListener(id, cl, ct);
    }

    public boolean isUnchanging(ChangeType ct) {
	return false;
    }
} 
