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
	

	String str = "";
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
		    String type = g.getType() ;
		    String record = "" ;
		    if ( type.equals("hetatm") ) {
			record = "HETATM";
		    } else {
			record = "ATOM  ";
		    }
	   
		    // format output ...
		    //AtomIterator aiter = new AtomIterator(g) ;
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

			int    seri       = a.getPDBserial() ;
			String serial     = alignRight(""+seri,5);
			String fullname   = a.getFullName() ;
			String altLoc     = " " ; // not supported, yet!
			String resseq     = alignRight(""+pdbcode,4);
			String x          = alignRight(""+d3.format(a.getX()),8);
			String y          = alignRight(""+d3.format(a.getY()),8);
			String z          = alignRight(""+d3.format(a.getZ()),8);
			String occupancy  = alignRight(""+d2.format(a.getOccupancy()),6);
			String tempfactor = alignRight(""+d2.format(a.getTempFactor()),6);
		
			line = record + serial + " " + fullname +altLoc 
			    + resName + " " + chainID + resseq 
			    + "    " + x+y+z 
			    + occupancy + tempfactor;
			str += line + "\n";
			//System.out.println(line);
		    }
		}
	    }

	}
    


	return str ;
    }
}
