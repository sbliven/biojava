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

public class SimpleWeightMatrix implements WeightMatrix {
  private int cols;
  private Map weights;
  private Alphabet alpha;

  {
    weights = new HashMap();
  }

  public Alphabet alphabet() {
    return alpha;
  }

  public double getWeight(Residue res, int column)
         throws IllegalResidueException {
    alphabet().validate(res);
    double [] w = (double []) weights.get(res);
    return w[column];
  }

  public void setWeight(Residue res, int column, double val)
         throws IllegalResidueException {
    alphabet().validate(res);
    double [] w = (double []) weights.get(res);
    w[column] = val;
  }

  public int columns() {
    return cols;
  }

  public SimpleWeightMatrix(Alphabet alpha, int cols) {
    this.alpha = alpha;
    this.cols = cols;
    ResidueList res = alpha.residues();
    for(int i = 0; i < res.length(); i++) {
      weights.put(res.residueAt(i+1), new double[cols]);
    }
  }
}
