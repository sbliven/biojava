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
 */

package org.biojava.bio.structure;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.biojava.bio.structure.AminoAcid;
import org.biojava.bio.structure.AminoAcidImpl;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.AtomIterator;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ChainImpl;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureImpl;
import org.biojava.bio.structure.io.PDBFileReader;
import org.biojava.bio.structure.io.PDBParseException;


/** A class that can change one amino acid to another. Side chain atoms are neglected, only the Cb atom is kept.
 * 
 *
 * example usage:
 * <pre>
 String filename   =  "/Users/ap3/WORK/PDB/5pti.pdb" ;
 String outputfile =  "/Users/ap3/WORK/PDB/mutated.pdb" ;
 
 PDBFileReader pdbreader = new PDBFileReader();
 
 try{
     Structure struc = pdbreader.getStructure(filename);
     System.out.println(struc);
 
 
     String chainId = " ";
     String pdbResnum = "3";
     String newType = "ARG";
 
     // mutate the original structure and create a new one.
      Mutator m = new Mutator();
      Structure newstruc = m.mutate(struc,chainId,pdbResnum,newType);
  
      FileOutputStream out= new FileOutputStream(outputfile); 
      PrintStream p =  new PrintStream( out );
  
      p.println (newstruc.toPDB());
  
      p.close();
  
  
  } catch (Exception e) {
      e.printStackTrace();
  } 
  </pre>
  */       
public class Mutator{
    List supportedAtoms;
    
    public Mutator(){
        supportedAtoms = new ArrayList();
        supportedAtoms.add("N");
        supportedAtoms.add("CA");
        supportedAtoms.add("C");
        supportedAtoms.add("O");
        supportedAtoms.add("CB");
    }
    
    /** creates a new structure which is identical with the original one. 
     * only one amino acid will be different.
     * 
     * 
     * 
     * 
     * @param struc
     * @param chainId
     * @param pdbResnum
     * @param newType
     * @return
     * @throws PDBParseException
     */
    public Structure  mutate(Structure struc, String chainId, String pdbResnum, String newType) 
    throws PDBParseException{
        
        
        // create a  container for the new structure
        Structure newstruc = new StructureImpl();
        
        // first we need to find our corresponding chain
        
        // get the chains for model nr. 0
        // if structure is xray there will be only one "model".
        List chains = struc.getChains(0);
        
        // iterate over all chains.
        Iterator iter = chains.iterator();
        while (iter.hasNext()){
            Chain c = (Chain)iter.next();
            if (c.getName().equals(chainId)) {
                // here is our chain!
                
                Chain newchain = new ChainImpl();
                newchain.setName(c.getName());
                
                List groups = c.getGroups();
                
                // now iterate over all groups in this chain.
                // in order to find the amino acid that has this pdbRenum.               
                
                Iterator giter = groups.iterator();
                while (giter.hasNext()){
                    Group g = (Group) giter.next();
                    String rnum = g.getPDBCode();
                    
                    // we only mutate amino acids
                    // and ignore hetatoms and nucleotides in this case                   
                    if (rnum.equals(pdbResnum) && (g.getType().equals("amino"))){
                        
                        // create the mutated amino acid and add it to our new chain
                        AminoAcid newgroup = mutateResidue((AminoAcid)g,newType);
                        newchain.addGroup(newgroup);
                    }
                    else {
                        // add the group  to the new chain unmodified.
                        newchain.addGroup(g);
                    }
                }
                
                // add the newly constructed chain to the structure;
                newstruc.addChain(newchain);
            } else {
                // this chain is not requested, add it to the new structure unmodified.
                newstruc.addChain(c);
            }
            
        }
        return newstruc;
    }
    
    /** create a new residue which is of the new type. 
     * Only the atoms N, Ca, C, O, Cb will be considered.
     * @param oldAmino
     * @param newType
     * @return
     */
    public AminoAcid mutateResidue(AminoAcid oldAmino, String newType)
    throws PDBParseException {
        
        AminoAcid newgroup = new AminoAcidImpl();
        
        newgroup.setPDBCode(oldAmino.getPDBCode());
        newgroup.setPDBName(newType);
        
        
        AtomIterator aiter =new AtomIterator(oldAmino);
        while (aiter.hasNext()){
            Atom a = (Atom)aiter.next();
            if ( supportedAtoms.contains(a.getName())){
                newgroup.addAtom(a);
            }
        }
        
        return newgroup;
        
    }
    
}