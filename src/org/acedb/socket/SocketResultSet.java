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


package org.acedb.socket;

import java.util.*;
import java.io.*;
import java.net.*;

import org.acedb.*;

/**
 * @author Thomas Down
 */

public class SocketResultSet implements AceSet {
    private SocketDatabase myDB;
    private List nameList;
    private AceSet parent;
    private AceURL url;

    public SocketResultSet(SocketDatabase db, AceSet parent, List names, AceURL url) {
	myDB = db;
	nameList = names;
	this.parent = parent;
	this.url = url;
    }

    public int size() {
	return nameList.size();
    }

    public Iterator nameIterator() {
	return new Iterator() {
	    Iterator fullNames = nameList.iterator();
	    
	    public boolean hasNext() {
		return fullNames.hasNext();
	    }

	    public Object next() {
		return fullNames.next();
	    }

	    public void remove() {
		throw new UnsupportedOperationException();
	    }
	} ;
    }

    public Iterator iterator() {
	return new Iterator() {
	    Iterator fullNames = nameList.iterator();
	    
	    public boolean hasNext() {
		return fullNames.hasNext();
	    }

	    public Object next() {
        try {
		return retrieve((String) fullNames.next());
        } catch (AceException ae) {
          throw new NoSuchElementException(ae.getMessage());
        }
	    }

	    public void remove() {
		throw new UnsupportedOperationException();
	    }
	} ;
    }

    public boolean contains(String name) {
	return nameList.contains(name);
    }

    public AceSet retrieve(String name) throws AceException {
      AceURL r = AceUtils.rootURL(url);
      String clazz = url.getFile();
      return Ace.fetch(r.relativeURL(clazz + '/' + name));
    }

    public AceSet filter(String pattern) {
	throw new UnsupportedOperationException("ImplementMe");
    }

    public AceSet getParent() {
	return parent;
    }

    public AceURL toURL() {
	return url;
    }

    public String getName() {
	return null;
    }
}
