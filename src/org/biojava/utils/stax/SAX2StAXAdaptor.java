package org.biojava.utils.stax;

import org.xml.sax.*;
import java.util.*;

/**
 * Lightweight adaptor which translates SAX content events into
 * StAX form, and provides delegation services.
 *
 * @author Thomas Down
 */

public class SAX2StAXAdaptor implements ContentHandler {
    private List stack;
    private HandlerBinding current;
        
    {
	stack = new ArrayList();
    }

    /**
     * Construct a new SAX Content handler which wraps a StAX
     * handler.
     */

    public SAX2StAXAdaptor(StAXContentHandler rootHandler) {
	current = new HandlerBinding(rootHandler);
	stack.add(current);
    }

    public void startDocument() throws SAXException {
	current.handler.startTree();
    }

    public void endDocument() throws SAXException {
	current.handler.endTree();
    }

    public void characters(char[] ch,
			   int start,
			   int end)
	throws SAXException
    {
	current.handler.characters(ch, start, end);
    }

    public void ignorableWhitespace(char[] ch,
				    int start,
				    int end)
	throws SAXException
    {
	current.handler.ignorableWhitespace(ch, start, end);
    }

    public void startPrefixMapping(String prefix, String uri)
        throws SAXException
    {
	current.handler.startPrefixMapping(prefix, uri);
    }

    public void endPrefixMapping(String prefix)
        throws SAXException
    {
	current.handler.endPrefixMapping(prefix);
    }

    public void processingInstruction(String target, String data)
        throws SAXException
    {
	current.handler.processingInstruction(target, data);
    }

    public void setDocumentLocator(Locator locator) {
	current.handler.setDocumentLocator(locator);
    }

    public void skippedEntity(String name)
        throws SAXException
    {
	current.handler.skippedEntity(name);
    }

    public void startElement(final String nsURI,
			     final String localName,
			     final String qName,
			     final Attributes attrs)
	throws SAXException
    {
	current.handler.startElement(nsURI,
				     localName,
				     qName,
				     attrs,
				     new DelegationManager() {

	     public void delegate(StAXContentHandler handler) 
	         throws SAXException
	     {
		 current = new HandlerBinding(handler);
		 stack.add(current);
		 current.handler.startTree();
		 current.handler.startElement(nsURI,
					      localName,
					      qName,
					      attrs,
					      this);
	     }

					 } );
	current.count++;
    }

    public void endElement(String nsURI, String localName, String qName)
        throws SAXException
    {
	current.handler.endElement(nsURI, localName, qName);
	current.count--;
	while (current.count == 0 && stack.size() > 1) {
	    current.handler.endTree();
	    stack.remove(stack.size() - 1);
	    current = (HandlerBinding) stack.get(stack.size() - 1);
	    current.handler.endElement(nsURI, localName, qName);
	}
    }

    private class HandlerBinding {
	public StAXContentHandler handler;
	public int count;

	private HandlerBinding(StAXContentHandler handler) {
	    this.handler = handler;
	    this.count = 0;
	}
    }
}
