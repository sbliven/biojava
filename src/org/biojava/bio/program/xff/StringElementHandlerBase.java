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

package org.biojava.bio.program.xff;

import java.util.*;

import org.biojava.utils.*;
import org.biojava.utils.stax.*;
import org.xml.sax.*;

public abstract class StringElementHandlerBase extends StAXContentHandlerBase {
    private int level = 0;
    private StringBuffer data = new StringBuffer();


    public void startElement(String nsURI,
			     String localName,
			     String qName,
			     Attributes attrs,
			     DelegationManager dm)
	 throws SAXException
    {
	level++;
	if (level > 1) {
	    throw new SAXException("Found child element when expecting character data");
	}
    }

    public void endElement(String nsURI,
			   String localName,
			   String qName)
	throws SAXException
    {
	level--;
	if (level == 0) {
	    setStringValue(data.toString());
	}
    }

    public void characters(char[] ch, int start, int end) 
        throws SAXException
    {
	data.append(ch, start, end);
    }

    /**
     * Override this method to do something useful with the
     * string we collect.  Maybe we should do this by delegation
     * rather than extension.
     */

    protected abstract void setStringValue(String s) throws SAXException;
}
