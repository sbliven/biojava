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
 * An reverse view of another ResidueList.
 *
 * @author Matthew Pocock
 */
public class ReverseResidueList extends AbstractResidueList {
  /**
   * The source sequence that we will transliterate.
   */
  private final ResidueList source;
  /**
   * Retrieve the underlying ResidueList being viewed.
   *
   * @return the source ResidueList
   */
  public ResidueList getSource() {
    return source;
  }
  
  /**
   * Create a reverse view of source.
   */
  public ReverseResidueList(ResidueList source) {
    this.source = source;
  }

  public Alphabet alphabet() {
    return source.alphabet();
  }

  public int length() {
    return source.length();
  }
  
  public Residue residueAt(int index)
  throws IndexOutOfBoundsException {
    return source.residueAt(length() - index + 1);
  }
}
