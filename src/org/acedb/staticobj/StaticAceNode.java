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

import java.net.*;
import java.util.*;
import org.acedb.*;

/**
 * @author Thomas Down
 */

public class StaticAceNode implements AceNode {
    private Map subSets;
    private AceNode parent;
    private String name;

    public StaticAceNode(String name, Map contents, AceNode parent) {
	this.name = name;
	this.subSets = contents;
	this.parent = parent;
    }

    public StaticAceNode(String name, AceNode parent) {
	this.name = name;
	this.parent = parent;
	this.subSets = null;
    }

    public String getName() {
	return name;
    }

    public AceType getType() {
	return AceType.TAG;
    }

    public URL toURL() {
	String parURL = parent.toURL().toString();
	String myName = parURL + (parURL.endsWith("/") ? "" : "/") + 
	                URLEncoder.encode(name); 
	try {
	    return new URL(myName);
	} catch (MalformedURLException ex) {
	    throw new AceError(ex, "Unable to generate URL for " + myName);
	}
    }

    public int size() {
	if (subSets != null)
	    return subSets.size();
	return 0;
    }

    public Iterator nameIterator() {
	if (subSets != null)
	    return subSets.keySet().iterator();
	return Collections.EMPTY_SET.iterator();
    }

    public Iterator iterator() {
	if (subSets != null)
	    return subSets.values().iterator();
	return Collections.EMPTY_SET.iterator();
    }

    public boolean contains(String name) {
	if (subSets != null)
	    return subSets.containsKey(name);
	return false;
    }

    public AceSet retrieve(String name) throws AceException {
      AceSet result = null;
      if (subSets != null)
  	    result = (AceSet) subSets.get(name);
      if(result == null)
        throw new AceException("Could not find child with the name '" +
                               name + "'" + " in object " + getName()
        );
      return result;
    }

    public void addNode(AceNode nd) {
	if (subSets == null)
	    subSets = new HashMap();
	subSets.put(nd.getName(), nd);
    }
}
