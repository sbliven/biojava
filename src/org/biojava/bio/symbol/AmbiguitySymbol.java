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

import org.biojava.bio.*;

/**
 * An ambiguity symbol.
 * <P>
 * Within DNA or protein sequences, there are sometimes ambiguous regions, where
 * for whatever reason the exact sequence is not known. These are usualy
 * represented in text form as anbiguity codes (e.g. n for any DNA nucleotide).
 * While this is a compact representation for text, it is not very friendly to
 * programs - they need special insider knowledge of what non-standard symbols
 * mean. Luckily this is usualy provided by convention (IUPAC codes for DNA and
 * RNA). This interface provides programatic access to these special symbols,
 * and hopefuly is a natural abstraction for them.
 *
 * @author Matthew Pocock
 */
public interface AmbiguitySymbol extends Symbol {
  /**
   * The alphabet containing the symbols matched by this ambiguity symbol.
   * <P>
   * This alphabet contains all of, and only, the symbols matched by this
   * ambiguity symobl. For example, the ambiguity symbol representing the DNA
   * ambiguity code for W would contain the symbol for A and T from the DNA
   * alphabet.
   *
   * @return  the Alphabet of simple non-ambiguity symbols matched by this
   *          ambiguity symbol
   */
  Alphabet getMatchingAlphabet();
}
