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
 * Parse a DASGFF document and build suitable Feature.Templates.
 * Now works with multi-segment DASGFF.  This isn't as efficient
 * as it could be (builds a whole DOM tree...) -- if you want fast,
 * use XFF instead.
 *
 * @author Thomas Down
 */

class DASGFFParser {
    private Map ticketsByID;
    private List doneTickets = new ArrayList();
    private int seqStart, seqStop;
    
    DASGFFParser(Map ticketsByID) {
	this.ticketsByID = ticketsByID;
    }

    List getDoneTickets() {
	return doneTickets;
    }

    void parseStream(InputStream data)
        throws BioException, ParseException, IOException, SAXException
    {
	    InputSource is = new InputSource(data);
	    DOMParser parser = new DOMParser();
	    parser.parse(is);
	    Element el = parser.getDocument().getDocumentElement();
	    NodeList gffl = el.getElementsByTagName("GFF");
	    if (gffl.getLength() != 1)
		throw new BioException("Couldn't find GFF element");
	    el = (Element) gffl.item(0);
	    String version = el.getAttribute("version");
	    if (version != null) {
		try {
		    double v = Double.parseDouble(version);
		    if (v < 0.95 || v > 1.0) {
			throw new ParseException("Unrecognized DASGFF version " + version);
		    }
		} catch (NumberFormatException ex) {
		    throw new ParseException(ex);
		}
	    }

	    Node n = el.getFirstChild();
	    while (n != null) {
		if (n instanceof Element) {
		    Element echld = (Element) n;
		    String tagName = echld.getTagName();
		    if (tagName.equals("SEGMENT")) {
			String segID = echld.getAttribute("id");
			FeatureRequestManager.Ticket t = (FeatureRequestManager.Ticket) ticketsByID.get(segID);
			if (t == null) {
			    throw new SAXException("Response segment " + segID + " wasn't requested");
			}
			parseSegment(echld, t.getOutputListener());
			t.setAsFetched();
			doneTickets.add(t);
		    } else if (tagName.equals("segmentNotAnnotated")) {
			String segID = echld.getAttribute("id");
			FeatureRequestManager.Ticket t = (FeatureRequestManager.Ticket) ticketsByID.get(segID);
			if (t == null) {
			    throw new SAXException("Response segment " + segID + " wasn't requested");
			}
			SeqIOListener siol = t.getOutputListener();
			siol.startSequence();
			siol.endSequence();
			t.setAsFetched();
			doneTickets.add(t);
		    } else if (tagName.equals("segmentError")) {
			String segID = echld.getAttribute("id");
			String segError = echld.getAttribute("error");
			
			throw new ParseException("Error " + segError + " fetching " + segID);
		    }
		}

		n = n.getNextSibling();
	    }
    }


    private void parseSegment(Element el, SeqIOListener siol)
        throws BioException, ParseException
    {
	siol.startSequence();
	
	String segStart = el.getAttribute("start");
	if (segStart != null) {
	    try {
		seqStart = Integer.parseInt(segStart);
	    } catch (NumberFormatException ex) {}
	    siol.addSequenceProperty("sequence.start", segStart);
	}
	String segStop = el.getAttribute("stop");
	if (segStop != null) {
	    try {
		seqStop = Integer.parseInt(segStop);
	    } catch (NumberFormatException ex) {}
	    siol.addSequenceProperty("sequence.stop", segStop);
	}

	Node segChld = el.getFirstChild();
	while (segChld != null) {
	    if (segChld instanceof Element) {
		Element featureEl = (Element) segChld;
		if (featureEl.getTagName().equals("FEATURE")) {
		    Feature.Template temp = parseDASFeature(featureEl);
		    if (temp != null) {
			siol.startFeature(temp);
			siol.endFeature();
		    }
		}
	    }
	    segChld = segChld.getNextSibling();
	}
	
	siol.endSequence();
    }

    private Feature.Template parseDASFeature(Element fe) 
        throws NumberFormatException, ParseException
    {
	String f_id = null;
	String f_label = null;
	String type = "unknown";
	String method = "unknown";
	int start = -1, end = -1;
	String orientation="0";
	String phase="-";
	Location loc = null;
	boolean isReferenceFeature = false;
	String category = null;
	String refName = null;
	int refStart = -1;
	int refStop = -1;

	//
	// Phase one: get stuff out of the XML
	//

	f_id = fe.getAttribute("id");
	f_label = fe.getAttribute("label");

	Node n = fe.getFirstChild();
	while (n != null) {
	    if (n instanceof Element) {
		Element nel = (Element) n;
		String tag = nel.getTagName();
		if (tag.equals("TYPE")) {
		    type = getChildText(nel);
		    String reference = nel.getAttribute("reference");
		    if ("yes".equals(reference)) {
			isReferenceFeature = true;
		    }
		    category = nel.getAttribute("category");
		} else if (tag.equals("METHOD")) {
		    method = getChildText(nel);
		} else if (tag.equals("START")) {
		    start = Integer.parseInt(getChildText(nel));
		} else if (tag.equals("END")) {
		    end = Integer.parseInt(getChildText(nel));
		} else if (tag.equals("ORIENTATION")) {
		    orientation = getChildText(nel);
		} else if (tag.equals("PHASE")) {
		    phase = getChildText(nel);
		} else if (tag.equals("GROUP")) {
		    Node gn = nel.getFirstChild();
		    while (gn != null) {
			if (gn instanceof Element) {
			    Element gel = (Element) gn;
			    String gtag = gel.getTagName();
			    if (gtag.equals("TARGET")) {
				refName = gel.getAttribute("ref");
				refStart = Integer.parseInt(gel.getAttribute("start"));
				refStop = Integer.parseInt(gel.getAttribute("stop"));
			    } else {
				// Right now the target is all we care about.
				// I didn't design this protocol, 'kay?
			    }
			}
			gn = gn.getNextSibling();
		    }
		} else {
		    // Unknown element ought to be an error in DASGFF0.95, but the
		    // standard may still be evolving, so we'll be flexible in what
		    // we accept, at least for now.
		}
		
	    }
	    n = n.getNextSibling();
	}

	//
	// Phase two: see what kind of template we can make
	//

	Feature.Template temp = null;
	if (isReferenceFeature && category.equals("component")) {
	    if (refName == null) {
		throw new ParseException("Can't template componentFeature without a specified TARGET");
	    }

	    //
	    // Ugly check for componentFeatures that aren't...
	    //

	    if (start < seqStart || start > seqStop || end < seqStart || end > seqStop || refStop == 0) {
		// System.err.println("*** Eliding strange componentFeature: " + refName);
		return null;
	    }

	    ComponentFeature.Template ctemp = new ComponentFeature.Template();
	    ctemp.componentLocation = new RangeLocation(refStart, refStop);
	    ctemp.strand = orientation.equals("+") ? StrandedFeature.POSITIVE :
	                                             StrandedFeature.NEGATIVE;

	    // Right now we're not going to do the sequence instantiation here -- trust it to
	    // the listener instead.

	    ctemp.annotation = new SmallAnnotation();
	    try {
		ctemp.annotation.setProperty("sequence.id", refName);
	    } catch (ChangeVetoException ex) {
		throw new BioError(ex);
	    }

	    temp = ctemp;
	} else if (orientation.equals("+") || orientation.equals("-")) {
	    StrandedFeature.Template stemp = new StrandedFeature.Template();
	    stemp.strand = orientation.equals("+") ? StrandedFeature.POSITIVE :
	                                             StrandedFeature.NEGATIVE;
	    temp = stemp;
	} else {
	    temp = new Feature.Template();
	}

	temp.type = type;
	temp.source = method;
	if (loc == null) {
	    temp.location = new RangeLocation(start, end);
	} else {
	    temp.location = loc;
	}

	if (temp.annotation == null) {
	    temp.annotation = new SmallAnnotation();
	}

	try {
	    if (f_id.length() > 0) {
		temp.annotation.setProperty(DASSequence.PROPERTY_FEATUREID, f_id);
	    }
	    if (f_label.length() > 0) {
		temp.annotation.setProperty(DASSequence.PROPERTY_FEATURELABEL, f_label);
	    }
	} catch (ChangeVetoException ex) {
	    throw new BioError(ex);
	}

	return temp;
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
