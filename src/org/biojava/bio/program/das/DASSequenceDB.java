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
    private static final int MAX_CAPACITY = 3000;
    private URL dataSourceURL;
    private Map sequences;
    private Cache symbolsCache;
    private FixedSizeCache featuresCache;
    private Set rootIDs;
    private FeatureRequestManager frm;

    {
	sequences = new HashMap();
	symbolsCache = new FixedSizeCache(20);
	featuresCache = new FixedSizeCache(50);
    }

    Cache getSymbolsCache() {
	return symbolsCache;
    }

    /**
     * @throws BioException if the capacity can't be reached.
     */
    void ensureFeaturesCacheCapacity(int min) throws BioException {
      if(min > MAX_CAPACITY) {
        throw new BioException( "Capacity of (" + MAX_CAPACITY +
                                " exceeded by " + min);
      }
	if (featuresCache.getLimit() < min) {
	    System.err.println("Setting cache limit up to " + min);
	    featuresCache.setLimit(min);
	}
    }

    Cache getFeaturesCache() {
	return featuresCache;
    }

    FeatureRequestManager getFeatureRequestManager() {
	if (frm == null) {
	    frm = new FeatureRequestManager(this);
	}

	return frm;
    }

    public DASSequenceDB(URL dataSourceURL) 
	throws BioException 
    {
	this.dataSourceURL = dataSourceURL;
    }

    DASSequence _getSequence(String id) 
        throws BioException, IllegalIDException
    {
	return _getSequence(id, Collections.singleton(dataSourceURL));
    }

    DASSequence _getSequence(String id, Set annoURLs) 
        throws BioException, IllegalIDException
    {
	DASSequence seq = (DASSequence) sequences.get(id);
	if (seq == null) {
	    seq = new DASSequence(this, dataSourceURL, id, annoURLs);
	    sequences.put(id, seq);
	}
	return seq;
    }

    /**
     * Return a SequenceDB exposing /all/ the entry points
     * in this DAS datasource.
     *
     */

    private SequenceDBLite allEntryPoints;

    public SequenceDBLite allEntryPointsDB() {
	if (allEntryPoints == null) {
	    allEntryPoints = new AllEntryPoints();
	}

	return allEntryPoints;
    }

    private class AllEntryPoints implements SequenceDBLite {
	public Sequence getSequence(String id)
	    throws BioException, IllegalIDException
	{
	    return _getSequence(id);
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

    public Sequence getSequence(String id) 
        throws BioException, IllegalIDException
    {
	if (! (ids().contains(id))) {
	    throw new IllegalIDException("Database does not contain " + id + " as a top-level sequence");
	}
	return _getSequence(id);
    }

    public Set ids() {
	if (rootIDs == null) {
	    try {
		Set ids = new HashSet();

		URL epURL = new URL(dataSourceURL, "entry_points");
		HttpURLConnection huc = (HttpURLConnection) epURL.openConnection();
		try {
		    huc.connect();
		} catch (Exception e) {
		    throw new BioException(e, "Can't connect to " + epURL);
		}
		int status = DASSequenceDB.tolerantIntHeader(huc, "X-DAS-Status");
		if (status == 0)
		    throw new BioException("Not a DAS server: " + dataSourceURL + " Query: " + epURL);
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
		    ids.add(id);
		}

		rootIDs = Collections.unmodifiableSet(ids);
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

	return rootIDs;
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

    public SequenceIterator sequenceIterator() 
    {
	return new SequenceIterator() {
	    private Iterator i = ids().iterator();

	    public boolean hasNext() {
		return i.hasNext();
	    }

	    public Sequence nextSequence() 
	        throws BioException
	    {
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

