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
 * Created on 25.04.2004
 * @author Andreas Prlic
 *
 */
package org.biojava.bio.structure;

import java.util.ArrayList ;

public interface Chain {
	
    /** add a group to this chain
     * 
     */
    public void addGroup(Group group);
	
    /** return the amino acid at position X
     * 
     */
    public Group getGroup (int position);
	
    /** return an ArrayList of all groups of a special type (e.g. amino,
     * hetatm, nucleotide)
     */
    public ArrayList getGroups (String type);

    /** return all groups of this chain */
    public ArrayList getGroups ();

    /** get total length of chain, including HETATMs.*/
    public int getLength();
    
    /** returns the length of the AminoAcids in chain, without HETATMs.
     * note: not all amino acids need to have 3D coords, in fact in could be that none
     * has!
     * so length always corresponds to Sequence ( = uniprot,swissprot) length
     */

    public int getLengthAminos();

    /** get and set the name of this chain (Chain id in PDB file )
     * 
     */
    public void setName(String name);	
    public String getName();
	
    /** string representation  */
    public String toString();
	
    /** return the amino acid sequqence of this chain
     * ( all aminos even if they do not have 3D data ...)
     */
    public String getSequence() ;

    /** set the Swissprot id of this chains */
    public void setSwissprotId(String sp_id);

    /** get the Swissprot id of this chains */
    public String getSwissprotId() ;

    
}
