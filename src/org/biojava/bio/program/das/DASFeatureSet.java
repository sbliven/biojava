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
import java.net.*;
import java.io.*;

import org.biojava.bio.*;
import org.biojava.utils.*;
import org.biojava.utils.cache.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.program.xff.*;

import org.apache.xerces.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.w3c.dom.*;

/**
 * FeatureHolder reflecting features provided by a DAS annotation
 * server.
 *
 * @since 1.1
 * @author Thomas Down
 * @author Matthew Pocock
 */

class DASFeatureSet implements FeatureHolder {
    private FeatureRequestManager.Ticket featureTicket;
    private CacheReference realFeatures;

    private DASSequence refSequence;
    private URL dataSource;
    private String sourceID;
    private String dataSourceString;

    DASFeatureSet(DASSequence seq, URL ds, String id)
        throws BioException
    {
	refSequence = seq;
	dataSource = ds;
	sourceID = id;
	dataSourceString = dataSource.toString();
    }

    void registerFeatureFetcher() {
	if (realFeatures != null && realFeatures.get() == null) {
	    realFeatures = null;
	    featureTicket = null;
	    // System.err.println("*** Real features got cleared out");
	}

	if (featureTicket == null) {
	    SeqIOListener listener = new DASFeatureSetPopulator();
	    FeatureRequestManager frm = refSequence.getParentDB().getFeatureRequestManager();
	    featureTicket = frm.requestFeatures(dataSource, sourceID, listener);
	}
    }

    protected FeatureHolder getFeatures() {
	if (realFeatures != null) {
	    FeatureHolder fh = (FeatureHolder) realFeatures.get();
	    if (fh != null) {
		return fh;
	    }
	}

	try {
	    registerFeatureFetcher();
	    featureTicket.doFetch();
	} catch (ParseException ex) {
	    throw new BioError(ex, "Error parsing feature table");
	} catch (BioException ex) {
	    throw new BioError(ex);
	}

	if (realFeatures == null) {
	    throw new BioError("Assertion failure: features didn't get fetched.");
	}

	FeatureHolder fh = (FeatureHolder) realFeatures.get();
	if (fh == null) {
	    throw new BioError("Assertion failure: cache is stupidly small...");
	}

	return fh;
    }

    public Iterator features() {
	return getFeatures().features();
    }
    
    public boolean containsFeature(Feature f) {
      return getFeatures().containsFeature(f);
    }

    public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
	return getFeatures().filter(ff, recurse);
    }
    
    public int countFeatures() {
	return getFeatures().countFeatures();
    }
    
    public Feature createFeature(Feature.Template temp) 
        throws ChangeVetoException
    {
	throw new ChangeVetoException("Can't create features on DAS sequences.");
    }

    public void removeFeature(Feature f) 
        throws ChangeVetoException
    {
	throw new ChangeVetoException("Can't remove features from DAS sequences.");
    }

    // 
    // Changeable stuff (which we're not, fortunately)
    //

    public void addChangeListener(ChangeListener cl) {}
    public void addChangeListener(ChangeListener cl, ChangeType ct) {}
    public void removeChangeListener(ChangeListener cl) {}
    public void removeChangeListener(ChangeListener cl, ChangeType ct) {}

    //
    // Listener which is responsible for populating this FeatureSet
    //

    private class DASFeatureSetPopulator extends SeqIOAdapter {
	private SimpleFeatureHolder holder;
	private List featureStack = new ArrayList();
	private Feature stackTop = null;
	
	public void startSequence() {
	    holder = new SimpleFeatureHolder();
	}

	public void endSequence() {
	    realFeatures = refSequence.getParentDB().getFeaturesCache().makeReference(holder);
	}

	public void startFeature(Feature.Template temp) 
	    throws ParseException
	{
	    if (temp instanceof ComponentFeature.Template) {
		// I'm not convinced there's an easy, safe, way to say we don't
		// want these server side, so we'll elide them here instead.
		// We push a null onto the stack so that we don't get confused
		// over endFeature().
		
		featureStack.add(null);
	    } else {
		try {
		    Feature f = null;
		    if (temp.annotation == Annotation.EMPTY_ANNOTATION) {
			temp.annotation = new SmallAnnotation();
		    } else {
			if (temp.annotation.containsProperty(XFFFeatureSetHandler.PROPERTY_XFF_ID)) {
			    temp.annotation.setProperty(DASSequence.PROPERTY_FEATUREID,
							temp.annotation.getProperty(XFFFeatureSetHandler.PROPERTY_XFF_ID));
			}
		    }
		    temp.annotation.setProperty(DASSequence.PROPERTY_ANNOTATIONSERVER, dataSource);
		    
		    if (stackTop == null) {
			f = ((RealizingFeatureHolder) refSequence).realizeFeature(refSequence, temp);
			holder.addFeature(f);
		    } else {
			f = stackTop.createFeature(temp);
		    }
		    
		    featureStack.add(f);
		    stackTop = f;
		} catch (Exception ex) {
		    ex.printStackTrace();
		    throw new ParseException(ex, "Couldn't realize feature in DAS");
		}
	    }
	}
	
	public void addFeatureProperty(Object key, Object value)
	    throws ParseException
	{
	    if (stackTop == null) {
		// Feature we're skipping
		return;
	    }

	    try {
		if (key.equals(XFFFeatureSetHandler.PROPERTY_XFF_ID)) {
		    stackTop.getAnnotation().setProperty(DASSequence.PROPERTY_FEATUREID, value);
		} else {
		    stackTop.getAnnotation().setProperty(key, value);
		}
	    } catch (ChangeVetoException ex) {
		throw new ParseException(ex, "Couldn't set feature property");
	    } catch (NullPointerException ex) {
		ex.printStackTrace();
	    }
	}

	public void endFeature()
	    throws ParseException
	{
	    if (featureStack.size() < 1) {
		throw new BioError("Missmatched endFeature()");
	    } else {
		featureStack.remove(featureStack.size() - 1);
		int pos = featureStack.size() - 1;
		stackTop = null;
		while (stackTop == null && pos >= 0) {
		    stackTop = (Feature) featureStack.get(pos--);
		}
	    }
	}
    }
}
