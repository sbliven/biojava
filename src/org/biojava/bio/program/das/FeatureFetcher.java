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
import org.biojava.utils.stax.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.program.xff.*;

import org.apache.xerces.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.w3c.dom.*;

/**
 * Encapsulate a single batch of feature requests to a DAS server.
 *
 * @since 1.2
 * @author Thomas Down
 */

class FeatureFetcher {
    private HashMap ticketsByID;
    private List doneTickets = Collections.EMPTY_LIST;
    
    private boolean useXMLFetch = false;
    private String category;
    private String type;
    private URL dataSource;

    {
	ticketsByID = new HashMap();
    }

    FeatureFetcher(URL dataSource, String type, String category) {
	this.dataSource = dataSource;
	this.type = type;
	this.category = category;
    }

    URL getDataSourceURL() {
	return dataSource;
    }

    void addTicket(FeatureRequestManager.Ticket ticket) {
	ticketsByID.put(ticket.getID(), ticket);
    }

    int size() {
	return ticketsByID.size();
    }

    List getDoneTickets() {
	return doneTickets;
    }

    void setUseXMLFetch(boolean b) {
	useXMLFetch = b;
    }

    void runFetch() 
	throws BioException, ParseException
    {
	DAS.startedActivity(this);
	URL fURL = null;
	    
	
	try {
	    String fetchEncoding = "dasgff";
	    if (DASCapabilities.checkCapable(new URL(dataSource, "../"), 
					     DASCapabilities.CAPABILITY_FEATURETABLE,
					     DASCapabilities.CAPABILITY_FEATURETABLE_XFF))
	    {
		fetchEncoding = "xff";
	    }

	    HttpURLConnection huc = null;
	    
	    if (useXMLFetch) {
		fURL = new URL(dataSource, "features");
		huc = (HttpURLConnection) fURL.openConnection();
		huc.setRequestMethod("POST");
		huc.setRequestProperty("Content-Type", "text/xml");
		huc.setDoOutput(true);
		 
		OutputStream os = huc.getOutputStream();
		PrintStream ps = new PrintStream(os);
		
		ps.print("<featureRequest encoding=\"" + fetchEncoding + "\"");
		if (type != null) {
		    ps.print(" type=\"" + type + "\"");
		}
		if (category != null) {
		    ps.print(" category=\"" + category + "\"");
		}
		ps.println(">");
		
		for (Iterator i = ticketsByID.keySet().iterator(); i.hasNext() ;) {
		    String id = (String) i.next();
		    ps.println("  <segment id=\"" + id + "\" />");
		}
		ps.println("</featureRequest>");
		ps.close();
	    } else {
		String segments;
		Set segmentIDs = ticketsByID.keySet();
		if (segmentIDs.size() == 1) {
		    segments = "ref=" + (String) segmentIDs.iterator().next();
		} else {
		    StringBuffer sb = new StringBuffer();
		    for (Iterator i = segmentIDs.iterator(); i.hasNext(); ) {
			String id = (String) i.next();
			sb.append("segment=");
			sb.append(id);
			if (i.hasNext()) {
			    sb.append(';');
			}
		    }
		    segments = sb.toString();
		}

		String encodingRequest;
		if (fetchEncoding.equals("dasgff")) {
		    encodingRequest = "";
		} else {
		    encodingRequest = "encoding=" + fetchEncoding + ";";
		}

		String typeRequest = "";
		if (type != null) {
		    typeRequest = "type=" + type + ";";
		}

		String categoryRequest = "";
		if (category != null) {
		    categoryRequest = "category=" + category + ";";
		}

		fURL = new URL(dataSource, "features?" + encodingRequest + categoryRequest + typeRequest + segments);
		huc = (HttpURLConnection) fURL.openConnection();
	    }

	    huc.connect();
	    // int status = huc.getHeaderFieldInt("X-DAS-Status", 0);
	    int status = DASSequenceDB.tolerantIntHeader(huc, "X-DAS-Status");
	    if (status == 0) {
		throw new BioError("Not a DAS server: " + fURL.toString());
	    } else if (status != 200) {
		throw new BioError("DAS error (status code = " + status + ")");
	    }

	    if (fetchEncoding.equals("dasgff")) {
		DASGFFParser gffParser = new DASGFFParser(ticketsByID);
		gffParser.parseStream(huc.getInputStream());

		doneTickets = gffParser.getDoneTickets();
	    } else if (fetchEncoding.equals("xff")) {
		InputSource is = new InputSource(huc.getInputStream());
		DASFeaturesHandler dfh = new DASFeaturesHandler(ticketsByID, this);
		SAXParser parser = DASSequence.nonvalidatingSAXParser();
		parser.setContentHandler(new SAX2StAXAdaptor(dfh));
		parser.parse(is);

		doneTickets = dfh.getDoneTickets();
	    }
	} catch (IOException ex) {
	    throw new ParseException(ex);
	} catch (SAXException ex) {
	    throw new ParseException(ex, "Error parsing XML from " + fURL);
	} finally {
	    DAS.completedActivity(this);
	}
    }

    //
    // StAX handler for the new, generic, DASFEATURES document (only in XFF mode atm.
    //

    private class DASFeaturesHandler extends StAXContentHandlerBase {
	private boolean inDocument = false;
	private Map ticketsById;
	private FeatureRequestManager.Ticket thisTicket;
	private List doneTickets = new ArrayList();
        private Object trigger;

	public List getDoneTickets() {
	    return doneTickets;
	}

	public DASFeaturesHandler(Map ticketsById, Object trigger) {
	    this.ticketsById = ticketsById;
            this.trigger = trigger;
	}

	public void startElement(String nsURI,
				 String localName,
				 String qName,
				 Attributes attrs,
				 DelegationManager dm)
	    throws SAXException
	{
	    if (!inDocument) {
		inDocument = true;
	    } else {
		if (localName.equals("SEGMENT")) {
		    String segID = attrs.getValue("id");
		    if (segID == null) {
			throw new SAXException("Missing segment ID");
		    }
		    thisTicket = (FeatureRequestManager.Ticket) ticketsById.get(segID);
		    if (thisTicket == null) {
			throw new SAXException("Response segment " + segID + " wasn't requested");
		    }

		    dm.delegate(new DASSegmentHandler(thisTicket.getOutputListener()));
		} else if (localName.equals("segmentNotAnnotated")) {
		    String segID = attrs.getValue("id");
		    if (segID == null) {
			throw new SAXException("Missing segment ID");
		    }
		    FeatureRequestManager.Ticket t = (FeatureRequestManager.Ticket) ticketsById.get(segID);
		    if (t == null) {
			throw new SAXException("Response segment " + segID + " wasn't requested");
		    }

		    SeqIOListener siol = t.getOutputListener();
		    try {
			siol.startSequence();
			siol.endSequence();
		    } catch (ParseException ex) {
			throw new SAXException(ex);
		    }
		    
		    t.setAsFetched();
		    doneTickets.add(t);
		} else if (localName.equals("segmentError")) {
		    String segID = attrs.getValue("id");
		    String segError = attrs.getValue("error");

		    throw new SAXException("Error " + segError + " fetching " + segID);
		}
	    }
	}

	public void endElement(String nsURI,
			       String localName,
			       String qName,
			       StAXContentHandler handler)
	    throws SAXException
	{
	    if (localName.equals("SEGMENT")) {
		thisTicket.setAsFetched();
		doneTickets.add(thisTicket);
                DAS.activityProgress(trigger, doneTickets.size()
                , ticketsById.size());
	    }
	}
    }

    private class DASSegmentHandler extends StAXContentHandlerBase {
	private SeqIOListener siol;
	private int level = 0;

	public DASSegmentHandler(SeqIOListener siol) {
	    this.siol = siol;
	}

	public void startElement(String nsURI,
				 String localName,
				 String qName,
				 Attributes attrs,
				 DelegationManager dm)
	    throws SAXException
	{
	    ++level;
	    if (level == 1) {
		try {
		    siol.startSequence();
		
		    String segStart = attrs.getValue("start");
		    if (segStart != null) {
			siol.addSequenceProperty("sequence.start", segStart);
		    }
		    String segStop = attrs.getValue("stop");
		    if (segStop != null) {
			siol.addSequenceProperty("sequence.stop", segStop);
		    }
		} catch (ParseException ex) {
		    throw new SAXException(ex);
		}
	    } else {
		if (localName.equals("featureSet")) {
		    XFFFeatureSetHandler xffh = new XFFFeatureSetHandler();
		    xffh.setFeatureListener(siol);
		    xffh.addFeatureHandler(new ElementRecognizer.ByLocalName("componentFeature"),
					   ComponentFeatureHandler.COMPONENTFEATURE_HANDLER_FACTORY);
		    xffh.addDetailHandler(new ElementRecognizer.ByNSName("http://www.biojava.org/dazzle",
									 "links"),
					  DASLinkHandler.LINKDETAIL_HANDLER_FACTORY);
		    dm.delegate(xffh);
		} else {
		    throw new SAXException("Expecting an XFF featureSet and got " + localName);
		}
	    }
	}

	public void endElement(String nsURI,
			       String localName,
			       String qName,
			       StAXContentHandler handler)
	    throws SAXException
	{
	    if (level == 1) {
		try {
		    siol.endSequence();
		} catch (ParseException ex) {
		    throw new SAXException(ex);
		}
	    }
	    --level;
	}
    }
}
