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
 * Created on 13.5.2004
 * @author Andreas Prlic
 *
 */

package org.biojava.bio.program.das.dasalignment;

import org.biojava.bio.program.ssbind.AnnotationFactory;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;

import java.util.ArrayList ;
import java.util.HashMap ;

/** a class to Parse the XML response of a DAS Alignment service 
 * returns an Alignment object
 */
public class DASAlignmentXMLResponseParser  extends DefaultHandler{
    ArrayList alignments ;
    Alignment alignment      ;
    HashMap current_object   ;
    String  current_position ;
    ArrayList current_block  ;
    HashMap  current_segment ;

    public DASAlignmentXMLResponseParser() {
	super() ;
	System.out.println("init DASAlignmentXMLResponseParser");
	alignment = new Alignment() ;
	current_position = "start";
	alignments = new ArrayList() ;
    }

    public ArrayList getAlignments() {

	return alignments;
    }

    public Alignment getAlignment(int position) {
	Alignment ra = (Alignment) alignments.get(position) ; 
	return ra ;
    }

    public void startElement (String uri, String name, String qName, Attributes atts){
	//System.out.println("startElement " + qName) ;
	if (qName.equals("OBJECT")     ) OBJECThandler     (atts);
	if (qName.equals("DESCRIPTION")) DESCRIPTIONhandler(atts);
	if (qName.equals("SEQUENCE")   ) SEQUENCEhandler   (atts);
	if (qName.equals("SCORE")      ) SCOREhandler      (atts);
	if (qName.equals("BLOCK")      ) BLOCKhandler      (atts);
	if (qName.equals("SEGMENT")    ) SEGMENThandler    (atts);
	if (qName.equals("CIGAR")      ) CIGARhandler      (atts);
	if (qName.equals("GEO3D")      ) GEO3Dhandler      (atts);
	
	
	    
    }

    public void endElement (String uri, String name, String qName){
	//System.out.println("endElement >" + qName + "< >" + name + "<") ;
	if (qName.equals("OBJECT")) {
	    try {
		alignment.addObject(AnnotationFactory.makeAnnotation(current_object));
	    } catch ( DASException  e) {
		e.printStackTrace() ;
	    }
	    current_object = new HashMap() ;
	}	
	if (qName.equals("SEGMENT")) {
	    current_block.add(current_segment);
	    current_segment = new HashMap() ;
	    
	}
	if (qName.equals("BLOCK")) {
	    //try {
		// alignment.addBlock(current_block);
	    //} catch ( DASException  e) {
		//e.printStackTrace() ;
	    //}
	    current_block = new ArrayList() ;
	}
	if (qName.equals("ALIGNMENT")){
	    alignments.add(alignment) ;

	    alignment = new Alignment() ;

	    
	}
	    
    }

    private void SEGMENThandler(Attributes atts) {
	current_position = "segment";
	current_segment  = new HashMap() ;
	
	String id     = atts.getValue("id");
	String start  = atts.getValue("start");
	String end    = atts.getValue("end");
	// orientation not implemented yet ...
	current_segment.put("id",id);
	current_segment.put("start",start);
	current_segment.put("end",end) ;
	
	
    }
    
    private void CIGARhandler(Attributes atts) {
	current_position = "cigar" ;
    }
    private void BLOCKhandler(Attributes atts) {
	current_block = new ArrayList() ;
    }

    

    private void DESCRIPTIONhandler(Attributes atts){
	current_position = "description";	
    }
    private void SEQUENCEhandler(Attributes atts){
	current_position = "sequence";	
    }

    private void SCOREhandler(Attributes atts) {
	System.out.println("SCOREhandler not implemented,yet...");

    }

    private void GEO3Dhandler(Attributes atts) {
	System.out.println("GEO3D not implemented,yet...");

    }

    private void OBJECThandler(Attributes atts) {
	// found a new object
	String id               = atts.getValue("id");
	String coordinateSystem = atts.getValue("coordinateSystem");
	String version          = atts.getValue("version");
	String type             = atts.getValue("type");
	
	HashMap object = new HashMap() ;
	object.put("id",id);
	object.put("coordinateSystem",coordinateSystem);
	object.put("version",version) ;
	object.put("type",type);
	
	current_object = object ;

       
    }
    

   public void startDocument() {
	//System.out.println("start document");
	
    }
	
    public void endDocument ()	{
	
    }

    public void characters (char ch[], int start, int length){
	String txt = "";
	for (int i = start; i < start + length; i++) {
	    txt += ch[i] ;
	}
	if (current_position == "description") {
	    current_object.put("description",txt);
	} else if ( current_position == "sequence") {
	    current_object.put("sequence",txt);
	} else if ( current_position == "cigar"){
	    current_segment.put("cigarstring",txt);
	}

    }

}
