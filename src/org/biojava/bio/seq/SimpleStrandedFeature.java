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
import org.biojava.bio.seq.tools.*;

/**
 * A no-frills implementation of StrandedFeature.
 *
 * @author Matthew Pocock
 */
public class SimpleStrandedFeature
extends SimpleFeature
implements StrandedFeature {
  /**
   * Register SimpleStrandedFeature with SimpleSequence as being good for
   * StrandedFeature.Template objects.
   */
  static {
    try {
      SimpleSequence.addFeatureImplementation(
        new SimpleSequence.TemplateFeature(
          StrandedFeature.Template.class, SimpleStrandedFeature.class
        )
      );
    } catch (NoSuchMethodException msme) {
      throw new org.biojava.bio.BioError(msme, "Could not find apropreate constructor");
    }
  }

  private int strand;
    
  public int getStrand() {
    return strand;
  }
  
  public ResidueList getResidues() {
    ResidueList resList = super.getResidues();
    if(getStrand() == NEGATIVE) {
      try {
        resList = new ComplementResidueList(resList);
      } catch (IllegalAlphabetException iae) {
        throw new BioError(
          iae,
          "Could not retrieve residues for feature as " +
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
    if(strand != POSITIVE && strand != NEGATIVE) {
      throw new IllegalArgumentException(
        "strand was not POSITIVE or NEGATIVE but " + strand
      );
    }
    this.strand = strand;
    if(!ComplementResidueList.isComplementable(sourceSeq.alphabet())) {
      throw new IllegalAlphabetException (
        "Can not create a stranded feature within a sequence of type " +
        sourceSeq.alphabet().getName()
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
