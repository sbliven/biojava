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
 * Map between Symbols and index numbers.
 * <P>
 * The Symbols will all come from a single finite alphabet. The indecies will
 * range from 0 to getAlphabet().size()-1 inclusive with each symbol having a
 * unique index. The resulting table can be used to look up array indecies by
 * symbol, which in many cases will be more efficient than performing a Map
 * operation on, for example, a HashMap.
 * <P>
 * An index should do whatever is necisary to stay synchronized with its
 * alphabet. It may chose to modify the index table with the alphabet, or to
 * veto all changes to the alphabet that would invalidate the indexing
 * scheim.
 *
 * @author Thomas Down
 * @author Matthew pocock
 * @since 1.1
 */

public interface AlphabetIndex {
  /**
   * Retrieve the alphabet that this indexes.
   *
   * @return the FiniteAlphabet that is indexed by this object
   */
  FiniteAlphabet getAlphabet();

  /**
   * Return the unique index for a symbol.
   *
   * @param s  the Symbol to index
   * @return   the unique index for the symbol
   * @throws   IllegalSymbolException if s is not a member of the indexed
   *           alphabet, or if the indexer only indexes the AttomicSymbols
   *           within an alphabet and s was not attomic.
   */
  int indexForSymbol(Symbol s) throws IllegalSymbolException;
  
  /**
   * Retrieve the symbol for an index.
   *
   * @param i  the index of the symbol
   * @return   the symbol at that index
   * @throws   IndexOutOfBoundsException if i is negative or >=
   *           getAlphabet().size()
   */
  Symbol symbolForIndex(int i) throws IndexOutOfBoundsException;
}
