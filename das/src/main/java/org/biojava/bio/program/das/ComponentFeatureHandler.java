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

import org.biojava.bio.program.xff.LocationHandlerBase;
import org.biojava.bio.program.xff.StrandedFeatureHandler;
import org.biojava.bio.program.xff.XFFFeatureSetHandler;
import org.biojava.bio.program.xff.XFFPartHandlerFactory;
import org.biojava.bio.seq.ComponentFeature;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.io.ParseException;
import org.biojava.bio.symbol.Location;
import org.biojava.utils.ChangeVetoException;
import org.biojava.utils.stax.DelegationManager;
import org.biojava.utils.stax.StAXContentHandler;
import org.biojava.utils.stax.StringElementHandlerBase;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * StAX handler for XFF componentFeature type.
 *
 * @author Thomas Down
 * @since 1.2
 */


class ComponentFeatureHandler extends StrandedFeatureHandler {
    boolean inFeature = false;
    
    // workaround for compiler/vm bug
    protected void setFeatureProperty(Object key, Object value) 
        throws ChangeVetoException, ParseException
    {
      super.setFeatureProperty(key, value);
    }
    
    public static final XFFPartHandlerFactory COMPONENTFEATURE_HANDLER_FACTORY = new XFFPartHandlerFactory() {
	    public StAXContentHandler getPartHandler(XFFFeatureSetHandler xffenv) {
		return new ComponentFeatureHandler(xffenv);
	    }
	} ;

    public ComponentFeatureHandler(XFFFeatureSetHandler xffenv) {
	super(xffenv);
    }

    protected Feature.Template createFeatureTemplate() {
	return new ComponentFeature.Template();
    }

    protected ComponentFeature.Template getComponentFeatureTemplate() {
	return (ComponentFeature.Template) getFeatureTemplate();
    }

    public void startElement(String nsURI,
			     String localName,
			     String qName,
			     Attributes attrs,
			     DelegationManager dm)
	 throws SAXException
    {
	if (localName.equals("componentID")) {
	    dm.delegate(getComponentIDHandler());
	} else if (localName.equals("componentLocation")) {
	    dm.delegate(getComponentLocationHandler());
	}

	// Pass everything else on to the basic feature parser.

	super.startElement(nsURI, localName, qName, attrs, dm);
    }

    protected StAXContentHandler getComponentIDHandler() {
	return new StringElementHandlerBase() {
		protected void setStringValue(String s) 
		    throws SAXException
		{
		    try {
			setFeatureProperty("sequence.id", s);
		    } catch (Exception ex) {
			throw new SAXException("Couldn't set property", ex);
		    }
		}
	    } ;
    }

    protected StAXContentHandler getComponentLocationHandler() {
	return new LocationHandlerBase() {
		protected void setLocationValue(Location l) {
		    getComponentFeatureTemplate().componentLocation = l;
		}
	    } ;
    }    
}
