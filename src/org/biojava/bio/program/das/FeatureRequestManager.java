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
	Ticket t = new Ticket(id, l);
	openTickets.add(t);
	return t;
    }

    private boolean fetchAll() 
        throws ParseException, BioException
    {
	try {
	    if (openTickets.size() <= 1) {
		return false;
	    }

	    boolean canFetchMulti = DASCapabilities.checkCapable(new URL(dataSourceURL, ".."),
								 DASCapabilities.CAPABILITY_EXTENDED,
								 DASCapabilities.CAPABILITY_EXTENDED_FEATURES);
	    if (!canFetchMulti) {
		return false;
	    }

	    //
	    // Server seems to do extended feature-fetches.  Try to honour all the tickets.
	    //

	    System.err.println("Wheee, extended fetch of " + openTickets.size() + " requests");

	    URL fURL = new URL(dataSourceURL, "features");
	    HttpURLConnection huc = (HttpURLConnection) fURL.openConnection();
	    huc.setRequestMethod("POST");
	    huc.setDoOutput(true);

	    OutputStream os = huc.getOutputStream();
	    PrintStream ps = new PrintStream(os);
	    Map ticketsById = new HashMap();

	    ps.println("<featureRequest encoding=\"xff\">");
	    for (Iterator i = openTickets.iterator(); i.hasNext(); ) {
		Ticket t = (Ticket) i.next();
		ps.println("  <segment id=\"" + t.getID() + "\" />");
		ticketsById.put(t.getID(), t);
	    }
	    ps.println("</featureRequest>");

	    // Transact, and hopefully get the segments back to honour our tickets.

	    huc.connect();
	    int status = huc.getHeaderFieldInt("X-DAS-Status", 0);
	    if (status == 0)
		throw new BioError("Not a DAS server: " + fURL.toString());
	    else if (status != 200)
		throw new BioError("DAS error (status code = " + status + ")");
	    
	    InputSource is = new InputSource(huc.getInputStream());
	    DOMParser parser = new DOMParser();
	    parser.parse(is);
	    Element el = parser.getDocument().getDocumentElement();

	    Node chld = el.getFirstChild();
	    while (chld != null) {
		if (chld instanceof Element) {
		    Element echld = (Element) chld;
		    String tagName = echld.getTagName();
		    if ("SEGMENT".equals(tagName)) {
			String segID = echld.getAttribute("id");
			Ticket t = (Ticket) ticketsById.get(segID);
			if (t == null) {
			    throw new ParseException("Response segment " + segID + " wasn't requested");
			}

			DASXFFParser.INSTANCE.parseSegment(echld, t.getOutputListener());
			t.setAsFetched();
			openTickets.remove(t);
		    } else if ("segmentError".equals(tagName)) {
			String segID = echld.getAttribute("id");
			String segError = echld.getAttribute("error");

			throw new ParseException("Error " + segError + " fetching " + segID);
		    }
		}
		chld = chld.getNextSibling();
	    }
	} catch (IOException ex) {
	    throw new ParseException(ex);
	} catch (SAXException ex) {
	    throw new ParseException(ex);
	}

	// Looks like this worked...

	return true;
    }

    private void fetchTicket(Ticket t) 
        throws ParseException, BioException
    {
	System.err.println("Sigh, just fetching one featureSet");

	try {
	    boolean useXFF = DASCapabilities.checkCapable(new URL(dataSourceURL, ".."),
							  DASCapabilities.CAPABILITY_FEATURETABLE,
							  DASCapabilities.CAPABILITY_FEATURETABLE_XFF);
	    
	    if (useXFF) {
		URL fURL = new URL(dataSourceURL, "features?encoding=xff;ref=" + t.getID());
		DASXFFParser.INSTANCE.parseURL(fURL, t.getOutputListener());
	    } else {
		URL fURL = new URL(dataSourceURL, "features?ref=" + t.getID());
		DASGFFParser.INSTANCE.parseURL(fURL, t.getOutputListener());
	    }

	    openTickets.remove(t);
	    t.setAsFetched();
	} catch (IOException ex) {
	    throw new ParseException(ex);
	}
    }

    public class Ticket {
	private boolean _isFired = false;
	private String id;
	private SeqIOListener outputListener;

	public Ticket(String id,
		      SeqIOListener listener)
	{
	    this.id = id;
	    this.outputListener = listener;
	}

	private String getID() {
	    return id;
	}

	private SeqIOListener getOutputListener() {
	    return outputListener;
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
		if (!fetchAll()) {
		    fetchTicket(this);
		}
	    }
	}

	public boolean isFetched() {
	    return _isFired;
	}
    }
}
