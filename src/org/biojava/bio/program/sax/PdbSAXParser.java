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

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.AttributesImpl;

/**
 * A SAX2 parser for dealing with native PDB files.  That is,
 * this class allows native PDB format files to be processed 
 * as if they were in PdbXML format, but without an interconversion
 * step.  That is, events are generated that call methods
 * on an XML document handler.
 * <p>
 * Currently memory usage whilst parsing is at the level of a single model.
 * <p>
 * <b>Note this code is experimental, in development and subject 
 * to change without notice - please do not use unless you're in
 * contact with the primary author preferably via the biojava-l
 * mailing list.
 * </b>
 * <p>
 *
 * Copyright &copy; 2000,2001 Cambridge Antibody Technology.
 * All Rights Reserved.
 * <p>
 * Primary author -<ul>
 * <li>Simon Brocklehurst (CAT)
 * </ul>
 * Other authors  -<ul>
 * <li>Tim Dilks          (CAT)
 * <li>Colin Hardman      (CAT)
 * <li>Stuart Johnston    (CAT)
 *</ul>
 *
 *
 * @author Cambridge Antibody Technology (CAT)
 * @version 0.2
 *
 */
public class PdbSAXParser extends AbstractNativeAppSAXParser {


    private ArrayList        oRecordList = new ArrayList();
    private String           oRecord;
    private int              iPos;
    private int              iModelStart;
    private int              iModelStop;

    private AttributesImpl          oAtts     = new AttributesImpl();
    private ArrayList               oHeader   = new ArrayList();
    private QName                   oAttQName = new QName(this);     

    /**
     * Sets namespace prefix to "biojava"
     */
    public PdbSAXParser() {
	this.setNamespacePrefix("biojava");
    }

    /**
     * Describe 'parse' method here.
     *
     * @param nil	 -
     */
    public void parse(InputSource poSource ) 
	throws IOException,SAXException {

	BufferedReader            oContents;
	String                    oLine = null;

	//Use method form superclass
	oContents = this.getContentStream(poSource);


	try {
	    // loop over file
	    oLine = oContents.readLine();
	    while (oLine != null) {
		
		// put line into ArrayList
		oRecordList.add(oLine);
		//System.out.println(oLine);
		oLine = oContents.readLine();
	    } // end while
	    
	    //-----------------------

	    //At this point, have the entire raw file in core memory.
	    //Now parse it and fire of relevant events
	    
	    //First preprocess file
	    
	    //Rule
	    //If there are no model records, then insert records
	    //for a single model.  MODEL record before first ATOM,
	    //ENDMDL and before, CONECT, MASTER, END
		
	    boolean tIsModel = false;
	    
	    for (int i = 0; i < oRecordList.size(); i++) {
		oRecord = (String)oRecordList.get(i);
		if (oRecord.startsWith("MODEL")) {
		    tIsModel = true;
		    break;
		}
	    }

	    boolean tFoundFirstAtom = false;
	    if (!tIsModel) {
		//System.out.println("No MODEL records");
		for (int i = 0; i < oRecordList.size(); i++) {
		    oRecord = (String)oRecordList.get(i);
		    
		    if ((oRecord.startsWith("ATOM  ")) &&
			(!tFoundFirstAtom))             {
			tFoundFirstAtom = true;

			//System.out.println("Found first atom>"+i+"<");

			oRecordList.add(i,"MODEL        1");
			break;
		    }
		}

		boolean tFoundLastAtom = false;
		for (int i = oRecordList.size() - 1; i > 0; i--) {
		    oRecord = (String)oRecordList.get(i);
			
		    if ( ((oRecord.startsWith("ATOM  ")) ||
			  (oRecord.startsWith("HETATM")) ||
			  (oRecord.startsWith("TER")  )) &&
			 (!tFoundLastAtom))                 {
			
			tFoundLastAtom = true;

			//System.out.println("Found last atom>"+i+"<");

			oRecordList.add(i+1,"ENDMDL");
			break;
		    }
		}

	    } //end if tIsModel == false


		//End preprocess file

		//At this point, the PDB records should be
		//in a suitable state for parsing...


		oAtts.clear();
		this.startElement(new QName(this,
			    this.prefix("MacromolecularStructure")),
			  (Attributes)oAtts);


		//Start at beginning of RecordList and progress
		//through to end using global iPos variable
		//to keep track of position

		iPos = 0;

		//keep track of start pos of model -
		//need this for multiple passes through
		//to get protein, dna, solvent etc.

		iModelStart = 0;  
		iModelStop = 0;  
		String oModelId;

		while (iPos < oRecordList.size()) {
		    //System.out.println("Line: "+iPos);
		    oRecord = (String)oRecordList.get(iPos);
		    if (oRecord.startsWith("MODEL")) {
			iModelStart = iPos;
			oModelId = oRecord.substring(10,14).trim();

			oAtts.clear();
			oAttQName.setQName("modelId");
			oAtts.addAttribute(oAttQName.getURI(),
					   oAttQName.getLocalName(),
					   oAttQName.getQName(),
					   "CDATA",oModelId);

			this.startElement(new QName(this,this.prefix("Model")),
					  (Attributes)oAtts);

		    }

		    if (oRecord.startsWith("ENDMDL")) {
			//keep position of the end of this model
			iModelStop = iPos;

			//at this point have start and end positions
			//of current model
			
			//do multiple passes for each type of molecule

			//parse protein for this model...

			oAtts.clear();
			this.startElement(new QName(this,this.prefix("Protein")),
					  (Attributes)oAtts);


			oAtts.clear();
			this.startElement(new QName(this,
					    this.prefix("ProteinChainList")),
					  (Attributes)oAtts);


			this.parseProtein(iModelStart,iModelStop);
			//close final Atom Residue and ProteinChain

			this.endElement(new QName(this,this.prefix("Atom")));
			this.endElement(new QName(this,this.prefix("Residue")));
			this.endElement(new QName(this,
						  this.prefix("ProteinChain")));
			this.endElement(new QName(this,
						  this.prefix("ProteinChainList")));


			//todo parse solvent, dna etc.
			
			//having parsed all content, end model
			this.endElement(new QName(this,this.prefix("Model")));

		    }
		    iPos++;
		}

		this.endElement(new QName(this,
					  this.prefix("MacromolecularStructure")));

		//System.out.println("Finished parsing");


            } catch (java.io.IOException x) {
                System.out.println(x.getMessage());
                System.out.println("File read interupted");
            } // end try/catch

    }
    //==================================================================
    //private methods
    //==================================================================

    /**
     * Parse protein content of pdb output
     *
     * @param nil	 -
     */
    private void parseProtein(int piStart, int piStop) 
    throws SAXException {

	String oChainId;
	
	String oAtomId;
	String oAtomType;

	String  oResidueId;
	String  oResidueType;

	String oX;
	String oY;
	String oZ;
	String oOccupancy;
	String oBFactor;


	String oCurrentChainId;
	String oCurrentResidueId;


	boolean tFirstChain = true;
	boolean tFirstResidue = true;

	oCurrentChainId="XXX";    //set as an impossible initial value
	oCurrentResidueId="A*ZZ**"; //set as an impossible initial value

	for (int i = piStart; i < piStop; i++) {
	    oRecord = (String)oRecordList.get(i);
	    //System.out.println("parsing protein>" + oRecord);

	    if (oRecord.startsWith("ATOM  ")) {
		//System.out.println(">"+oRecord.substring(17,20)+"<");

		oAtomId = oRecord.substring(6,11).trim();
		oAtomType = oRecord.substring(12,16).trim();

		oResidueType = oRecord.substring(17,20).trim();

		//go straight to next atom if this one not protein
		if (!checkIfProtein(oResidueType)) continue;
	    
		//assign varables from ATOM record
		oChainId = oRecord.substring(21,23).trim();
		oResidueId = oRecord.substring(23,27).trim();

		oX = oRecord.substring(30,38).trim();
		oY = oRecord.substring(38,46).trim();
		oZ = oRecord.substring(47,53).trim();
		


		//check new residue event

		if (!oResidueId.equals(oCurrentResidueId)) {
		    if (!tFirstResidue) {
			this.endElement(new QName(this,
					  this.prefix("Residue")));

		    }
		    if (!oChainId.equals(oCurrentChainId)) {
			if (!tFirstChain) {

			this.endElement(new QName(this,
					  this.prefix("ProteinChain")));

			}
			//check new chain event
			oAtts.clear();
			oAttQName.setQName("chainId");
			oAtts.addAttribute(oAttQName.getURI(),
					   oAttQName.getLocalName(),
					   oAttQName.getQName(),
					   "CDATA",oChainId);

			this.startElement(new QName(this,
      				          this.prefix("ProteinChain")),
					  (Attributes)oAtts);


			tFirstChain = false; //a bit ugly to set all the time.
			oCurrentChainId = oChainId;
		    }

		    oAtts.clear();
		    oAttQName.setQName("residueId");
		    oAtts.addAttribute(oAttQName.getURI(),
				       oAttQName.getLocalName(),
				       oAttQName.getQName(),
				       "CDATA",oResidueId);
		    oAttQName.setQName("residueType");
		    oAtts.addAttribute(oAttQName.getURI(),
				       oAttQName.getLocalName(),
				       oAttQName.getQName(),
				       "CDATA",oResidueType);

		    this.startElement(new QName(this,this.prefix("Residue")),
					  (Attributes)oAtts);

		    tFirstResidue = false; //a bit ugly to set all the time.
		    oCurrentResidueId = oResidueId;
		}

		//finally fire new atom-related events

		oAtts.clear();
		oAttQName.setQName("atomId");
		oAtts.addAttribute(oAttQName.getURI(),
				   oAttQName.getLocalName(),
				   oAttQName.getQName(),
				   "CDATA",oAtomId);
		oAttQName.setQName("atomType");
		oAtts.addAttribute(oAttQName.getURI(),
				   oAttQName.getLocalName(),
				   oAttQName.getQName(),
				   "CDATA",oAtomType);
		
		this.startElement(new QName(this,this.prefix("Atom")),
					  (Attributes)oAtts);


		oAtts.clear();
		oAttQName.setQName("x");
		oAtts.addAttribute(oAttQName.getURI(),
				   oAttQName.getLocalName(),
				   oAttQName.getQName(),
				   "CDATA",oX);
		oAttQName.setQName("y");
		oAtts.addAttribute(oAttQName.getURI(),
				   oAttQName.getLocalName(),
				   oAttQName.getQName(),
				   "CDATA",oY);
		
		oAttQName.setQName("z");
		oAtts.addAttribute(oAttQName.getURI(),
				   oAttQName.getLocalName(),
				   oAttQName.getQName(),
				   "CDATA",oZ);
		
		this.startElement(new QName(this,this.prefix("Coordinates")),
					  (Attributes)oAtts);


		this.endElement(new QName(this,this.prefix("Coordinates")));
		this.endElement(new QName(this,this.prefix("Atom")));

	    }
	}

    }


    /**
     * Parses an ATOM record.  This does the following:
     *
     *    o Identifies which chain the atom belongs to
     *    o Creates a new Atom object and adds it to the chain
     *
     * @param poRecord	 -
     */
    private void parseAtomRecord(String poRecord) {

	String oChainId;

	String oAtomId;
	String oAtomType;

	String  oResidueId;
	String  oResidueType;

	double dX;
	double dY;
	double dZ;
	double dOccupancy;
	double dBFactor;

	System.out.println(poRecord);

	//parse atom line
	oChainId = poRecord.substring(21,23);
	oResidueId = poRecord.substring(23,27);

	//trim white-space from all parsed fields

	oChainId     = oChainId.trim();
	oResidueId   = oResidueId.trim();

	System.out.println("ChainId>" + oChainId + "<");
	//System.out.println("ResidueId>" + oResidueId + "<");

    }

    /**
     * Checks to see if a given residue type is part of a protein.
     * NB at the moment, this doesn't work - just returns true.
     * FIX THIS
     *
     * @param poResType	 Three-letter residue code
     * @return boolean	 Returns true if a protein, false if not.
     */
    private boolean checkIfProtein(String poResType) {

	return true;
    }


}
