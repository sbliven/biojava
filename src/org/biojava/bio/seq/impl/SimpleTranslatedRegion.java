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

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.genomic.*;

/**
 * This is a feature that sits within an RNA sequence, and represents a region
 * that is translated into protein. All sequence before this is 5'utr, and all
 * sequence after is 3'utr.
 *
 * @author Matthew Pocock
 * @since 1.1
 */
public class SimpleTranslatedRegion
extends SimpleFeature implements TranslatedRegion {
  protected Sequence translation;
  
  public Sequence getTranslation() {
    if(translation == null) {
      SequenceFactory sf = new SimpleSequenceFactory();
      SymbolList asProtein;
      try {
        asProtein = RNATools.translate(getSymbols());
      } catch (IllegalAlphabetException iae) {
        throw new BioError(
          iae,
          "Assertion Failure: Could not translate RNA into Protein"
        );
      }
      translation = sf.createSequence(
        asProtein,
        getSequence().getURN() + "/" + getType() + "/" + getLocation(),
        getType() + "/" + getLocation(),
        Annotation.EMPTY_ANNOTATION
      );        
    }
    return translation;
  }
  
  public SimpleTranslatedRegion(
    Sequence sourceSeq,
    FeatureHolder parent,
    TranslatedRegion.Template template
  ) {
    super(sourceSeq, parent, template);
    this.translation = template.translation;
  }
  
  public Feature.Template makeTemplate() {
    TranslatedRegion.Template trt = new TranslatedRegion.Template();
    fillTemplate(trt);
    return trt;
  }
  
  protected void fillTempalte(TranslatedRegion.Template ft) {
    super.fillTemplate(ft);
    ft.translation = getTranslation();
  }
}
