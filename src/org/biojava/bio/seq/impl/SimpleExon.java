/*                    BioJava development code
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

package org.biojava.bio.seq.impl;

import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.genomic.*;

/**
 * A no-frills implementation of Exon.
 *
 * @author Matthew Pocock
 * @since 1.1
 */
public class SimpleExon extends SimpleRNAFeature implements Exon {
  public Sequence getRNA() {
    if(rna == null) {
      SequenceFactory sf = new SimpleSequenceFactory();
      SymbolList asRNA;
      try {
        asRNA = RNATools.transcribe(getSymbols());
      } catch (IllegalAlphabetException iae) {
        throw new BioError(iae, "Assertion Failure: Could not view sequence as RNA");
      }
      rna = sf.createSequence(
        asRNA,
        getSequence().getURN() + "/" + getType() + "/" + getLocation(),
        getType() + "/" + getLocation(),
        Annotation.EMPTY_ANNOTATION
      );
    }
    return rna;
  }
  
  public SimpleExon(
    Sequence sourceSeq,
    FeatureHolder parent,
    Exon.Template template
  ) throws IllegalAlphabetException {
    super(sourceSeq, parent, template);
  }
  
  public Feature.Template makeTemplate() {
    Exon.Template et = new Exon.Template();
    fillTemplate(et);
    return et;
  }
}
