

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
 * Created on 06.05.2004
 * @author Andreas Prlic
 *
 */

/** takes care of the communication with a DAS Structure service
 */

package org.biojava.bio.program.das.dasstructure ;

import org.biojava.bio.structure.* ;
import org.biojava.bio.structure.io.* ;


import java.net.HttpURLConnection;
import java.io.*;
import java.net.URL;
import java.util.Properties ;

import org.xml.sax.helpers.*;
import org.xml.sax.*;
import javax.xml.parsers.*;

public class DASStructureCall {
    
    String serverurl;


    public DASStructureCall() {
	serverurl = "" ;
    }
    
    
    public DASStructureCall(String url){
	serverurl = url;
    }
    
    /** set url of structure service */
    public void   setServerurl(String s) { serverurl=s;     }

    /** get url of structure service */
    public String getServerurl(        ) { return serverurl;}
    

    /** connect to a DAS structure service and retreive 3D data.
	return a biojava Structure object
    */
    
    public Structure getStructure(String pdb_code)
	throws IOException
    {
	/* now connect to DAS server */
	String connstr = serverurl + pdb_code ;
	URL dasUrl = null ;
	try {
	    dasUrl = new URL(connstr);
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
	//System.out.println("connecting to "+connstr);
	InputStream inStream = connectDASServer(dasUrl);
	

	Structure structure = null;
	try{
	    structure = parseDASResponse(inStream) ;
	} catch (Exception e) {
	    e.printStackTrace() ;
	}
	return structure;
	
    }

    /** connect to DAS server and return result as an InputStream */
    
    private InputStream connectDASServer(URL url) 
	throws IOException
    {
	InputStream inStream = null ;
				
	
	    HttpURLConnection huc = null;
	    //huc = (HttpURLConnection) dasUrl.openConnection();
	    
	    //huc = proxyUrl.openConnection();
	    
	    //System.out.println("opening "+url);
	    huc = (HttpURLConnection) url.openConnection();
	    
	    
	    //System.out.println(huc.getResponseMessage());
	    String contentEncoding = huc.getContentEncoding();
	    //System.out.println("encoding: " + contentEncoding);
	    //System.out.println("code:" + huc.getResponseCode());
	    //System.out.println("message:" + huc.getResponseMessage());
	    inStream = huc.getInputStream();
	    

	return inStream;
	
    }

    /** parse the Response of a DAS Structure service and return a
     * biojava Structure */
    private StructureImpl parseDASResponse(InputStream inStream) 
	throws IOException, SAXException
    {
	
	
	System.setProperty("org.xml.sax.driver", 
			   "org.apache.crimson.parser.XMLReaderImpl");


	SAXParserFactory spfactory =
	    SAXParserFactory.newInstance();
	
	spfactory.setValidating(false);
	/*
	  try {
	  spfactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);
	  } catch (ParserConfigurationException e){
	  e.printStackTrace();
	  }
	*/
	SAXParser saxParser = null ;
	try{
	    saxParser =
		spfactory.newSAXParser();
	} catch (ParserConfigurationException e) {
	    e.printStackTrace();
	}
	
	

	XMLReader xmlreader = saxParser.getXMLReader();
	//http://apache.org/xml/features/nonvalidating/load-external-dtd

	//XMLReader xmlreader = XMLReaderFactory.createXMLReader();	
	//xmlreader.setValidating(false);

	// try to deactivate validation
	
	
	try {
	    //System.out.println("deactivating validation");
	    xmlreader.setFeature("http://xml.org/sax/features/validation", false);
	    //System.out.println("done...");
	} catch (SAXException e) {
	    System.err.println("Cannot deactivate validation."); 
	}
       	
	try {
	    xmlreader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);
	} catch (SAXNotRecognizedException e){
	    e.printStackTrace();
	    //System.out.println("continuing ...");
	}
	
	//System.out.println("DASStructureCall setting DASStructureXMLResponseParser");

	DASStructureXMLResponseParser cont_handle = new DASStructureXMLResponseParser() ;
	xmlreader.setContentHandler(cont_handle);
	xmlreader.setErrorHandler(new org.xml.sax.helpers.DefaultHandler());
	InputSource insource = new InputSource() ;
	insource.setByteStream(inStream);

	//System.out.println("DASStructureCall parse XML response ...");
	xmlreader.parse(insource);

	/*
	BufferedReader buf = new BufferedReader (new InputStreamReader (inStream));
	String line = buf.readLine ();
	while (line != null) {
	    line = buf.readLine ();
	    System.out.println(line);
	}
	*/
	return cont_handle.get_structure();
	
    }
    

}

