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
import  org.biojava.bio.structure.io.PDBParseException ;


import java.util.HashMap ;
import java.util.ArrayList ;
import java.util.Iterator ;

/**
 *  
 * This is the datastructure for a single Group of atoms.  A protein
 * sequence (Chain in PDB file) is represented as a list of this kind
 * of objects. Groups can be of type
 * "amino","hetatm","nucleotide". There are corresponding classes for
 * each of this type which implement interface Group.
 * 
 * @see Hetatom
 * @see AminoAcid
 * @see Nucleotide
 */
public interface Group {
    

    /* returns and identical copy of this Group 
       public Object clone() ;
    */

    /**
     * return the PDBcode of this amino acid
     */
    public String getPDBCode(); 
    
    /**
     * Specifies the PDBCode value.
     */    
    public void setPDBCode(String pdbcode);

   
    /** getnumber of atoms */
    public int size();

    /**
     *  returns true or false, depending if this group has 3D coordinates or not.
     */
    public boolean has3D ();
    
    /** flag if group has 3D data */
    public void setPDBFlag(boolean flag);
    
    /** 
     * get Type of group, e.g. amino, hetatom, nucleotide
     * 
     */
    public String getType();

    /** add an atom to this group */
    public void addAtom(Atom atom);
    
    /** get list of atoms */
    public ArrayList getAtoms() ;
        
    /** get an atom throws StructureException if atom not found*/
    public Atom getAtom(String name) throws StructureException;
    
    /** get at atom by position */
    public Atom getAtom(int position) throws StructureException;

    /** returns flag whether a particular atom is existing within this group */
    public boolean hasAtom(String name);
    
    /** set the PDB 3 character name for this group
     * 
     */
    public String getPDBName();

    /** get the PDB 3 character name for this group */
    public void setPDBName(String s) throws PDBParseException;


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
    public boolean hasAminoAtoms() ;



    /** properties of this amino acid. currerntly available properties
     * are:
     * phi
     * psi
     * 
     */

    public void setProperties(HashMap properties) ;
    
    /** return properties. @see setProperties() */
    public HashMap getProperties() ;

    /** set a single property */
    public void setProperty(String key, Object value) ;
    /** get a single property */
    public Object getProperty(String key) ;

    /** get an Atom Iterator */
    public Iterator iterator() ;

}
