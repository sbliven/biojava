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

/**
 * A symbol that is indivisible.
 * <P>
 * Atomic symbols are the real underlying elements that a SymbolList is meant
 * to be composed of. DNA nucleotides are atomic, as are amino-acids. The
 * getMatches() method should return an alphabet containing just itself.
 *
 * @author Matthew Pocock
 */
public interface AtomicSymbol extends Symbol {
}
