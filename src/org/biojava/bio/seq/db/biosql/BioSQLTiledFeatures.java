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
import org.biojava.utils.cache.*;

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
 * @since 1.3
 */

class BioSQLTiledFeatures implements FeatureHolder, RealizingFeatureHolder
{
    private Sequence seq;
    private BioSQLSequenceDB seqDB;
    private int bioentry_id;

    private Location[]            tileLocations;
    private FeatureTile[]         tileFeatures;
    private SimpleFeatureHolder   overlappingFeatures;
    private MergeFeatureHolder    allFeatures;
	
    BioSQLTiledFeatures(Sequence seq,
			BioSQLSequenceDB seqDB,
			int bioentry_id,
			int tileSize)
    {
	this.seq = seq;
	this.seqDB = seqDB;
	this.bioentry_id = bioentry_id;

	int numTiles = (int) Math.ceil((1.0 * seq.length()) / tileSize);
	tileLocations = new Location[numTiles];
	tileFeatures = new FeatureTile[numTiles];

	try {
	    allFeatures = new MergeFeatureHolder();
	    for (int t = 0; t < numTiles; ++t) {
		tileLocations[t] = new RangeLocation(1 + (t * tileSize),
						     Math.min((t + 1) * tileSize, seq.length()));
		tileFeatures[t] = new FeatureTile(t);
		allFeatures.addFeatureHolder(tileFeatures[t], new FeatureFilter.ContainedByLocation(tileLocations[t]));
	    }
	    
	    overlappingFeatures = new SimpleFeatureHolder();
	    allFeatures.addFeatureHolder(overlappingFeatures);
	} catch (ChangeVetoException ex) {
	    throw new BioError(ex);
	}
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

    private void _addFeature(Feature f) 
        throws ChangeVetoException
    {
	for (int t = 0; t < tileLocations.length; ++t) {
	    if (tileLocations[t].contains(f.getLocation())) {
		tileFeatures[t].addFeature(f);
		return;
	    }
	}

	overlappingFeatures.addFeature(f);
    }

    public Feature createFeature(Feature.Template ft)
        throws ChangeVetoException, BioException
    {
	Feature f = realizeFeature(seq, ft);
	
	BioSQLChangeHub hub = ((BioSQLSequenceI) seq).getSequenceDB().getChangeHub();
	ChangeEvent cev = new ChangeEvent(seq, FeatureHolder.FEATURES, f);
	synchronized (hub) {
	    hub.fireEntryPreChange(cev);
	    seqDB.getFeaturesSQL().persistFeature(f, -1, bioentry_id); // No parent
	    _addFeature(f);
	    hub.fireEntryPostChange(cev);
	}

	return f;
    }

    public void removeFeature(Feature f)
        throws ChangeVetoException
    {
	FeatureHolder fh = overlappingFeatures;
	for (int t = 0; t < tileLocations.length; ++t) {
	    if (tileLocations[t].contains(f.getLocation())) {
		fh = tileFeatures[t];
	    }
	}

        if (!fh.containsFeature(f)) {
            throw new ChangeVetoException("Feature doesn't come from this sequence");
        }
        if (!(f instanceof BioSQLFeature)) {
            throw new ChangeVetoException("This isn't a normal BioSQL feature");
        }
        
        BioSQLChangeHub hub = ((BioSQLSequenceI) seq).getSequenceDB().getChangeHub();
	ChangeEvent cev = new ChangeEvent(seq, FeatureHolder.FEATURES, f);
	synchronized (hub) {
	    hub.fireEntryPreChange(cev);
	    seqDB.getFeaturesSQL().removeFeature((BioSQLFeature) f);
	    fh.removeFeature(f);
	    hub.fireEntryPostChange(cev);
        }
    }

    protected FeatureHolder getFeatures() {
	return allFeatures;
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

    private class TileFeatureReceiver extends BioSQLFeatureReceiver {
	private final Location tileLocation;
	private final SimpleFeatureHolder tileFeatures;

	private TileFeatureReceiver(SimpleFeatureHolder tileFeatures,
				    Location tileLocation)
	{
	    super(seq);
	    this.tileLocation = tileLocation;
	    this.tileFeatures = tileFeatures;
	}

	protected void deliverTopLevelFeature(Feature f)
	    throws ParseException, ChangeVetoException
	{
	    if (LocationTools.contains(tileLocation, f.getLocation())) {
		tileFeatures.addFeature(f);
	    } else {
		if (!overlappingFeatures.containsFeature(f)) {
		    // System.err.println("Adding feature at " + f.getLocation() + " to overlaps");
		    overlappingFeatures.addFeature(f);
		}
	    }
	}
    }

    private class FeatureTile implements FeatureHolder {
	private int tileNumber;
	private CacheReference featuresRef;

	FeatureTile(int tileNumber) {
	    this.tileNumber = tileNumber;
	}

	public Iterator features() {
	    return getTileFeatures().features();
	}
	
	public int countFeatures() {
	    return getTileFeatures().countFeatures();
	}

	public boolean containsFeature(Feature f) {
	    return getTileFeatures().containsFeature(f);
	}

	public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
	    return getTileFeatures().filter(ff, recurse);
	}

	private synchronized SimpleFeatureHolder getTileFeatures() {
	    if (featuresRef != null) {
		SimpleFeatureHolder fh = (SimpleFeatureHolder) featuresRef.get();
		if (fh == null) {
		    // System.err.println("*** Tile cache was cleared: " + tileNumber);
		} else {
		    return fh;
		}
	    }

	    try {
		SimpleFeatureHolder features = new SimpleFeatureHolder();
		FeaturesSQL adaptor = seqDB.getFeaturesSQL();
		adaptor.retrieveFeatures(bioentry_id, 
					 new TileFeatureReceiver(features, tileLocations[tileNumber]),
					 tileLocations[tileNumber],
					 -1,
					 -1);
		featuresRef = seqDB.getTileCache().makeReference(features);
		return features;
	    } catch (SQLException ex) {
		throw new BioRuntimeException(ex, "SQL error while reading features");
	    } catch (BioException ex) {
		throw new BioRuntimeException(ex);
	    } 
	}

	public void addFeature(Feature f) 
	    throws ChangeVetoException
	{
	    getTileFeatures().addFeature(f);
	}

	public void removeFeature(Feature f) 
	    throws ChangeVetoException
	{
	    getTileFeatures().removeFeature(f);
	}
	    
	public Feature createFeature(Feature.Template ft)
	    throws ChangeVetoException, BioException
	{
	    throw new ChangeVetoException();
	}

	// Not changeable
    
	public void addChangeListener(ChangeListener cl) {}
	public void addChangeListener(ChangeListener cl, ChangeType ct) {}
	public void removeChangeListener(ChangeListener cl) {}
	public void removeChangeListener(ChangeListener cl, ChangeType ct) {}
	public boolean isUnchanging(ChangeType ct) {
	    return true;
	}
    }

    
    public void addChangeListener(ChangeListener cl) {}
    public void addChangeListener(ChangeListener cl, ChangeType ct) {}
    public void removeChangeListener(ChangeListener cl) {}
    public void removeChangeListener(ChangeListener cl, ChangeType ct) {}
    public boolean isUnchanging(ChangeType ct) {
	return true;
    }
}
