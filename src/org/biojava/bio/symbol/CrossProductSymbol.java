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


package org.biojava.bio.symbol;

import java.util.*;

/**
 * Symbol in a CrossProductAlphabet.
 * <P>
 * The getName method should return a string that is bracketed, and seperates
 * each contained Symbol with a comma.
 *
 * @author Thomas Down
 */

public interface CrossProductSymbol extends Symbol {
    /**
     * Return an immutable ordered list of symbols which are
     * represented by this CrossProductSymbol.
     */

    public List getSymbols();
}

