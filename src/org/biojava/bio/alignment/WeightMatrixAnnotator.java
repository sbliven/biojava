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


package org.biojava.bio.alignment;

import java.util.*;

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
         throws IllegalResidueException {
    if(!(seq instanceof MutableFeatureHolder)) {
      return false;
    }
    
    int cols = matrix.columns();
    for(int offset = 1;
        offset <= seq.length() - cols + 1;
        offset++) {
      double score = DP.score(matrix, seq.subList(offset, offset+cols-1));
      double q = Math.exp(score) * prior;
      double pmd = q/(1.0+q);
      if(pmd >= threshold) {
        Feature hit =
         seq.createFeature((MutableFeatureHolder) seq,
                           new RangeLocation(offset, offset+cols-1),
                           "WeightMatrix", "WeightMatrix",
                           null);
        hit.getAnnotation().setProperty("score", new Double(score));
        hit.getAnnotation().setProperty("weightMatrix", matrix);
        hit.getAnnotation().setProperty("p-value", new Double(pmd));
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
