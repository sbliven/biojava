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
 * Queue and schedule requests for DAS features.
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
	    frm = new FeatureRequestManager();
	    requestManagers.put(dataSource, frm);
	}
	return frm;
    }

    private Set openTickets;

    {
	openTickets = new HashSet();
    }

    private FeatureRequestManager() {
    }

    public Ticket requestFeatures(URL ds, String id, SeqIOListener l) {
	return requestFeatures(ds, id, l, null, null);
    }

    public Ticket requestFeatures(URL ds, String id, SeqIOListener l, String type, String category)
    {
	Ticket t = new Ticket(ds, id, l, type, category);
	openTickets.add(t);
	return t;
    }

    private static boolean stringCompare(String a, String b) {
	if (a == null || b == null) {
	    return (a == b);
	}
	return a.equals(b);
    }

    private FeatureFetcher makeFeatureFetcher(URL dataSourceURL,
					      String triggerType,
					      String triggerCategory)
	throws BioException
    {
	FeatureFetcher ffetcher = new FeatureFetcher(dataSourceURL, triggerType, triggerCategory);
	try {
  	    boolean doXMLRequest = DASCapabilities.checkCapable(new URL(dataSourceURL, "../"),
  								DASCapabilities.CAPABILITY_EXTENDED,
  								DASCapabilities.CAPABILITY_EXTENDED_FEATURES);
  	    ffetcher.setUseXMLFetch(doXMLRequest);
  	} catch (MalformedURLException ex) {
  	    throw new BioException(ex);
	}

	return ffetcher;
    }

    private synchronized void fetch(Ticket trigger) 
        throws ParseException, BioException
    {
	String triggerType = trigger.getType();
	String triggerCategory = trigger.getCategory();
	Map fetchers = new HashMap();
	   
	for (Iterator i = openTickets.iterator(); i.hasNext(); ) {
	    Ticket t = (Ticket) i.next();
	    if (stringCompare(triggerType, t.getType()) && 
		stringCompare(triggerCategory, t.getCategory())) 
	    {
		URL dataSourceURL = t.getDataSource();
		FeatureFetcher ffetcher = (FeatureFetcher) fetchers.get(dataSourceURL);
		if (ffetcher == null) {
		    ffetcher = makeFeatureFetcher(dataSourceURL, triggerType, triggerCategory);
		    fetchers.put(dataSourceURL, ffetcher);
		}
		ffetcher.addTicket(t);
	    }
	}

	if(fetchers.size() < 1) {
	    System.err.println("*** Hmmm, don't actually seem to be fetching anything...");
	    return;
	}

	// System.err.println("*** Built " + fetchers.size() + " feature-fetch jobs");

	if (DAS.getThreadFetches() && (fetchers.size() > 1)) {
	    FetchMonitor monitor = new FetchMonitor();
	    for (Iterator i = fetchers.values().iterator(); i.hasNext(); ) {
		monitor.addJob((FeatureFetcher) i.next());
	    }
	    List okay = monitor.doFetches();
	    for (Iterator i = okay.iterator(); i.hasNext(); ) {
		FeatureFetcher ffetcher = (FeatureFetcher) i.next();
		openTickets.removeAll(ffetcher.getDoneTickets());
	    }
	} else {
	    for (Iterator i = fetchers.values().iterator(); i.hasNext(); ) {
		FeatureFetcher ffetcher = (FeatureFetcher) i.next();
		ffetcher.runFetch();
		openTickets.removeAll(ffetcher.getDoneTickets());
	    }
	}
    }

    private class FetchMonitor {
	private Set pending = new HashSet();
	private List successes = new ArrayList();

	private FetchJob failedJob;
	private Exception failure;

	public void addJob(FeatureFetcher ff) {
	    pending.add(new FetchJob(ff, this));
	}

	public void jobSucceeded(FetchJob j) {
	    synchronized (successes) {
		successes.add(j.getFetcher());
	    }
	    synchronized (pending) {
		pending.remove(j);
		if (pending.size() == 0) {
		    pending.notifyAll();
		}
	    }
	    // System.err.println("*** Job checked in (success)");
	}

	public void jobFailed(FetchJob j, Exception ex) {
	    if (failedJob == null) {
		failedJob = j;
		failure = ex;
	    }

	    synchronized (pending) {
		pending.remove(j);
		if (pending.size() == 0) {
		    pending.notifyAll();
		}
	    }
	    // System.err.println("*** Job checked in (failure)");
	}

	public List doFetches() throws BioException {
	    synchronized (pending) {
		for (Iterator i = pending.iterator(); i.hasNext(); ) {
		    FetchJob job = (FetchJob) i.next();
		    job.start();
		    // System.err.println("*** Job checked out");
		}
		try {
		    pending.wait();
		} catch (InterruptedException ex ) {}
	    }

	    if (pending.size() != 0) {
		throw new BioError("Assertion failed: threads going screwy");
	    }

	    if (failedJob != null) {
		throw new BioException(failure, "Failure while fetching features from " + failedJob.getFetcher().getDataSourceURL());
	    }

	    return successes;
	}
    }

    private class FetchJob extends Thread {
	private FeatureFetcher fetcher;
	private FetchMonitor monitor;
	
	FetchJob(FeatureFetcher fetcher,
		 FetchMonitor monitor) 
	{
	    this.fetcher = fetcher;
	    this.monitor = monitor;
	}

	public FeatureFetcher getFetcher() {
	    return fetcher;
	}

	public void run() {
	    try {
		fetcher.runFetch();
		monitor.jobSucceeded(this);
	    } catch (Exception ex) {
		monitor.jobFailed(this, ex);
	    }
	}
    }

    public class Ticket {
	private boolean _isFired = false;
	private String id;
	private String type;
	private String category;
	private SeqIOListener outputListener;
	private URL dataSource;

	public Ticket(URL dataSource,
		      String id,
		      SeqIOListener listener,
		      String type,
		      String category)
	{
	    this.dataSource = dataSource;
	    this.id = id;
	    this.outputListener = listener;
	    this.type = type;
	    this.category = category;
	}

	private URL getDataSource() {
	    return dataSource;
	}

	String getID() {
	    return id;
	}

	SeqIOListener getOutputListener() {
	    return outputListener;
	}

	private String getType() {
	    return type;
	}

	private String getCategory() {
	    return category;
	}

	void setAsFetched() {
	    _isFired = true;
	    id = null;
	    outputListener = null;
	}

        public void doFetch() 
	    throws ParseException, BioException
	{
	    if (!_isFired) {
		fetch(this);
	    }
	}

	public boolean isFetched() {
	    return _isFired;
	}
    }
}
