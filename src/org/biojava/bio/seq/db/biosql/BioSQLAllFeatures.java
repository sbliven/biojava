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
 * Top-level SeqFeature set for a BioEntry
 *
 * @author Thomas Down
 * @author Matthew Pocock
 * @author David Huen
 * @since 1.3
 */

class BioSQLAllFeatures implements FeatureHolder, RealizingFeatureHolder
{
    private Sequence seq;
    private BioSQLSequenceDB seqDB;
    private int bioentry_id;
    
    BioSQLAllFeatures(Sequence seq,
		      BioSQLSequenceDB seqDB,
		      int bioentry_id)
    {
	this.seq = seq;
	this.seqDB = seqDB;
	this.bioentry_id = bioentry_id;
    }

    public FeatureFilter getSchema() {
        return FeatureFilter.top_level;
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

    public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
        return getFeatures().filter(ff, recurse);
    }

    public FeatureHolder filter(FeatureFilter ff) {
        return getFeatures().filter(ff);
    }

    public Feature createFeature(Feature.Template ft)
        throws ChangeVetoException, BioException
    {
	Feature f = realizeFeature(seq, ft);
	
	BioSQLEntryChangeHub entryHub = ((BioSQLSequenceI) seq).getSequenceDB().getEntryChangeHub();
	ChangeEvent cev = new ChangeEvent(seq, FeatureHolder.FEATURES, f);
	synchronized (entryHub) {
	    entryHub.firePreChange(cev);
	    seqDB.getFeaturesSQL().persistFeature(f, -1, bioentry_id); // No parent
	    getFeatures().addFeature(f);
	    entryHub.firePostChange(cev);
	}

	return f;
    }

    public void removeFeature(Feature f)
        throws ChangeVetoException
    {
	FeatureHolder fh = getFeatures();
        if (!fh.containsFeature(f)) {
            throw new ChangeVetoException("Feature doesn't come from this sequence");
        }
        if (!(f instanceof BioSQLFeature)) {
            throw new ChangeVetoException("This isn't a normal BioSQL feature");
        }
        
        BioSQLEntryChangeHub entryHub = ((BioSQLSequenceI) seq).getSequenceDB().getEntryChangeHub();
	ChangeEvent cev = new ChangeEvent(seq, FeatureHolder.FEATURES, f);
	synchronized (entryHub) {
	    entryHub.firePreChange(cev);
	    seqDB.getFeaturesSQL().removeFeature((BioSQLFeature) f);
	    fh.removeFeature(f);
	    entryHub.firePostChange(cev);
        }
    }

    private SimpleFeatureHolder features;

    protected synchronized SimpleFeatureHolder getFeatures() {
	if (features == null) {
	    try {
		features = new SimpleFeatureHolder();
		FeaturesSQL adaptor = seqDB.getFeaturesSQL();
		adaptor.retrieveFeatures(bioentry_id, new FeatureReceiver(), null, -1, -1);
	    } catch (SQLException ex) {
		throw new BioRuntimeException(ex, "SQL error while reading features");
	    } catch (BioException ex) {
		throw new BioRuntimeException(ex);
	    } 
	}

	return features;
    }

    private class FeatureReceiver extends BioSQLFeatureReceiver {
	FeatureReceiver() {
	    super(seq);
	}

	protected void deliverTopLevelFeature(Feature f)
	    throws ParseException, ChangeVetoException
	{
	    features.addFeature(f);
	}
    }

    //
    // implements RealizingFeatureHolder
    //

    private BioSQLFeature _realizeFeature(FeatureHolder parent, Feature.Template templ)
        throws BioException
    {
	if (parent != seq && !seqDB.isHierarchySupported()) {
	    throw new BioException("This database doesn't support feature hierarchy.  Please create a seqfeature_relationship table");
	}

	if (templ instanceof StrandedFeature.Template && seq.getAlphabet() == DNATools.getDNA()) {
	    return new BioSQLStrandedFeature(seq, parent, (StrandedFeature.Template) templ);
	} else {
	    return new BioSQLFeature(seq, parent, templ);
	}
    }

    public Feature realizeFeature(FeatureHolder parent, Feature.Template templ)
        throws BioException
    {
	return _realizeFeature(parent, templ);
    }

    //
    // Dummy Changable implementation
    //
    
    public void addChangeListener(ChangeListener cl) {}
    public void addChangeListener(ChangeListener cl, ChangeType ct) {}
    public void removeChangeListener(ChangeListener cl) {}
    public void removeChangeListener(ChangeListener cl, ChangeType ct) {}
    public boolean isUnchanging(ChangeType ct) {
	return true;
    }
}
