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
 */
public class WeightMatrixAnnotator implements Annotator {
  private WeightMatrix matrix;
  private double prior;
  private double threshold;

  public SequenceDB annotate(SequenceDB sdb, FeatureFactory ffact)
        throws IllegalResidueException, java.io.IOException {
    HashSequenceDB hitDB = new HashSequenceDB(null);

    for(Iterator i = sdb.ids().iterator(); i.hasNext(); ) {
      try {
        String id = (String) i.next();
        Sequence seq = sdb.getSequence(id);
        if(annotate(seq, ffact))
          hitDB.addSequence(id, seq);
      } catch (SeqException se){
      }
    }

    return hitDB;
  }

  public boolean annotate(Sequence seq, FeatureFactory ffact)
         throws IllegalResidueException {
    int cols = matrix.columns();
    System.out.println("Seq length = " + seq.length());
    System.out.println("wm cols = " + cols);
    System.out.println("max index = " + (seq.length() - cols + 1));
    for(int offset = 1;
        offset <= seq.length() - cols + 1;
        offset++) {
      System.out.println("Trying " + offset);
      double score = DP.score(matrix, seq.subList(offset, offset+cols-1));
      double q = Math.exp(score) * prior;
      double pmd = q/(1.0+q);
      if(pmd >= threshold) {
        Feature hit =
         ffact.createFeature(new RangeLocation(offset, offset+cols-1),
                            "WeightMatrix", "WeightMatrix",
                            seq, seq, null);
        System.out.println("Hit at " + offset + " to " + (offset+cols-1));
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
