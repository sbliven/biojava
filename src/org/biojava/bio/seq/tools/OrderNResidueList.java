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

package org.biojava.bio.seq.tools;

import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;

/**
 * An n-th order view of another ResidueList.
 * <P>
 * In practice, what this means is that you can view a DNA sequence into an
 * overlapping dinucleotide sequence without having to do any work yourself.
 */
public class OrderNResidueList extends AbstractResidueList {
  /**
   * The source sequence that we will transliterate.
   */
  private final ResidueList source;
  
  /**
   * The alphabet for each overlapping tuple.
   */
  private final CrossProductAlphabet alpha;
  
  /**
   * The view order.
   */
  private final int order;

  /**
   * Retrieve the underlying ResidueList being viewed.
   *
   * @return the source ResidueList
   */
  public ResidueList getSource() {
    return source;
  }
  
  /**
   * Create an order n OrderNResidueList from source.
   */
  public OrderNResidueList(ResidueList source, int order)
  throws IllegalAlphabetException {
    this.source = source;
    Alphabet a = source.alphabet();
    this.alpha = CrossProductAlphabetFactory.createAlphabet(
      Collections.nCopies(order, a)
    );
    this.order = order;
  }
  
  public Alphabet alphabet() {
    return alpha;
  }

  public int length() {
    return source.length() - order + 1;
  }
  
  public Residue residueAt(int index)
  throws IndexOutOfBoundsException {
    if(index < 1 || index > length()) {
      throw new IndexOutOfBoundsException(
        "index must be within (1 .. " +
        length() + "), not " + index
      );
    }
    
    try {
      return alpha.getResidue(source.subList(index, index+order-1).toList());
    } catch (IllegalAlphabetException iae) {
      throw new BioError(iae, "Alphabet changed underneath me");
    }
  }
}
