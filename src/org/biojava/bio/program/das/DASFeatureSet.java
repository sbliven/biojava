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
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;

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
 */

class DASFeatureSet implements FeatureHolder {
    private SimpleFeatureHolder realFeatures;
    private final Sequence refSequence;
    private final URL dataSource;
    private final String sourceID;

    DASFeatureSet(Sequence seq, URL ds, String id)
        throws BioException
    {
	refSequence = seq;
	dataSource = ds;
	sourceID = id;
    }

    protected FeatureHolder getFeatures() {
	if (realFeatures != null)
	    return realFeatures;

	try {
	    realFeatures = new SimpleFeatureHolder();

	    URL fURL = new URL(dataSource, "features?ref=" + sourceID);
	    DASGFFParser.INSTANCE.parseURL(fURL, new SeqIOAdapter() {
		    public void startFeature(Feature.Template temp) 
		        throws ParseException
		    {
			if (temp instanceof ComponentFeature.Template) {
			    // I'm not convinced there's an easy, safe, way to say we don't
			    // want these server side, so we'll elide them here instead.

			    return;
			}

			try {
			    Feature f = ((RealizingFeatureHolder) refSequence).realizeFeature(refSequence, temp);
			    realFeatures.addFeature(f);
			} catch (Exception ex) {
			    throw new ParseException(ex, "Couldn't realize feature in DAS");
			}
		    }
		} );
	    
	} catch (IOException ex) {
	    throw new BioError(ex, "Error connecting to DAS server");
	} catch (ParseException ex) {
	    throw new BioError(ex, "Error parsing feature table");
	} catch (BioException ex) {
	    throw new BioError(ex);
	}

	return realFeatures;
    }

    public Iterator features() {
	return getFeatures().features();
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
}
