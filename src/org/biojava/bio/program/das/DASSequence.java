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
import org.biojava.utils.cache.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
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

    public static final String PROPERTY_ANNOTATIONSERVER = "org.biojava.bio.program.das.annotation_server";
    public static final String PROPERTY_FEATUREID = "org.biojava.bio.program.das.feature_id";
    public static final String PROPERTY_FEATURELABEL = "org.biojava.bio.program.das.feature_label";

    private final DASSequenceDB parentdb;
    private final Alphabet alphabet = DNATools.getDNA();
    private final URL dataSourceURL;
    private final String seqID;
    private final FeatureRealizer featureRealizer = FeatureImpl.DEFAULT;

    private CacheReference refSymbols;
    private int length = -1;

    private Map featureSets;
    private FeatureHolder structure;
    private MergeFeatureHolder features;

    protected transient ChangeSupport changeSupport = null;

    {
	featureSets = new HashMap();
	features = new MergeFeatureHolder();
    }

    DASSequence(DASSequenceDB db, URL dataSourceURL, String seqID) 
        throws BioException
    {
	this(db, dataSourceURL, seqID, null);
    }

    DASSequence(DASSequenceDB db, URL dataSourceURL, String seqID, String parentID) 
        throws BioException
    {
	this.parentdb = db;
	this.dataSourceURL = dataSourceURL;
	this.seqID = seqID;
	
	try {
	    //
	    // Check for deep structure.  This also checks that the sequence
	    // really exists, and hopefully picks up the length along the way.
	    //

	    structure = new SimpleFeatureHolder();

	    System.err.println("Doing a skeleton fetch...");

	    SeqIOListener listener = new SeqIOAdapter() {
		    public void addSequenceProperty(Object key, Object value)
		        throws ParseException
		    {
			try {
			    if (key.equals("sequence.start")) {
				int start = Integer.parseInt(value.toString());
				if (start != 1) {
				    throw new ParseException("Server doesn't think sequence starts at 1.  Wierd.");
				}
			    } else if (key.equals("sequence.stop")) {
				length = Integer.parseInt(value.toString());
			    }
			} catch (NumberFormatException ex) {
			    throw new ParseException(ex, "Expect numbers for segment start and stop");
			}
		    }

		    public void startFeature(Feature.Template temp)
		        throws ParseException
		    {
			if (temp instanceof ComponentFeature.Template) {
			    String id = (String) temp.annotation.getProperty("sequence.id");

			    try {
				ComponentFeature.Template ctemp = (ComponentFeature.Template) temp;
				ComponentFeature cf = new DASComponentFeature(DASSequence.this,
									      ctemp);
				((SimpleFeatureHolder) structure).addFeature(cf);

				length = Math.max(length, ctemp.location.getMax());
			    } catch (BioException ex) {
				throw new ParseException(ex, "Error instantiating DASComponent");
			    } catch (ChangeVetoException ex) {
				throw new BioError(ex, "Immutable FeatureHolder when trying to build structure");
			    }					  
			} else {
			    // Server seems not to honour category=
			    // This hurts performance, but we can just elide the unwanted
			    // features on the client side.
			}
		    }
		} ;

	    boolean useXFF = DASCapabilities.checkCapable(new URL(dataSourceURL, ".."),
							  DASCapabilities.CAPABILITY_FEATURETABLE,
							  DASCapabilities.CAPABILITY_FEATURETABLE_XFF);
	    if (useXFF) {
		URL fUrl = new URL(dataSourceURL, "features?encoding=xff;ref=" + seqID + ";category=component");
		DASXFFParser.INSTANCE.parseURL(fUrl, listener);
	    } else {
		URL fUrl = new URL(dataSourceURL, "features?ref=" + seqID + ";category=component");
		DASGFFParser.INSTANCE.parseURL(fUrl, listener);
	    }

	    if (structure.countFeatures() > 0) {
		features.addFeatureHolder(structure);
	    }

	    //
	    // Pick up the default annotation set (this should maybe be optional)
	    //

	    FeatureHolder refServerFeatureSet = new DASFeatureSet(this, dataSourceURL, seqID);
	    featureSets.put(dataSourceURL, refServerFeatureSet);
	    features.addFeatureHolder(refServerFeatureSet);
	} catch (IOException ex) {
	    throw new BioException(ex, "Error connecting to DAS server");
	} catch (NumberFormatException ex) {
	    throw new BioException(ex);
	} 
    }

    URL getDataSourceURL() {
	return dataSourceURL;
    }

    DASSequenceDB getParentDB() {
	return parentdb;
    }

    private void _addAnnotationSource(URL dataSourceURL) 
        throws BioException
    {
      if(!featureSets.containsKey(dataSourceURL)) {
	FeatureHolder fs = new DASFeatureSet(this, dataSourceURL, this.seqID);
	featureSets.put(dataSourceURL, fs);
	features.addFeatureHolder(fs);
      }
    }
    
    public Set dataSourceURLs() {
      return Collections.unmodifiableSet(featureSets.keySet());
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
	if (fh != null) {
	    features.removeFeatureHolder(fh);
            featureSets.remove(dataSourceURL);
        }
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

    private void registerLocalFeatureFetchers() {
	for (Iterator i = featureSets.values().iterator(); i.hasNext(); ) {
	    DASFeatureSet dfs = (DASFeatureSet) i.next();
	    dfs.registerFeatureFetcher();
	}
    }

    void registerFeatureFetchers() {
	registerLocalFeatureFetchers();

	for (Iterator fi = structure.features(); fi.hasNext(); ) {
	    ComponentFeature cf = (ComponentFeature) fi.next();
	    DASSequence cseq = (DASSequence) cf.getComponentSequence();
	    cseq.registerFeatureFetchers();
	}
    }

    void registerFeatureFetchers(Location l) {
	registerLocalFeatureFetchers();
	
	if (structure.countFeatures() > 0) {
	    FeatureHolder componentsBelow = structure.filter(new FeatureFilter.OverlapsLocation(l), false);
	    for (Iterator fi = componentsBelow.features(); fi.hasNext(); ) {
		ComponentFeature cf = (ComponentFeature) fi.next();
		DASSequence cseq = (DASSequence) cf.getComponentSequence();
		if (l.contains(cf.getLocation())) {
		    cseq.registerFeatureFetchers();
		} else {
		    Location partNeeded = l.intersection(cf.getLocation());
		    if (cf.getStrand() == StrandedFeature.POSITIVE) {
			partNeeded = partNeeded.translate(cf.getComponentLocation().getMin() - cf.getLocation().getMin());
			cseq.registerFeatureFetchers(partNeeded);
		    } else {
			// FIXME: Couldn't work out the appropriate xform.
			cseq.registerFeatureFetchers();
		    }
		}
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
	// If the sequence isn't an assembly we're actually in a kind-of bad
	// way for getting the length.  Right now I'm getting the DNA.

	if (length < 0) {
	    length = getSymbols().length();
	}

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
	SymbolList sl = null;
	if (refSymbols != null) {
	    sl = (SymbolList) refSymbols.get();
	}

	if (sl == null) {
	    if (structure.countFeatures() == 0) {
		sl = getTrueSymbols();
	    } else {
		AssembledSymbolList asl = new AssembledSymbolList();
		asl.setLength(length);
		for (Iterator i = structure.features(); i.hasNext(); ) {
		    ComponentFeature cf = (ComponentFeature) i.next();
		    asl.putComponent(cf.getLocation(), cf);
		}

		sl = asl;
	    }

	    refSymbols = parentdb.getSymbolsCache().makeReference(sl);
	}

	return sl;
    }

    protected SymbolList getTrueSymbols() {
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
	    if (length >= 0) {
		if (len != length())
		    throw new BioError("Returned DNA length incorrect: expecting " + length() + " but got " + len);
	    }
	    // el.normalize();
	    CharacterData t = (CharacterData) el.getFirstChild();
	    String seqstr = t.getData();
	    StringTokenizer toke = new StringTokenizer(seqstr);
	    List symList = new ArrayList(length());
	    SymbolParser sp = alphabet.getParser("token");
	    while (toke.hasMoreTokens())
		symList.addAll(sp.parse(toke.nextToken()).toList());
	    return new SimpleSymbolList(alphabet, symList);
	} catch (SAXException ex) {
	    throw new BioError(ex, "Exception parsing DAS XML");
	} catch (IOException ex) {
	    throw new BioError(ex, "Error connecting to DAS server");
	} catch (NumberFormatException ex) {
	    throw new BioError(ex);
	} catch (BioException ex) {
	    throw new BioError(ex);
	}
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

	if (ff instanceof FeatureFilter.OverlapsLocation || ff instanceof FeatureFilter.ContainedByLocation)
	{
	    Location l = null;
	    if (ff instanceof FeatureFilter.OverlapsLocation) {
		l = ((FeatureFilter.OverlapsLocation) ff).getLocation();
	    } else {
		l = ((FeatureFilter.ContainedByLocation) ff).getLocation();
	    }
	    registerFeatureFetchers(l);
	} else {
	    registerFeatureFetchers();
	}

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
