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

package org.biojava.bio.seq;

import java.util.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;

/**
 * SymbolList which gives a view of the complement of
 * another SymbolList (not the reverse-complement).
 * <P>
 * The parent SymbolList must use
 * either the DNA or DNA-AMBIGUITY alphabet, as provided
 * by the DNATools utility class.
 *
 * @author Thomas Down
 */

class ComplementSymbolList extends AbstractSymbolList {
    private SymbolList parent;

    public ComplementSymbolList(SymbolList p) throws IllegalAlphabetException {
	Alphabet a = p.getAlphabet();
	if (!isComplementable(a))
	    throw new IllegalAlphabetException("Only DNA can be complemented.");
	parent = p;
    }

    public Alphabet getAlphabet() {
	return parent.getAlphabet();
    }

    public int length() {
	return parent.length();
    }

    public Symbol symbolAt(int pos) {
      try {
        return DNATools.complement(parent.symbolAt(pos));
      } catch (IllegalSymbolException ex) {
        throw new BioError(ex);
      }
    }
    
    public static boolean isComplementable(Alphabet alpha) {
      return alpha == DNATools.getDNA();
    }
}
