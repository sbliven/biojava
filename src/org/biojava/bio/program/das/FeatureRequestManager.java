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
 * Class which controls the fetching of features from DAS servers.
 *
 * @since 1.1
 * @author Thomas Down
 */

class FeatureRequestManager {
    private static Map requestManagers;

    static {
	requestManagers = new HashMap();
    }

    public static FeatureRequestManager getManager(URL dataSource) {
	FeatureRequestManager frm = (FeatureRequestManager) requestManagers.get(dataSource);
	if (frm == null) {
	    frm = new FeatureRequestManager(dataSource);
	    requestManagers.put(dataSource, frm);
	}
	return frm;
    }

    private Set openTickets;
    private URL dataSourceURL;

    {
	openTickets = new HashSet();
    }

    public FeatureRequestManager(URL dataSource) {
	dataSourceURL = dataSource;
    }

    public Ticket requestFeatures(String id, SeqIOListener l) {
	return requestFeatures(id, l, null, null);
    }

    public Ticket requestFeatures(String id, SeqIOListener l, String type, String category)
    {
	Ticket t = new Ticket(id, l, type, category);
	openTickets.add(t);
	return t;
    }

    private static boolean stringCompare(String a, String b) {
	if (a == null || b == null) {
	    return (a == b);
	}
	return a.equals(b);
    }

    private synchronized boolean fetchAll(Ticket trigger) 
        throws ParseException, BioException
    {
	boolean startedActivity = false;

	try {
	    boolean canFetchMulti = DASCapabilities.checkCapable(new URL(dataSourceURL, ".."),
								 DASCapabilities.CAPABILITY_EXTENDED,
								 DASCapabilities.CAPABILITY_EXTENDED_FEATURES);
	    if (!canFetchMulti) {
		return false;
	    }

	    if (openTickets.size() <= 1) {
		return false;
	    }

	    //
	    // Server seems to do extended feature-fetches.  Try to honour all the tickets.
	    //

	    Set matchingTickets = new HashSet();
	    String triggerType = trigger.getType();
	    String triggerCategory = trigger.getCategory();
	    for (Iterator i = openTickets.iterator(); i.hasNext(); ) {
		Ticket t = (Ticket) i.next();
		if (stringCompare(triggerType, t.getType()) && stringCompare(triggerCategory, t.getCategory())) {
		    matchingTickets.add(t);
		}
	    }

            if(matchingTickets.size() <= 1) {
              return false;
            }
            
	    // System.err.println("Wheee, extended fetch of " + matchingTickets.size() + " requests (" + triggerType + "," + triggerCategory + ")");

	    DAS.startedActivity(trigger);
	    startedActivity = true;

	    URL fURL = new URL(dataSourceURL, "features");
	    HttpURLConnection huc = (HttpURLConnection) fURL.openConnection();
	    huc.setRequestMethod("POST");
	    huc.setRequestProperty("Content-Type", "text/xml");
	    huc.setDoOutput(true);

	    OutputStream os = huc.getOutputStream();
	    PrintStream ps = new PrintStream(os);
	    Map ticketsById = new HashMap();

	    ps.print("<featureRequest encoding=\"xff\"");
	    if (triggerType != null) {
		ps.print(" type=\"" + triggerType + "\"");
	    }
	    if (triggerCategory != null) {
		ps.print(" category=\"" + triggerCategory + "\"");
	    }
	    ps.println(">");

	    for (Iterator i = matchingTickets.iterator(); i.hasNext(); ) {
		Ticket t = (Ticket) i.next();
		ps.println("  <segment id=\"" + t.getID() + "\" />");
		ticketsById.put(t.getID(), t);
	    }
	    ps.println("</featureRequest>");

	    // Transact, and hopefully get the segments back to honour our tickets.

	    huc.connect();
	    int status = huc.getHeaderFieldInt("X-DAS-Status", 0);
	    if (status == 0) {
		throw new BioError("Not a DAS server: " + fURL.toString());
	    } else if (status != 200) {
		throw new BioError("DAS error (status code = " + status + ")");
	    }

	    InputSource is = new InputSource(huc.getInputStream());
	    DASFeaturesHandler dfh = new DASFeaturesHandler(ticketsById, trigger);
	    SAXParser parser = new SAXParser();
	    parser.setContentHandler(new SAX2StAXAdaptor(dfh));
	    parser.parse(is);
	    openTickets.removeAll(dfh.getDoneTickets());
	} catch (IOException ex) {
	    throw new ParseException(ex);
	} catch (SAXException ex) {
	    throw new ParseException(ex);
	} finally {
	    if (startedActivity) {
		DAS.completedActivity(trigger);
	    }
	}

	// Looks like this worked...

	return true;
    }

    private class DASFeaturesHandler extends StAXContentHandlerBase {
	private boolean inDocument = false;
	private Map ticketsById;
	private Ticket thisTicket;
	private List doneTickets = new ArrayList();
        private Ticket trigger;

	public List getDoneTickets() {
	    return doneTickets;
	}

	public DASFeaturesHandler(Map ticketsById, Ticket trigger) {
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
		    thisTicket = (Ticket) ticketsById.get(segID);
		    if (thisTicket == null) {
			throw new SAXException("Response segment " + segID + " wasn't requested");
		    }

		    dm.delegate(new DASSegmentHandler(thisTicket.getOutputListener()));
		} else if (localName.equals("segmentNotAnnotated")) {
		    String segID = attrs.getValue("id");
		    if (segID == null) {
			throw new SAXException("Missing segment ID");
		    }
		    Ticket t = (Ticket) ticketsById.get(segID);
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
			       String qName)
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
			       String qName)
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

    private synchronized void fetchTicket(Ticket t) 
        throws ParseException, BioException
    {
	// System.err.println("Sigh, just fetching one featureSet (" + t.getType() + "," + t.getCategory() + ")");

	try {
	    DAS.startedActivity(t);
	    boolean useXFF = DASCapabilities.checkCapable(new URL(dataSourceURL, ".."),
							  DASCapabilities.CAPABILITY_FEATURETABLE,
							  DASCapabilities.CAPABILITY_FEATURETABLE_XFF);
	    
	    String filter = "";
	    if (t.getType() != null) {
		filter += ";type=" + t.getType();
	    }
	    if (t.getCategory() != null) {
		filter += ";category=" + t.getCategory();
	    }

	    if (useXFF) {
		URL fURL = new URL(dataSourceURL, "features?encoding=xff;ref=" + t.getID() + filter);
		HttpURLConnection huc = (HttpURLConnection) fURL.openConnection();
		huc.connect();
		int status = huc.getHeaderFieldInt("X-DAS-Status", 0);
		if (status == 0)
		    throw new BioError("Not a DAS server: " + fURL.toString());
		else if (status != 200)
		    throw new BioError("DAS error (status code = " + status + ")");

		Map ticketsById = new HashMap();
		ticketsById.put(t.getID(), t);
		InputSource is = new InputSource(huc.getInputStream());
		DASFeaturesHandler dfh = new DASFeaturesHandler(ticketsById, t);
		SAXParser parser = new SAXParser();
		parser.setContentHandler(new SAX2StAXAdaptor(dfh));
		parser.parse(is);
		openTickets.removeAll(dfh.getDoneTickets());		
	    } else {
		URL fURL = new URL(dataSourceURL, "features?ref=" + t.getID() + filter);
		DASGFFParser.INSTANCE.parseURL(fURL, t.getOutputListener());
		openTickets.remove(t);
		t.setAsFetched();
	    }
	} catch (IOException ex) {
	    throw new ParseException(ex);
	} catch (SAXException ex) {
	    throw new ParseException(ex);
	} finally {
	    DAS.completedActivity(t);
	}
    }

    public class Ticket {
	private boolean _isFired = false;
	private String id;
	private String type;
	private String category;
	private SeqIOListener outputListener;

	public Ticket(String id,
		      SeqIOListener listener,
		      String type,
		      String category)
	{
	    this.id = id;
	    this.outputListener = listener;
	    this.type = type;
	    this.category = category;
	}

	private String getID() {
	    return id;
	}

	private SeqIOListener getOutputListener() {
	    return outputListener;
	}

	private String getType() {
	    return type;
	}

	private String getCategory() {
	    return category;
	}

	private void setAsFetched() {
	    _isFired = true;
	    id = null;
	    outputListener = null;
	}

        public void doFetch() 
	    throws ParseException, BioException
	{
	    if (!_isFired) {
		if (!fetchAll(this)) {
		    fetchTicket(this);
		}
	    }
	}

	public boolean isFetched() {
	    return _isFired;
	}
    }
}
