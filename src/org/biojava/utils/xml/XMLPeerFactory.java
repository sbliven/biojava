/*
 * @(#)XMLPeerFactory.java      0.1 99/10/18
 *
 * (c) Thomas Down
 */

package utils.xml;

import org.xml.sax.*;
import java.util.*;

/**
 * Interface to an factory which produces Java objects which
 * reflect element in an XML document.
 */

public interface XMLPeerFactory {
    /**
     * Return a Java object to reflect an XML element.
     *
     * @param tag the XML tag.
     * @param children a List (may be empty) of objects reflecting
     *                 child nodes.
     * @param attrs attribute list for the tag.
     *
     * @return a Java object to reflect the XML tag, or <code>null</code> if
     *         this element should be silently ignored.
     */

    public Object getXMLPeer(String tag, List children,
			     AttributeList attrs);
}
