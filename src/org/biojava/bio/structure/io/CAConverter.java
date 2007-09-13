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
 * Created on Sep 12, 2007
 *
 */
package org.biojava.bio.structure.io;

import java.util.ArrayList;
import java.util.List;

import org.biojava.bio.structure.AminoAcid;
import org.biojava.bio.structure.AminoAcidImpl;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ChainImpl;
import org.biojava.bio.structure.Group;

/** converts full atom representations to Calpha only ones
 * 
 * @author Andreas Prlic
 * @version %I% %G%
 */
public class CAConverter {

    
    public static List<Chain> getCAOnly(List<Chain> chains){
        List<Chain> newChains = new ArrayList<Chain>();
       
        for (Chain chain : chains){
                Chain newChain = getCAOnly(chain);
                newChains.add(newChain);
            
        }
        
        return newChains;
        
        
        
    }
    
    public static Chain getCAOnly(Chain chain){

        Chain newChain = new ChainImpl();
        newChain.setName(chain.getName());
        newChain.setHeader(chain.getHeader());
        newChain.setSwissprotId(chain.getSwissprotId());
        
        List<Group> groups = chain.getAtomGroups();
        
        grouploop:                
        for (Group g: groups){
            List<Atom> atoms = g.getAtoms();
            
            if ( ! (g instanceof AminoAcid))
                continue;
            
            for (Atom a : atoms){
                
                if ( a.getFullName().equals(" CA ")){
                    // we got a CA atom in this group!
                    AminoAcid n = new AminoAcidImpl();
                    try {
                    n.setPDBName(g.getPDBName());
                    } catch (PDBParseException e){
                        e.printStackTrace();
                    }
                    n.setPDBCode(g.getPDBCode());
                    n.addAtom(a);
                    newChain.addGroup(n);
                    continue grouploop;
                    
                }
            }
            
        }
        return newChain;
    }
}
