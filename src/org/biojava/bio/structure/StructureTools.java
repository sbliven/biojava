/*
 *                  BioJava development code
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
 * Created on Jan 4, 2006
 *
 */
package org.biojava.bio.structure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/** a class that provides some tool methods
 * 
 * @author Andreas Prlic
 * @since 1.0
 * @version %I% %G%
 */
public class StructureTools {

    public static final String   caAtomName         = "CA" ;
 
    public static final String[] backboneAtomNames = {"N","CA","C","O","CB"};
       
    
    /** count how many number of Atoms are contained within a Structure object.
     * 
     * @param s
     * @return the number of Atoms in this Structure
     */
    public static int getNrAtoms(Structure s){
       
        int nrAtoms = 0;
        
        Iterator<Group> iter = new GroupIterator(s);
        
        while ( iter.hasNext()){
            Group g = (Group) iter.next();
            nrAtoms += g.size();
        }
        
        return nrAtoms;
    }
    
    
    /** count how many groups are contained within a structure object
     * 
     * @param s
     * @return the number of groups
     */
    public static int getNrGroups(Structure s){
        int nrGroups = 0;
        
        List<Chain> chains = s.getChains(0);
        Iterator<Chain> iter = chains.iterator();
        while (iter.hasNext()){
            Chain c = (Chain) iter.next();
            nrGroups += c.getAtomLength();
        }
        return nrGroups;
    }
    
    
    /** returns an array of the requested Atoms from the Structure object. Iterates over all groups
     * and checks if the requested atoms are in this group, no matter if this is a AminoAcid or Hetatom group.
     *
     * 
     * @param s the structure to get the atoms from 
     * 
     * @param atomNames  contains the atom names to be used.
     * @return an Atom[] array
     */ 
    public static Atom[] getAtomArray(Structure s, String[] atomNames){
        Iterator<Group> iter = new GroupIterator(s);
        List<Atom> atoms = new ArrayList<Atom>();
        while ( iter.hasNext()){
            Group g = (Group) iter.next();
           
            // a temp container for the atoms of this group
            List<Atom> thisGroupAtoms = new ArrayList<Atom>();
            // flag to check if this group contains all the requested atoms.
            boolean thisGroupAllAtoms = true;
            for ( int i = 0 ; i < atomNames.length; i++){
                String atomName = atomNames[i];
                try {
                    Atom a = g.getAtom(atomName);
                    thisGroupAtoms.add(a);
                } catch (StructureException e){
                    // this group does not have a required atom, skip it...
                    thisGroupAllAtoms = false;
                    continue;
                    
                }
            
            }
            if ( thisGroupAllAtoms){
                // add the atoms of this group to the array.
                Iterator<Atom> aIter = thisGroupAtoms.iterator();
                while(aIter.hasNext()){
                    Atom a = (Atom) aIter.next();
                    atoms.add(a);
                }
            }
            
        }
        return (Atom[]) atoms.toArray(new Atom[atoms.size()]);
   
    } 
    
   
    
    
    /** returns an Atom array of the CA atoms
     * @param s
     * @return an Atom[] array
     */
    public static Atom[] getAtomCAArray(Structure s){
        String[] atomNames = {caAtomName};
        return getAtomArray(s,atomNames);
    }
    
    /** returns an Atom array of the MainChain atoms
    
     * @param s
     * @return an Atom[] array
     */
    public static Atom[] getBackboneAtomArray(Structure s){
        String[] atomNames = backboneAtomNames;
        return getAtomArray(s,atomNames);
    }

}
