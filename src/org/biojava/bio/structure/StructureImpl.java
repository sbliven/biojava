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
 *
 * @author Andreas Prlic
 * @since 1.4
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

    /* returns an identical copy of this structure 
       public Object clone() {
       
       StructureImpl n = new StructureImpl();
       // pfuuuh go through whole substructure and clone! 
       
       }
    */


    /**
     *
     * set PDB code of structure 
     */
    public void setPDBCode (String pdb_id_) {
	pdb_id = pdb_id_ ;
    }
    /**
     *
     * get PDB code of structure 
     */
    public String  getPDBCode () {
	return pdb_id ;
    }
	
	
  
    /** set biological name of Structure */
    public void   setName(String nam) { name = nam; }
    /** get biological name of Structure */
    public String getName()           { return name;  }
    
    /** set the Header data */
    public void    setHeader(Map h){ header = (HashMap) h;    }
    /** get Header data */
    public Map getHeader()         { return header ;}

    /** @see Structure interface */
    public void      setConnections(List conns) { connections = (ArrayList)conns ; }
    /** @see Structure interface */
    public List getConnections()                { return connections ;}

    /** add a new chain */
    public void addChain(Chain chain) {
	int modelnr = 0 ;
	addChain(chain,modelnr);
    }

    /** add a new chain, if several models are available*/
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
    
    /** retrieve a chain by it's position within the Structure */
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
    public void addModel(List model){
	models.add((ArrayList)model);
    }

    /** string representation */
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
    
    /** return number of chains , if NMR return number of chains of first model 
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
    
    /** return number of chains  of model */
    public int size(int modelnr) { return getChains(modelnr).size();   }

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
    public List getChains(int modelnr){
	return getModel(modelnr);
    }

    /** retrieve all Chains belonging to a model */
    public List getModel(int modelnr) {

	ArrayList model = (ArrayList)models.get(modelnr);		
	return model;
    }    


   

    /** create a String that contains the contents of a PDB file */
    public String toPDB() {
	FileConvert f = new FileConvert(this) ;
	
	String str = f.toPDB();


	return str ;
	
    }
   
    
}
