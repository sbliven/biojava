/*
 * BioJava development code
 * 
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 * 
 * http://www.gnu.org/copyleft/lesser.html
 * 
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 * 
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 * 
 * http://www.biojava.org
 *
 */

package org.biojava.bio.symbol;

import java.util.*;
import java.lang.reflect.*;
import java.io.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.io.*;

/**  
 * Symbol list which just consists of 'N' symbols.
 *
 * @author Thomas Down
 * @since 1.2
 */

public class DummySymbolList extends AbstractSymbolList implements Serializable {
    private final Symbol sym;
    private final FiniteAlphabet alpha;
    private final int length;

    public DummySymbolList(FiniteAlphabet alpha, int length) {
	super();
	this.alpha = alpha;
	this.length = length;
	sym = AlphabetManager.getAllAmbiguitySymbol(alpha);
    }

    public Alphabet getAlphabet() {
	return alpha;
    }

    public int length() {
	return length;
    }

    public Symbol symbolAt(int i) {
	return sym;
    }
}
    
