/*
 * @(#)XMLDispatcher.java      0.1 99/10/18
 *
 * (c) Thomas Down
 */

package org.biojava.utils.xml;

import java.util.*;
import org.xml.sax.*;

/**
 * Simple implementation of XMLPeerFactory which delegates object-
 * construction on the basis of tag name.
 *
 * @author Thomas Down
 */

public class XMLDispatcher implements XMLPeerFactory {
    private Map factoryMap;

    /**
     * Construct a new XMLDispatcher which does not know
     * about any tag names.
     */

    public XMLDispatcher() {
	factoryMap = new HashMap();
    }

    /**
     * Add an XMLPeerFactory which constructs objects corresponding
     * to a specific XML tag.
     *
     * @param tag the tag name to bind.
     * @param fact an XMLPeerFactory implementation to invoke when
     *             the specified tag is encountered.
     */

    public void mapTag(String tag, XMLPeerFactory fact) {
	factoryMap.put(tag, fact);
    }

    public Object getXMLPeer(String tag, List children, AttributeList al) {
	XMLPeerFactory f = (XMLPeerFactory) factoryMap.get(tag);
	if (f != null)
	    return f.getXMLPeer(tag, children, al);
	return null;
    }
}
