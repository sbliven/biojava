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


package org.acedb.staticobj;

import org.acedb.*;
import java.net.*;
import java.util.*;

/**
 * @author Thomas Down
 */

public class StaticAceObject extends StaticAceNode implements AceObject {
    private AceType type;
    private Database db;

    public StaticAceObject(String name,
			   AceType type,
			   Map contents,
			   Database db) 
    {
	super(name, contents, null);
	this.type = type;
	this.db = db;
    }

    public AceType getType() {
	return type;
    }

    public URL toURL() {
	String parURL = db.toURL().toString();
	String myName = parURL + (parURL.endsWith("/") ? "" : "/") + 
	    type.getName() + "/" + getName();
	try {
	    return new URL(myName);
	} catch (MalformedURLException ex) {
	    throw new AceError(ex, "Unable to generate URL for " + myName);
	}
    }
}
