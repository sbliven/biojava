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

package org.biojava.bio.seq.tools;

import java.util.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;

/**
 * ResidueList which gives a view of the reverse complement of
 * another ResidueList.  The parent ResidueList must use
 * either the DNA or DNA-AMBIGUITY alphabet, as provided
 * by the DNATools utility class.
 *
 * @author Thomas Down
 */

public class ComplementResidueList extends AbstractResidueList {
    private ResidueList parent;

    public ComplementResidueList(ResidueList p) throws IllegalAlphabetException {
	Alphabet a = p.alphabet();
	if (a != DNATools.getAlphabet() && a != DNATools.getAmbiguity())
	    throw new IllegalAlphabetException("Only DNA can be complemented.");
	parent = p;
    }

    public Alphabet alphabet() {
	return parent.alphabet();
    }

    public int length() {
	return parent.length();
    }

    public Residue residueAt(int pos) {
	try {
	    return DNATools.complement(parent.residueAt(parent.length() - pos + 1));
	} catch (IllegalResidueException ex) {
	    throw new BioError(ex);
	}
    }
}
