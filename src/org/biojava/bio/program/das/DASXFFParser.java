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
 * Parse a DASGFF document and build suitable Feature.Templates
 *
 * @author Thomas Down
 */

class DASXFFParser {
    final static DASXFFParser INSTANCE;

    static {
	INSTANCE = new DASXFFParser();
    }

    private DASXFFParser() {
    }

    public void parseURL(URL fUrl, SeqIOListener siol)
        throws BioException, ParseException, IOException
    {
	try {
	    // System.err.println("Wheeee, using XFF parser on: " + fUrl.toString());

	    HttpURLConnection huc = (HttpURLConnection) fUrl.openConnection();
	    huc.connect();
	    int status = huc.getHeaderFieldInt("X-DAS-Status", 0);
	    if (status == 0)
		throw new BioError("Not a DAS server: " + fUrl.toString());
	    else if (status != 200)
		throw new BioError("DAS error (status code = " + status + ")");
	    
	    InputSource is = new InputSource(huc.getInputStream());
	    DOMParser parser = new DOMParser();
	    parser.parse(is);
	    Element el = parser.getDocument().getDocumentElement();
	    NodeList segl = el.getElementsByTagName("SEGMENT");
	    if (segl.getLength() != 1) {
		segl = el.getElementsByTagName("segmentNotAnnotated");
		if (segl.getLength() != 1) {
		    throw new BioException("Non-extended DASFEATURES documents must contain one SEGMENT");
		} else {
		    siol.startSequence();
		    siol.endSequence();
		    return;
		}
	    }

	    el = (Element) segl.item(0); 
	    
	    parseSegment(el, siol);
	} catch (SAXException ex) {
	    throw new ParseException(ex);
	}
    }
    
    public void parseSegment(Element el, SeqIOListener siol) 
        throws ParseException
    {
	siol.startSequence();
	    
	String segStart = el.getAttribute("start");
	if (segStart != null) {
	    siol.addSequenceProperty("sequence.start", segStart);
	}
	String segStop = el.getAttribute("stop");
	if (segStop != null) {
	    siol.addSequenceProperty("sequence.stop", segStop);
	}

	Node segChld = el.getFirstChild();
	while (segChld != null) {
	    if (segChld instanceof Element) {
		Element featureEl = (Element) segChld;
		if (featureEl.getTagName().equals("featureSet")) {
		    parseXFFFeatureSet(featureEl, siol);
		}
	    }
	    segChld = segChld.getNextSibling();
	}

	siol.endSequence();
    }

    public void parseXFFFeatureSet(Element fe, SeqIOListener siol) 
        throws ParseException
    {
	Node chld = fe.getFirstChild();
	while (chld != null) {
	    if (chld instanceof Element) {
		Element featureEl = (Element) chld;
		String tagName = featureEl.getTagName();
		Feature.Template templ;
		if (tagName.equals("componentFeature")) {
		    templ = new ComponentFeature.Template();
		    parseComponentFeature(featureEl, (ComponentFeature.Template) templ);
		} else if (tagName.equals("strandedFeature")) {
		    templ = new StrandedFeature.Template();
		    parseStrandedFeature(featureEl, (StrandedFeature.Template) templ);
		} else {
		    templ = new Feature.Template();
		    parseFeature(featureEl, templ);
		}

		siol.startFeature(templ);

		Node fChld = featureEl.getFirstChild();
		while (fChld != null) {
		    if (fChld instanceof Element) {
			Element e = (Element) fChld;
			if (e.getTagName().equals("featureSet")) {
			    parseXFFFeatureSet(e, siol);
			}
		    }
		    fChld = fChld.getNextSibling();
		}

		siol.endFeature();
	    }

	    chld = chld.getNextSibling();
	}
    }

    public void parseComponentFeature(Element el, ComponentFeature.Template templ)
        throws ParseException
    {
	Node chld = el.getFirstChild();
	while (chld != null) {
	    if (chld instanceof Element) {
		Element e = (Element) chld;
		String tagName = e.getTagName();
		if (tagName.equals("componentID")) {
		    if (templ.annotation == null) {
			templ.annotation = new SimpleAnnotation();
		    }
		    try {
			templ.annotation.setProperty("sequence.id", getChildText(e));
		    } catch (ChangeVetoException ex) {
			throw new BioError(ex);
		    }
		} else if (tagName.equals("componentLocation")) {
		    templ.componentLocation = parseXFFLocation(e);
		}
	    }
	    chld = chld.getNextSibling();
	}

	parseStrandedFeature(el, templ);
    }

    public void parseStrandedFeature(Element el, StrandedFeature.Template templ)
        throws ParseException
    {
	String strand = el.getAttribute("strand");
	if (strand.equals("-")) {
	    templ.strand = StrandedFeature.NEGATIVE;
	} else {
	    templ.strand = StrandedFeature.POSITIVE;
	}
	parseFeature(el, templ);
    }

    public void parseFeature(Element el, Feature.Template templ) 
        throws ParseException
    {
	Node chld = el.getFirstChild();
	while (chld != null) {
	    if (chld instanceof Element) {
		Element e = (Element) chld;
		String tagName = e.getTagName();
		if (tagName.equals("type")) {
		    templ.type = getChildText(e);
		} else if (tagName.equals("source")) {
		    templ.source = getChildText(e);
		} else if (tagName.equals("location")) {
		    templ.location = parseXFFLocation(e);
		} else if (tagName.equals("id")) {
		    String id = getChildText(e);

		    if (templ.annotation == null) {
			templ.annotation = new SimpleAnnotation();
		    }
		    try {
			templ.annotation.setProperty(DASSequence.PROPERTY_FEATUREID, id);
		    } catch (ChangeVetoException ex) {
			throw new BioError(ex);
		    }
		} else if (tagName.equals("details")) {
		    parseDetails(e, templ);
		}
	    }
	    chld = chld.getNextSibling();
	}
	
	if (templ.annotation == null) {
	    templ.annotation = Annotation.EMPTY_ANNOTATION;
	}
    }

    public void parseDetails(Element el, Feature.Template templ)
        throws ParseException
    {
	Node chld = el.getFirstChild();
	while (chld != null) {
	    if (chld instanceof Element) {
		Element e = (Element) chld;
		String tagName = e.getTagName();
		if (tagName.equals("prop")) {
		    String key = e.getAttribute("key");
		    String value = getChildText(e);
		    if (templ.annotation == null) {
			templ.annotation = new SimpleAnnotation();
		    }
		    try {
			templ.annotation.setProperty(key, value);
		    } catch (ChangeVetoException ex) {
			throw new BioError(ex);
		    }
		}
	    }
	    chld = chld.getNextSibling();
	}
    }

    public Location parseXFFLocation(Element el)
        throws ParseException
    {
	List locationList = new ArrayList();

	Node chld = el.getFirstChild();
	while (chld != null) {
	    if (chld instanceof Element) {
		Element e  = (Element) chld;
		String tagName = e.getTagName();
		if (tagName.equals("span")) {
		    Location l = parseXFFSpan(e);
		    locationList.add(l);
		}
	    }

	    chld = chld.getNextSibling();
	}

	if (locationList.size() == 0) {
	    return Location.empty;
	} else if (locationList.size() == 1) {
	    return (Location) locationList.get(0);
	} else {
	    return new CompoundLocation(locationList);
	}
    }

    public Location parseXFFSpan(Element el) 
        throws ParseException
    {
	try {
	    String start = el.getAttribute("start");
	    String stop = el.getAttribute("stop");

	    int min = Integer.parseInt(start);
	    int max = Integer.parseInt(stop);
	    return new RangeLocation(min, max);
	} catch (NumberFormatException ex) {
	    throw new ParseException(ex);
	}
    }

    private String getChildText(Element el) {
	StringBuffer sb = new StringBuffer();
	Node n = el.getFirstChild();
	while (n != null) {
	    if (n instanceof Text) 
	        sb.append(((Text) n).getData());
	    n = n.getNextSibling();
	}
	return sb.toString().trim();
    }
}
