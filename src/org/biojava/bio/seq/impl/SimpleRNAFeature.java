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

import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.genomic.*;

/**
 * A no-frills implementation of RNAFeature.
 *
 * @author Matthew Pocock
 * @since 1.1
 */
public class SimpleRNAFeature extends SimpleStrandedFeature implements RNAFeature {
  protected Sequence rna;
    
  public Sequence getRNA() {
    return rna;
  }
  
  public SimpleRNAFeature(
    Sequence sourceSeq,
    FeatureHolder parent,
    RNAFeature.Template template
  ) throws IllegalAlphabetException {
    super(sourceSeq, parent, template);
    this.rna = rna;
  }
  
  public Feature.Template makeTemplate() {
    RNAFeature.Template rft = new RNAFeature.Template();
    fillTemplate(rft);
    return rft;
  }
  
  protected void fillTemplate(RNAFeature.Template ft) {
    super.fillTemplate(ft);
    ft.rna = this.getRNA();
  }
}
