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
import org.biojava.bio.symbol.*;

import org.apache.xerces.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.w3c.dom.*;

/**
 * FeatureHolder reflecting features provided by a DAS annotation
 * server.
 *
 * @since 1.1
 * @author Thomas Down
 */

class DASFeatureSet implements FeatureHolder {
    private SimpleFeatureHolder realFeatures;
    private final Sequence refSequence;
    private final URL dataSource;
    private final String sourceID;

    DASFeatureSet(Sequence seq, URL ds, String id)
        throws BioException
    {
	refSequence = seq;
	dataSource = ds;
	sourceID = id;
    }

    protected FeatureHolder getFeatures() {
	if (realFeatures != null)
	    return realFeatures;

	try {
	    URL epURL = new URL(dataSource, "features?ref=" + sourceID);
	    HttpURLConnection huc = (HttpURLConnection) epURL.openConnection();
	    huc.connect();
	    int status = huc.getHeaderFieldInt("X-DAS-Status", 0);
	    if (status == 0)
		throw new BioError("Not a DAS server");
	    else if (status != 200)
		throw new BioError("DAS error (status code = " + status + ")");

	    InputSource is = new InputSource(huc.getInputStream());
	    DOMParser parser = new DOMParser();
	    parser.parse(is);
	    Element el = parser.getDocument().getDocumentElement();
	    NodeList gffl = el.getElementsByTagName("GFF");
	    if (gffl.getLength() != 1)
		throw new BioError("Couldn't find GFF element");
	    el = (Element) gffl.item(0);
	    String version = el.getAttribute("version");
	    if (version == null || !version.equals("0.95"))
		throw new BioError("Unrecognized DASGFF version " + version);
	    NodeList segl = el.getElementsByTagName("SEGMENT");
	    if (segl.getLength() != 1)
		throw new BioError("DASGFF documents must contain one SEGMENT");
	    el = (Element) segl.item(0); 
	    
	    realFeatures = new SimpleFeatureHolder();
	    
	    Node segChld = el.getFirstChild();
	    while (segChld != null) {
		if (segChld instanceof Element) {
		    Element featureEl = (Element) segChld;
		    if (featureEl.getTagName().equals("FEATURE")) {
			Feature.Template temp = parseDASFeature(featureEl);
			realFeatures.addFeature(((RealizingFeatureHolder) refSequence).realizeFeature(refSequence, temp));
		    }
		}
		segChld = segChld.getNextSibling();
	    }
	    
	} catch (SAXException ex) {
	    throw new BioError(ex, "Exception parsing DAS XML");
	} catch (IOException ex) {
	    throw new BioError(ex, "Error connecting to DAS server");
	} catch (NumberFormatException ex) {
	    throw new BioError(ex);
	} catch (BioException ex) {
	    throw new BioError(ex);
	} catch (ChangeVetoException ex) {
	    throw new BioError(ex, "Assertion failed: Uncooperative hidden FeatureHolder");
	}

	return realFeatures;
    }

    private Feature.Template parseDASFeature(Element fe) 
        throws NumberFormatException
    {
	String type = "unknown";
	String method = "unknown";
	int start = -1, end = -1;
	String orientation="0";
	String phase="-";
	Location loc = null;

	Node n = fe.getFirstChild();
	while (n != null) {
	    if (n instanceof Element) {
		Element nel = (Element) n;
		String tag = nel.getTagName();
		if (tag.equals("TYPE"))
		    type = getChildText(nel);
		else if (tag.equals("METHOD"))
		    method = getChildText(nel);
		else if (tag.equals("START"))
		    start = Integer.parseInt(getChildText(nel));
		else if (tag.equals("END"))
		    end = Integer.parseInt(getChildText(nel));
		else if (tag.equals("COMPOUNDLOCATION")) 
		    loc = parseCompoundLocation(getChildText(nel).trim());
		else if (tag.equals("ORIENTATION"))
		    orientation = getChildText(nel);
		else if (tag.equals("PHASE"))
		    phase = getChildText(nel);
		
	    }
	    n = n.getNextSibling();
	}

	Feature.Template temp = null;
	if (orientation.equals("+") || orientation.equals("-")) {
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
	temp.annotation = Annotation.EMPTY_ANNOTATION;

	return temp;
    }

    private Location parseCompoundLocation(String loc) 
        throws NumberFormatException
    {
	boolean ranging = false;
	int from = 0;
	Location result = Location.empty;

	    StringTokenizer toke = new StringTokenizer(loc, ",.", true);
	    while (toke.hasMoreTokens()) {
		String t = toke.nextToken();
		if (t.equals(".")) {
		    ranging = true;
		} else if (t.equals(",")) {
		    ranging = false;
		} else {
		    if (ranging) {
			int to = Integer.parseInt(t);
			Location segment = new RangeLocation(from, to);
			result = result.union(segment);
		    } else {
			from = Integer.parseInt(t);
		    }
		}
	    }
	
	    System.out.println(result.toString());

	return result;
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

    public Iterator features() {
	return getFeatures().features();
    }

    public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
	return getFeatures().filter(ff, recurse);
    }
    
    public int countFeatures() {
	return getFeatures().countFeatures();
    }
    
    public Feature createFeature(Feature.Template temp) 
        throws ChangeVetoException
    {
	throw new ChangeVetoException("Can't create features on DAS sequences.");
    }

    public void removeFeature(Feature f) 
        throws ChangeVetoException
    {
	throw new ChangeVetoException("Can't remove features from DAS sequences.");
    }

    // 
    // Changeable stuff (which we're not, fortunately)
    //

    public void addChangeListener(ChangeListener cl) {}
    public void addChangeListener(ChangeListener cl, ChangeType ct) {}
    public void removeChangeListener(ChangeListener cl) {}
    public void removeChangeListener(ChangeListener cl, ChangeType ct) {}
}
