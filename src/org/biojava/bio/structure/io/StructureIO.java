

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

/**
 * Defines the interface how to access Structure (- PDB file) input,output readers, writers.
 *
 * @author Andreas Prlic
 * @version %I% %G%
 */
public interface StructureIO {
    
    /** set PDB id. 
     * @param id  a String specifying the id value (PDB code)
     * @see #getId
     */
    public void setId(String id) ;

    /** get PDB id. 
     * @return PDB code
     * @see #setId
     */
    public String getId() ;

    /** parse the file/ connect to DB, etc. 
     * requires id to be set before ...
     * @return a Structure object
     * @throws IOException ...
     */
    public Structure getStructure() throws IOException;

    /** open filename and returns
     * a Structure object.
     * @param filename  a String
     * @return a Structure object
     * @throws IOException ...
     */
    public Structure getStructure(String filename) throws IOException ;
    
}
  
