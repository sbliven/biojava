

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
package org.biojava.bio.structure.io;

import org.biojava.bio.structure.Structure;
import java.io.IOException ;


public interface StructureIO {
    
    /* set PDB id */
    public void setId(String id) ;

    /* get PDB id */
    public String getId() ;

    /* parse the file/ connect to DB, etc. 
     * requires id to be set before ...
     */
    public Structure getStructure() throws IOException;

    /** open filename (does not support compressed files, yet...) and returns
     * a PDBStructure object 
     */
    public Structure getStructure(String filename) throws IOException ;
    
}
  
