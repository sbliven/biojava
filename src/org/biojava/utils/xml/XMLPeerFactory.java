/*
 * @(#)XMLPeerFactory.java      0.1 99/10/18
 *
 * (c) Thomas Down
 */

package utils.xml;

import org.xml.sax.*;
import java.util.*;

public interface XMLPeerFactory {
    public Object getXMLPeer(String tag, List children,
			     AttributeList attrs);
}
