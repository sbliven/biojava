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
 * Created on 11.05.2004
 * @author Andreas Prlic
 *
 */


package org.biojava.bio.program.das.dasalignment ;

//import org.xml.sax.helpers.DefaultHandler;
//import org.xml.sax.Attributes;

import java.io.*;

import javax.xml.parsers.*;
import org.xml.sax.SAXException;
//import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.XMLReader;




/** parse a MSD mapping file from PDB to uniprot and return a DAS- Alignment object */

public class MSDMappingReader {
    

    public MSDMappingReader() {	
    }
    
    
    
    public Alignment[] parseXMLFile(String filename) 
	throws ParserConfigurationException, SAXException,IOException
    {
    


	SAXParserFactory spf = SAXParserFactory.newInstance();
	spf.setNamespaceAware(true);
	XMLReader r = spf.newSAXParser().getXMLReader();

	MSD_Mapping_ContentHandler cont_handle = new MSD_Mapping_ContentHandler();

	r.setContentHandler(cont_handle);
	r.setErrorHandler(new org.xml.sax.helpers.DefaultHandler());
			
	//System.out.println("parsing ");
	r.parse(filename);
	//System.out.println("done parsing ");

	Alignment[] alignments = cont_handle.get_alignments() ;
	System.out.println(alignments.length);
	for (int i=0;i<alignments.length;i++){
	    Alignment ali = alignments[i];
	    System.out.println(i + " " + ali);
	}
	//System.out.println(pdb_container);
	
	return alignments;
    }
}


