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
 * Created on 10.05.2004
 * @author Andreas Prlic
 *
 */
package org.biojava.bio.program.das.dasalignment;

import org.biojava.bio.program.ssbind.AnnotationFactory;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import java.util.* ;

/**
 * parse MSD-XMLmapping of uniprot - PDB file
 * retunr Aligmnent object
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class MSD_Mapping_ContentHandler extends DefaultHandler {


    ArrayList alignments      ;
    ArrayList current_block   ;
    ArrayList current_objects ;
    String    current_chain   ;
    Alignment alignment  ;
		

    /**
     * 
     */
    public MSD_Mapping_ContentHandler() {
	super();
	// TODO Auto-generated constructor stub
	
	//number_chains = 0 ;

	//segment_length = 0 ;
	alignments = new ArrayList() ;
	alignment  = new Alignment() ;
	current_block = new ArrayList();
	current_objects = new ArrayList();
	current_chain = "" ;

    }
	
    public void startDocument(){
	//System.out.println("starting document");
    }
    

    public void endDocument ()
    {
	// do not forget to add the last chain to container ..

	//System.out.println("End document");
       
    }
	
	

    public void endElement (String uri, String name, String qName)
    {
	/*
	  if ("".equals (uri))
	  System.out.println("End element: " + qName);
	  else
	  System.out.println("End element:   {" + uri + "}" + name);
			*/
	//System.out.println("in endElement "+name+" "+qName+" "+uri);
	if (name.equals("residue")) {
	    handle_residue_end(uri,name,qName);

	} else if (name.equals("entity")) {
	    // add objects to alignment
	    for ( int i =0 ; i<current_objects.size(); i++ ) {
		HashMap object = (HashMap) current_objects.get(i);
		try {
		    alignment.addObject(AnnotationFactory.makeAnnotation(object));
		} catch ( DASException e) {
		    e.printStackTrace() ;
		}
	    }

	    alignments.add(alignment);
	    
	    // initialize everything ...
	    alignment     = new Alignment() ;
	    current_block = new ArrayList();
	    current_objects = new ArrayList();
	    current_chain = "" ;
	    

	}
    }


    public void characters (char ch[], int start, int length){
	/*
	  System.out.print("Characters:    \"");
	  for (int i = start; i < start + length; i++) {
			switch (ch[i]) {
			case '\\':
				System.out.print("\\\\");
				break;
			case '"':
				System.out.print("\\\"");
				break;
			case '\n':
				System.out.print("\\n");
				break;
			case '\r':
				System.out.print("\\r");
				break;
			case '\t':
				System.out.print("\\t");
				break;
			default:
				System.out.print(ch[i]);
				break;
			}
		}
		System.out.print("\"\n");
		*/
	}
	
    public Alignment[] get_alignments(){
	return (Alignment[])alignments.toArray(new Alignment[alignments.size()]) ;
    }
    
    public void startElement (String uri, String name,
			      String qName, Attributes atts)
    {
	/*
	  if ("".equals (uri))
	  System.out.println("Start element: " + qName);
	  else
	  System.out.println("Start element: {" + uri + "}" + name);
	*/
	//System.out.println("in startElemnt "+name+" "+qName+" "+uri);
	if ( name.equals("segment") ) 
	    handle_segment(uri,name,qName,atts);
	else if ( name.equals("residue")) 			
	    handle_residue(uri,name,qName,atts);
	else if (name.equals("crossRef"))
	    handle_crossRef(uri,name,qName,atts);
	else if (name.equals("entity"))
	    handle_entity(uri,name,qName,atts);
	
	
	//System.out.println(atts);
    }
	
	
	
	
    
	
    
  
    /* 
     * we arrived a t a new chain
     * line looks like this:
     * <segment sys="MSD" id="1a4a_B_1_129" start="1" end="129" >
     */
    private void handle_segment(String uri, String name, String qName, Attributes atts){
	String sys 	= atts.getValue("sys") ;
	String id 	= atts.getValue("id") ;
	String start 	= atts.getValue("start") ;
	String end 	= atts.getValue("end");
	
	//System.out.println("new segment:"+id+" "+start+":"+end);
	
	// not much used right now, might be used for checks...
	//segment_length = Integer.valueOf(end).intValue();
		
	}
    
    
    /* handle a line of type
     * <residue numSys="122" monSys="LYS"></residue>
     */
    private void handle_residue(String uri, String name, String qName, Attributes atts){
	// everything that is within one residue corresponds to one block ...
	current_block = new ArrayList();
	
		
    }
	
    private void handle_residue_end(String uri, String name, String qName){
	//try{
	   
	    // alignment.addBlock(current_block);
	    
	//} catch ( DASException e) {
	//    e.printStackTrace() ;
	//}
		
    }

	
    /* handle a cross reference line
     * <crossRef sys="PDB" id="1a4a" num="122" mon="LYS"></crossRef>
     * and
     * <crossRef sys="UniProt" id="P00280" num="142" mon="K"></crossRef>
     * 
     */
    private void handle_crossRef(String uri, String name, String qName, Attributes atts){
	//System.out.println("in crossref" );
		
	String sys = atts.getValue("sys") ;
	String id  = atts.getValue("id") ;
	String num = atts.getValue("num");
	String mon = atts.getValue("mon");

	// WHAT IS THE OBJECT VERSION ???????
	// only available for uniprot in listdbmap section ... aargh.
	// no ersion for PDB
	// well this parser is just temporary, set version to "20040219"
	String version = "20040219" ;
	String coordinateSystem ="" ;
	if (sys.equals("PDB")) {
	    coordinateSystem="urn:proteins:pdb";
	    id = id + "." + current_chain;
	} else {
	    coordinateSystem="urn:proteins:uniprot";
	}

	    

	HashMap object = getObject(id);
	if (object == null) {
	    object = new HashMap() ;
	    object.put("id",id);
	    object.put("type",sys);
	    object.put("version",version);
	    object.put("coordinateSystem",coordinateSystem);
	    current_objects.add(object);
	}

	//System.out.println("xref sys:" + sys);
	HashMap block = new HashMap();

	block.put("start",num);
	block.put("end",num);
	block.put("id",""+id);
	current_block.add(block);

    }

    private HashMap getObject(String id){
	int i ;
	for (i =0 ; i< current_objects.size();i++){
	    HashMap object = (HashMap)current_objects.get(i);
	    if ( object.get("id").equals(id)){
		return object ;
	    }
	}

	return null;
	    
    }

    /*
     *  deal with line <entity type="polymer"  id="A">
     */
    private void handle_entity(String uri, String name, String qName, Attributes atts){

	//String sys 	= atts.getValue("sys") ;
	String id 		= atts.getValue("id") ;
	//String start 	=atts.getValue("start") ;
	//String end 	= atts.getValue("end");
	
	System.out.println("new entity (chain):"+id+" " + name + " " + qName );

	// AARGH the Chain identifier should be stored together with the PDB code in crossref....
	current_chain = id ;
	
	
    }
    
}
