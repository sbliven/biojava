package org.biojava.utils.stax;

import org.xml.sax.*;

/**
 * Simple implementation of the <code>StAXContentHandler</code>
 * interface, with empty implementations for all the methods.
 *
 * <p>
 * This class is provided as a base for content handlers where
 * the implementor does not wish to provide all the methods.
 * </p>
 *
 * @author Thomas Down
 */

public class StAXContentHandlerBase implements StAXContentHandler {
    public void startTree()
	throws SAXException
    {
    }

    public void endTree()
	throws SAXException
    {
    }

    public void characters(char[] ch,
			   int start,
			   int end)
	throws SAXException
    {
    }

    public void ignorableWhitespace(char[] ch,
				    int start,
				    int end)
	throws SAXException
    {
    }

    public void startPrefixMapping(String prefix, String uri)
        throws SAXException
    {
    }

    public void endPrefixMapping(String prefix)
        throws SAXException
    {
    }

    public void processingInstruction(String target, String data)
        throws SAXException
    {
    }

    public void setDocumentLocator(Locator locator)
    {
    }

    public void skippedEntity(String name)
        throws SAXException
    {
    }

    public void startElement(String nsURI,
			     String localName,
			     String qName,
			     Attributes attrs,
			     DelegationManager dm)
	throws SAXException
    {
    }

    public void endElement(String nsURI,
			   String localName,
			   String qName,
			   StAXContentHandler delegate)
        throws SAXException
    {
    }
}
