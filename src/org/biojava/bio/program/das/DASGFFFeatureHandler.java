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

import java.util.*;
import java.net.*;
import java.io.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;

import org.biojava.utils.*;
import org.biojava.utils.stax.*;
import org.xml.sax.*;

/**
 * StAX handler which parses DASGFF features.
 *
 * @author Thomas Down
 * @since 1.2
 */

public class DASGFFFeatureHandler extends StAXContentHandlerBase {
    private SeqIOListener featureListener;

    private String f_id = null;
    private String f_label = null;
    private String type = "unknown";
    private String method = "unknown";
    private int start = -1, end = -1;
    private String orientation="0";
    private String phase="-";
    private Location loc = null;
    private boolean isReferenceFeature = false;
    private String category = null;
    private String refName = null;
    private int refStart = -1;
    private int refStop = -1;

    public DASGFFFeatureHandler(SeqIOListener siol) {
	this.featureListener = siol;
    }

    public void startElement(String nsURI,
			     String localName,
			     String qName,
			     Attributes attrs,
			     DelegationManager dm)
	 throws SAXException
    {
	if ("FEATURE".equals(localName)) {
	    f_id = attrs.getValue("id");
	    f_label = attrs.getValue("label");
	} else if ("TYPE".equals(localName)) {
	    String reference = attrs.getValue("reference");
	    if ("yes".equals(reference)) {
		isReferenceFeature = true;
	    }
	    category = attrs.getValue("category");
	    dm.delegate(new StringElementHandlerBase() {
		    protected void setStringValue(String s) {
			type = s.trim();
		    }
		} );
	} else if ("METHOD".equals(localName)) {
	    dm.delegate(new StringElementHandlerBase() {
		    protected void setStringValue(String s) {
			method = s.trim();
		    }
		} );
	} else if ("START".equals(localName)) {
	    dm.delegate(new IntElementHandlerBase() {
		    protected void setIntValue(int i) {
			start = i;
		    }
		} );
	} else if ("END".equals(localName)) {
	    dm.delegate(new IntElementHandlerBase() {
		    protected void setIntValue(int i) {
			end = i;
		    }
		} );
	} else if ("ORIENTATION".equals(localName)) {
	    dm.delegate(new StringElementHandlerBase() {
		    protected void setStringValue(String s) {
			orientation = s.trim();
		    }
		} );
	} else if ("GROUP".equals(localName)) {
	    dm.delegate(new StAXContentHandlerBase());
	} else {
	    // Unknown element -- let's be flexible for now...

	    dm.delegate(new StAXContentHandlerBase());
	}
    }

    public void endTree()
        throws SAXException
    {
	Feature.Template temp;
	if (orientation.equals("+") || orientation.equals("-")) {
	    StrandedFeature.Template stemp = new StrandedFeature.Template();
	    stemp.strand = orientation.equals("+") ? StrandedFeature.POSITIVE :
	                                             StrandedFeature.NEGATIVE;
	    temp = stemp;
	} else {
	    temp = new Feature.Template();
	}

	temp.type = type;
	temp.source = method;
	if (loc == null) {
	    temp.location = new RangeLocation(start, end);
	} else {
	    temp.location = loc;
	}

	
	if (temp.annotation == null) {
	    temp.annotation = new SmallAnnotation();
	}

	try {
	    if (f_id != null && f_id.length() > 0) {
		temp.annotation.setProperty(DASSequence.PROPERTY_FEATUREID, f_id);
	    }
	    if (f_label != null && f_label.length() > 0) {
		temp.annotation.setProperty(DASSequence.PROPERTY_FEATURELABEL, f_label);
	    }
	} catch (ChangeVetoException ex) {
	    throw new BioError(ex);
	}

	try {
	    featureListener.startFeature(temp);
	    featureListener.endFeature();
	} catch (Exception ex) {
	    throw new BioRuntimeException(ex);
	}
    }
}
