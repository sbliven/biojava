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

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;

/**
 * A no-frills implementation of StrandedFeature.
 *
 * @author Matthew Pocock
 */
class SimpleStrandedFeature extends SimpleFeature implements StrandedFeature {
  private int strand;
    
  public int getStrand() {
    return strand;
  }
  
  public SymbolList getSymbols() {
    SymbolList resList = super.getSymbols();
    if(getStrand() == NEGATIVE) {
      try {
        resList = new ComplementSymbolList(resList);
      } catch (IllegalAlphabetException iae) {
        throw new BioError(
          iae,
          "Could not retrieve symbols for feature as " +
          "the alphabet can not be complemented."
        );
      }
    }
    return resList;
  }
  
  public SimpleStrandedFeature(Sequence sourceSeq,
                               StrandedFeature.Template template)
  throws IllegalArgumentException, IllegalAlphabetException {
    super(sourceSeq, template);
    if(template.strand != POSITIVE && template.strand != NEGATIVE) {
      throw new IllegalArgumentException(
        "strand was not POSITIVE or NEGATIVE but " + template.strand
      );
    }
    this.strand = template.strand;
    if(!ComplementSymbolList.isComplementable(sourceSeq.getAlphabet())) {
      throw new IllegalAlphabetException (
        "Can not create a stranded feature within a sequence of type " +
        sourceSeq.getAlphabet().getName()
      );
    }
  }
  
  public String toString() {
    String pm;
    if(getStrand() == POSITIVE) {
      pm = "+";
    } else {
      pm = "-";
    }
    return super.toString() + " " + pm;
  }
}
