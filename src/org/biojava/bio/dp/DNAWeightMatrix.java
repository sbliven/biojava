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
import org.biojava.bio.seq.tools.DNATools;

public class DNAWeightMatrix implements WeightMatrix {
  private double [][] weights;

  public Alphabet alphabet() {
    return DNATools.getAlphabet();
  }

  public double getWeight(Residue res, int column)
         throws IllegalResidueException {
    return weights[DNATools.index(res)][column];
  }

  public void setWeight(Residue res, int column, double val)
         throws IllegalResidueException {
    weights[DNATools.index(res)][column] = val;
  }

  public int columns() {
    return weights[0].length;
  }

  public double score(ResidueList resList)
         throws IllegalResidueException {
    double score = 0;
    int cols = columns();

    for(int c = 0; c < cols; c++)
      score += getWeight(resList.residueAt(c+1), c);

    return score;
  }

  public DNAWeightMatrix(int cols) {
    this.weights = new double[4][cols];
  }
}
