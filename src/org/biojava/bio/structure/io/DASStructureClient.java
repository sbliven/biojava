
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
 * Created on 05.05.2004
 * @author Andreas Prlic
 *
 */

package org.biojava.bio.structure.io;

// the biojava-structure stuff
import org.biojava.bio.structure.*;
import org.biojava.bio.structure.io.*;
       
// das communication part
import org.biojava.bio.program.das.dasstructure.* ;

import java.io.*;

/**
 * A DAS client that connects to a DAS structure service and
 * returns a Biojava structure class.
 * @author Andreas Prlic
 * @since 1.4
 */
public class DASStructureClient implements StructureIO { 

    String pdb_code              ;
    String serverurl             ;
    StructureImpl structure      ;
    
    /**
     * Constructs a DASStructureClient object.
     */

    public DASStructureClient() {
	pdb_code = null ;
	serverurl = "http://127.0.0.1:8080/dazzle/mystruc/structure?query=";
    }

    /**
     * Constructs a DASStructureClient object.
     *
     * @param url  a String ...
     */
    public DASStructureClient(String url) {
	pdb_code = null ;
	serverurl = url;
    }

    

    // the interfaced procedures: //
    
    /** 
     * set the PDB code of a structure.
     * @see #getId
     */
    public void setId(String id) {
	pdb_code = id ;   
    }

    /** 
     * get the PDB code of a structure.
     *
     * @return a String representing the id value
     * @see #setId
     */
    public String getId() {
	return pdb_code ;  
    }
    

    /** 
     * Connect to a DAS-structure service and retreive data.     
     *
     * @param pdb_code  a String, representing a PDB code e.g. 5pti
     * @return a Structure object
     * @throws IOException ...     
     */
    public Structure getStructure(String pdb_code) 
	throws IOException
    {
	setId(pdb_code);
	return getStructure();
    }


    /** 
     * if pdb code is set (setId):
     * connect to a DAS-structure service and retreive data.  
     *
     * @return the Structure object
     * @throws IOException ...
     */
 
    public Structure getStructure()
	throws IOException 
    {
	
	if (pdb_code == null) {
	    throw new IOException ("no pdb code found - call setId() first!");
	}

	/* now connect to DAS server */

	DASStructureCall dasstructure = new DASStructureCall(serverurl);

	Structure structure = dasstructure.getStructure(pdb_code);

	return structure;
    }


   
}
