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
import org.biojava.bio.seq.db.*;
import org.biojava.bio.symbol.*;

import org.apache.xerces.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.w3c.dom.*;

/**
 * Collection of sequences retrieved from the DAS network.
 *
 * <p>The DAS-specific parts of this API are still subject
 * to change.</p>
 *
 * @author Thomas Down
 * @since 1.1
 */

public class DASSequenceDB implements SequenceDB {
    private URL dataSourceURL;
    private Map sequences;
    private Cache symbolsCache;
    private boolean gotAllIDs = false;

    {
	sequences = new HashMap();
	symbolsCache = new FixedSizeCache(20);
    }

    Cache getSymbolsCache() {
	return symbolsCache;
    }

    public DASSequenceDB(URL dataSourceURL) 
	throws BioException 
    {
	this.dataSourceURL = dataSourceURL;
    }

    /**
     * Return a SequenceDB exposing /all/ the entry points
     * in this DAS datasource.
     *
     */

    private SequenceDB allEntryPoints;

    public SequenceDB allEntryPointsDB() {
	if (allEntryPoints == null) {
	    // allEntryPoints = new HashSequenceDB("All entry points from " + getURL().toString());
	    //  try {
//  		for (SequenceIterator si = sequenceIterator(); si.hasNext(); ) {
//  		    Sequence seq = si.nextSequence();
//  		    allEntryPoints.addSequence(seq);
//  		    FeatureHolder allComponents = seq.filter(
//  		            new FeatureFilter.ByClass(ComponentFeature.class),
//  			    true);
//  		    for (Iterator cfi = allComponents.features(); cfi.hasNext(); ) {
//  			ComponentFeature cf = (ComponentFeature) cfi.next();
//  			allEntryPoints.addSequence(cf.getComponentSequence());
//  		    }
//  		}
//  	    } catch (BioException ex) {
//  		throw new BioError(ex);
//  	    } catch (ChangeVetoException ex) {
//  		throw new BioError(ex, "Assertion failed: Couldn't modify our SequenceDB");
//  	    }

	    allEntryPoints = new AllEntryPoints();
	}

	return allEntryPoints;
    }

    private class AllEntryPoints implements SequenceDB {
	public Sequence getSequence(String id)
	    throws BioException
	{
	    return new DASSequence(DASSequenceDB.this, dataSourceURL, id);
	}

	public Set ids() {
	    throw new BioError("ImplementMe");
	}

	public void addSequence(Sequence seq)
	    throws ChangeVetoException
	{
	    throw new ChangeVetoException("No way we're adding sequences to DAS");
	}

	public void removeSequence(String id)
	    throws ChangeVetoException
	{
	    throw new ChangeVetoException("No way we're removing sequences from DAS");
	}

	public SequenceIterator sequenceIterator() {
	    throw new BioError("ImplementMe");
	}

	public String getName() {
	    return "All sequences in " + dataSourceURL.toString();
	}

	// 
	// Changeable stuff (which we're not, fortunately)
	//

	public void addChangeListener(ChangeListener cl) {}
	public void addChangeListener(ChangeListener cl, ChangeType ct) {}
	public void removeChangeListener(ChangeListener cl) {}
	public void removeChangeListener(ChangeListener cl, ChangeType ct) {}
    }


    /**
     * Return the URL of the reference server for this database.
     */

    public URL getURL() {
	return dataSourceURL;
    }

    public String getName() {
	return dataSourceURL.toString();
    }

    public Sequence getSequence(String id) {
	// if (! sequences.containsKey(id))
	//    throw new NoSuchElementException("Couldn't find sequence " + id);
	Sequence seq = (Sequence) sequences.get(id);
	if (seq == null) {
	    try {
		seq = new DASSequence(this, dataSourceURL, id);
	    } catch (Exception ex) {
		throw new BioError(ex);
	    }
	    sequences.put(id, seq);
	}
	return seq;
    }

    public Set ids() {
	if (!gotAllIDs) {
	    try {
		URL epURL = new URL(dataSourceURL, "entry_points");
		HttpURLConnection huc = (HttpURLConnection) epURL.openConnection();
		try {
		    huc.connect();
		} catch (Exception e) {
		    throw new BioException(e, "Can't connect to " + epURL);
		}
		// int status = huc.getHeaderFieldInt("X-DAS-Status", 0);
		int status = DASSequenceDB.tolerantIntHeader(huc, "X-DAS-Status");
		if (status == 0)
		    throw new BioException("Not a DAS server: " + dataSourceURL);
		else if (status != 200)
		    throw new BioException("DAS error (status code = " + status +
					   ") connecting to " + dataSourceURL + " with query " + epURL);


		InputSource is = new InputSource(huc.getInputStream());
		DOMParser parser = DASSequence.nonvalidatingParser();
		parser.parse(is);
		Element el = parser.getDocument().getDocumentElement();
		
		NodeList segl = el.getElementsByTagName("SEGMENT");
		Element segment = null;
		for (int i = 0; i < segl.getLength(); ++i) {
		    el = (Element) segl.item(i);
		    String id = el.getAttribute("id");
		    if (! sequences.containsKey(id)) 
			sequences.put(id, null);
		}

		gotAllIDs = true;
	    } catch (SAXException ex) {
		throw new BioError(ex, "Exception parsing DAS XML");
	    } catch (IOException ex) {
		throw new BioError(ex, "Error connecting to DAS server");
	    } catch (NumberFormatException ex) {
		throw new BioError(ex);
	    } catch (BioException ex) {
		throw new BioError(ex);
	    }
	}

	return sequences.keySet();
    }

    public void addSequence(Sequence seq)
        throws ChangeVetoException
    {
	throw new ChangeVetoException("No way we're adding sequences to DAS");
    }

    public void removeSequence(String id)
        throws ChangeVetoException
    {
	throw new ChangeVetoException("No way we're removing sequences from DAS");
    }

    public SequenceIterator sequenceIterator() {
	return new SequenceIterator() {
	    private Iterator i = ids().iterator();

	    public boolean hasNext() {
		return i.hasNext();
	    }

	    public Sequence nextSequence() {
		return getSequence((String) i.next());
	    }
	} ;
    }

    static int tolerantIntHeader(HttpURLConnection huc, String name)
    {
	try {
	    String header = huc.getHeaderField(name);
	    if (header == null) {
		return 0;
	    }

	    String firstToken = new StringTokenizer(header).nextToken();
	    return Integer.parseInt(firstToken);
	} catch (NumberFormatException ex) {
	    return 0;
	}
    }

    // 
    // Changeable stuff (which we're not, fortunately)
    //

    public void addChangeListener(ChangeListener cl) {}
    public void addChangeListener(ChangeListener cl, ChangeType ct) {}
    public void removeChangeListener(ChangeListener cl) {}
    public void removeChangeListener(ChangeListener cl, ChangeType ct) {}
}

