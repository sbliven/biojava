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
import org.biojava.utils.*;
import org.biojava.utils.stax.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.program.xff.*;

/**
 * Segment to request from DAS
 *
 * @since 1.2
 * @author Thomas Down
 */


class Segment {
    private String id;
    private int start;
    private int stop;
   
    public Segment(String id) {
	this.id = id;
	this.start = this.stop = -1;
    }
 
    public Segment(String id, int start, int stop) {
	this.id = id;
	this.start = start;
	this.stop = stop;
    }
    
    String getID() {
	return id;
    }
    
    int getStart() {
	return start;
    }
    
    int getStop() {
	return stop;
    }
    
    public boolean isBounded() {
	return (stop > 0);
    } 

    public int hashCode() {
	return id.hashCode() ^ start ^ stop;
    }
    
    public boolean equals(Object o) {
	if (! (o instanceof Segment)) {
	    return false;
	}

	Segment so = (Segment) o;
	if (!so.getID().equals(getID())) {
	    return false;
	} else {
	    if (isBounded()) {
		if (so.isBounded()) {
		    return so.getStart() == getStart() && so.getStop() == getStop();
		} else {
		    return false;
		}
	    } else {
		return (! so.isBounded());
	    }
	}
    }
}
