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
 * A view of windows onto another ResidueList.
 * <P>
 * In practice, what this means is that you can view a DNA sequence as codons which
 * do not overlap.
 *
 * @author Matthew Pocock
 */
public class WindowedResidueList extends AbstractResidueList {
  /**
   * The source sequence that we will transliterate.
   */
  private final ResidueList source;
  
  /**
   * The alphabet of tokens we will return.
   */
  private final CrossProductAlphabet alpha;
  
  /**
   * The width of the window.
   */
  private final int width;

  /**
   * Retrieve the underlying ResidueList being viewed.
   *
   * @return the source ResidueList
   */
  public ResidueList getSource() {
    return source;
  }
  
  /**
   * Create a WindowedResidueList with the given window width.
   */
  public WindowedResidueList(ResidueList source, int width)
  throws IllegalArgumentException {
    if( (source.length() % width) != 0 ) {
      throw new IllegalArgumentException(
        "The source length must be divisible by the window width: " +
        source.length() + " % " + width + " = " + (source.length() % width)
      );
    }
    this.source = source;
    Alphabet a = source.alphabet();
    this.alpha = AlphabetManager.instance().getCrossProductAlphabet(
      Collections.nCopies(width, a)
    );
    this.width = width;
  }
  
  public Alphabet alphabet() {
    return alpha;
  }

  public int length() {
    return source.length() / width;
  }
  
  public Residue residueAt(int index)
  throws IndexOutOfBoundsException {
    if(index < 1 || index > length()) {
      throw new IndexOutOfBoundsException(
        "index must be within (1 .. " +
        length() + "), not " + index
      );
    }
    
    index = (index-1)*width + 1;
    
    try {
      return alpha.getResidue(source.subList(index, index+width-1).toList());
    } catch (IllegalAlphabetException iae) {
      throw new BioError(iae, "Alphabet changed underneath me");
    }
  }
}
