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
  private ResidueList source;
  
  private CrossProductAlphabet alpha;
  
  private int order;

  /**
   * Create an order n OrderNResidueList from source.
   */
  public OrderNResidueList(ResidueList source, int order) {
    this.source = source;
    this.alpha = new SimpleCrossProductAlphabet(
      Collections.nCopies(order, source.alphabet())
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
