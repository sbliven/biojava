package org.biojava.utils.stax;

import org.xml.sax.*;

/**
 * Interface which exposes delegation services offered by a StAX
 * event source.
 *
 * @author Thomas Down
 */

public interface DelegationManager {
    public void delegate(StAXContentHandler handler)
        throws SAXException;
}
