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

/* *
 * 
 * Interface to a protein structure 
 * Any kind of PDB parser / DB interface, etc. will return an object that implements this interface
 *
 */
public interface Structure {
	
    /**
     * String representation of object
     */
    public String toString();

    /**
     *
     * set PDB code of structure 
     */
    public void setPDBCode (String pdb_id) ;

    /**
     *
     * get PDB code of structure 
     */
    public String  getPDBCode () ;

    /** set biological name of Structure */
    public void setName(String name);

    /** get biological name of Structure */
    public String getName();

    /** set the Header data */
    public void setHeader(HashMap h) ;

    /** get Header data */
    public HashMap getHeader() ;

    /** CONECT data 
       sets/gets an ArrayList of  HashMaps which corresponds to the CONECT lines in the PDB file:

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
       <ul>
       <li>atomserial (mandatory) : Atom serial number
       <li>bond1 .. bond4 (optional): Serial number of bonded atom
       <li>hydrogen1 .. hydrogen4 (optional):Serial number of hydrogen bonded atom
       <li>salt1 .. salt2 (optional): Serial number of salt bridged atom
       </ul>
       
    */
    public void setConnections(ArrayList connections);
    public ArrayList getConnections();

    /** return number of Chains in file */
    public int size() ;

    /** return number of chains of model */
    public int size(int modelnr);

    /** return number of models 
     * in this implementation also XRAY structures have "1 model", since
     * model is the container for the chains.
     * to test if a Structure is an NMR structure use @see isNMR ,
     * since this is based on the info in the PDB file header.
     */
    public int nrModels() ;

    /** test if this structure is an nmr structure */
    public boolean isNmr() ;
    
    /* set NMR flag */
    public void setNmr(boolean nmr);
    

    /** add a new model */
    public void addModel(ArrayList model);

    /** retrieve all Chains belonging to a model */
    public Chain[] getModel(int modelnr);

    /** add a new chain */
    public void addChain(Chain chain);

    /** add a new chain, if several models are available*/
    public void addChain(Chain chain, int modelnr);

    /** retrieve a chain by it's position within the Structure */
    public Chain getChain(int pos);

    /** retrieve a chain by it's position within the Structure and model number*/
    public Chain getChain(int pos, int modelnr);

    /** retrieve all chains of a model*/
    public Chain[] getChains(int modelnr);

 

}
