/*
 * @(#)XMLDispatcher.java      0.1 99/10/18
 *
 * (c) Thomas Down
 */

package utils.xml;

import java.util.*;
import org.xml.sax.*;

public class XMLDispatcher implements XMLPeerFactory {
    private Map factoryMap;

    public XMLDispatcher() {
	factoryMap = new HashMap();
    }

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
