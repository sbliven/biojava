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

/**
 * Queue and schedule requests for DAS features.
 *
 * @since 1.1
 * @author Thomas Down
 */

class FeatureRequestManager {
    private Set openTickets;
    private DASSequenceDB seqDB;

    {
	openTickets = new HashSet();
    }

    FeatureRequestManager(DASSequenceDB seqDB) {
	this.seqDB = seqDB;
    }

    public Ticket requestFeatures(URL ds, String id, SeqIOListener l) {
	return requestFeatures(ds, id, l, null, null);
    }

    public Ticket requestFeatures(URL ds, String id, SeqIOListener l, Location loc) {
	return requestFeatures(ds, id, l, loc, null, null);
    }

    public Ticket requestFeatures(URL ds, String id, SeqIOListener l, String type, String category) {
	return requestFeatures(ds, id, l, null, type, category);
    }

    public Ticket requestFeatures(URL ds,
				  String id,
				  SeqIOListener l,
				  Location loc,
				  String type,
				  String category)
    {
	Segment seg;
	if (loc != null) {
	    seg = new Segment(id, loc.getMin(), loc.getMax());
	} else {
	    seg = new Segment(id);
	}
	Ticket t = new FeatureTicket(ds, seg, type, category, l);
	openTickets.add(t);
	return t;
    }

    public Ticket requestTypes(URL ds,
			       Segment segment,
			       TypesListener l)
    {
	Ticket t = new TypeTicket(ds, segment, null, null, l);
	openTickets.add(t);
	return t;
    }

    private static boolean stringCompare(String a, String b) {
	if (a == null || b == null) {
	    return (a == b);
	}
	return a.equals(b);
    }

    private Fetcher makeFetcher(Ticket trigger)
	throws BioException
    {
	if (trigger instanceof FeatureTicket) {
	    FeatureFetcher ffetcher = new FeatureFetcher(trigger.getDataSource(), 
							 trigger.getType(), 
							 trigger.getCategory());
	    return ffetcher;
	} else if (trigger instanceof TypeTicket) {
	    Fetcher f = new TypesFetcher(trigger.getDataSource(), 
					 trigger.getType(), 
					 trigger.getCategory());
	    return f;
	} else {
	    throw new BioError("Unknown ticket class");
	}
    }

    private synchronized void fetch(Ticket trigger) 
        throws ParseException, BioException
    {
	seqDB.ensureFeaturesCacheCapacity(openTickets.size() * 3);

	String triggerType = trigger.getType();
	String triggerCategory = trigger.getCategory();
	Class triggerClass = trigger.getClass();
	Map fetchers = new HashMap();
	   
	for (Iterator i = openTickets.iterator(); i.hasNext(); ) {
	    Ticket t = (Ticket) i.next();
	    if (triggerClass.isInstance(t) &&
		stringCompare(triggerType, t.getType()) && 
		stringCompare(triggerCategory, t.getCategory())) 
	    {
		URL dataSourceURL = t.getDataSource();
		Fetcher ffetcher = (Fetcher) fetchers.get(dataSourceURL);
		if (ffetcher == null) {
		    ffetcher = makeFetcher(t);
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
		monitor.addJob((Fetcher) i.next());
	    }
	    List okay = monitor.doFetches();
	    for (Iterator i = okay.iterator(); i.hasNext(); ) {
		Fetcher ffetcher = (Fetcher) i.next();
		openTickets.removeAll(ffetcher.getDoneTickets());
	    }
	} else {
	    for (Iterator i = fetchers.values().iterator(); i.hasNext(); ) {
		Fetcher ffetcher = (Fetcher) i.next();
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

	public void addJob(Fetcher ff) {
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
	private Fetcher fetcher;
	private FetchMonitor monitor;
	
	FetchJob(Fetcher fetcher,
		 FetchMonitor monitor) 
	{
	    this.fetcher = fetcher;
	    this.monitor = monitor;
	}

	public Fetcher getFetcher() {
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

    class FeatureTicket extends Ticket {
	private SeqIOListener outputListener;

	public FeatureTicket(URL dataSource,
			     Segment segment,
			     String type,
			     String category,
			     SeqIOListener outputListener)
	{
	    super(dataSource, segment, type, category);
	    this.outputListener = outputListener;
	}

	public SeqIOListener getOutputListener() {
	    return outputListener;
	}

	void setAsFetched() {
	    super.setAsFetched();
	    outputListener = null;
	}
    }

    
    class TypeTicket extends Ticket {
	private TypesListener outputListener;

	public TypeTicket(URL dataSource,
			  Segment segment,
			  String type,
			  String category,
			  TypesListener outputListener)
	{
	    super(dataSource, segment, type, category);
	    this.outputListener = outputListener;
	}

	public TypesListener getTypesListener() {
	    return outputListener;
	}

	void setAsFetched() {
	    super.setAsFetched();
	    outputListener = null;
	}
    }

    public abstract class Ticket {
	private boolean _isFired = false;
	private Segment segment;
	private String type;
	private String category;
	private URL dataSource;

	public Ticket(URL dataSource,
		      Segment segment,
		      String type,
		      String category)
	{
	    this.dataSource = dataSource;
	    this.type = type;
	    this.category = category;
	    this.segment = segment;
	}

	private URL getDataSource() {
	    return dataSource;
	}

	private String getType() {
	    return type;
	}

	private String getCategory() {
	    return category;
	}


	public Segment getSegment() {
	    return segment;
	}

	void setAsFetched() {
	    _isFired = true;
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
