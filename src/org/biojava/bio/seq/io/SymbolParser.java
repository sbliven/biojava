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


package org.biojava.bio.seq.io;

import org.biojava.bio.symbol.*;

/**
 * These objects are responsible for converting strings into Symbols and SymbolLists.
 * It is possible to parse either single tokens or block of character data.  For 1.1,
 * there is a new method for parsing character streams, and it is recommended that
 * this should be used instead of either the parse or parseToken methods.
 *
 * @author Matthew Pocock
 * @author Thomas Down
 */

public interface SymbolParser {
  /**
   * The alphabet that all Symbols produced will belong to.
   *
   * @return  the Alphabet
   */
  Alphabet getAlphabet();
  
  /**
   * Parse an entire string into a SymbolList.
   * <P>
   * The SymbolList produced will have the same Alphabet as this SymbolParser.
   *
   * @param seq the String to parse
   * @return  the SymbolList containing the parsed value of the String
   * @throws  IllegalSymbolException if any part of the String can not be parsed
   */
  SymbolList parse(String seq) throws IllegalSymbolException;
  
  /**
   * Returns the symbol for a single token.
   * <P>
   * The Symbol will be a member of the alphabet. If the token is not recognized
   * as mapping to a symbol, an exception will be thrown.
   *
   * @param token the token to retrieve a Symbol for
   * @return the Symbol for that token
   * @throws IllegalSymbolException if there is no Symbol for the token
   */
  Symbol parseToken(String token) throws IllegalSymbolException;

    /**
     * Return an object which can parse an arbitrary character stream into
     * symbols.
     *
     * @param listener The listener which gets notified of parsed symbols.
     * @since 1.1
     */

    public StreamParser parseStream(SeqIOListener listener);
}
