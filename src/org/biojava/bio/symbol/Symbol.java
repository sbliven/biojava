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
 * A single symbol.
 * <P>
 * This is the atomic unit of a SymbolList, or a sequence. It allows
 * for fine-grain fly-weighting, so that there can be one instance
 * of each symbol that is referenced multiple times.
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
}
