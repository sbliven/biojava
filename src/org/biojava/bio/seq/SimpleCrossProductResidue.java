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

package org.biojava.bio.seq;

import java.util.*;

/**
 * Concrete implementation of CrossProductResidue, as returned
 * by a SimpleCrossProductAlphabet and InfiniteCrossProductAlphabet.
 *
 * @author Thomas Down
 * @author Matthew Pocock
 */

class SimpleCrossProductResidue implements CrossProductResidue {
  private final List l;
  private final char symbol;

  SimpleCrossProductResidue(List l, char symbol) {
    this.l = l;
    this.symbol = symbol;
  }

  public List getResidues() {
    return l;
  }

  public String getName() {
    StringBuffer name = new StringBuffer("(");
    for (int i = 0; i < l.size(); ++i) {
      Residue r = (Residue) l.get(i);
      name.append(r.getName());
      if (i < l.size() - 1) {
        name.append(" ");
      }
    }
    name.append(")");
    return name.toString();
  }

  public char getSymbol() {
    return symbol;
  }

  public Annotation getAnnotation() {
    return Annotation.EMPTY_ANNOTATION;
  }
}

