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
import java.util.HashMap   ;
import java.util.Map       ;
import java.util.List      ;

import org.biojava.bio.structure.io.FileConvert ;
/**
 * Implementation of a PDBStructure. This class
 * provides the data contained in a PDB file.
 * to get structure objects from different sources
 * see io package.
 *
 * @author Andreas Prlic
 * @since 1.4
 * @version %I% %G%
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
     *  Constructs a StructureImpl object.
     */
    public StructureImpl() {
	super();

	models 		= new ArrayList() ;
	name 		= ""              ;
	nmrflag 	= false ;
	header = new HashMap();
    }


    /** returns an identical copy of this structure .
     * @return an identical Structure object     
     */
    public Object clone() {
       
	StructureImpl n = new StructureImpl();
       // go through whole substructure and clone ...
	
	// copy structure data
	if (isNmr()) n.setNmr(true);
	
	n.setPDBCode(getPDBCode());
	n.setName(getName());
	n.setHeader(getHeader());
	n.setConnections(getConnections());
	
	// go through each chain and clone chain
	for (int i=0;i<nrModels();i++){
	    ArrayList cloned_model = new ArrayList();

	    for (int j=0;j<size(i);j++){
		//System.out.println("getting chain "+i+" "+j); 
		Chain current_chain = (Chain) getChain(i,j); 
		Chain cloned_chain  = (Chain) current_chain.clone();
		
		cloned_model.add(cloned_chain);
	    }
	    n.addModel(cloned_model);

	}
	return n ;
    }


    /**
     *
     * set PDB code of structure .
     * @see #getPDBCode
     *
     */
    public void setPDBCode (String pdb_id_) {
	pdb_id = pdb_id_ ;
    }
    /**
     *
     * get PDB code of structure .
     * 
     * @return a String representing the PDBCode value
     * @see #setPDBCode
     */
    public String  getPDBCode () {
	return pdb_id ;
    }
	
	
  
    /** set biological name of Structure.
     *
     * @see #getName
     *
     */
    public void   setName(String nam) { name = nam; }

    /** get biological name of Structure. 
     *
     * @return a String representing the name 
     * @see #setName
     */
    public String getName()           { return name;  }
    
    /** set the Header data.
     *
     *
     * @see #getHeader
     */
    public void    setHeader(Map h){ header = (HashMap) h;    }
    /** get Header data.
     *
     * @return a Map object representing the header of the Structure
     *
     * @see #setHeader
     */
    public Map getHeader()         { return header ;}

    /** @see Structure interface.
     *
     *

     */
    public void      setConnections(List conns) { connections = (ArrayList)conns ; }
    /** 
     * Returns the connections value.
     *
     * @return a List object representing the connections value
     * @see Structure interface 
     * @see #setConnections
     */
    public List getConnections()                { return connections ;}

    /** add a new chain.
     *
     */
    public void addChain(Chain chain) {
	int modelnr = 0 ;
	addChain(chain,modelnr);
    }

    /** add a new chain, if several models are available.
     *
     */
    public void addChain(Chain chain, int modelnr) {
	// if model has not been initialized, init it!
	if ( models.size() == 0  ) {
	    ArrayList model = new ArrayList() ;
	    model.add(chain);
	    models.add(model);
	    
	} else {
	    ArrayList model = (ArrayList)models.get(modelnr);	    
	    model.add(chain);
	}
    }
    
    /** retrieve a chain by it's position within the Structure.
     *
     * @param number  an int
     * @return a Chain object
     */
    public Chain getChain(int number) {
	// TODO Auto-generated method stub
	int modelnr = 0 ;
	return getChain(modelnr,number);
    }

    /** retrieve a chain by it's position within the Structure and model number.
     *
     * @param modelnr  an int
     * @param number   an int
     * @return a Chain object
     */
    public Chain getChain(int modelnr,int number) {
	// TODO Auto-generated method stub
	//System.out.println ("in get_chain "+modelnr+" "+number);
	ArrayList model  = ( ArrayList ) models.get(modelnr);
	//System.out.println("model:"+model);
	Chain chain = ( Chain ) model.get (number );

	//System.out.println ("pdbch:"+pdbch);
	return chain ;
    }

  
    /** add a new model.
     *
     */
    public void addModel(List model){
	models.add((ArrayList)model);
    }

    /** string representation.
     *
     */
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
		ArrayList agr = cha.getGroups("amino");
		ArrayList hgr = cha.getGroups("hetatm");
		ArrayList ngr = cha.getGroups("nucleotide");
		    
		str += "chain: >"+cha.getName()+"<"+
		    " length: " +cha.getLength()+
		    " aminos: " +agr.size()+
		    " hetatms: "+hgr.size()+
		    " nucleotides: "+ngr.size() +"\n";
	    }
	}


	return str ;
    }
    
    /** return number of chains , if NMR return number of chains of first model .
     * 
     */
    public int size() {
	int modelnr = 0 ;
	ArrayList model = null ;
	if ( models.size() > 0) {
	    model = (ArrayList)models.get(modelnr);
	}
	else {
	    return 0 ;
	}

	return model.size() ; 
    } 
    
    /** return number of chains  of model.
     *
     */
    public int size(int modelnr) { return getChains(modelnr).size();   }

    // some NMR stuff : 

    /** return number of models. */
    public int nrModels() {
	return models.size() ;
    }

    /** is this structure an nmr structure ? 
     */
    public boolean isNmr() {return nmrflag ;  }

    /* set the nmr flag */
    public void setNmr(boolean nmr) {	nmrflag = nmr ; }
    

    /** retrieve all chains of a model.
     *
     * @param modelnr  an int
     * @return a List object     
    */
    public List getChains(int modelnr){
	return getModel(modelnr);
    }

    /** retrieve all Chains belonging to a model .
     *
     * @param modelnr  an int
     * @return a List object
     */
    public List getModel(int modelnr) {

	ArrayList model = (ArrayList)models.get(modelnr);		
	return model;
    }    


   

    /** create a String that contains the contents of a PDB file. 
     *
     * @return a String that represents the structure as a PDB file.
     */
    public String toPDB() {
	FileConvert f = new FileConvert(this) ;
	
	String str = f.toPDB();


	return str ;
	
    }
   
    
}
