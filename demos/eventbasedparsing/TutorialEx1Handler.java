package eventbasedparsing;

import java.util.*;

import java.io.PrintStream;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.biojava.bio.program.sax.BlastLikeSAXParser;

/**
 * An XML ContentHandler that populates a list of
 * Hit Ids from the summary section of Blast-like
 * output.. Hit Ids are typically either database
 * accession
 * ids or  from the summary section of Blast-like output.
 * <p>
 * Copyright &copy; 2001 Cambridge Antibody Technology.
 * All Rights Reserved.
 * <p>
 * Primary author -<ul>
 * <li>Simon Brocklehurst (CAT)
 * </ul>
 *
 * @author Cambridge Antibody Technology (CAT)
 * @version 1.0
 *
 */
class TutorialEx1Handler extends DefaultHandler {

    /*
     * Handler-specific member data
     */
    private ArrayList oDatabaseIdList;

    /*
     * Generic XML ContentHandler member data
     */
    private String oName;
    private StringBuffer oPCDataBuffer = new StringBuffer();
    private Stack oNameStack = new Stack();
    private Stack oTmpStack = new Stack();
    private int iCount;


    /**
     * Creates a new <code>TutorialEx1Handler</code> instance.
     *
     * @param poDatabaseIdList an <code>ArrayList</code> representation
     * for DatabaseIds
     */
    public TutorialEx1Handler(ArrayList poDatabaseIdList)  {
	super();
	oDatabaseIdList = poDatabaseIdList;
    }

    /**
     * Overides parent class method.
     */
    public void startElement(String poURI, String poLocalName, String poQName,
			     Attributes poAtts) {

	oNameStack.push(poLocalName);
	oPCDataBuffer.setLength(0);

	if ( (oNameStack.peek().toString().equals("HitId")) &&
             (this.findInStack("Summary") != -1) ){
	    oDatabaseIdList.add(poAtts.getValue("id"));
	}

    }



    /**
     * Overides parent class method.
     *
     */
    public void endElement(String poURI, String poLocalName, String poQName) {

	//Deal with the PC Data - trim leading and trailing whitespace

	String oFinalBuffer = oPCDataBuffer.toString().trim();
	//System.out.println(oFinalBuffer);


	//Clear PC Data Buffer as soon as dealt with
	oPCDataBuffer.setLength(0);
	

	oNameStack.pop();
    }

    /**
     * Overrides parent class method.
     *
     */
    public void characters(char[] ch, int start, int length) {
	oPCDataBuffer.append(new String(ch,start,length));

    }


    /**
     * Finds the distance of a query string from the top
     * of the stack of element names.
     *
     * @param poQuery    A string description of an element name
     * @return int   An integer representing distance. NB
     * it returns -1 if the query can't be found. Yes, it
     * should throw an exception really!
     */
    public int findInStack(String poQuery) {
	iCount = 0;
	while ( ! (oNameStack.peek().toString().equals(poQuery)) ) {
	    //here if we haven't found the object we're looking for
	    //pop the object off the stack, and push onto tmpStack
	    if (oNameStack.empty()) {
		//could not find query string in stack
		iCount = -1;
		break;
	    }
	    iCount++;
	    oTmpStack.push(oNameStack.pop());
	    //System.out.println("Popped: " + oTmpStack.peek().toString());
	    
	    //Final check if stack empty, because loop check will fail
	    //if it is.
	    if (oNameStack.empty()) {
		//could not find query string in stack
		iCount = -1;
		break;
	    }
	}
 
	// Now restore name stack to original state
	while ( ! (oTmpStack.empty()) ) {
	    oNameStack.push(oTmpStack.pop());
	}
				
	return iCount;
    }


}
