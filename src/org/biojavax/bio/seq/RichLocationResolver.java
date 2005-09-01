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

package org.biojavax.bio.seq;

import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.SymbolList;
import org.biojavax.CrossRef;

/**
 * This interface returns symbols for a given location.
 * @author Richard Holland
 */
public interface RichLocationResolver {
    
    /**
     * Given a cross reference, return the corresponding symbol list.
     * @param cr the cross reference to look up.
     * @param a the alphabet it needs symbols in.
     * @return the symbol list matching it. If none, return an
     * infintely-ambiguous symbol list rather than null.
     */
    public SymbolList getRemoteSymbolList(CrossRef cr, Alphabet a);
    
}
