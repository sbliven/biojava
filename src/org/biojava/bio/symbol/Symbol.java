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

import org.biojava.bio.*;

/**
 * A single symbol.
 * <P>
 * This is the atomic unit of a SymbolList, or a sequence. It allows
 * for fine-grain fly-weighting, so that there can be one instance
 * of each symbol that is referenced multiple times.
 * <P>
 * Symbols from finite alphabets are identifiable using the == operator.
 * Symbols from infinite alphabets may have some specific API to test for
 * equality, but should realy over-ride the equals() method.
 * <P>
 * Some symbols represent a single token in the sequence. For example, there is
 * a Symbol instance for adenine in DNA, and another one for cytosine.
 * Symbols can potentialy represent sets of Symbols. For example, n represents
 * any DNA Symbol, and X any protein Symbol. Gap represents the knowledge that
 * there is no Symbol. In addition, some symbols represent ordered lists of
 * other Symbols. For example, the codon agt can be represented by a single
 * Symbol from the Alphabet DNAxDNAxDNA. Symbols can represent ambiguity over
 * these complex symbols. For example, you could construct a Symbol instance
 * that represents the codons atn. This matches the codons {ata, att, atg, atc}.
 * It is also possible to build a Symbol instance that represents all stop
 * codons {taa, tag, tga}, which can not be easily represented in terms of a
 * single ambiguous n'tuple (In this case, the codons could be represented by
 * {ta[ag], tga} or {t[ag]a, tag} but not by t[ag][ag] as this would also
 * contain tgg).
 * <P>
 * There are three Symbol interfaces. Symbol is the most generic. It has the
 * methods getToken and getName so that the Symbol can be textualy represented.
 * In addition, it defines getMatches that returns an Alphabet over all the
 * AtomicSymbol instances that match the Symbol (N would return an Alphabet
 * containing {A, G, C, T}, and Gap would return {}). In addition, it defines
 * getBasies, which returns a Set of BasisSymbol instances. These should be a
 * minimal set of (possibly) ambiguous Symbols that contain every Symbol in
 * getMatches. For example, in the atn case, getBasies would return a Set
 * containing only atn, and getMatches would return the four matching
 * Atomic Symbols. The stop-codon ambiguity symbol would return a Set like
 * {ta[ag], tga} that represents all of the matching codons as compactly as
 * possible.
 * <P>
 * Invoking getBasies for a BasisSymbol will always return a Set containing just
 * that Symbol. BasisSymbol adds the getSymbols method that returns the List of
 * BasisSymbol instances that are concatonated together to make that Symbol.
 * For example, the getSymbols method for the BasisSymbol instance for ant would
 * return the List [a, n, t].
 * <P>
 * AtomicSymbol instances specialize BasisSymbol by guaranteeing that getMatches
 * returns a set containing only that instance. That is, they are indivisable.
 * The DNA nucleotides are instances of AtomicSymbol, as are individual codons.
 * The stop codon {tag} will have a getMatches method that returns {tag},
 * a getBasies method that also returns {tag} and a getSymbols method that returns
 * the List [t, a, g].
 *
 * @author Matthew Pocock
 */
public interface Symbol extends Annotatable {
  /**
   * The token for the symbol.
   *
   * @return  the token
   */
  char getToken();

  /**
   * The long name for the symbol.
   *
   * @return  the long name
   */
  String getName();
  
  /**
   * The alphabet containing the symbols matched by this ambiguity symbol.
   * <P>
   * This alphabet contains all of, and only, the symbols matched by this
   * symbol. For example, the symbol representing the DNA
   * ambiguity code for W would contain the symbol for A and T from the DNA
   * alphabet.
   *
   * @return  the Alphabet of symbols matched by this
   *          symbol
   */
  Alphabet getMatches();
  
  /**
   * The set of (possibly ambiguous) BasisSymbols that allow the entire set of
   * symbols in getMatches to be spanned.
   *
   * @return a Set of BasisSymbol instances
   */
  Set getBasies();
}
