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
 * Created on 26.04.2004
 * @author Andreas Prlic
 *
 */
package org.biojava.bio.structure;

import java.util.ArrayList ;
import java.util.HashMap ;

/**
 * Implementation of a PDBStructure. This class
 * provides the data contained in a PDB file.
 */
public class StructureImpl implements Structure {

   
    
    String pdb_id ;
    /* models is an ArrayList of ArrayLists */
    ArrayList models;

    HashMap header ;
    ArrayList connections ;
    String name ;

    boolean nmrflag ;

    /**
     * 
     */
    public StructureImpl() {
	super();

	models 		= new ArrayList() ;
	name 		= ""              ;
	nmrflag 	= false ;

    }

    public void setPDBCode (String pdb_id_) {
	pdb_id = pdb_id_ ;
    }
    public String  getPDBCode () {
	return pdb_id ;
    }
	
	
    /* (non-Javadoc)
     * @see org.biojava.bio3d.PDB_Container#set_name(java.lang.String)
     */
    public void   setName(String nam) { name = nam; }
    public String getName()           { return name;  }
    
    /* set the Header data */
    public void    setHeader(HashMap h){ header = h;    }
    public HashMap getHeader()         { return header ;}

    public void      setConnections(ArrayList conns) { connections = conns ; }
    public ArrayList getConnections()                { return connections ;}


    public void addChain(Chain chain) {
	// TODO Auto-generated method stub
	int modelnr = 0 ;
	addChain(chain,modelnr);
    }

    /** add a new chain, if several models are available*/
    public void addChain(Chain chain, int modelnr) {
	ArrayList model = (ArrayList)models.get(modelnr);
	model.add(chain);
    }
    

    public Chain getChain(int number) {
	// TODO Auto-generated method stub
	int modelnr = 0 ;
	return getChain(modelnr,number);
    }

    /** retrieve a chain by it's position within the Structure and model number*/
    public Chain getChain(int modelnr,int number) {
	// TODO Auto-generated method stub
	//System.out.println ("in get_chain "+modelnr+" "+number);
	ArrayList model  = ( ArrayList ) models.get(modelnr);
	//System.out.println("model:"+model);
	Chain chain = ( Chain ) model.get (number );

	//System.out.println ("pdbch:"+pdbch);
	return chain ;
    }

  
    /** add a new model */
    public void addModel(ArrayList model){
	models.add(model);
    }

    public String toString(){
	String str = "structure "+name+ " " + pdb_id ;
	if ( isNmr() ) str += " models: "+nrModels()+"\n" ;
	str += header ;
	
	for (int i=0;i<nrModels();i++){
	    if (isNmr() ) str += " model["+i+"]:";
	    str +=" chains:";
		
	    str += "\n";
	    for (int j=0;j<size(i);j++){
		//System.out.println("getting chain "+i+" "+j); 
		Chain cha = (Chain)getChain(i,j); 
		Group[] agr = cha.getGroups("amino");
		Group[] hgr = cha.getGroups("hetatm");
		Group[] ngr = cha.getGroups("nucleotide");
		    
		str += "chain: >"+cha.getName()+"<"+
		    " length: " +cha.getLength()+
		    " aminos: " +agr.length+
		    " hetatms: "+hgr.length+
		    " nucleotides: "+ngr.length +"\n";
	    }
	}


	return str ;
    }
    
    /** return number of chains in this container 
     * 
     */
    public int size(){
	int modelnr = 0 ;
	ArrayList model = (ArrayList)models.get(modelnr);
	return models.size() ;
    }
    
    /** return number of chains of of model */
    public int size(int modelnr) { return getChains(modelnr).length;   }

    // some NMR stuff : 

    /** return number of models */
    public int nrModels() {
	return models.size() ;
    }

    /** is this structure an nmr strucutre ? 
     */
    public boolean isNmr() {return nmrflag ;  }

    /* set the nmr flag */
    public void setNmr(boolean nmr) {	nmrflag = nmr ; }
    

    /** retrieve all chains of a model*/
    public Chain[] getChains(int modelnr){
	return getModel(modelnr);
    }


    /** retrieve all Chains belonging to a model */
    public Chain[] getModel(int modelnr) {
	ArrayList model = (ArrayList)models.get(modelnr);
	
	Chain[] chains = (Chain[])model.toArray(new Chain[model.size()]);
	return chains ;
    }


    
}
