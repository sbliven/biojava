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

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.w3c.dom.*;

/**
 * Discover the capabilities of a given DAS server
 *
 * @author Thomas Down
 * @since 1.1
 */

class DASCapabilities {
    private static Map capabilityCache;

    public static final String CAPABILITY_FEATURETABLE = "featureTable";
    public static final String CAPABILITY_FEATURETABLE_DASGFF = "dasgff";
    public static final String CAPABILITY_FEATURETABLE_XFF = "xff";

    public static final String CAPABILITY_EXTENDED = "dasExtendedRequest";
    public static final String CAPABILITY_EXTENDED_FEATURES = "features";

    public static final String CAPABILITY_INDEX = "index";

    private static final Map DEFAULT_CAPABILITIES;
    
    static {
	Map m = new HashMap();
	m.put(CAPABILITY_FEATURETABLE, Collections.nCopies(1, CAPABILITY_FEATURETABLE_DASGFF));
	DEFAULT_CAPABILITIES = Collections.unmodifiableMap(m);

	capabilityCache = new HashMap();
    }

    public static boolean checkCapable(URL dasURL, String type) {
	return getCapabilities(dasURL).containsKey(type);
    }

    public static boolean checkCapable(URL dasURL, String type, String value) {
	List l = (List) getCapabilities(dasURL).get(type);
	if (l == null)
	    return false;
	return l.contains(value);
    }

    public static Map getCapabilities(URL dasURL) {
	Map caps = (Map) capabilityCache.get(dasURL);
	if (caps == null) {
	    caps = fetchCapabilities(dasURL);
	    capabilityCache.put(dasURL, caps);
	}
	
	return caps;
    }

    private static Map fetchCapabilities(URL dasURL) {
	// System.out.println("Getting capabilities for: " + dasURL);

	try {
	    URL capURL = new URL(dasURL, "capabilities");
	    HttpURLConnection huc = (HttpURLConnection) capURL.openConnection();
	    huc.connect();
	    int status = huc.getHeaderFieldInt("X-DAS-Status", 0);
	    if (status == 0) {
		throw new BioException("Not a DAS server");
	    } else if (status != 200) {
		throw new BioException("DAS error (status code = " + status +
				       ") connecting to " + dasURL);
	    }

	    InputSource is = new InputSource(huc.getInputStream());
	    DocumentBuilder parser = DASSequence.nonvalidatingParser();
	    Element el = parser.parse(is).getDocumentElement();

	    Map caps = new HashMap();
	    Node n = el.getFirstChild();
	    while (n != null) {
		if (n instanceof Element) {
		    Element capEl = (Element) n;
		    if (capEl.getTagName().equals("capability")) {
			String type = capEl.getAttribute("type");
			String value = capEl.getAttribute("value");
			if (caps.containsKey(type)) {
			    ((List) caps.get(type)).add(value);
			} else {
			    List l = new ArrayList();
			    l.add(value);
			    caps.put(type, l);
			}
		    }
		}
		n = n.getNextSibling();
	    }

	    return Collections.unmodifiableMap(caps);
	} catch (Exception ex) {
	    return DEFAULT_CAPABILITIES;
	}
    }
}
