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
    

    /**
     * return the PDBcode of this amino acid
     */
    public String getPDBCode(); 
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
    public Atom[] getAtoms();
    
    /** get an atom */
    public Atom getAtom(String name);
    
    /** set the PDB 3 character name for this group
     * 
     */
    public String getPDBName();

    /** get the PDB 3 character name for this group */
    public void setPDBName(String s) throws PDBParseException;
}
