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
 * @since 1.3
 */

class BioEntryFeatureSet
  extends
    AbstractChangeable
  implements
    FeatureHolder,
    RealizingFeatureHolder
{
    private Sequence seq;
    private BioSQLSequenceDB seqDB;
    private int bioentry_id;
    
    BioEntryFeatureSet(Sequence seq,
		       BioSQLSequenceDB seqDB,
		       int bioentry_id)
    {
	this.seq = seq;
	this.seqDB = seqDB;
	this.bioentry_id = bioentry_id;
    }

    private DBHelper getDBHelper() {
	return seqDB.getDBHelper();
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


    public Feature createFeature(Feature.Template ft)
        throws ChangeVetoException, BioException
    {
	Feature f = realizeFeature(seq, ft);
	if (!hasListeners()) {
	    persistFeature(f, -1);
	    getFeatures().addFeature(f);
	} else {
    ChangeSupport changeSupport = getChangeSupport(FeatureHolder.FEATURES);
	    synchronized (changeSupport) {
		ChangeEvent cev = new ChangeEvent(seq, FeatureHolder.FEATURES, f);
		changeSupport.firePreChangeEvent(cev);
		persistFeature(f, -1); // No parent
		getFeatures().addFeature(f);
		changeSupport.firePostChangeEvent(cev);
	    }
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
        if (!(f instanceof BioSQLFeatureI)) {
            throw new ChangeVetoException("This isn't a normal BioSQL feature");
        }
        
        if (hasListeners()) {
            seqDB.getFeaturesSQL().removeFeature((BioSQLFeatureI) f);
            fh.removeFeature(f);
        } else {
          ChangeSupport changeSupport = getChangeSupport(FeatureHolder.FEATURES);
            synchronized (changeSupport) {
                ChangeEvent cev = new ChangeEvent(seq, FeatureHolder.FEATURES, f);
                changeSupport.firePreChangeEvent(cev);
                seqDB.getFeaturesSQL().removeFeature((BioSQLFeatureI) f);
                fh.removeFeature(f);
                changeSupport.firePostChangeEvent(cev);
            }
        }
    }

    private SimpleFeatureHolder features;

    protected synchronized SimpleFeatureHolder getFeatures() {
	if (features == null) {
	    try {
		features = new SimpleFeatureHolder();
		FeaturesSQL adaptor = seqDB.getFeaturesSQL();
		adaptor.retrieveFeatures(bioentry_id, new FeatureReceiver());
	    } catch (SQLException ex) {
		throw new BioRuntimeException(ex, "SQL error while reading features");
	    } catch (BioException ex) {
		throw new BioRuntimeException(ex);
	    } catch (ChangeVetoException ex) {
		throw new BioError(ex, "Assertion failed: couldn't modify internal FeatureHolder");
	    }
	}

	return features;
    }

    private class FeatureReceiver extends SeqIOAdapter {
	private List stack = new ArrayList();
	
	public void startFeature(Feature.Template templ)
	    throws ParseException
	{
	    BioSQLFeatureI parentFeature = getCurrent();
	    FeatureHolder parent = parentFeature;
	    if (parent == null) {
		parent = seq;
	    }

	    try {
		BioSQLFeatureI newFeature = _realizeFeature(parent, templ);
		if (parentFeature != null) {
		    parentFeature._addFeature(newFeature);
		} else {
		    features.addFeature(newFeature);
		}
	    stack.add(newFeature);
	    } catch (BioException ex) {
		throw new ParseException(ex, "Couldn't realize feature");
	    } catch (ChangeVetoException ex) {
		throw new BioError(ex, "Assertion failed: couldn't modify internal FeatureHolder");
	    }
	}

	public void endFeature()
	    throws ParseException
	{
	    if (stack.size() > 0) {
		stack.remove(stack.size() - 1);
	    } else {
		throw new ParseException("start/end feature messages don't match");
	    }
	}

	public void addFeatureProperty(Object key, Object value)
	     throws ParseException
	{
	    if ("_biosql_internal.feature_id".equals(key)) {
		Integer fid = (Integer) value;
		getCurrent()._setInternalID(fid.intValue());
		getCurrent()._setAnnotation(new BioSQLFeatureAnnotation(seqDB, fid.intValue()));
	    }
	}

	private BioSQLFeatureI getCurrent() {
	    if (stack.size() > 0) {
		return (BioSQLFeatureI) stack.get(stack.size() - 1);
	    } else {
		return null;
	    }
	}
    }

    //
    // implements RealizingFeatureHolder
    //

    private BioSQLFeatureI _realizeFeature(FeatureHolder parent, Feature.Template templ)
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
    // Feature persistance
    //

    void persistFeature(Feature f, int parent_id)
        throws BioException
    {
	Connection conn = null;
	try {
	    conn = seqDB.getPool().takeConnection();
	    conn.setAutoCommit(false);
	    int f_id = seqDB.getFeaturesSQL().persistFeature(conn, bioentry_id, f, parent_id);
	    if (f instanceof BioSQLFeatureI) {
		((BioSQLFeatureI) f)._setInternalID(f_id);
		((BioSQLFeatureI) f)._setAnnotation(new BioSQLFeatureAnnotation(seqDB, f_id));
	    }
	    conn.commit();
	    seqDB.getPool().putConnection(conn);
	} catch (SQLException ex) {
	    boolean rolledback = false;
	    if (conn != null) {
		try {
		    conn.rollback();
		    rolledback = true;
		} catch (SQLException ex2) {}
	    }
	    throw new BioException(ex, "Error adding BioSQL tables" + (rolledback ? " (rolled back successfully)" : ""));
	}
    }
}
