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
import java.util.zip.*;
import java.net.*;
import java.io.*;

import org.biojava.utils.*;
import org.biojava.utils.cache.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.bio.symbol.*;

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.w3c.dom.*;
import org.biojava.utils.stax.*;

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
 * @author Matthew Pocock
 * @author David Huen
 */

public class DASSequence implements Sequence, RealizingFeatureHolder {
    /**
     * Change type which indicates that the set of annotation servers used
     * by this DASSequence has been changed. This extends Feature.FEATURES as
     * the addition and removal of annotation servers adds and removes features.
     */

    public static final ChangeType ANNOTATIONS = new ChangeType(
	    "Annotation sets have been added or removed from the DAS sequence",
	    "org.biojava.bio.program.das.DASSequence",
	    "ANNOTATIONS",
            Feature.FEATURES
    );

    public static final String PROPERTY_ANNOTATIONSERVER = "org.biojava.bio.program.das.annotation_server";
    public static final String PROPERTY_FEATUREID = "org.biojava.bio.program.das.feature_id";
    public static final String PROPERTY_FEATURELABEL = "org.biojava.bio.program.das.feature_label";
    public static final String PROPERTY_LINKS = "org.biojava.bio.program.das.links";
    public static final String PROPERTY_SEQUENCEVERSION = "org.biojava.bio.program.das.sequence_version";

    public static final int SIZE_THRESHOLD = 500000;
    
    private DASSequenceDB parentdb;
    private Alphabet alphabet = DNATools.getDNA();
    private URL dataSourceURL;
    private String seqID;
    private String version = null;
    private FeatureRealizer featureRealizer = FeatureImpl.DEFAULT;
    private FeatureRequestManager.Ticket structureTicket;

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

    DASSequence(DASSequenceDB db, URL dataSourceURL, String seqID, Set dataSources) 
        throws BioException, IllegalIDException
    {
	this.parentdb = db;
	this.dataSourceURL = dataSourceURL;
	this.seqID = seqID;
	
	//
	// Check for deep structure.  This also checks that the sequence
	// really exists, and hopefully picks up the length along the way.
	//

	SeqIOListener listener = new SkeletonListener();
	FeatureRequestManager frm = getParentDB().getFeatureRequestManager();
	this.structureTicket = frm.requestFeatures(dataSourceURL, seqID, listener, null, "component");

	//
	// Pick up some annotations
	//

	for (Iterator dsi = dataSources.iterator(); dsi.hasNext(); ) {
	    URL annoURL = (URL) dsi.next();

	    FeatureHolder newFeatureSet = new DASFeatureSet(this, annoURL, seqID);
	    featureSets.put(annoURL, newFeatureSet);
	    features.addFeatureHolder(newFeatureSet);
	}
    }

    private class SkeletonListener extends SeqIOAdapter {
	private SimpleFeatureHolder structureF;

	public void startSequence() {
	    structureF = new SimpleFeatureHolder();
	}

	public void endSequence() {
	    structure = structureF;
	    if (structure.countFeatures() > 0) {
		features.addFeatureHolder(structure);
	    } 
	}

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
		} else if (key.equals("sequence.version")) {
		    version = value.toString();
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
		    structureF.addFeature(cf);
		    
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
    }

    URL getDataSourceURL() {
	return dataSourceURL;
    }

    DASSequenceDB getParentDB() {
	return parentdb;
    }
    
    FeatureHolder getStructure() throws BioException {
	if(!this.structureTicket.isFetched()) {
	    this.structureTicket.doFetch();   
	}
      
	return this.structure;
    }

    private void _addAnnotationSource(URL dataSourceURL) 
        throws BioException, ChangeVetoException
    {
        FeatureHolder structure = getStructure();
	for (Iterator i = structure.features(); i.hasNext(); ) {
	    DASComponentFeature dcf = (DASComponentFeature) i.next();
	    DASSequence seq = dcf.getSequenceLazy();
	    if (seq != null) {
		seq.addAnnotationSource(dataSourceURL);
	    }
	}

	FeatureHolder fs = new DASFeatureSet(this, dataSourceURL, this.seqID);
	featureSets.put(dataSourceURL, fs);
	features.addFeatureHolder(fs);
    }
    
    public Set dataSourceURLs() {
      return Collections.unmodifiableSet(featureSets.keySet());
    }

    public void addAnnotationSource(URL dataSourceURL) 
        throws BioException, ChangeVetoException
    {
	if(!featureSets.containsKey(dataSourceURL)) {
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
    }

    private void _removeAnnotationSource(URL dataSourceURL) 
        throws ChangeVetoException, BioException
    {
        FeatureHolder structure = getStructure();
	FeatureHolder fh = (FeatureHolder) featureSets.get(dataSourceURL);
	if (fh != null) {
	    for (Iterator i = structure.features(); i.hasNext(); ) {
		DASComponentFeature dcf = (DASComponentFeature) i.next();
		DASSequence seq = dcf.getSequenceLazy();
		if (seq != null) {
		    seq.removeAnnotationSource(dataSourceURL);
		}
	    }

	    features.removeFeatureHolder(fh);
            featureSets.remove(dataSourceURL);
        }
    }

    public void removeAnnotationSource(URL dataSourceURL) 
        throws ChangeVetoException, BioException
    {
	if (featureSets.containsKey(dataSourceURL)) {
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
    }

    private int registerLocalFeatureFetchers() {
	// System.err.println(getName() + ": registerLocalFeatureFetchers()");

	for (Iterator i = featureSets.values().iterator(); i.hasNext(); ) {
	    DASFeatureSet dfs = (DASFeatureSet) i.next();
	    dfs.registerFeatureFetcher();
	}

	return featureSets.size();
    }

    private int registerLocalFeatureFetchers(Location l) {
	// System.err.println(getName() + ": registerLocalFeatureFetchers(" + l.toString() + ")");

	for (Iterator i = featureSets.values().iterator(); i.hasNext(); ) {
	    DASFeatureSet dfs = (DASFeatureSet) i.next();
	    dfs.registerFeatureFetcher(l);
	}

	return featureSets.size();
    }

    int registerFeatureFetchers() throws BioException {
	// System.err.println(getName() + ": registerFeatureFetchers()");

	for (Iterator i = featureSets.values().iterator(); i.hasNext(); ) {
	    DASFeatureSet dfs = (DASFeatureSet) i.next();
	    dfs.registerFeatureFetcher();
	}

	int num = featureSets.size();

        FeatureHolder structure = getStructure();
	if (length() < SIZE_THRESHOLD && structure.countFeatures() > 0) {
	    List sequences = new ArrayList();
	    for (Iterator fi = structure.features(); fi.hasNext(); ) {
		ComponentFeature cf = (ComponentFeature) fi.next();
		DASSequence cseq = (DASSequence) cf.getComponentSequence();
		sequences.add(cseq);
	    }

	    for (Iterator si = sequences.iterator(); si.hasNext(); ) {
		DASSequence cseq = (DASSequence) si.next();
		num += cseq.registerFeatureFetchers();
	    }
	}

	return num;
    }

    int registerFeatureFetchers(Location l) throws BioException {
	// System.err.println(getName() + ": registerFeatureFetchers(" + l.toString() + ")");

	for (Iterator i = featureSets.values().iterator(); i.hasNext(); ) {
	    DASFeatureSet dfs = (DASFeatureSet) i.next();
	    dfs.registerFeatureFetcher(l);
	}

	int num = featureSets.size();
	
        FeatureHolder structure = getStructure();
	if (structure.countFeatures() > 0) {
	    FeatureHolder componentsBelow = structure.filter(new FeatureFilter.OverlapsLocation(l), false);

	    Map sequencesToRegions = new HashMap();

	    for (Iterator fi = componentsBelow.features(); fi.hasNext(); ) {
		ComponentFeature cf = (ComponentFeature) fi.next();
		DASSequence cseq = (DASSequence) cf.getComponentSequence();
		if (l.contains(cf.getLocation())) {
		    sequencesToRegions.put(cseq, null);
		} else {
		    Location partNeeded = l.intersection(cf.getLocation());
		    if (cf.getStrand() == StrandedFeature.POSITIVE) {
			partNeeded = partNeeded.translate(cf.getComponentLocation().getMin() - cf.getLocation().getMin());
			sequencesToRegions.put(cseq, partNeeded);
		    } else {
			sequencesToRegions.put(cseq, null);
		    }
		}
	    }

	    for (Iterator sri = sequencesToRegions.entrySet().iterator(); sri.hasNext(); ) {
		Map.Entry srme = (Map.Entry) sri.next();
		DASSequence cseq = (DASSequence) srme.getKey();
		Location partNeeded = (Location) srme.getValue();
		if (partNeeded != null) {
		    num += cseq.registerFeatureFetchers(partNeeded);
		} else {
		    num += cseq.registerFeatureFetchers();
		}
	    }
	}

	return num;
    }

    //
    // SymbolList stuff
    //

    public Alphabet getAlphabet() {
	return alphabet;
    }

    public Iterator iterator() {
      try {
	return getSymbols().iterator();
      } catch (BioException be) {
        throw new BioRuntimeException(be, "Can't iterate over symbols");
      }
    }

    public int length() {
	try {
	    if (length < 0 && !structureTicket.isFetched()) {
		// Hope that the length is set when we get the structure
		
		structureTicket.doFetch();    
	    }

	    if (length < 0) {
		// Nasty fallback

		length = getSymbols().length();
	    }
		
	    return length;
	} catch (BioException be) {
	    throw new BioRuntimeException(be, "Can't calculate length");
	}
    }

    public String seqString() {
	try {
	    return getSymbols().seqString();
	} catch (BioException be) {
	    throw new BioRuntimeException(be, "Can't create seqString");
	}
    }

    public String subStr(int start, int end) {
	try {
	    return getSymbols().subStr(start, end);
	} catch (BioException be) {
	    throw new BioRuntimeException(be, "Can't create substring");
	}
    }

    public SymbolList subList(int start, int end) {
	try {
	    return getSymbols().subList(start, end);
	} catch (BioException be) {
	    throw new BioRuntimeException(be, "Can't create subList");
	}
    }

    public Symbol symbolAt(int pos) {
	try {
	    return getSymbols().symbolAt(pos);
	} catch (BioException be) {
	    throw new BioRuntimeException(be, "Can't fetch symbol");
	}
    }

    public List toList() {
	try {
	    return getSymbols().toList();
	} catch (BioException be) {
	    throw new BioRuntimeException(be, "Can't create list");
	}
    }

    public void edit(Edit e) 
        throws ChangeVetoException
    {
	throw new ChangeVetoException("/You/ try implementing read-write DAS");
    }

    // 
    // DNA fetching stuff
    //

    protected SymbolList getSymbols() throws BioException {
	SymbolList sl = null;
	if (refSymbols != null) {
	    sl = (SymbolList) refSymbols.get();
	}

	if (sl == null) {
            FeatureHolder structure = getStructure();
	    if (structure.countFeatures() == 0) {
		sl = getTrueSymbols();
	    } else {
		//
		// VERY, VERY, naive approach to identifying canonical components.  FIXME
		//

		Location coverage = Location.empty;
		AssembledSymbolList asl = new AssembledSymbolList();
		asl.setLength(length);

		for (Iterator i = structure.features(); i.hasNext(); ) {
		    ComponentFeature cf = (ComponentFeature) i.next();
		    Location loc = cf.getLocation();
		    if (LocationTools.overlaps(loc, coverage)) {
			// Assume that this is non-cannonical.  Maybe not a great thing to
			// do, but...
		    } else {
			asl.putComponent(cf);
			coverage = LocationTools.union(coverage, loc);
		    }
		}

		sl = asl;
	    }

	    refSymbols = parentdb.getSymbolsCache().makeReference(sl);
	}

	return sl;
    }

    protected SymbolList getTrueSymbols() {
            long startGet = (new Date()).getTime();

	try {
	    DAS.startedActivity(this);

	    URL epURL = new URL(dataSourceURL, "dna?ref=" + seqID);
	    HttpURLConnection huc = (HttpURLConnection) epURL.openConnection();
            huc.setRequestProperty("Accept-Encoding", "gzip");

	    huc.connect();
	    // int status = huc.getHeaderFieldInt("X-DAS-Status", 0);
	    int status = DASSequenceDB.tolerantIntHeader(huc, "X-DAS-Status");

	    if (status == 0)
		throw new BioRuntimeException("Not a DAS server");
	    else if (status != 200)
		throw new BioRuntimeException("DAS error (status code = " + status + ")");

	    SequenceBuilder sb = new SimpleSequenceBuilder();
	    sb.setURI(epURL.toString());
	    sb.setName(getName());
	    SymbolParser sparser = DNATools.getDNA().getParser("token");
	    StreamParser ssparser = sparser.parseStream(sb);

	    StAXContentHandler dnaHandler = new DNAHandler(ssparser);

            // determine if I'm getting a gzipped reply
            String contentEncoding = huc.getContentEncoding();
            InputStream inStream = huc.getInputStream();
	     
            if (contentEncoding != null) {
                if (contentEncoding.indexOf("gzip") != -1) {
		    // we have gzip encoding
		    inStream = new GZIPInputStream(inStream);
		    // System.out.println("gzip encoded dna!");
                }
            }

	    InputSource is = new InputSource(inStream);
	    is.setSystemId(epURL.toString());
	    XMLReader parser = nonvalidatingSAXParser();
	    parser.setContentHandler(new SAX2StAXAdaptor(dnaHandler));
	    parser.parse(is);
	    SymbolList sl = sb.makeSequence();
	    return sl;
	} catch (SAXException ex) {
	    throw new BioRuntimeException(ex, "Exception parsing DAS XML");
	} catch (IOException ex) {
	    throw new BioRuntimeException(ex, "Error connecting to DAS server");
	} catch (BioException ex) {
	    throw new BioRuntimeException(ex);
	} finally {
	    DAS.completedActivity(this);
	}
    }

    private class DNAHandler extends StAXContentHandlerBase {
	private StreamParser ssparser;

	DNAHandler(StreamParser ssparser) {
	    this.ssparser = ssparser;
	}

	public void startElement(String nsURI,
				 String localName,
				 String qName,
				 Attributes attrs,
				 DelegationManager dm)
	    throws SAXException
	{
	    if (localName.equals("DNA")) {
		dm.delegate(new SymbolsHandler(ssparser));
	    }
	}
    }

    private class SymbolsHandler extends StAXContentHandlerBase {
	private StreamParser ssparser;

	SymbolsHandler(StreamParser ssparser) {
	    this.ssparser = ssparser;
	}

	public void endElement(String nsURI,
			   String localName,
			   String qName,
			   StAXContentHandler handler)
	    throws SAXException
	{
	    try {
		ssparser.close();
	    } catch (IllegalSymbolException ex) {
		throw new SAXException(ex);
	    }
	}

	public void characters(char[] ch, int start, int length) 
	    throws SAXException
	{
	    try {
		int parseStart = start;
		int parseEnd   = start;
		int blockEnd = start + length;

		while (parseStart < blockEnd) {
		    while (parseStart < blockEnd && Character.isSpace(ch[parseStart])) {
			++parseStart;
		    }
		    if (parseStart >= blockEnd) {
			return;
		    }

		    parseEnd = parseStart + 1;
		    while (parseEnd < blockEnd && !Character.isSpace(ch[parseEnd])) {
			++parseEnd;
		    }

		    ssparser.characters(ch, parseStart, parseEnd - parseStart);

		    parseStart = parseEnd;
		}
	    } catch (IllegalSymbolException ex) {
		throw new SAXException(ex);
	    }
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
	    throw new BioRuntimeException(ex);
	}
    }

    //
    // FeatureHolder stuff
    //

    public Iterator features() {
      try {
	registerFeatureFetchers();
	return features.features();
      } catch (BioException be) {
        throw new BioRuntimeException(be, "Couldn't create features iterator");
      }
    }

    public boolean containsFeature(Feature f) {
	return features.containsFeature(f);
    }
    
    public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
	try {
	    //
	    // We optimise for the case of just wanting `structural' features,
	    // which improves the scalability of the Dazzle server (and probably
	    // other applications, too)
	    //
	    
	    FeatureHolder structure = getStructure();
	    FeatureFilter structureMembershipFilter = new FeatureFilter.ByClass(ComponentFeature.class);

	    if (FilterUtils.areProperSubset(ff, structureMembershipFilter)) {
		if (recurse) {
		    for (Iterator fi = structure.features(); fi.hasNext(); ) {
			ComponentFeature cf = (ComponentFeature) fi.next();
			Sequence cseq = cf.getComponentSequence(); 
			// Just ensuring that the sequence is instantiated should be sufficient. 
		    }
		}
		return structure.filter(ff, recurse);
	    }

	    //
	    // Otherwise they want /real/ features, I'm afraid...
	    //
	    
	    Location ffl = extractInterestingLocation(ff);
	    if (recurse) {
		int numComponents = 1;
		if (ffl != null) {
		    numComponents = registerFeatureFetchers(ffl);
		} else {
		    numComponents = registerFeatureFetchers();
		}
		getParentDB().ensureFeaturesCacheCapacity(numComponents * 3);
	    } else {
		if (ffl != null) {
		    registerLocalFeatureFetchers(ffl);
		} else {
		    new Exception("Hmmm, this is dangerous...").printStackTrace();
		    registerLocalFeatureFetchers();
		}
	    }
	    
	    return features.filter(ff, recurse);
	} catch (BioException be) {
	    throw new BioRuntimeException(be, "Can't filter");
	}
    }


    private Location extractInterestingLocation(FeatureFilter ff) {
	if (ff instanceof FeatureFilter.OverlapsLocation) {
	    return ((FeatureFilter.OverlapsLocation) ff).getLocation();
	} else if (ff instanceof FeatureFilter.ContainedByLocation) {
	    return ((FeatureFilter.ContainedByLocation) ff).getLocation();
	} else if (ff instanceof FeatureFilter.And) {
	    FeatureFilter.And ffa = (FeatureFilter.And) ff;
	    Location l1 = extractInterestingLocation(ffa.getChild1());
	    Location l2 = extractInterestingLocation(ffa.getChild2());

	    if (l1 != null) {
		if (l2 != null) {
		    return l1.intersection(l2);
		} else {
		    return l1;
		}
	    } else {
		if (l2 != null) {
		    return l2;
		} else {
		    return null;
		}
	    }
	}

	// Don't know how this filter relates to location.

	return null;
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
	try {
	    Annotation anno = new SmallAnnotation();
	    anno.setProperty(PROPERTY_SEQUENCEVERSION, version);
	    return anno;
	} catch (ChangeVetoException ex) {
	    throw new BioError("Expected to be able to modify annotation");
	}
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

    static DocumentBuilder nonvalidatingParser() {
	try {
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    dbf.setValidating(false);
	    return dbf.newDocumentBuilder();
	} catch (Exception ex) {
	    throw new BioError(ex);
	} 
    }

    static XMLReader nonvalidatingSAXParser() {
	try {
	    SAXParserFactory spf = SAXParserFactory.newInstance();
	    spf.setValidating(false);
	    spf.setNamespaceAware(true);
	    return spf.newSAXParser().getXMLReader();
	} catch (Exception ex) {
	    throw new BioError(ex);
	}
    }
}
