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

import org.biojava.bio.structure.* ;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.text.DecimalFormat;

// xml writer ...
import org.biojava.utils.xml.*;
import java.io.IOException ;

public class FileConvert {
    Structure structure ;

    public FileConvert(Structure struc) {
	structure = struc ;
    }

    private String alignRight(String input, int length){

	String str = "" ;
	int diff = length - input.length() ;
	
	for (int i = length; i>0; i--) {

	    if  ( i <= diff) {
		str = " " + str;
	    } else {
		int pos = input.length() - length +  i -1;
		//System.out.println(input + " " +pos + " " + input.charAt(pos) );
		str = input.charAt(pos) + str ;
	    }                    
        }
	return str ;
    }



    public String toPDB() {
	

	StringBuffer str = new StringBuffer();
	int i = 0 ;
	
	DecimalFormat d3 = new DecimalFormat("0.000");
	DecimalFormat d2 = new DecimalFormat("0.00");
	
	// do for all models
	int nrModels = structure.nrModels() ;
	for (int m = 0 ; m < nrModels ; m++) {
	    ArrayList model = structure.getModel(m);
	    // todo support NMR structures ...
	    //if ( structure.isNMR()) {
	    //str += "MODEL  " + (m+1);
	    //}
	    // do for all chains
	    int nrChains = model.size();
	    for ( int c =0; c<nrChains;c++) {
		Chain  chain   = (Chain)model.get(c);
		String chainID = chain.getName();

		// do for all groups
		int nrGroups = chain.getLength();
		for ( int h=0; h<nrGroups;h++){
		    Group g= chain.getGroup(h);
		    //System.out.println(g);
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
			String resseq     = alignRight(""+pdbcode,4);
			String x          = alignRight(""+d3.format(a.getX()),8);
			String y          = alignRight(""+d3.format(a.getY()),8);
			String z          = alignRight(""+d3.format(a.getZ()),8);
			String occupancy  = alignRight(""+d2.format(a.getOccupancy()),6) ;
			String tempfactor = alignRight(""+d2.format(a.getTempFactor()),6);
		
			line = record + serial + " " + fullname +altLoc 
			    + resName + " " + chainID + resseq 
			    + "    " + x+y+z 
			    + occupancy + tempfactor;
			str.append(line + "\n");
			//System.out.println(line);
		    }
		}
	    }

	}
    


	return str.toString() ;
    }

    // convert a protein Structure to a DAS Structure XML response .
    public void toDASStructure(XMLWriter xw)
	throws IOException 
    {
	
	// do for all models
	for (int modelnr = 0;modelnr<structure.nrModels();modelnr++){
	    
	    // do for all chains:
	    for (int chainnr = 0;chainnr<structure.size(modelnr);chainnr++){
		Chain chain = (Chain)structure.getChain(modelnr,chainnr); 
		xw.openTag("CHAIN");
		xw.attribute("id",chain.getName());
		if (structure.isNmr()){
		    xw.attribute("model",Integer.toString(modelnr+1));
		}
		
		//do for all groups:
		for (int groupnr =0;groupnr<chain.getLength();groupnr++){
		    Group gr = chain.getGroup(groupnr);
		    xw.openTag("GROUP");
		    xw.attribute("name",gr.getPDBName());
		    xw.attribute("type",gr.getType());
		    xw.attribute("groupid",gr.getPDBCode());
		    
		    
		    // do for all atoms:
		    //Atom[] atoms  = gr.getAtoms();
		    ArrayList atoms = gr.getAtoms();
		    for (int atomnr=0;atomnr<atoms.size();atomnr++){
			Atom atom = (Atom)atoms.get(atomnr);
			xw.openTag("ATOM");
			xw.attribute("atomID",Integer.toString(atom.getPDBserial()));
			xw.attribute("atomName",atom.getFullName());
			xw.attribute("x",Double.toString(atom.getX()));
			xw.attribute("y",Double.toString(atom.getY()));
			xw.attribute("z",Double.toString(atom.getZ()));
			xw.closeTag("ATOM");
		    }
		    xw.closeTag("GROUP") ;
		}
		
		xw.closeTag("CHAIN");


		ArrayList cons = structure.getConnections();
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
	}
    }

        private void addConnection(XMLWriter xw,String connType, int atomserial, ArrayList atomids){
	try{
	    xw.openTag("CONNECT");
	    xw.attribute("atomserial",Integer.toString(atomserial));
	    xw.attribute("connectionType",connType);
	    for (int i=0;i<atomids.size();i++){
		Integer atomid = (Integer)atomids.get(i);
		int aid = atomid.intValue();
		xw.openTag("ATOMID");
		xw.attribute("atomid",Integer.toString(aid));
		xw.closeTag("ATOMID");
	    }
	    xw.closeTag("CONNECT"); 
	} catch( Exception e) {
	    e.printStackTrace();
	}
    }


}
