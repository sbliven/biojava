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
 * Created on 12.03.2004
 * @author Andreas Prlic
 *
 */
package org.biojava.bio.structure;


import java.util.ArrayList ;

/**
 * A Chain in a PDB file. It contains several groups which can be of
 * type "amino", "hetatm", "nucleotide".
 * @author Andreas Prlic
 * @since 1.4
 */
public class ChainImpl implements Chain {

    String swissprot_id ; 
    String name ; // like in PDBfile
    ArrayList groups;
	
    /**
     *  Constructs a ChainImpl object.
     */
    public ChainImpl() {
	super();
	// TODO Auto-generated constructor stub
	name = "";
	groups = new ArrayList() ;
    }


    /** returns an identical copy of this Chain .
     * @return an identical copy of this Chain 
     */
    public Object clone() {
	// go through all groups and add to new Chain.
	Chain n = new ChainImpl();
	// copy chain data:
	
	n.setName( getName());
	n.setSwissprotId ( getSwissprotId());
	for (int i=0;i<groups.size();i++){
	    Group g = (Group)groups.get(i);
	    n.addGroup((Group)g.clone());
	}
	
	return n ;
    }

    /** set the Swissprot id of this chains .
     * @param sp_id  a String specifying the swissprot id value
     * @see #getSwissprotId
     */

    public void setSwissprotId(String sp_id){
	swissprot_id = sp_id ;
    }
    
    /** get the Swissprot id of this chains .
     * @return a String representing the swissprot id value
     * @see #setSwissprotId
     */
    public String getSwissprotId() {
	return swissprot_id ;
    }
	

    public void addGroup(Group group) {
	// TODO Auto-generated method stub

	groups.add(group);
    }

    /** return the amino acid at position .
     * 
     *
     * @param position  an int
     * @return a Group object
     */
    public Group getGroup(int position) {
	// TODO Auto-generated method stub
	return (Group)groups.get(position);
    }

    /** return an array of all groups of a special type (e.g. amino,
     * hetatm, nucleotide).
     * @param type  a String
     * @return an List object containing the groups of type...

     */
    public ArrayList getGroups( String type) {
	ArrayList tmp = new ArrayList() ;
	for (int i=0;i<groups.size();i++){
	    Group g = (Group)groups.get(i);
	    if (g.getType().equals(type)){
		tmp.add(g);
	    }
	}
	//Group[] g = (Group[])tmp.toArray(new Group[tmp.size()]);
	return tmp ;
    }
    /** return all groups of this chain .
     * @return an ArrayList object representing the Groups of this Chain.
     */
    public ArrayList getGroups(){
	return groups ;
    }

    

    public int getLength() {return groups.size();  }
    
    public int getLengthAminos() {

	ArrayList g = getGroups("amino");
	return g.size() ;
    }

    
    /** get and set the name of this chain (Chain id in PDB file ).
     * @param nam a String specifying the name value
     * @see #getName
     * 
     */

    public void   setName(String nam) { name = nam;   }

    /** get and set the name of this chain (Chain id in PDB file ).
     * @return a String representing the name value
     * @see #setName
     */
    public String getName()           {	return name;  }
	

    /** string representation. */
    public String toString(){
	
	String str = "Chain >"+getName() + "< total length:" + getLength() + " residues";
	// loop over the residues
	int i = 0 ;
	do {
	    Group gr = (Group)groups.get(i);
	    str += gr.toString() + "\n" ;
	    i++ ;
	} while ( i< groups.size()) ;
	    
	return str ;
			
    }

    /** get amino acid sequence.
     * @return a String representing the sequence.	    
     */
    public String getSequence(){
	String str = "" ;
	ArrayList gr = getGroups("amino");
	for (int i=0 ; i<gr.size();i++){
	    AminoAcid a = (AminoAcid)gr.get(i);
	    
	    str += a.getPDBName() ;
	}

	    

	return str ;
    }
	
}
