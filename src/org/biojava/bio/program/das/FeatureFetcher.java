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

import org.biojava.bio.*;
import org.biojava.utils.*;
import org.biojava.utils.stax.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.program.xff.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * Encapsulate a single batch of feature requests to a DAS server.
 *
 * @since 1.2
 * @author Thomas Down
 * @author David Huen
 */

class FeatureFetcher implements Fetcher {
    private HashMap ticketsBySegment;
    private List doneTickets = Collections.EMPTY_LIST;
    private String category;
    private String type;
    private URL dataSource;

    {
	ticketsBySegment = new HashMap();
    }

    FeatureFetcher(URL dataSource, String type, String category) {
	this.dataSource = dataSource;
	this.type = type;
	this.category = category;
    }

    public URL getDataSourceURL() {
	return dataSource;
    }

    public void addTicket(FeatureRequestManager.Ticket ticket) {
	ticketsBySegment.put(ticket.getSegment(), ticket);
    }

    public int size() {
	return ticketsBySegment.size();
    }

    public List getDoneTickets() {
	return doneTickets;
    }

    public void runFetch() 
	throws BioException, ParseException
    {
        // get time
        long startFetch = (new Date()).getTime();
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

	    // We don't bother with XML-encoded fetches any more.
	    // Nice idea, but now kind-of redundant.

	    HttpURLConnection huc = null;
	    Set segmentObjs = ticketsBySegment.keySet();
	    StringBuffer sb = new StringBuffer();
	    for (Iterator i = segmentObjs.iterator(); i.hasNext(); ) {
		Object seg = (Object) i.next();
		sb.append("segment=");
		Segment segment = (Segment) seg;
		sb.append(segment.getID());
		if (segment.isBounded()) {
		    sb.append(':');
		    sb.append(segment.getStart());
		    sb.append(',');
		    sb.append(segment.getStop());
		}
		
		if (i.hasNext()) {
		    sb.append(';');
		}
	    }
	    String segments = sb.toString();
	    // System.err.println("Fetching: " + segments);
	   
	    String encodingRequest = "categorize=yes;";
	    if (fetchEncoding.equals("dasgff")) {
		encodingRequest += "";
	    } else {
		encodingRequest += "encoding=" + fetchEncoding + ";";
	    }
	    
	    String typeRequest = "";
	    if (type != null) {
		typeRequest = "type=" + type + ";";
	    }

	    String categoryRequest = "";
	    if (category != null) {
		categoryRequest = "category=" + category + ";";
	    }

	    String queryString = encodingRequest + categoryRequest + typeRequest + segments;

	    // System.err.println("Fetching: " + queryString);

	    // fURL = new URL(dataSource, "features?" + encodingRequest + categoryRequest + typeRequest + segments);
	    // huc = (HttpURLConnection) fURL.openConnection();
	    // huc.setRequestProperty("Accept-Encoding", "gzip");
	    
	    fURL = new URL(dataSource, "features");
	    huc = (HttpURLConnection) fURL.openConnection();
	    huc.setRequestMethod("POST");
	    huc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	    huc.setRequestProperty("Accept-Encoding", "gzip");
	    huc.setDoOutput(true);
	    OutputStream os = huc.getOutputStream();
	    PrintStream ps = new PrintStream(os);
	    ps.print(queryString);
	    ps.close();

	    huc.connect();

	    // int status = huc.getHeaderFieldInt("X-DAS-Status", 0);
	    int status = DASSequenceDB.tolerantIntHeader(huc, "X-DAS-Status");
	    if (status == 0) {
		throw new BioRuntimeException("Not a DAS server: " + fURL.toString());
	    } else if (status != 200) {
		throw new BioRuntimeException("DAS error (status code = " + status + ") fetching " + fURL.toString() + " with query " + queryString);
	    }

            // determine if I'm getting a gzipped reply
            String contentEncoding = huc.getContentEncoding();
            
            InputStream inStream = huc.getInputStream();

	    if (contentEncoding != null) {
                if (contentEncoding.indexOf("gzip") != -1) {
		    // we have gzip encoding
		    inStream = new GZIPInputStream(inStream);
		    // System.out.println("gzip encoding!");
                }
            }

	    InputSource is = new InputSource(inStream);
	    is.setSystemId(fURL.toString());
	    DASFeaturesHandler dfh = new DASFeaturesHandler(ticketsBySegment, this, fetchEncoding);
	    XMLReader parser = DASSequence.nonvalidatingSAXParser();
	    parser.setContentHandler(new SAX2StAXAdaptor(dfh));
	    parser.parse(is);
	    
	    doneTickets = dfh.getDoneTickets();
	} catch (IOException ex) {
	    throw new ParseException(ex);
	} catch (SAXException ex) {
	    throw new ParseException(ex, "Error parsing XML from " + fURL);
	} finally {
	    DAS.completedActivity(this);
	}
    }

    //
    // StAX handler for the new, generic, DASFEATURES document
    //

    private class DASFeaturesHandler extends StAXContentHandlerBase {
	private boolean inDocument = false;
	private Map ticketsBySegment;
	private FeatureRequestManager.Ticket thisTicket;
	private List doneTickets = new ArrayList();
	private String fetchEncoding; 
        private Object trigger;

	public List getDoneTickets() {
	    return doneTickets;
	}

	public DASFeaturesHandler(Map ticketsBySegment,
				  Object trigger,
				  String fetchEncoding) 
	{
	    this.ticketsBySegment = ticketsBySegment;
            this.trigger = trigger;
	    this.fetchEncoding = fetchEncoding;
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
		    thisTicket = (FeatureRequestManager.Ticket) ticketsBySegment.get(new Segment(segID));
		    if (thisTicket == null) {
			int start = Integer.parseInt(attrs.getValue("start"));
			int stop = Integer.parseInt(attrs.getValue("stop"));
			Segment seg = new Segment(segID, start, stop);
			thisTicket = (FeatureRequestManager.Ticket) ticketsBySegment.get(seg);
			if (thisTicket == null) {
			    throw new SAXException("Response segment " + segID + ":" + start + 
						   "," + stop + " wasn't requested");
			}
			segID = segID + ":" + start + "," + stop;
		    }

		    // System.err.println("Got segment: " + segID);

		    dm.delegate(new DASSegmentHandler(((FeatureRequestManager.FeatureTicket) thisTicket).getOutputListener(), fetchEncoding));
		} else if (localName.equals("segmentNotAnnotated")) {
		    String segID = attrs.getValue("id");
		    if (segID == null) {
			throw new SAXException("Missing segment ID");
		    }
		    thisTicket = (FeatureRequestManager.Ticket) ticketsBySegment.get(new Segment(segID));
		    if (thisTicket == null) {
			int start = Integer.parseInt(attrs.getValue("start"));
			int stop = Integer.parseInt(attrs.getValue("stop"));
			Segment seg = new Segment(segID, start, stop);
			thisTicket = (FeatureRequestManager.Ticket) ticketsBySegment.get(seg);
			if (thisTicket == null) {
			    throw new SAXException("Response segment " + segID + ":" + start + 
						   "," + stop + " wasn't requested");
			}
		    }

		    SeqIOListener siol = ((FeatureRequestManager.FeatureTicket) thisTicket).getOutputListener();
		    try {
			siol.startSequence();
			siol.endSequence();
		    } catch (ParseException ex) {
			throw new SAXException(ex);
		    }
		    
		    thisTicket.setAsFetched();
		    doneTickets.add(thisTicket);
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
                , ticketsBySegment.size());
	    }
	}
    }

    private class DASSegmentHandler extends StAXContentHandlerBase {
	private SeqIOListener siol;
	private int level = 0;
	private String fetchEncoding;

	public DASSegmentHandler(SeqIOListener siol,
				 String fetchEncoding) 
	{
	    this.siol = siol;
	    this.fetchEncoding = fetchEncoding;
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
		    String segVersion = attrs.getValue("version");
		    if (segVersion != null) {
			siol.addSequenceProperty("sequence.version", segVersion);
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
		} else if (localName.equals("FEATURE")) {
		    dm.delegate(new DASGFFFeatureHandler(siol));
		} else {
		    throw new SAXException("Expecting an XFF featureSet or DASGFF FEATURE, but got " + localName);
		}
	    }
	}

	public void endElement(String nsURI,
			       String localName,
			       String qName,
			       StAXContentHandler handler)
	    throws SAXException
	{
	    // System.err.println("endElement: " + localName);
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
