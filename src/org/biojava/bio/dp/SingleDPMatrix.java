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

import org.biojava.bio.symbol.*;

class SingleDPMatrix implements DPMatrix {
  private final State [] states;
  private final MarkovModel model;
  private final SymbolList [] resList;
  final double [][] scores; // [symbol][state]
  double score;
 
  public State [] states() {
    return states;
  }
  
  public MarkovModel model() {
    return model;
  }
  
  public SymbolList [] resList() {
    return resList;
  }
  
  public double getScore() {
    return score;
  }
  
  public double getCell(int [] index)
  throws IndexOutOfBoundsException {
    if(index.length != 2) {
      throw new IndexOutOfBoundsException("index must be two-dimensional");
    }
    return scores[index[1]][index[0]];
  }
  
  public SingleDPMatrix(DP dp, SymbolList resList) {
    this.model = dp.getModel();
    this.states = dp.getStates();
    this.resList = new SymbolList [] { resList };
    this.score = Double.NEGATIVE_INFINITY;
    this.scores = new double[resList.length() + 2][states.length];
  }
}
