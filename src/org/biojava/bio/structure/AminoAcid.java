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
 * Created on 05.03.2004
 * @author Andreas Prlic
 *
 */
package org.biojava.bio.structure;
import  org.biojava.bio.structure.io.PDBParseException;

import java.util.ArrayList ;
/**
 *
 *  AminoAcid inherits most from Hetatom.  Adds a few AminoAcid
 *  specific methods.
 * 
 */
public class AminoAcid extends Hetatom {

    public static String type = "amino";
   
    /* IUPAC amino acid residue names 
     */
    Character amino_char ;
          

    /*
     * inherits most from Hetero and has just a few extensions
     */
    public AminoAcid() {
	super();

	amino_char = null;

	
    }

    
    public String getType(){ return type;}
    
    /** browse through atoms and find the right one*/
    private Atom findAtom(String name){

	for (int i=0;i<atoms.size();i++){
	    Atom atom = (Atom)atoms.get(i);
	    if (atom.getName().equals(name)){
		return atom;
	    }
	}
	return null;
	
    }

    /** get N atom*/
    public Atom getN()  {return findAtom("N");  }
    /** get CA atom*/
    public Atom getCA() {return findAtom("CA"); }
    /** get C atom*/
    public Atom getC()  {return findAtom("C");  }
    /** get O atom*/
    public Atom getO()  {return findAtom("O");  }
    /** get CB atom*/
    public Atom getCB() {return findAtom("CB"); }

    


    /** returns the name of the AA, in single letter code */
    public  Character getAminoType() {
	return amino_char;
    }

    /** set the name of the AA, in single letter code */
    public void setAminoType(Character aa){
	amino_char  = aa ;
    }

    /** string representation */
    public String toString(){
		
	String str = "PDB: "+ pdb_name + " " + amino_char + " " + pdb_code +  " "+ pdb_flag;
	if (pdb_flag) {
	    str = str + "atoms: "+atoms.size();
	}
	return str ;
		
    }

   


}
