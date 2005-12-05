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
import java.util.List ;
import java.util.HashMap ;
import java.util.Map ;
import java.util.Iterator ;

/**
 *
 * Generic Implementation of a Group interface.
 * AminoAcidImpl and NucleotideImpl are closely related classes.
 * @see AminoAcidImpl
 * @see NucleotideImpl
 * @author Andreas Prlic
 * @version %I% %G%
 * @since 1.4
 */
public class HetatomImpl implements Group {
    
    /** this is a "hetatm".
     *
     */
    public static String type = "hetatm" ;
    
    HashMap properties ;


    /* stores if 3d coordinates are available. */
    boolean pdb_flag ;

    /* 3 letter name of amino acid in pdb file. */
    String pdb_name ;

    /* pdb numbering. */
    String pdb_code ;
	
    ArrayList atoms ;

    /* Construct a Hetatom instance. */
    public HetatomImpl() {
	super();

		
	pdb_flag = false;
	pdb_name = null ;
	pdb_code = null ;
	atoms    = new ArrayList();    
	properties = new HashMap();
	
    }

    /* returns an identical copy of this structure 
    public Object clone() {
    Hetatom n = new Hetatom();	
    }
    */

   
    /**
     *  returns true or false, depending if this group has 3D coordinates or not.     
     * @return true if Group has 3D coordinates
     */
    public boolean has3D() {
	return pdb_flag;
    }

    /** flag if group has 3D data.
     * 
     * @param flag  true to set flag that this Group has 3D coordinates
     */
    public void setPDBFlag(boolean flag){
	pdb_flag = flag ;
    }
  
    /**
     * Returns the PDBCode.
     * @see #setPDBCode
     * @return a String representing the PDBCode value
     */
    public String getPDBCode() {
	return pdb_code;
    }
  
    /** set the PDB code.
     * @see #getPDBCode
     */
    public void setPDBCode(String pdb) {
	pdb_code = pdb ;
    }

    /** set three character name of Group .
     *
     * @param s  a String specifying the PDBName value
     * @see #getPDBName
     * @throws PDBParseException ...
     */
    public void setPDBName(String s) 
	throws PDBParseException
    {
	// hetatoms can have pdb_name length < 3. e.g. CU (see 1a4a position 1200 )
	//if (s.length() != 3) {
	//throw new PDBParseException("amino acid name is not of length 3!");
	//}
	pdb_name =s ;
    }

    /**
     * Returns the PDBName.
     *
     * @return a String representing the PDBName value
     * @see #setPDBName
     */
    public String getPDBName() { return pdb_name;}

    /** add an atom to this group. */
    public void addAtom(Atom atom){
	atoms.add(atom);
	if (atom.getCoords() != null){
	    // we have got coordinates!
	    setPDBFlag(true);
	}
    };

    
    /** remove all atoms 
     * 
     */
    public void clearAtoms() {
        atoms.clear(); 
        setPDBFlag(false);
    }
    
    /** getnumber of atoms. 
     *  @return number of atoms
     */
    public int size(){ return atoms.size();   }
    
    /** get all atoms of this group .
     * returns a List of all atoms in this Group
     * @return an List object representing the atoms value
    */
    public List getAtoms(){
	//Atom[] atms = (Atom[])atoms.toArray(new Atom[atoms.size()]);
	
	return atoms ;
    }

    /**  get an atom throws StructureException if atom not found.	 
     * @param name  a String
     * @return an Atom object
     * @throws StructureException ...
    */
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

    /** return an atom by its position in the internal List.
     *
     * @param position  an int
     * @return an Atom object
     * @throws StructureException ...     
     */
    public Atom getAtom(int position) 
	throws StructureException
    {
	if ((position < 0)|| ( position >= atoms.size())) {
	    throw new StructureException("No atom found at position "+position);
	}
	Atom a = (Atom)atoms.get(position);
	return a ;	
    }

    /** test is an Atom with name is existing. */
    public boolean hasAtom(String name){
	for (int i=0;i<atoms.size();i++){
	    Atom atom = (Atom)atoms.get(i);
	    if (atom.getName().equals(name)){
		return true;
	    }
	}
	return false ;
    }

    /**
     * Returns the type value.
     *
     * @return a String representing the type value
     */
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
	are labeled hetatoms into some computations ... the usual way
	to identify if a group is an amino acid is getType() !
	<p>
	amino atoms are : N, CA, C, O, CB
	GLY does not have CB (unless we would calculate some artificially
	</p>
	
	Example: 1DW9 chain A first group is a Selenomethionine, provided as HETATM, but here returns true.
	<pre>
	HETATM    1  N   MSE A   1      11.720  20.973   1.584  0.00  0.00           N
	HETATM    2  CA  MSE A   1      10.381  20.548   1.139  0.00  0.00           C
	HETATM    3  C   MSE A   1       9.637  20.037   2.398  0.00  0.00           C
	HETATM    4  O   MSE A   1      10.198  19.156   2.985  0.00  0.00           O
	HETATM    5  CB  MSE A   1      10.407  19.441   0.088  0.00  0.00           C
	</pre>
	@see #getType
		
    */


    public boolean hasAminoAtoms(){
	// if this method call is performed too often, it should become a
	// private method and provide a flag for Group object ...
	
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


    /** properties of this amino acid. currerntly available properties.
     * are:
     * phi
     * psi
     * 
     * @see #getProperties
     */
    public void setProperties(Map props) {
	properties = (HashMap) props ;
    }

    /** return properties. 
     *
     * @return a HashMap object representing the properties value
     * @see #setProperties
     */
    public Map getProperties() {
	return properties ;
    }

    /** set a single property .
     *
     * @see #getProperties  
     * @see #getProperty
     */
    public void setProperty(String key, Object value){
	properties.put(key,value);
    }

    /** get a single property .
     * @param key  a String
     * @return an Object
     * @see #setProperty
     * @see #setProperties
     */
    public Object getProperty(String key){
	return properties.get(key);
    }
    

    /** return an AtomIterator. 
     *
     * @return an Iterator object
     */
    public Iterator iterator() {
	AtomIterator iter = new AtomIterator(this);
	return iter ;
    }
   
    /** returns and identical copy of this Group object .
     * @return  and identical copy of this Group object 
     */
    public Object clone(){
        
	HetatomImpl n = new HetatomImpl();
	n.setPDBFlag(has3D());
	n.setPDBCode(getPDBCode());
	try {
	    n.setPDBName(getPDBName());
	} catch (PDBParseException e) {
	    e.printStackTrace();
	}
	// copy the atoms
	for (int i=0;i<atoms.size();i++){
	    Atom atom = (Atom)atoms.get(i);
	    n.addAtom((Atom)atom.clone());
	}
	return n;
    }


}
