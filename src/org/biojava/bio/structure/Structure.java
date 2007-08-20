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

import java.util.List;
import java.util.Map;

/**
 * 
 * Interface for a structure object. Provides access to the data of a PDB file.
 *
<hr>
</hr>
 * <p>
 * Q: How can I get a Structure object from a PDB file?
 * </p>
 * <p>
 * A:
 </p>
 * <pre>
 String filename =  "path/to/pdbfile.ent" ;

 PDBFileReader pdbreader = new PDBFileReader();

 try{
      	Structure struc = pdbreader.getStructure(filename);
	System.out.println(struc);
 } catch (Exception e) {
 	e.printStackTrace();
 }
 </pre>

<hr>
</hr>
<p>
Q: How can I calculate Phi and Psi angles of the AminoAcids?
</p>
<p>
A:
</p>
<pre>
	ArrayList chains = (ArrayList)struc.getModel(0);
	Chain tmpchn  = (Chain)chains.get(0) ;
	ArrayList aminos = tmpchn.getGroups("amino");
	
	AminoAcid a;
	AminoAcid b;
	AminoAcid c ;

	for ( int i=0; i < aminos.size(); i++){

	    b = (AminoAcid)aminos.get(i);
	    double phi =360.0;
	    double psi =360.0;

	    if ( i > 0) {
		a = (AminoAcid)aminos.get(i-1) ;
		try {
		    phi = Calc.getPhi(a,b);	   			   
		} catch (StructureException e){		    
		    e.printStackTrace();
		    phi = 360.0 ;
		}
	    }
	    if ( i < aminos.size()-1) {
		c = (AminoAcid)aminos.get(i+1) ;
		try {
		    psi = Calc.getPsi(b,c);
		}catch (StructureException e){
		    e.printStackTrace();
		    psi = 360.0 ;
		}
	    }

	    String str = b.getPDBCode() + " " + b.getPDBName() + ":"  ;
	    str += "\tphi: " + phi + "\tpsi: " + psi;
	    System.out.println(str);
</pre>
<hr>
</hr> 

 *
 *
 * @author Andreas Prlic
 * @since 1.4
 * @version %I% %G%
 */
public interface Structure {
	
    /** returns an identical copy of this structure .
     * @return an identical Structure object     
     */
    public Object clone();

    
    /**
     * String representation of object.
     */
    public String toString();

    /**
     *
     * set PDB code of structure .
     *
     * @param pdb_id  a String specifying the PDBCode
     * @see #getPDBCode
     *
     */
    public void setPDBCode (String pdb_id) ;

    /**
     *
     * get PDB code of structure.
     *
     * @return a String representing the PDBCode value
     * @see #setPDBCode
     */
    public String  getPDBCode () ;

    /** set biological name of Structure .
     *
     * @param name  a String specifying the biological name of the Structure
     * @see #getName
     */
    public void setName(String name);

    /** get biological name of Structure. 
     *
     * @return a String representing the biological name of the Structure
     * @see #setName
     */
    public String getName();

    /** set the Header data .
     *
     * @param h  a Map object specifying the header
     * @see #getHeader
     */
    public void setHeader(Map<String,Object> h) ;

    /** get Header data .
     *
     * @return a Map object representing the header value
     * @see #setHeader
     */
    public Map<String,Object> getHeader() ;

    /** 
       sets/gets an List of  Maps which corresponds to the CONECT lines in the PDB file:

       <pre>
       COLUMNS         DATA TYPE        FIELD           DEFINITION
       ---------------------------------------------------------------------------------
        1 -  6         Record name      "CONECT"
        7 - 11         Integer          serial          Atom serial number
       12 - 16         Integer          serial          Serial number of bonded atom
       17 - 21         Integer          serial          Serial number of bonded atom
       22 - 26         Integer          serial          Serial number of bonded atom
       27 - 31         Integer          serial          Serial number of bonded atom
       32 - 36         Integer          serial          Serial number of hydrogen bonded
       atom
       37 - 41         Integer          serial          Serial number of hydrogen bonded
       atom
       42 - 46         Integer          serial          Serial number of salt bridged
       atom
       47 - 51         Integer          serial          Serial number of hydrogen bonded
       atom
       52 - 56         Integer          serial          Serial number of hydrogen bonded
       atom
       57 - 61         Integer          serial          Serial number of salt bridged
       atom
       </pre>

       the HashMap for a single CONECT line contains the following fields:

       <li> atomserial (mandatory) : Atom serial number</li>
       <li> bond1 .. bond4 (optional): Serial number of bonded atom</li>
       <li> hydrogen1 .. hydrogen4 (optional):Serial number of hydrogen bonded atom</li>
       <li> salt1 .. salt2 (optional): Serial number of salt bridged atom</li>

       *
       * @param connections  a List object specifying the connections 
       * @see #getConnections
    */
    public void setConnections(List<Map<String,Integer>> connections);

    /**
     * Returns the connections value.
     * @return a List object representing the connections value
     * @see #setConnections
     */
    public List<Map<String,Integer>> getConnections();

    /** return number of Chains in this Structure.
     * @return an int representing the number of Chains in this Structure
     */
    public int size() ;

    /** return number of chains of model.	
    *
    * @param modelnr  an int specifying the number of the Model that should be used
    * @return an int representing the number of Chains in this Model
    */
    public int size(int modelnr);

    /** return number of models .
     * in this implementation also XRAY structures have "1 model", since
     * model is the container for the chains.
     * to test if a Structure is an NMR structure use @see isNMR ,
     * since this is based on the info in the PDB file header.
     *
     * @return an int representing the number of models in this Structure
     */
    public int nrModels() ;

    /** test if this structure is an nmr structure.
     *
     * @return true if this Structure has been resolved by NMR
     */
    public boolean isNmr() ;
    
    /** set NMR flag.
     *
     * @param nmr  true to declare that this Structure has been solved by NMR.
     */
    public void setNmr(boolean nmr);
    

    /** add a new model.
     *
     * @param model  a List object containing the Chains of the new Model
     */
    public void addModel(List<Chain> model);

    /** retrieve all Chains belonging to a model .
     * @see #getChains
     *
     * @param modelnr  an int
     * @return a List object containing the Chains of Model nr. modelnr

     */
    public List<Chain> getModel(int modelnr);

    /** retrieve all chains of a model.
     * @see #getModel
     *
     * @param modelnr  an int
     * @return a List object containing the Chains of Model nr. modelnr
     */
    public List<Chain> getChains(int modelnr);


    /** add a new chain.
     *
     * @param chain  a Chain object
     */
    public void addChain(Chain chain);

    /** add a new chain, if several models are available.
     *
     * @param chain    a Chain object
     * @param modelnr  an int specifying to which model the Chain should be added
     */
    public void addChain(Chain chain, int modelnr);

    /** retrieve a chain by it's position within the Structure .
     * 
     * @param pos  an int for the position in the List of Chains.
     * @return a Chain object
    */
    public Chain getChain(int pos);

    /** retrieve a chain by it's position within the Structure and model number.
     *
     * @param pos      an int
     * @param modelnr  an int
     * @return a Chain object
    */
    public Chain getChain(int pos, int modelnr);


    
    /** request a particular chain from a structure.
     * by default considers only the first model.
     * @param chainId the ID of a chain that should be returned
     * @return Chain the requested chain
     * @throws StructureException
     */
    public Chain findChain(String chainId)
    throws StructureException;
    
    
    /** check if a chain with the id chainId is contained in this structure.
     * 
     * @param chainId the name of the chain
     * @return true if a chain with the id (name) chainId is found
     */
    public boolean hasChain(String chainId);
    
    /** request a particular chain from a particular model
     * @param modelnr the number of the model to use
     * @param chainId the ID of a chain that should be returned
     * @return Chain the requested chain
     * @throws StructureException 
     */
    public Chain findChain(String chainId, int modelnr)
    throws StructureException;

    /** request a particular group from a structure.
    * by default considers only the first model in the structure.
    * @param chainId the ID of the chain to use
    * @param pdbResnum the PDB residue number of the requested group
    * @return Group the requested Group
    * @throws StructureException 
    * 
    */
    public  Group findGroup(String chainId, String pdbResnum)
    throws StructureException;
    
    /** request a particular group from a structure.
     * considers only model nr X. count starts with 0.
     * @param chainId the ID of the chain to use
     * @param pdbResnum the PDB residue number of the requested group
     * @param modelnr the number of the model to use
     * @return Group the requested Group
     * @throws StructureException  
     */
     public  Group findGroup(String chainId, String pdbResnum, int modelnr)
     throws StructureException;
     
    
     /** request a chain by it's PDB code
      * by default takes only the first model
      * 
      * @param chainId the chain identifier 
      * @return the Chain that matches the chainID
      * @throws StructureException 
      */
     public Chain getChainByPDB(String chainId)
         throws StructureException;
     
     /** request a chain by it's PDB code
      * by default takes only the first model
      * 
      * @param chainId the chain identifier
      * @param modelnr request a particular model; 
      * @return the Chain that matches the chainID in the model
      * @throws StructureException 
      */
     public Chain getChainByPDB(String chainId, int modelnr)
         throws StructureException;
     
     
    /** create a String that contains the contents of a PDB file .
     *
     * @return a String that looks like a PDB file
     */
    public String toPDB();
    
    
    public void setCompoundList(List<Compound>molList);
    public List<Compound> getCompoundList();
    public Compound getCompoundById(String molId);
}
