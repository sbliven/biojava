/*
 * @(#)XMLPeerBuilder.java      0.1 99/10/18
 *
 * (c) Thomas Down
 */

package org.biojava.utils.xml;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.util.*;

/**
 * SAX DocumentHandler which uses an XMLPeerFactory to construct
 * Java objects reflecting an XML document.  The XMLPeerBuilder
 * system takes a depth-first approach to constructing the Object
 * tree.  This means that, before attempting to construct an
 * Object to represent a particular element, it first constructs
 * Objects for all child elelments.
 *
 * <P>
 * Currently, Text nodes are automatically converted to Java strings
 * and treated as normal children.  Treatment of Text may be
 * configurable in a future release.
 * </P>
 *
 * @author Thomas Down
 */

public class XMLPeerBuilder implements DocumentHandler {
    private static AttributeList emptyAttributes = new AttributeListImpl();

    private XMLPeerFactory peerFactory;
    private boolean isComplete = true;
    private List stack;
    private StackEntry stackTop = null;
    private Object topLevel = null;

    /**
     * Construct a new XMLPeerBuilder, using the specified XMLPeerFactory
     *
     */

    public XMLPeerBuilder(XMLPeerFactory f) {
	peerFactory = f;
	stack = new LinkedList();
    }

    /**
     * Once the XMLPeerBuilder has been used, return an Object
     * which represents the whole document.
     *
     * @return an Object reflecting the document, or <code>null</code>
     *         if none is available.
     */

    public Object getTopLevelObject() {
	if (isComplete)
	    return topLevel;
	return null;
    }

    public void characters(char[] ch, int start, int len) {
	String child = new String(ch, start, len);
	stacktop.objs.add(child);
    }

    public void ignorableWhitespace(char[] ch, int start, int len) {
    }

    public void startDocument() {
	isComplete = false;
	topLevel = new LinkedList();
    }

    public void setDocumentLocator(Locator l) {
    }

    public void endDocument() {
	isComplete = true;
    }

    public void processingInstruction(String target, String data) {
    }

    public void startElement(String name, AttributeList al) {
	stack.add(0, stackTop);
	stackTop = new StackEntry();
	if (al.getLength() == 0)
	    stackTop.al = emptyAttributes;
	else
	    stackTop.al = new AttributeListImpl(al);
        stackTop.objs = null;
    }

    public void endElement(String name) {
	Object o = peerFactory.getXMLPeer(name,
		stackTop.objs != null ? stackTop.objs : Collections.EMPTY_LIST,
		stackTop.al);
	stackTop = (StackEntry) stack.remove(0);
	if (o != null) {
	    if (stackTop == null) {
		topLevel = o;
	    } else {
		if (stackTop.objs == null)
		    stackTop.objs = new LinkedList();
		stackTop.objs.add(o);
	    }   
	}
    }

    class StackEntry {
	List objs;
	AttributeList al;
    }
}
