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
import org.biojava.bio.structure.io.PDBParseException;

import java.util.ArrayList ;
/**
 *
 * Generic Implementation of a Group interface.
 * AminoAcid and Nucleotide are closely related classes.
 * @see AminoAcid
 * @see Nucleotide
 */
public class Hetatom implements Group {
    
    public static String type = "hetatm" ;

    /* stores if 3d coordinates are available */
    boolean pdb_flag ;
    /* 3 letter name of amino acid in pdb file */
    String pdb_name ;
    /* pdb numbering */
    String pdb_code ;
	
    ArrayList atoms ;
    public Hetatom() {
	super();

		
	pdb_flag = false;
	pdb_name = null ;
	pdb_code = null ;
	atoms    = new ArrayList();
    }
    public boolean has3D() {
	// TODO Auto-generated method stub
	return pdb_flag;
    }
  
    public void setPDBFlag(boolean flag){
	pdb_flag = flag ;
    }
  
    /* (non-Javadoc)
     * @see org.biojava.bio3d.AminoAcid_Map#get_PDB_code()
     */
    public String getPDBCode() {
	// TODO Auto-generated method stub
	return pdb_code;
    }
  
    public void setPDBCode(String pdb) {
	pdb_code = pdb ;
    }

    /** set three character name of Amino acid */
    public void setPDBName(String s) 
	throws PDBParseException
    {
	if (s.length() != 3) {
	    throw new PDBParseException("amino acid name is not of length 3!");
	}
	pdb_name =s ;
    }

    public String getPDBName() { return pdb_name;}

    /** add an atom to this group */
    public void addAtom(Atom atom){
	atoms.add(atom);
	if (atom.getCoords() != null){
	    // we have got coordinates!
	    setPDBFlag(true);
	}
    };

    public int size(){ return atoms.size();   }
    
    /** get all atoms of this group */
    public Atom[] getAtoms(){
	Atom[] atms = (Atom[])atoms.toArray(new Atom[atoms.size()]);
	return atms ;
    }

    /** get an Atom or null, if Atom not in this group */
    public Atom getAtom(String name)
	throws StructureException
    {
	
	for (int i=0;i<atoms.size();i++){
	    Atom atom = (Atom)atoms.get(i);
	    if (atom.getName().equals(name)){
		return atom;
	    }
	}

	throw new StructureException(" No atom "+name + " in group " + pdb_name + " " + pdb_code + " !");
	
    }

    /** test is an Atom with name is existing */
    public boolean hasAtom(String name){
	for (int i=0;i<atoms.size();i++){
	    Atom atom = (Atom)atoms.get(i);
	    if (atom.getName().equals(name)){
		return true;
	    }
	}
	return false ;
    }

    public String getType(){ return type;}

    public String toString(){
		
	String str = "PDB: "+ pdb_name + " " + pdb_code +  " "+ pdb_flag;
	if (pdb_flag) {
	    str = str + "atoms: "+atoms.size();
	}
	return str ;
		
    }

    /** calculate if a groups has all atoms required for an amino acid
	this allows to include chemically modified amino acids that
	are labeled hetatoms into some computations ...  the usual way
	to identify if a group is an amino acid is getType() !
	@see getType()
	<p>
	amino atoms are : N, CA, C, O, CB
	GLY does not have CB (unless we would calculate some artificially
	</p>

	if this method call is performed too often, it should become a
	private method and set a flag ...
     */
    public boolean hasAminoAtoms(){

	String[] atoms ; 
	if ( getType().equals("amino") & getPDBName().equals("GLY")){
	    atoms = new String[] { "N","CA","C","O"};
	} else {
	    atoms = new String[] { "N","CA","C","O","CB" };
	}

	
	for (int i = 0 ; i < atoms.length; i++) {
	    if ( ! hasAtom(atoms[i])) {
		//System.out.println("not amino atoms");
		return false ;
	    }
	}
	     
	return true ;
    }

    

}
