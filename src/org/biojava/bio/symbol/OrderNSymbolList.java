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
import java.io.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;

/**
 * An n-th order view of another SymbolList.
 * <P>
 * In practice, what this means is that you can view a DNA sequence into an
 * overlapping dinucleotide sequence without having to do any work yourself.
 *
 * @author Matthew Pocock
 */

class OrderNSymbolList extends AbstractSymbolList implements Serializable {
  /**
   * The source sequence that we will transliterate.
   */
  private final SymbolList source;
  
  /**
   * The alphabet for each overlapping tuple.
   */
  private final Alphabet alpha;
  
  /**
   * The view order.
   */
  private final int order;

  /**
   * Retrieve the underlying SymbolList being viewed.
   *
   * @return the source SymbolList
   */
  public SymbolList getSource() {
    return source;
  }
  
  /**
   * Create an order n OrderNSymbolList from source.
   */
  public OrderNSymbolList(SymbolList source, int order)
  throws IllegalAlphabetException {
    this.source = source;
    Alphabet a = source.getAlphabet();
    this.alpha = AlphabetManager.getCrossProductAlphabet(
      Collections.nCopies(order, a)
    );
    this.order = order;
  }
  
  public Alphabet getAlphabet() {
    return alpha;
  }

  public int length() {
    return source.length() - order + 1;
  }
  
  public Symbol symbolAt(int index)
  throws IndexOutOfBoundsException {
    if(index < 1 || index > length()) {
      throw new IndexOutOfBoundsException(
        "index must be within (1 .. " +
        length() + "), not " + index
      );
    }
    
    try {
      // changed to this form to avoid constructing the sub-list objects
      Symbol [] syms = new Symbol[order];
      for(int i = 0; i < order; i++) {
        syms[i] = source.symbolAt(index + i);
      }
      return alpha.getSymbol(Arrays.asList(syms));
    } catch (IllegalSymbolException iae) {
      throw new BioError(iae, "Alphabet changed underneath me");
    }
  }
}
