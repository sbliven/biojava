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

import java.io.IOException;

import org.biojava.bio.structure.Structure;

/**
 * interface StructureIOFile extends the StructureIO interface
 * and adds a few File specific methods.
 * @author Andreas Prlic
 */
public interface StructureIOFile extends StructureIO {
    
    /** set path to file / connection string to db.
     * @param path  a String specifying the path value
     */
    public void setPath(String path) ;

    /** add a known File extension.
     * @param ext  a String ...
     */
    public void addExtension(String ext);

    /** open filename and returns
     * a Structure object.
     * @param filename  a String
     * @return a Structure object
     * @throws IOException ...
     */
    public Structure getStructure(String filename) throws IOException ;

    
}
