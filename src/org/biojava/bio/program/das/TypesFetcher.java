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
 * Encapsulate a single batch of types requests to a DAS server.
 *
 * @since 1.2
 * @author Thomas Down
 * @author David Huen
 */

class TypesFetcher implements Fetcher {
    private HashMap ticketsBySegment;
    private List doneTickets = Collections.EMPTY_LIST;
    private String category;
    private String type;
    private URL dataSource;
    private TypesListener nullSegmentHandler;

    {
	ticketsBySegment = new HashMap();
    }

    TypesFetcher(URL dataSource, String type, String category) {
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

    public void setNullSegmentHandler(TypesListener tl) {
	nullSegmentHandler = tl;
    }

    public void runFetch() 
	throws BioException, ParseException
    {
	DAS.startedActivity(this);
	URL fURL = null;
	    
	
	try {
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
	    // System.err.println("*** Types-Fetching: " + segments);
	    
	    String encodingRequest = "";
	    
	    String typeRequest = "";
	    if (type != null) {
		typeRequest = "type=" + type + ";";
	    }

	    String categoryRequest = "";
	    if (category != null) {
		categoryRequest = "category=" + category + ";";
	    }

	    String queryString = encodingRequest + categoryRequest + typeRequest + segments;

	    // fURL = new URL(dataSource, "types?" + encodingRequest + categoryRequest + typeRequest + segments);
	    // huc = (HttpURLConnection) fURL.openConnection();
	    // huc.setRequestProperty("Accept-Encoding", "gzip");
	    
	    fURL = new URL(dataSource, "types");
	    huc = (HttpURLConnection) fURL.openConnection();
	    if (queryString.length() > 0) {
		huc.setRequestMethod("POST");
		huc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		huc.setRequestProperty("Accept-Encoding", "gzip");
		huc.setDoOutput(true);
		OutputStream os = huc.getOutputStream();
		PrintStream ps = new PrintStream(os);
		ps.print(queryString);
		ps.close();
	    } else {
		huc.setRequestProperty("Accept-Encoding", "gzip");
	    }

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
	    DASTypesHandler dfh = new DASTypesHandler(ticketsBySegment, this);
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
    // StAX handler for the DASTYPES document
    //

    private class DASTypesHandler extends StAXContentHandlerBase {
	private boolean inDocument = false;
	private Map ticketsBySegment;
	private FeatureRequestManager.Ticket thisTicket;
	private List doneTickets = new ArrayList();
        private Object trigger;

	public List getDoneTickets() {
	    return doneTickets;
	}

	public DASTypesHandler(Map ticketsBySegment,
			       Object trigger)
	{
	    this.ticketsBySegment = ticketsBySegment;
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
		    if (nullSegmentHandler != null) {
			dm.delegate(new DASSegmentHandler(nullSegmentHandler));
		    } else {
			String segID = attrs.getValue("id");
			if (segID == null) {
			    throw new SAXException("Missing segment ID");
			}
			Segment seg = new Segment(segID);
			thisTicket = (FeatureRequestManager.Ticket) ticketsBySegment.get(seg);
			if (thisTicket == null) {
			    int start = Integer.parseInt(attrs.getValue("start"));
			    int stop = Integer.parseInt(attrs.getValue("stop"));
			    seg = new Segment(segID, start, stop);
			    thisTicket = (FeatureRequestManager.Ticket) ticketsBySegment.get(seg);
			    if (thisTicket == null) {
				throw new SAXException("Response segment " + segID + ":" + start + 
						       "," + stop + " wasn't requested");
			    }
			    segID = segID + ":" + start + "," + stop;
			}

			ticketsBySegment.remove(seg);

			dm.delegate(new DASSegmentHandler(((FeatureRequestManager.TypeTicket) thisTicket).getTypesListener()));
		    }
		} else if (localName.equals("segmentNotAnnotated") || localName.equals("SEGMENTUNKNOWN")) {
		    String segID = attrs.getValue("id");
		    if (segID == null) {
			throw new SAXException("Missing segment ID");
		    }
		    Segment seg = new Segment(segID);
		    thisTicket = (FeatureRequestManager.Ticket) ticketsBySegment.get(seg);
		    if (thisTicket == null) {
			int start = Integer.parseInt(attrs.getValue("start"));
			int stop = Integer.parseInt(attrs.getValue("stop"));
			seg = new Segment(segID, start, stop);
			thisTicket = (FeatureRequestManager.Ticket) ticketsBySegment.get(seg);
			if (thisTicket == null) {
			    throw new SAXException("Response segment " + segID + ":" + start + 
						   "," + stop + " wasn't requested");
			}
		    }

		    ticketsBySegment.remove(seg);

		    TypesListener siol = ((FeatureRequestManager.TypeTicket) thisTicket).getTypesListener();
		    siol.startSegment();
		    siol.endSegment();
		    
		    thisTicket.setAsFetched();
		    doneTickets.add(thisTicket);
		} else if (localName.equals("segmentError") || localName.equals("SEGMENTERROR")) {
		    String segID = attrs.getValue("id");
		    String segError = attrs.getValue("error");

		    throw new SAXException("Error " + segError + " fetching " + segID);
		}
	    }
	}

	
	public void endTree()
	    throws SAXException
	{
	    for (Iterator i = ticketsBySegment.entrySet().iterator(); i.hasNext(); ) {
		Map.Entry me = (Map.Entry) i.next();
		Segment seg = (Segment) me.getKey();
		System.err.println("*** Not got anything back for segment " + seg.toString());
		TypesListener siol = ((FeatureRequestManager.TypeTicket) me.getValue()).getTypesListener();
		siol.startSegment();
		siol.endSegment();
	    }
	}

	public void endElement(String nsURI,
			       String localName,
			       String qName,
			       StAXContentHandler handler)
	    throws SAXException
	{
	    if (localName.equals("SEGMENT")) {
		if (thisTicket != null) {
		    thisTicket.setAsFetched();
		    doneTickets.add(thisTicket);
		
		    DAS.activityProgress(trigger, doneTickets.size()
					 , ticketsBySegment.size());
		}
	    }
	}
    }

    private class DASSegmentHandler extends StAXContentHandlerBase {
	private TypesListener tl;
	private int level = 0;

	public DASSegmentHandler(TypesListener tl)
	{
	    this.tl = tl;
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
		tl.startSegment();
	    } else {
		if (localName.equals("TYPE")) {
		    final String typeId = attrs.getValue("id");
		    dm.delegate(new StringElementHandlerBase() {
			    protected void setStringValue(String s) {
				String count = s.trim();
				if (count.length() > 0) {
				    try {
					tl.registerType(typeId, Integer.parseInt(count));
					return;
				    } catch (NumberFormatException ex) {
				    }
				}
				tl.registerType(typeId);
			    }
			} );
		} else {
		    throw new SAXException("Unexpected element in DASTYPES: " + localName);
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
		tl.endSegment();
	    }
	    --level;
	}
    }
}
