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

package org.biojava.bio.structure.io;

import org.biojava.bio.*;

/**
 * An exception during the parsing of a PDB file
 *
 * @author Andreas Prlic, Thomas Down, Benjamin Schuster-Böckler
 */

public class PDBParseException extends BioException {
    public PDBParseException(String s) {
	super(s);
    }

    public PDBParseException (Throwable t, String s) {
	super(t, s);
    }

    public PDBParseException (Throwable t) {
	super(t);
    }
}
