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

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.bio.symbol.*;

import org.apache.xerces.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.w3c.dom.*;

/**
 * Sequence reflecting a DAS reference sequence, possibly
 * decorated with one of more annotation sets.
 *
 * <p>
 * This is an first-pass implementation.  In future, I hope
 * to add query optimization for better performance on large
 * sequences, and pluggable transducers to parameterize the
 * creation of BioJava features.
 * </p>
 *
 * @since 1.1
 * @author Thomas Down
 */

public class DASSequence implements Sequence, RealizingFeatureHolder {
    /**
     * Change type which indicates that the set of annotation servers used
     * by this DASSequence has been changed.
     */

    public static final ChangeType ANNOTATIONS = new ChangeType(
	    "Annotation sets have been added or removed from the DAS sequence",
	    "org.biojava.bio.program.das.DASSequence",
	    "ANNOTATIONS"
    );

    private final Alphabet alphabet = DNATools.getDNA();
    private final URL dataSourceURL;
    private final String seqID;
    private final FeatureRealizer featureRealizer = FeatureImpl.DEFAULT;

    private SymbolList refSymbols;
    private int length;

    private Map featureSets;
    private FeatureHolder structure;
    private MergeFeatureHolder features;

    protected transient ChangeSupport changeSupport = null;

    {
	featureSets = new HashMap();
	features = new MergeFeatureHolder();
    }

    DASSequence(URL dataSourceURL, String seqID) 
        throws BioException
    {
	this(dataSourceURL, seqID, null);
    }

    private DASSequence(URL dataSourceURL, String seqID, String parentID) 
        throws BioException
    {
	this.dataSourceURL = dataSourceURL;
	this.seqID = seqID;
	
	try {
	    //
	    // Check existance, and get length
	    //
	    
	    URL epURL = (parentID == null) ?
		    new URL(dataSourceURL, "entry_points") :
		    new URL(dataSourceURL, "entry_points?ref=" + parentID);
	    HttpURLConnection huc = (HttpURLConnection) epURL.openConnection();
	    huc.connect();
	    int status = huc.getHeaderFieldInt("X-DAS-Status", 0);
	    if (status == 0)
		throw new BioException("Not a DAS server");
	    else if (status != 200)
		throw new BioException("DAS error (status code = " + status + ")");

	    InputSource is = new InputSource(huc.getInputStream());
	    DOMParser parser = nonvalidatingParser();
	    parser.parse(is);
	    Element el = parser.getDocument().getDocumentElement();
	    NodeList segl = el.getElementsByTagName("SEGMENT");
	    Element segment = null;
	    for (int i = 0; i < segl.getLength(); ++i) {
		el = (Element) segl.item(i);
		if (el.getAttribute("id").equals(seqID)) {
		    segment = el;
		    break;
		}
	    }
	    if (segment == null)
		throw new BioException("Couldn't find requested segment " + seqID);

	    int start = Integer.parseInt(segment.getAttribute("start"));
	    int stop = Integer.parseInt(segment.getAttribute("stop"));
	    length = stop - start + 1;

	    //
	    // Pick up features
	    //

	    features.addFeatureHolder(new DASFeatureSet(this, dataSourceURL, seqID));

	    //
	    // Check for deep structure
	    //

	    epURL = new URL(dataSourceURL, "entry_points?ref=" + seqID);
	    huc = (HttpURLConnection) epURL.openConnection();
	    huc.connect();
	    status = huc.getHeaderFieldInt("X-DAS-Status", 0);
	    if (status == 0)
		throw new BioException("Not a DAS server");
	    else if (status != 200)
		throw new BioException("DAS error (status code = " + status + ")");

	    is = new InputSource(huc.getInputStream());
	    parser = nonvalidatingParser();
	    parser.parse(is);
	    el = parser.getDocument().getDocumentElement();

	    segl = el.getElementsByTagName("SEGMENT");	    
	    if (segl.getLength() != 0) {
	        structure = new SimpleFeatureHolder();
		for (int i = 0; i < segl.getLength(); ++i) {
		    Element seg = (Element) segl.item(i);
		    String segId = seg.getAttribute("id");
		    int segStart = Integer.parseInt(seg.getAttribute("start"));
		    int segStop = Integer.parseInt(seg.getAttribute("stop"));
		    DASSequence segSeq = new DASSequence(dataSourceURL, segId, seqID);
		    ComponentFeature.Template cft = new ComponentFeature.Template();
		    cft.location = new RangeLocation(segStart, segStop);
		    cft.type = "SubSequence";
		    cft.source = "Dazzle_Client_Internals";
		    cft.annotation = Annotation.EMPTY_ANNOTATION;
		    cft.strand = StrandedFeature.POSITIVE;
		    cft.componentSequence = segSeq;
		    cft.componentLocation = new RangeLocation(1, segSeq.length());
		    ((SimpleFeatureHolder) structure).addFeature(new DASComponentFeature(this, cft));
		}
		features.addFeatureHolder(structure);
	    } else {
		structure = FeatureHolder.EMPTY_FEATURE_HOLDER;
	    }
	} catch (SAXException ex) {
	    throw new BioException(ex, "Exception parsing DAS XML");
	} catch (IOException ex) {
	    throw new BioException(ex, "Error connecting to DAS server");
	} catch (NumberFormatException ex) {
	    throw new BioException(ex);
	} catch (ChangeVetoException ex) {
	    throw new BioError("Assertion failed: we should be able to add to hidden FeatureHolders");
	}
    }

    private void _addAnnotationSource(URL dataSourceURL) 
        throws BioException
    {
	FeatureHolder fs = new DASFeatureSet(this, dataSourceURL, this.seqID);
	featureSets.put(dataSourceURL, fs);
	features.addFeatureHolder(fs);
    }

    public void addAnnotationSource(URL dataSourceURL) 
        throws BioException, ChangeVetoException
    {
	if (changeSupport == null) {
	    _addAnnotationSource(dataSourceURL);
	} else {
	    synchronized (changeSupport) {
		ChangeEvent ce = new ChangeEvent(
		    this,
		    ANNOTATIONS,
		    null,
		    null
		) ;
		changeSupport.firePreChangeEvent(ce);
		_addAnnotationSource(dataSourceURL);
		changeSupport.firePostChangeEvent(ce);
	    }
	}
    }

    private void _removeAnnotationSource(URL dataSourceURL) {
	FeatureHolder fh = (FeatureHolder) featureSets.get(dataSourceURL);
	if (fh != null)
	    features.removeFeatureHolder(fh);
    }

    public void removeAnnotationSource(URL dataSourceURL) 
        throws ChangeVetoException
    {
	if (changeSupport == null) {
	    _removeAnnotationSource(dataSourceURL);
	} else {
	    synchronized (changeSupport) {
		ChangeEvent ce = new ChangeEvent(
		    this,
		    ANNOTATIONS,
		    null,
		    null
		) ;
		changeSupport.firePreChangeEvent(ce);
		_removeAnnotationSource(dataSourceURL);
		changeSupport.firePostChangeEvent(ce);
	    }
	}
    }

    //
    // SymbolList stuff
    //

    public Alphabet getAlphabet() {
	return alphabet;
    }

    public Iterator iterator() {
	return getSymbols().iterator();
    }

    public int length() {
	return length;
    }

    public String seqString() {
	return getSymbols().seqString();
    }

    public String subStr(int start, int end) {
	return getSymbols().subStr(start, end);
    }

    public SymbolList subList(int start, int end) {
	return getSymbols().subList(start, end);
    }

    public Symbol symbolAt(int pos) {
	return getSymbols().symbolAt(pos);
    }

    public List toList() {
	return getSymbols().toList();
    }

    public void edit(Edit e) 
        throws ChangeVetoException
    {
	throw new ChangeVetoException("/You/ try implementing read-write DAS");
    }

    // 
    // DNA fetching stuff
    //

    protected SymbolList getSymbols() {
	if (refSymbols != null)
	    return refSymbols;

	try {
	    URL epURL = new URL(dataSourceURL, "dna?ref=" + seqID);
	    HttpURLConnection huc = (HttpURLConnection) epURL.openConnection();
	    huc.connect();
	    int status = huc.getHeaderFieldInt("X-DAS-Status", 0);
	    if (status == 0)
		throw new BioError("Not a DAS server");
	    else if (status != 200)
		throw new BioError("DAS error (status code = " + status + ")");


	    InputSource is = new InputSource(huc.getInputStream());
	    DOMParser parser = nonvalidatingParser();
	    parser.parse(is);
	    Element el = parser.getDocument().getDocumentElement();

	    NodeList dnal = el.getElementsByTagName("DNA");
	    if (dnal.getLength() < 1)
		throw new BioError("Didn't find DNA element");
	    el = (Element) dnal.item(0);
	    int len = Integer.parseInt(el.getAttribute("length"));
	    if (len != length())
		throw new BioError("Returned DNA length incorrect");
	    // el.normalize();
	    CharacterData t = (CharacterData) el.getFirstChild();
	    String seqstr = t.getData();
	    StringTokenizer toke = new StringTokenizer(seqstr);
	    List symList = new ArrayList(length());
	    SymbolParser sp = alphabet.getParser("token");
	    while (toke.hasMoreTokens())
		symList.addAll(sp.parse(toke.nextToken()).toList());
	    refSymbols = new SimpleSymbolList(alphabet, symList);
	} catch (SAXException ex) {
	    throw new BioError(ex, "Exception parsing DAS XML");
	} catch (IOException ex) {
	    throw new BioError(ex, "Error connecting to DAS server");
	} catch (NumberFormatException ex) {
	    throw new BioError(ex);
	} catch (BioException ex) {
	    throw new BioError(ex);
	}

	return refSymbols;
    }

    //
    // Identification stuff
    //

    public String getName() {
	return seqID;
    }

    public String getURN() {
	try {
	    return new URL(dataSourceURL, "?ref=" + seqID).toString();
	} catch (MalformedURLException ex) {
	    throw new BioError(ex);
	}
    }

    //
    // FeatureHolder stuff
    //

    public Iterator features() {
	return features.features();
    }

    public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
	//
	// We optimise for the case of just wanting `structural' features,
	// which improves the scalability of the Dazzle server (and probably
	// other applications, too)
	//

	if (ff instanceof FeatureFilter.ByClass) {
	    FeatureFilter.ByClass ffbc = (FeatureFilter.ByClass) ff;
	    if (ffbc.getTestClass() == ComponentFeature.class) {
		if (recurse) {
		    SimpleFeatureHolder sfh = new SimpleFeatureHolder();
		    try {
			for (Iterator i = structure.features(); i.hasNext(); ) {
			    Feature f = (Feature) i.next();
			    sfh.addFeature(f);
			    for (Iterator j = f.filter(ff, true).features(); j.hasNext(); ) {
				sfh.addFeature((Feature) j.next());
			    }
			}
		    } catch (ChangeVetoException ex) {
			throw new BioError(ex, "Assertion failure");
		    }
		    return sfh;
		} else {
		    return structure;
		}
	    }
	}

	//
	// Otherwise they want /real/ features, I'm afraid...
	//

	return features.filter(ff, recurse);
    }
    
    public int countFeatures() {
	return features.countFeatures();
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
    // Feature realization stuff
    //

    public Feature realizeFeature(FeatureHolder dest,
				  Feature.Template temp)
	throws BioException
    {
	return featureRealizer.realizeFeature(this, dest, temp);
    }

    //
    // Annotatable stuff
    //

    public Annotation getAnnotation() {
	return Annotation.EMPTY_ANNOTATION;
    }

    //
    // Changeable stuff (which, unfortunately, we are.  Drat)
    //

    protected void generateChangeSupport(ChangeType changeType) {
	if(changeSupport == null) {
	    changeSupport = new ChangeSupport();
	}
    }
  
    public void addChangeListener(ChangeListener cl) {
	generateChangeSupport(null);
	
	synchronized(changeSupport) {
	    changeSupport.addChangeListener(cl);
	}
    }
  
    public void addChangeListener(ChangeListener cl, ChangeType ct) {
	generateChangeSupport(ct);
	
	synchronized(changeSupport) {
	    changeSupport.addChangeListener(cl, ct);
	}
    }
  
    public void removeChangeListener(ChangeListener cl) {
	if(changeSupport != null) {
	    synchronized(changeSupport) {
		changeSupport.removeChangeListener(cl);
	    }
	}
    }
  
    public void removeChangeListener(ChangeListener cl, ChangeType ct) {
	if(changeSupport != null) {
	    synchronized(changeSupport) {
		changeSupport.removeChangeListener(cl, ct);
	    }
	}
    }  

    //
    // Utility method to turn of the awkward bits of Xerces-J
    //

    static DOMParser nonvalidatingParser() {
	DOMParser dp = new DOMParser();
	try {
	    dp.setFeature("http://xml.org/sax/features/validation", false);
	    // dp.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
	    dp.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
	} catch (SAXNotRecognizedException ex) {
	    ex.printStackTrace();
	} catch (SAXNotSupportedException ex) {
	    ex.printStackTrace();
	} 

	return dp;
    }
}
