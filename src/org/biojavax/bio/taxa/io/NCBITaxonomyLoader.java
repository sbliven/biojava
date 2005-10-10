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
 */

package org.biojavax.bio.taxa.io;

import java.io.IOException;
import java.util.Set;
import org.biojava.bio.seq.io.ParseException;

/**
 * Implementors are able to load taxonomy files and generate sets of NCBITaxon objects
 * that represent them. Taxon objects should be generated using RichObjectFactory.
 * @author Richard Holland
 */
public interface NCBITaxonomyLoader {
    
    /**
     * Runs the parser. It is intended that the constructor for the
     * implementation of this interface should be passed any necessary
     * parameters to enable the parsing to take place, as it could consist
     * of several files. Results should be loaded via RichObjectFactory to make
     * them available across the board.
     * NOTE: You better have a whole load of memory available, unless you are
     * using a memory-efficient RichObjectBuilder with RichObjectFactory.
     * @throws IOException in case of IO problems.
     * @throws ParseException if the files were unparseable.
     */
    public void parseTaxonomyFile() throws IOException, ParseException;
   
}
