
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
 * Created on 12.05.2004
 * @author Andreas Prlic
 *
 */


package org.biojava.bio.program.das.dasalignment ;


import java.io.*   ;
import java.util.* ;

/**
 * A DAS client that connects to a DAS aligmnent service and
 * returns a Biojava Alignment object.
 */


public class DASAlignmentClient {
    String query                 ;
    String serverurl             ;
    
    public DASAlignmentClient() {
	query = null;
	serverurl = "http://127.0.0.1:8080/dazzle/myalig/alignment?query=";
    }

    public DASAlignmentClient(String url) {
	serverurl = url ;
	query = null ;
    }

    public void setQuery(String q) {
	query = q ;
	
    }

    public String getQuery() {
	return query ;
    }

    public  ArrayList getAlignments(String que)
	throws IOException
    {
	query = que ;
	return getAlignments() ;
    }

    public ArrayList getAlignments()
	throws IOException
    {
	if (query == null) {
	    throw new IOException ("no query specified - call setQuery() first!");
	}
	
	DASAlignmentCall alicall = new DASAlignmentCall(serverurl);
	ArrayList ali = alicall.getAlignments(query);
	return ali ;
	
    }

}
