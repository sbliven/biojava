/*
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
 * Created on 26.04.2004
 * @author Andreas Prlic
 *
 */
package org.biojava.bio.structure.io;

import org.biojava.bio.structure.*  ;

import java.util.ArrayList          ;
import java.util.HashMap            ;
import java.text.DecimalFormat      ;

// xml writer ...
import org.biojava.utils.xml.*      ;
import java.io.IOException          ;

// for formatting on numbers
import java.util.Locale             ;
import java.text.NumberFormat       ;

/** Methods to convert a structure object into different file formats.
 * @author Andreas Prlic
 * @since 1.4
 */
public class FileConvert {
    Structure structure ;

    //static String DEFAULTCHAIN = "_" ;
    /**
     * Constructs a FileConvert object.
     *
     * @param struc  a Structure object
     */
    public FileConvert(Structure struc) {
	structure = struc ;
    }

    /** align a string to the right
     * length is the total length the new string should take, inlcuding spaces on the left
     * incredible that this tool is missing in java !!!
     */
    private String alignRight(String input, int length){

	String spaces = "                           " ;
	int n = input.length();
	int diff = length - n ;
	String s = "";

	if (n < length) {
	    s = spaces.substring(0,diff) + input;
	} else {
	    // does not work
	    return input ;
	}
	return s;
    }


    /** Convert a structure into a PDB file.
     * @return a String representing a PDB file.
     */
    public String toPDB() {
	

	StringBuffer str = new StringBuffer();
	int i = 0 ;
	
	// Locale should be english, e.g. in DE separator is "," -> PDB files have "." !
	DecimalFormat d3 = (DecimalFormat)NumberFormat.getInstance(java.util.Locale.UK);
	d3.setMaximumIntegerDigits(3);	
	d3.setMinimumFractionDigits(3);
	d3.setMaximumFractionDigits(3);

	DecimalFormat d2 = (DecimalFormat)NumberFormat.getInstance(java.util.Locale.UK);
	d2.setMaximumIntegerDigits(3);	
	d3.setMinimumFractionDigits(3);
	d3.setMaximumFractionDigits(3);


	// do for all models
	int nrModels = structure.nrModels() ;
	if ( structure.isNmr()) {
	    str.append("EXPDTA    NMR, "+ nrModels+" STRUCTURES\n") ;
	}
	for (int m = 0 ; m < nrModels ; m++) {
	    ArrayList model = (ArrayList)structure.getModel(m);
	    // todo support NMR structures ...
	    if ( structure.isNmr()) {
		str.append("MODEL      " + (m+1)+"\n");
	    }
	    // do for all chains
	    int nrChains = model.size();
	    for ( int c =0; c<nrChains;c++) {
		Chain  chain   = (Chain)model.get(c);
		String chainID = chain.getName();
		//if ( chainID.equals(DEFAULTCHAIN) ) chainID = " ";
		// do for all groups
		int nrGroups = chain.getLength();
		for ( int h=0; h<nrGroups;h++){
		    
		    Group g= chain.getGroup(h);
		    String type = g.getType() ;

		    String record = "" ;
		    if ( type.equals("hetatm") ) {
			record = "HETATM";
		    } else {
			record = "ATOM  ";
		    }
		   
		    
		    // format output ...
		    int groupsize  = g.size();
		    String resName = g.getPDBName();
		    String pdbcode = g.getPDBCode();
		    String line    = "" ;

		    // iteratate over all atoms ...
		    for ( int atompos = 0 ; atompos < groupsize; atompos++) {
			Atom a = null ;
			try {
			    a = g.getAtom(atompos);
			} catch ( StructureException e) {
			    System.err.println(e);
			    continue ;
			}

			int    seri       = a.getPDBserial()        ;
			String serial     = alignRight(""+seri,5)   ;
			String fullname   = a.getFullName()         ;

			Character  altLoc = a.getAltLoc()           ;
			String resseq = "" ;
			if ( hasInsertionCode(pdbcode) )
			    resseq     = alignRight(""+pdbcode,5);
			else 
			    resseq     = alignRight(""+pdbcode,4)+" ";
			String x          = alignRight(""+d3.format(a.getX()),8);
			String y          = alignRight(""+d3.format(a.getY()),8);
			String z          = alignRight(""+d3.format(a.getZ()),8);
			String occupancy  = alignRight(""+d2.format(a.getOccupancy()),6) ;
			String tempfactor = alignRight(""+d2.format(a.getTempFactor()),6);
		
			//System.out.println("fullname,zise:" + fullname + " " + fullname.length());

			line = record + serial + " " + fullname +altLoc 
			    + resName + " " + chainID + resseq 
			    + "   " + x+y+z 
			    + occupancy + tempfactor;
			str.append(line + "\n");
			//System.out.println(line);
		    }
		}
	    }
	    
	    if ( structure.isNmr()) {
		str.append("ENDMDL\n");
	    }
	}

	return str.toString() ;
    }



    /** test if pdbserial has an insertion code */
    private boolean hasInsertionCode(String pdbserial) {
	try {
	    int pos = Integer.parseInt(pdbserial) ;
	} catch (NumberFormatException e) {
	    return true ;
	}
	return false ;
    }


    /** convert a protein Structure to a DAS Structure XML response .
     * @param xw  a XMLWriter object
     * @throws IOException ...
     *
     */
    public void toDASStructure(XMLWriter xw)
	throws IOException 
    {

	/*xmlns="http://www.sanger.ac.uk/xml/das/2004/06/17/dasalignment.xsd" xmlns:align="http://www.sanger.ac.uk/xml/das/2004/06/17/alignment.xsd" xmlns:xsd="http://www.w3.org/2001/XMLSchema-instance" xsd:schemaLocation="http://www.sanger.ac.uk/xml/das/2004/06/17/dasalignment.xsd http://www.sanger.ac.uk/xml/das//2004/06/17/dasalignment.xsd"*/

	HashMap header = (HashMap) structure.getHeader();
	
	xw.openTag("object");
	xw.attribute("dbAccessionId",structure.getPDBCode());	    
	xw.attribute("intObjectId"  ,structure.getPDBCode());	    
	// missing modification date
	String modificationDate = (String)header.get("modDate") ;
	xw.attribute("objectVersion",modificationDate);	    
	xw.attribute("type","protein structure");	    
	xw.attribute("dbSource","PDB");	    
	xw.attribute("dbVersion","20040621");
	xw.attribute("dbCoordSys","PDBresnum");

	// do we need object details ???
	xw.closeTag("object");


	// do for all models
	for (int modelnr = 0;modelnr<structure.nrModels();modelnr++){
	    
	    // do for all chains:
	    for (int chainnr = 0;chainnr<structure.size(modelnr);chainnr++){
		Chain chain = (Chain)structure.getChain(modelnr,chainnr); 
		xw.openTag("chain");
		xw.attribute("id",chain.getName());
		xw.attribute("SwissprotId",chain.getSwissprotId() );
		if (structure.isNmr()){
		    xw.attribute("model",Integer.toString(modelnr+1));
		}
		
		//do for all groups:
		for (int groupnr =0;groupnr<chain.getLength();groupnr++){
		    Group gr = chain.getGroup(groupnr);
		    xw.openTag("group");
		    xw.attribute("name",gr.getPDBName());
		    xw.attribute("type",gr.getType());
		    xw.attribute("groupID",gr.getPDBCode());
		    
		    
		    // do for all atoms:
		    //Atom[] atoms  = gr.getAtoms();
		    ArrayList atoms = (ArrayList) gr.getAtoms();
		    for (int atomnr=0;atomnr<atoms.size();atomnr++){
			Atom atom = (Atom)atoms.get(atomnr);
			xw.openTag("atom");
			xw.attribute("atomID",Integer.toString(atom.getPDBserial()));
			xw.attribute("atomName",atom.getFullName());
			xw.attribute("x",Double.toString(atom.getX()));
			xw.attribute("y",Double.toString(atom.getY()));
			xw.attribute("z",Double.toString(atom.getZ()));
			xw.closeTag("atom");
		    }
		    xw.closeTag("group") ;
		}
		
		xw.closeTag("chain");
	    }
	}
	
	// do connectivity for all chains:
	
	ArrayList cons = (ArrayList) structure.getConnections();
	for (int cnr = 0; cnr<cons.size();cnr++){
		

	    /*
	      the HashMap for a single CONECT line contains the following fields:
	      <ul>
	      <li>atomserial (mandatory) : Atom serial number
	      <li>bond1 .. bond4 (optional): Serial number of bonded atom
	      <li>hydrogen1 .. hydrogen4 (optional):Serial number of hydrogen bonded atom
	      <li>salt1 .. salt2 (optional): Serial number of salt bridged atom
	      </ul>
	    */
		    
	    HashMap con = (HashMap)cons.get(cnr);
	    Integer as = (Integer)con.get("atomserial");
	    int atomserial = as.intValue();
		    
		    
	    ArrayList atomids = new ArrayList() ;
		    
	    // test salt and hydrogen first //
	    if (con.containsKey("salt1")) atomids.add(con.get("salt1"));
	    if (con.containsKey("salt2")) atomids.add(con.get("salt2"));
		    
	    if (atomids.size()!=0){
		addConnection(xw,"salt",atomserial,atomids);
		atomids = new ArrayList() ;		    
	    }
	    if (con.containsKey("hydrogen1")) atomids.add(con.get("hydrogen1"));
	    if (con.containsKey("hydrogen2")) atomids.add(con.get("hydrogen2"));		
	    if (con.containsKey("hydrogen3")) atomids.add(con.get("hydrogen3"));		
	    if (con.containsKey("hydrogen4")) atomids.add(con.get("hydrogen4"));
	    if (atomids.size()!=0){
		addConnection(xw,"hydrogen",atomserial,atomids);
		atomids = new ArrayList() ;		    
	    }
		    
	    if (con.containsKey("bond1")) atomids.add(con.get("bond1"));
	    if (con.containsKey("bond2")) atomids.add(con.get("bond2"));
	    if (con.containsKey("bond3")) atomids.add(con.get("bond3"));
	    if (con.containsKey("bond4")) atomids.add(con.get("bond4"));
		    
	    if (atomids.size()!=0){
		addConnection(xw,"bond",atomserial,atomids);
	    }
	}
    }

    private void addConnection(XMLWriter xw,String connType, int atomserial, ArrayList atomids){
	try{
	    xw.openTag("connect");
	    xw.attribute("atomSerial",Integer.toString(atomserial));
	    xw.attribute("type",connType);
	    for (int i=0;i<atomids.size();i++){
		Integer atomid = (Integer)atomids.get(i);
		int aid = atomid.intValue();
		xw.openTag("atomID");
		xw.attribute("atomID",Integer.toString(aid));
		xw.closeTag("atomID");
	    }
	    xw.closeTag("connect"); 
	} catch( Exception e) {
	    e.printStackTrace();
	}
    }


}
