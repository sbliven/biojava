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


package org.biojava.bio.dp;

import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

/**
 * Annotates a database with hits to a weight-matrix.
 *
 * @author Matthew Pocock
 */
public class WeightMatrixAnnotator extends AbstractAnnotator {
  private WeightMatrix matrix;
  private double prior;
  private double threshold;

  public boolean annotate(Sequence seq)
         throws IllegalSymbolException, BioException {
    if(!(seq instanceof MutableFeatureHolder)) {
      return false;
    }
    
    int cols = matrix.columns();
    Feature.Template template = new Feature.Template();
    template.source = "WeightMatrixAnnotator";
    template.type = "hit";
    for(int offset = 1;
        offset <= seq.length() - cols + 1;
        offset++) {
      double score = SingleDP.scoreWeightMatrix(matrix, seq, offset);
      double q = Math.exp(score) * prior;
      double pmd = q/(1.0+q);
      if(pmd >= threshold) {
        template.location = new RangeLocation(offset, offset+cols-1);
        SimpleAnnotation ann = new SimpleAnnotation();
        ann.setProperty("score", new Double(score));
        ann.setProperty("weightMatrix", matrix);
        ann.setProperty("p-value", new Double(pmd));
        seq.createFeature((MutableFeatureHolder) seq, template);
        return true;
      }
    }
    return false;
  }

  public WeightMatrixAnnotator(WeightMatrix wm, double prior, double threshold) {
    this.matrix = wm;
    this.prior = prior;
    this.threshold = threshold;
  }
}
