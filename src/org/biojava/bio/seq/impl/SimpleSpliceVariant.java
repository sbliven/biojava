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

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.genomic.*;

/**
 * A no-frills implementation of SpliceVariant.
 *
 * @author Matthew Pocock
 * @since 1.1
 */
public class SimpleSpliceVariant
extends SimpleRNAFeature implements SpliceVariant {
  public Sequence getRNA() {
    if(rna == null) {
      FeatureHolder fh = filter(new FeatureFilter.ByClass(Exon.class), false);
      Exon[] exons = new Exon[fh.countFeatures()];
      int i = 0;
      for(Iterator fi = fh.features(); fi.hasNext(); ) {
        exons[i++] = (Exon) fi.next();
      }
      Arrays.sort(exons, new Comparator() {
        public int compare(Object a, Object b) {
          Location la = (Location) a;
          Location lb = (Location) b;
          
          return la.getMin() - lb.getMin();
        }
        
        public boolean equals(Object o) {
          return o == this;
        }
      });
      
      String name = getType() + "/" + getLocation();
      String uri = getSequence().getURN() + "/" + name;
      SimpleAssembly sa = new SimpleAssembly(name, uri);
      
      ComponentFeature.Template cft = new ComponentFeature.Template();
      cft.annotation = Annotation.EMPTY_ANNOTATION;
      cft.strand = StrandedFeature.POSITIVE;
      
      int last = 0;
      for(i = 0; i < exons.length; i++) {
        Sequence rna = exons[i].getRNA();
        int length = rna.length();
        cft.componentSequence = rna;
        cft.componentLocation = new RangeLocation(1, length);
        cft.location = new RangeLocation(last+1, last+length);
        last += length;
        try {
          sa.createFeature(cft);
        } catch (BioException be) {
          throw new BioError(
            be,
            "Assertion Failure: Could not splice exons together"
          );
        } catch (ChangeVetoException cve) {
          throw new BioError(
            cve,
            "Assertion Failure: Spliced exons could not be modified"
          );
        }
      }
      
      rna = sa;
    }
    return rna;
  }

  public SimpleSpliceVariant(
    Sequence sourceSeq,
    FeatureHolder parent,
    SpliceVariant.Template template
  ) throws IllegalAlphabetException {
    super(sourceSeq, parent, template);
  }
  
  public Feature.Template makeTemplate() {
    SpliceVariant.Template svt = new SpliceVariant.Template();
    fillTemplate(svt);
    return svt;
  }
}
