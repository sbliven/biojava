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

import org.biojava.bio.program.xff.XFFFeatureSetHandler;
import org.biojava.bio.program.xff.XFFPartHandlerFactory;
import org.biojava.bio.seq.io.ParseException;
import org.biojava.utils.stax.DelegationManager;
import org.biojava.utils.stax.StAXContentHandler;
import org.biojava.utils.stax.StAXContentHandlerBase;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * StAX handler for xff:prop detail elements.
 *
 * @author Thomas Down
 */

class DASLinkHandler extends StAXContentHandlerBase {
    public static final XFFPartHandlerFactory LINKDETAIL_HANDLER_FACTORY = new XFFPartHandlerFactory() {
	    public StAXContentHandler getPartHandler(XFFFeatureSetHandler xffenv) {
		return new DASLinkHandler(xffenv);
	    }
	} ;

    private XFFFeatureSetHandler xffenv;
    private int level = 0;

    public DASLinkHandler(XFFFeatureSetHandler xffenv) {
	super();
	this.xffenv = xffenv;
    }

    public void startElement(String nsURI,
			     String localName,
			     String qName,
			     Attributes attrs,
			     DelegationManager dm)
	 throws SAXException
    {
	++level;
	if (level == 2 && localName.equals("link")) {
	    String url = attrs.getValue("http://www.w3.org/1999/xlink", "href");
	    String role = attrs.getValue("http://www.w3.org/1999/xlink", "role");
	    if (url != null) {
		try {
		    xffenv.getFeatureListener().addFeatureProperty(DASSequence.PROPERTY_LINKS,
								   new DASLink(url, role));
		} catch (ParseException ex) {
		    ex.printStackTrace();
		    throw new SAXException(ex);
		}
	    }
	}
    }

    public void endElement(String nsURI,
			   String localName,
			   String qName,
			   StAXContentHandler handler)
    {
	--level;
    }
}
