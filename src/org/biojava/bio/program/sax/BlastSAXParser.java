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
package org.biojava.bio.program.sax;

import java.util.*;
import java.io.*;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * A SAX-like parser for dealing with native NCBI-Blast,Wu-Blastoutput,
 * and HMMER output (a single dataset).  That is this class allows
 * native BLAST-like format files to be processed  as if they were in
 * an XML format i.e. it sends messages to a user-written XML Handler.
 *
 * This class has package-level visibility, and is used
 * by generic BlastLikeSAXParser objects.
 *
 * Some functionality is delegated to Part objects within the class
 * whose implementations are selected on-the-fly according
 * the the program/version that produced the output.
 *  SummaryLineHelperIF
 *
 * NB Support for HMMER is currently only partial and likely to change
 * without notice as more functionality is added.
 *
 * Primary author -
 *                 Simon Brocklehurst (CAT)
 * Other authors  -
 *                 Tim Dilks          (CAT)
 *                 Colin Hardman      (CAT)
 *                 Stuart Johnston    (CAT)
 *
 * Copyright 2000 Cambridge Antibody Technology Group plc.
 * All Rights Reserved.
 *
 * This code released to the biojava project, May 2000
 * under the LGPL license.
 *
 * @author Cambridge Antibody Technology Group plc
 * @version 0.1
 *
 * @see BlastLikeSAXParser
 */
final class BlastSAXParser extends AbstractNativeAppSAXParser {

    private BufferedReader       oContents;
    private AttributesImpl       oAtts              = new AttributesImpl();
    private QName                oAttQName          = new QName(this);     
    private ArrayList            oBuffer            = new ArrayList();
    private char[]               aoChars;
    private char[]               aoLineSeparator;
    private String[]             aoKeys;
    private String[]             aoArrayType        = new String[1];
    private HashMap              oMap               = new HashMap();
    private int                  iVer;

    private BlastLikeVersionSupport      oVersion;
    private HitSectionSAXParser          oHits;
    private SummaryLineHelperIF          oSummaryLineHelper;



    private static final int STARTUP             = 0;
    private static final int IN_TRAILER          = 1;
    private static final int AT_END              = 2;
    private static final int IN_HEADER           = 3;
    private static final int IN_SUMMARY          = 4;
    private static final int FINISHED_HITS       = 5;

    private boolean          tDoneSummary        = false;

    /**
     * Creates a new <code>BlastSAXParser</code> instance.
     *
     * @param poVersion a <code>BlastLikeVersionSupport</code> value.
     *
     * @exception SAXException if an error occurs
     */
    BlastSAXParser(BlastLikeVersionSupport poVersion, 
		   String poNamespacePrefix) throws SAXException {

	oVersion = poVersion;
	this.setNamespacePrefix(poNamespacePrefix);

        oHits = new HitSectionSAXParser(oVersion,this.getNamespacePrefix());

	this.changeState(STARTUP);
	aoLineSeparator = System.getProperty("line.separator").toCharArray();

	//Beginnings of using a Builder type pattern to create
	//parser from part objects. Only for done
	//Summary sections at present according to program type
	//at present. Significant benefit would be
	//for detail, allowing choice of part object optimised
	//for speed of processing etc.
	this.choosePartImplementations();
    }
    /**
     * Describe 'parse' method here.
     *
     */
    public String parse(BufferedReader poContents, String poLine)
	throws SAXException {

	String         oLine = null;

	oContents = poContents;

	//First deal with first line which must be the start of 
	//a new Blast output
	//For a brand new collection, check for the start of a 
	//new BlastDataSet

	    //look for characteristic of start of dataset
	    if (oVersion.isStartOfDataSet(poLine)) {
		//just figure out whether it's a new DataSet
		//or not. i The onNewBlastDatSet method
		//takes care of the rest...
		this.onNewBlastDataSet(poLine);

	    } else {
		//should throw excpetion here
		//message should be unexpected poLine parameter
		return poLine;
		
	    }
	    //now parse stream...
	    try {

		oLine = oContents.readLine();
		while ( (oLine != null) &&
			(!checkNewBlastLikeDataSet(oLine)) ) {

		    //System.out.println(oLine);
		    
		    //interpret line and send messages accordingly
		    this.interpret(oLine);
		    oLine = oContents.readLine();
		    
		} // end while
	    } catch (java.io.IOException x) {
		System.out.println(x.getMessage());
		System.out.println("File read interupted");
	    } // end try/catch

	    //Now close open elements...
	    if (iState == IN_TRAILER) {
		this.emitRawOutput(oBuffer);
		this.endElement(new QName(this,this.prefix("Trailer")));
		this.changeState(AT_END);
	    }

	    this.endElement(new QName(this,this.prefix("BlastLikeDataSet")));

	    return oLine;
    }
    /**
     * Deal with line according to state parser is in.
     *
     * @param poLine	 A line of Blast output
     */
    private void interpret(String poLine) throws SAXException {


	if (iState == IN_HEADER) {
	    //accumulate header information

	    //things that can end header section
	    //start of summmary section
	    //start of detail section

	    if ( (poLine.startsWith(
          	  "Sequences producing significant alignments")) ||
		 (poLine.startsWith(
                  "Sequences producing High-scoring Segment Pairs")) ||
		 (poLine.startsWith(
                  "--------   ")) ) {

		this.emitRawOutput(oBuffer);

		this.endElement(new QName(this,this.prefix("Header")));
		
		//change state
		this.changeState(IN_SUMMARY);
		oAtts.clear();
		this.startElement(new QName(this,this.prefix("Summary")),
				      (Attributes)oAtts);

		//eat a blank line if there is one...
		//		    this.interpret(oLine);
		//read next line

		try {
		    poLine = oContents.readLine();

		} catch (java.io.IOException x) {
		    System.out.println(x.getMessage());
		    System.out.println("File read interupted");
		} // end try/catch

		if (poLine.trim().equals("")) {
		    //System.out.println("BLANK LINE");
		} else {
		    //recursively interpret it...
		    this.interpret(poLine);
		}

		return;
	    } //end check start of summary

	    //Currently doesn't handle output that starts
	    //with a detail section i.e. no summary.
	    //This is a BUG/FEATURE.

	    oBuffer.add(poLine);
	} //end if inHeader state

	//Deal with summary state
	if (iState == IN_SUMMARY) {
	    //check to see if end of summary
	    //has been reached...

	    //(signal is a blank line for NCBIBlast and Wu-Blast,
	    //(signal is either a blank line or 
	    //[no more scores below E threshold] for HMMER.

	    if ( (poLine.trim().equals("")) ||
		 (poLine.trim().startsWith("[no more scores")) ) {
		//Can't change state, because we still want to
		//check for start of detail... 

		//Forgive non-standard white-space
		//between end of summary and start of detail
		if (!tDoneSummary) {
		    tDoneSummary = true;
		    this.endElement(new QName(this,this.prefix("Summary")));
		} 
		return; //return before attempting to parse Summary Line

	    }

	    //HMMER-specific
	    if (poLine.startsWith("Parsed for domains:")) {
		//signifies domain summary info
		this.hmmerDomainSummaryReached(poLine);
	    }



	    if (poLine.startsWith(">")) {
		//signifies start of detail section
		this.hitsSectionReached(poLine);

		return;
	    }
	    //need to check that we've end of summary
	    //'cos could be spurious data between end of
	    //summary and start of detail e.g. multi-line WARNINGs
	    //at end of Summary section in Wu-Blast.
	    if (!tDoneSummary) {
		this.parseSummaryLine(poLine);
	    }
	    return;
	}
	//State set to this when parsing of Hits finished
	if (iState == FINISHED_HITS) {

	    //check end of detail section

		this.endElement(new QName(this,this.prefix("Detail")));
		oAtts.clear();
		this.startElement(new QName(this,this.prefix("Trailer")),
				  (Attributes)oAtts);

		//change state to Trailer and initialse Buffer
		this.changeState(IN_TRAILER);
		oBuffer.clear();
		return;


	} //end if finishedHists

	if (iState == IN_TRAILER) {
	    oBuffer.add(poLine);
	}
    }
    /**
     * This method is called when a line indicating that
     * that a new BlastLikeDataSet has been reached.
     *
     * NB This class deals NCBI-BLAST WU-BLAST and HMMER:
     *
     *  o flavours of NCBI Blast (e.g. blastn, blastp etc)
     *  o flavours of WU Blast (e.g. blastn, blastp etc)
     *
     * When this method is called, the line will look something line:
     *
     * BLASTN 2.0.11 [Jan-20-2000] for NCBI Blast
     *
     * The above would be parsed to program ncbi-blastn, and version number
     * 2.0.11
     *
     * @param poLine	 -
     */
    private void onNewBlastDataSet(String poLine) throws SAXException {

	if (!oVersion.isSupported()) {
		throw (new SAXException(
		"Program " + 
		oVersion.getProgramString() + " Version " +
		oVersion.getVersionString() +
	" is not supported by the biojava blast-like parsing framework"));

	}

	oAtts.clear();
	oAttQName.setQName("program");
	oAtts.addAttribute(oAttQName.getURI(),
			   oAttQName.getLocalName(),
			   oAttQName.getQName(),
			   "CDATA",oVersion.getProgramString());

	oAttQName.setQName("version");
	oAtts.addAttribute(oAttQName.getURI(),
			   oAttQName.getLocalName(),
			   oAttQName.getQName(),
			   "CDATA",oVersion.getVersionString());

	this.startElement(new QName(this,this.prefix("BlastLikeDataSet")),
			  (Attributes)oAtts);

	//change state to reflect the fact we're in the Header
	iState = IN_HEADER;
	oBuffer.clear();
	
	oAtts.clear();
	this.startElement(new QName(this,this.prefix("Header")),
			  (Attributes)oAtts);
    }
    /**
     * Describe constructor here.
     *
     * @param oArrayList	 -
     */
    private void emitRawOutput(ArrayList poList) throws SAXException {

	oAtts.clear();
	oAttQName.setQName("xml:space");
	oAtts.addAttribute(oAttQName.getURI(),
			   oAttQName.getLocalName(),
			   oAttQName.getQName(),
			   "NMTOKEN","preserve");
	this.startElement(new QName (this,this.prefix("RawOutput")),
			  (Attributes)oAtts);

	//Cycle through ArrayList and send character array data to
	//XML ContentHandler.

    int iTmpListSize = poList.size();
	for (int i = 0; i < iTmpListSize; i++) {
	    //System.out.println("RAW:" + (String)poList.get(i));
	    aoChars = ((String)poList.get(i)).toCharArray();
	    this.characters(aoLineSeparator,0,1);
	    this.characters(aoChars,0,aoChars.length);
	}

	this.endElement(new QName(this,this.prefix("RawOutput")));
    }
    /**
     * Parses a summary line.  Actualy parsing functionality
     * is delegated to static method of a reusable Helper Class.
     *
     * For NCBI Blast, a summary line looks something like:
     *
     * U00431 Mus musculus HMG-1 mRNA, complete cds.        353  7e-95
     *
     * UO0431 is typically a database accession code
     * Mus musculs.. is a description of the hit (this is optional)
     *
     * 353 is a bit score
     *
     * 7e-95 is an E Value
     *
     */
    private void parseSummaryLine(String poLine) throws SAXException {

	//Also remember in header add query attribute and database type?

	//Should split this out into different implementations
	//according to program and version

	oSummaryLineHelper.parse(poLine,oMap,oVersion);

	aoKeys = (String[])(oMap.keySet().toArray(aoArrayType));

	oAtts.clear();

	//output contents of Map as either elements or
	//attribute lists

	//two passes first get HitsSummary element started
	for (int i = 0; i < aoKeys.length; i++) {
	    
	    if ( (aoKeys[i].equals("hitId")) || 
		 (aoKeys[i].equals("hitDescription")) ) {
		//do nothing
	    } else {

	oAttQName.setQName(aoKeys[i]);
	oAtts.addAttribute(oAttQName.getURI(),
			   oAttQName.getLocalName(),
			   oAttQName.getQName(),
			   "CDATA",(String)oMap.get(aoKeys[i]));
	    }
	}

	this.startElement(new QName(this,this.prefix("HitSummary")),
			  (Attributes)oAtts);

	for (int i = 0; i < aoKeys.length; i++) {
	    
	    if (aoKeys[i].equals("hitId")) {
		oAtts.clear();
		oAttQName.setQName("id");
		oAtts.addAttribute(oAttQName.getURI(),
				   oAttQName.getLocalName(),
				   oAttQName.getQName(),
				   "CDATA",(String)oMap.get(aoKeys[i]));

		oAttQName.setQName("metaData");
		oAtts.addAttribute(oAttQName.getURI(),
				   oAttQName.getLocalName(),
				   oAttQName.getQName(),
				   "CDATA","none");
		this.startElement(new QName(this,this.prefix("HitId")),
				  (Attributes)oAtts);
		this.endElement(new QName(this,this.prefix("HitId")));

	    } else if (aoKeys[i].equals("hitDescription")) {
		oAtts.clear();
		this.startElement(new QName(this,this.prefix("HitDescription")),
				  (Attributes)oAtts);
		aoChars = ((String)oMap.get(aoKeys[i])).toCharArray();
		this.characters(aoChars,0,aoChars.length);
		this.endElement(new QName(this,this.prefix("HitDescription")));

	    }
	    //System.out.print(aoKeys[i] + ": ");
	    //System.out.println(oMap.get(aoKeys[i]));
	}
	this.endElement(new QName(this,this.prefix("HitSummary")));
    }
    /**
     * From the specified line, hand over
     * parsing of stream to a helperclass
     *
     * @param poLine	 -
     * @exception SAXException thrown if
     * @exception  thrown if
     */
    private void hitsSectionReached(String poLine)
	throws SAXException {

	//Parse Contents stream up to end of Hits
	oHits.setContentHandler(oHandler);

	//this returns when end of hits section reached...

	oAtts.clear();
	this.startElement(new QName(this,this.prefix("Detail")),
			  (Attributes)oAtts);

	int iProgram = oVersion.getProgram();

	if ( (iProgram == BlastLikeVersionSupport.NCBI_BLASTN) ||
	     (iProgram == BlastLikeVersionSupport.NCBI_BLASTX) ||
	     (iProgram == BlastLikeVersionSupport.NCBI_BLASTP) ||
	     (iProgram == BlastLikeVersionSupport.NCBI_TBLASTN) ||
	     (iProgram == BlastLikeVersionSupport.NCBI_TBLASTX) ) {

	    oHits.parse(oContents,poLine,"Database:");

	}

	if ( (iProgram == BlastLikeVersionSupport.WU_BLASTN) ||
	     (iProgram == BlastLikeVersionSupport.WU_BLASTX) ||
	     (iProgram == BlastLikeVersionSupport.WU_BLASTP) ||
	     (iProgram == BlastLikeVersionSupport.WU_TBLASTN) ||
	     (iProgram == BlastLikeVersionSupport.WU_TBLASTX) ) {

	    oHits.parse(oContents,poLine,"Parameters:");

	}


	this.changeState(FINISHED_HITS);

    }

    /**
     * Method to be implemented.
     *
     * @param poLine a <code>String</code> value
     * @exception SAXException if an error occurs
     */
    private void hmmerDomainSummaryReached(String poLine)
	throws SAXException {

	//To be implemented - parse Contents stream up to end of 
	//domain summary via delegation (analagous to hits parsing
	

	oAtts.clear();
	this.startElement(new QName(this,this.prefix("DomainSummary")),
			  (Attributes)oAtts);

	//... insert functionality.

	this.endElement(new QName(this,this.prefix("Detail")));

    }

    /**
     * Checks to see if a line of Blast like output 
     * represents the start of a new BlastLike data set.
     * Current supports:
     *   o ncbi-blast all variants
     *   o wu-blast all variants
     *
     * @param poLine	 A String representation of the line
     * @return boolean	 true if it is a new dataset, false if not.
     */
    private boolean checkNewBlastLikeDataSet(String poLine) {


	if ( (poLine.startsWith("BLAST")) ||
	     (poLine.startsWith("TBLAST")) )
	    {
		return true;
	    } else {
		return false;
	    }

    }
    /**
     * Choose particular implementations of Part objects
     * that comprise the aggregate object according
     * to version/type of program
     *
     * @param nil	 -
     */
    private void choosePartImplementations() throws SAXException {

	iVer = oVersion.getProgram();

	if ( (iVer == BlastLikeVersionSupport.NCBI_BLASTN) ||
	     (iVer == BlastLikeVersionSupport.NCBI_BLASTX) ||
	     (iVer == BlastLikeVersionSupport.NCBI_BLASTP) ||
	     (iVer == BlastLikeVersionSupport.NCBI_TBLASTN) ||
	     (iVer == BlastLikeVersionSupport.NCBI_TBLASTX) ) {
	      oSummaryLineHelper = 
		  (SummaryLineHelperIF) new NcbiBlastSummaryLineHelper();
	      return;
	}

	if ( (iVer == BlastLikeVersionSupport.WU_BLASTN) ||
	     (iVer == BlastLikeVersionSupport.WU_BLASTX) ||
	     (iVer == BlastLikeVersionSupport.WU_BLASTP) ||
	     (iVer == BlastLikeVersionSupport.WU_TBLASTN) ||
	     (iVer == BlastLikeVersionSupport.WU_TBLASTX) ) {
	      oSummaryLineHelper = 
		  (SummaryLineHelperIF) new WuBlastSummaryLineHelper();
	      return;
	  }

	if (iVer == BlastLikeVersionSupport.HMMER) {
	      oSummaryLineHelper = 
		  (SummaryLineHelperIF) new HmmerSummaryLineHelper();
	      return;
	}


	//If get to here, no implementation available

	//Exception to help SAX parser writers track down
	//problems writing software to support the framework
	throw (new SAXException(
	"Could not choose a suitable implementation of the ".
	    concat("SummaryLineHelperIF for program ").
	    concat(oVersion.getProgramString()).
	    concat(" version ").
	    concat(oVersion.getVersionString())));

    }
}
