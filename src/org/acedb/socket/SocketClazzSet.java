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

import org.acedb.*;

/**
 * AceSet representing all the objects in a class
 */

class SocketClazzSet implements AceSet {
    private AceURL url;
    private AceSet parent;
    private AceSet _allMembers = null;

    public SocketClazzSet(AceURL url, AceSet parent) {
	this.url = url;
	this.parent = parent;
    }

    private AceSet allMembers() throws AceException {
	if (_allMembers == null)
	    _allMembers = filter("*");
	return _allMembers;
    }

    public int size() throws AceException {
	return allMembers().size();
    }

    public Iterator nameIterator() throws AceException {
	return allMembers().nameIterator();
    }

    public Iterator iterator() throws AceException {
	return allMembers().iterator();
    }

    public boolean contains(String name) throws AceException {
	return allMembers().contains(name);
    }

    public AceSet retrieve(String name) throws AceException {
	return Ace.fetch(url.relativeURL(name));
    }

    public AceSet filter(String rule) throws AceException {
	return Ace.fetch(url.relativeURL("?" + rule));
    }

    public AceSet getParent() {
	return parent;
    }

    public AceURL toURL() {
	return url;
    }
}
