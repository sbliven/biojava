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
 * Created on 15.05.2004
 * @author Andreas Prlic
 *
 */


/** takes care of the communication with a DAS Alignment service
 */

package org.biojava.bio.program.das.dasalignment ;

import java.net.HttpURLConnection;
import java.io.*;
import java.net.URL;
import java.util.Properties ;
import java.util.zip.GZIPInputStream;



import org.xml.sax.helpers.*;
import org.xml.sax.*;
import javax.xml.parsers.*;

import java.util.*;

public class DASAlignmentCall {
    
    String serverurl;


    public DASAlignmentCall() {
	serverurl = "" ;
    }
    
    
    public DASAlignmentCall(String url){
	serverurl = url;
    }
    
    /** set url of structure service */
    public void   setServerurl(String s) { serverurl = s;     }

    /** get url of structure service */
    public String getServerurl(        ) { return serverurl;}
    

    /** connect to a DAS structure service and retreive 3D data.
	return a biojava Structure object
    */
    
    public ArrayList getAlignments(String query)
	throws IOException
    {
	/* now connect to DAS server */
	String connstr = serverurl + query ;
	URL dasUrl = null ;
	try {
	    dasUrl = new URL(connstr);
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
	System.out.println("connecting to "+connstr);
	InputStream inStream = connectDASServer(dasUrl);
	

	ArrayList ali = null;
	try{
	    ali = parseDASResponse(inStream) ;
	} catch (Exception e) {
	    e.printStackTrace() ;
	}
	return ali;
	
    }


    /** connect to DAS server and return result as an InputStream */    
    private InputStream connectDASServer(URL url) 
	throws IOException
    {
	InputStream inStream = null ;
				
	
	    HttpURLConnection huc = null;
	    huc = (HttpURLConnection) url.openConnection();	    
	    // should make communication much faster!
	    huc.setRequestProperty("Accept-Encoding", "gzip");
	
	    System.out.println("response code " +huc.getResponseCode());
	    String contentEncoding = huc.getContentEncoding();
	    inStream = huc.getInputStream(); 
	    if (contentEncoding != null) {
                if (contentEncoding.indexOf("gzip") != -1) {
		    // we have gzip encoding
		    inStream = new GZIPInputStream(inStream);
		    System.out.println("using gzip encoding!");
                }
            }

	return inStream;
	
    }

    /** parse the Response of a DAS ALignment service and return a
     * biojava Alignment object */
    private ArrayList parseDASResponse(InputStream inStream) 
	throws IOException, SAXException
    {
	
	
	
	SAXParserFactory spfactory =
	    SAXParserFactory.newInstance();
	
	spfactory.setValidating(true);

	SAXParser saxParser = null ;

	try{
	    saxParser =
		spfactory.newSAXParser();
	} catch (ParserConfigurationException e) {
	    e.printStackTrace();
	}
	
	XMLReader xmlreader = saxParser.getXMLReader();

	
	try {
	    xmlreader.setFeature("http://xml.org/sax/features/validation", true);
	} catch (SAXException e) {
	    System.err.println("Cannot activate validation."); 
	}
       	
	try {
	    xmlreader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",true);
	} catch (SAXNotRecognizedException e){
	    e.printStackTrace();
	}
	
	//System.out.println("DASStructureCall setting DASStructureXMLResponseParser");

	DASAlignmentXMLResponseParser cont_handle = new DASAlignmentXMLResponseParser() ;
	xmlreader.setContentHandler(cont_handle);
	xmlreader.setErrorHandler(new org.xml.sax.helpers.DefaultHandler());
	InputSource insource = new InputSource() ;
	insource.setByteStream(inStream);

	//System.out.println("DASAlignmentCall parse XML response ...");
	xmlreader.parse(insource);
	//System.out.println("DASAlignmentCall parse XML response done.");

	return cont_handle.getAlignments();
	
    }
    
}
