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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.biojava.bio.structure.io.PDBParseException;


/**
 *  
 * This is the data structure for a single Group of atoms.  A protein
 * sequence ({@link Chain} in PDB file) is represented as a list of Groups.
 * There are 3 types of Groups:
 * 
 * <ul>
 * <li>{@link AminoAcid}</li>
 * <li>{@link HetatomImpl Hetatom}</li>
 * <li>{@link NucleotideImpl Nucleotide}</li>
 * </ul>
 *  
 *   
 * @see HetatomImpl
 * @see AminoAcidImpl
 * @see NucleotideImpl
 * @author Andreas Prlic
 * @author Horvath Tamas
 * @since 1.4
 * @version %I% %G%
 */
public interface Group {
    
    
    /* returns and identical copy of this Group .
     public Object clone() ;
     */
    
    /**
     * return the PDBcode (residue number) of this group.
     * @see #setPDBCode
     * @return a String representing the PDBCode value
     */
    public String getPDBCode(); 
    
    /**
     * Specifies the PDBCode value.
     *
     * @param pdbcode  a String specifying the PDBCode value
     * @see #getPDBCode     
     */    
    public void setPDBCode(String pdbcode);
    
    
    /** getnumber of atoms.
     *  @return number of atoms of this Group
     */
    public int size();
    
    /**
     *  returns true or false, depending if this group has 3D coordinates or not.
     *
     * @return true if Group has 3D coordinates
     */
    public boolean has3D ();
    
    /** flag if group has 3D data .
     *
     * @param flag  true to set flag that this Group has 3D coordinates
     */
    public void setPDBFlag(boolean flag);
    
    /** 
     * get Type of group, e.g. amino, hetatom, nucleotide.
     * 
     *
     * @return a String representing the type value     
     */
    public String getType();
    
    /** add an atom to this group.
     *
     * @param atom  an Atom object
     */
    public void addAtom(Atom atom);
    
    /** get list of atoms.
     *
     * @return an List object representing the atoms 
     */
    public List<Atom> getAtoms() ;
    
    
    /** set the atoms of this group
     * @see org.biojava.bio.structure.Atom
     * @param atoms a list of atoms
     */
    public void setAtoms(List<Atom> atoms);
    
    /** remove all atoms from this group
     * 
     *
     */
    public void clearAtoms();
    
    /** get an atom throws StructureException if atom not found.
     *
     * @param name  a String
     * @return an Atom object
     * @throws StructureException ...     
     */
    public Atom getAtom(String name) throws StructureException;
    
    /** get at atom by position.
     *
     * @param position  an int
     * @return an Atom object
     * @throws StructureException ...     
     */
    public Atom getAtom(int position) throws StructureException;
    
    /** returns flag whether a particular atom is existing within this group .
     *
     * @param name  a String ...
     * @return true if Atom with name is existing within this group
     */
    public boolean hasAtom(String name);
    
    /** set the PDB 3 character name for this group.
     * 
     * @return a String representing the PDBName value
     * @see #setPDBName
     */
    public String getPDBName();
    
    /** get the PDB 3 character name for this group.
     *
     * @param s  a String specifying the PDBName value
     * @throws PDBParseException ...     
     * @see #getPDBName
     */
    public void setPDBName(String s) throws PDBParseException;
    
    
    /** calculate if a groups has all atoms required for an amino acid.
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
     *
     * @return true if all Atoms required for an AminoAcid are available (N, CA, C, O, CB)	
     @see #getType
     */
    public boolean hasAminoAtoms() ;
    
    
    
    /** properties of this amino acid. currerntly available properties.
     * are:
     * phi
     * psi
     * 
     *
     * @param properties  a Map object specifying the properties value
     * @see #getProperties
     
     */
    
    public void setProperties(Map<String,Object> properties) ;
    
    /** return properties. 
     * @see #setProperties
     *
     * @return a HashMap object representing the properties value
     */
    public Map<String,Object> getProperties() ;
    
    /** set a single property .
     *
     * @param key    a String
     * @param value  an Object
     * @see #getProperty
     
     */
    public void setProperty(String key, Object value) ;
    
    /** get a single property .
     *
     * @param key  a String
     * @return an Object
     * @see #setProperty
     */
    public Object getProperty(String key) ;
    
    /** get an Atom Iterator.
     *
     * @return an Iterator object
     */
    public Iterator<Atom> iterator() ;
    
    
    /** returns and identical copy of this Group object .
     * @return  and identical copy of this Group object 
     */
    public Object clone();
    
    
    /** Set the back-reference (to its parent Chain)
     * @param parent a WeakReference to the parent Chain
     * 
     */
    public void setParent(Chain parent) ; 
    
    /** Returns the parent Chain of the Group
     * 
     * @return Chain the Chain object that contains the Group
     */
    
    public Chain getParent() ;
    
}
