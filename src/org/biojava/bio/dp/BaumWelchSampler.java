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

import org.biojava.bio.BioError;
import org.biojava.bio.seq.*;

public class BaumWelchSampler extends AbstractTrainer {
  protected double singleSequenceIteration(DP dp, ModelTrainer trainer,
                                           Sequence seq)
  throws IllegalResidueException {
    EmissionState [] states = dp.getStates();
    int [][] forwardTransitions = dp.getForwardTransitions();
    double [][] forwardTransitionScores = dp.getForwardTransitionScores();    
    int [][] backwardTransitions = dp.getBackwardTransitions();
    double [][] backwardTransitionScores = dp.getBackwardTransitionScores();    
    
    double [][] fm = new double [seq.length() + 2][states.length];
    double [][] bm = new double [seq.length() + 2][states.length];

    DPCursor fc = new MatrixCursor(states, seq,
                                   seq.iterator(), fm, +1);
    DPCursor bc = new MatrixCursor(states, seq,
                                   new DP.ReverseIterator(seq), bm, -1);

    dp.forward_initialize(fc);
    dp.forward_recurse(fc);
    double fs = dp.forward_termination(fc);

    dp.backward_initialize(bc);
    dp.backward_recurse(bc);
    double bs = dp.backward_termination(bc);

    // state trainer
    for (int i = 1; i <= seq.length(); i++) {
      Residue res = seq.residueAt(i);
      double [] fCol = fm[i];
      double [] bCol = bm[i];
      double p = Math.random();
      for (int s = 0; s < states.length; s++) {
        if (! (states[s] instanceof MagicalState)) {
          p -= Math.exp(fCol[s] + bCol[s] - fs);
          if (p <= 0.0) {
            trainer.addStateCount(states[s], res, 1.0);
            break;
          }
        }
      }
    }

    // transition trainer
    for (int i = 0; i <= seq.length(); i++) {
      Residue res = (i < seq.length()) ? seq.residueAt(i + 1) :
                    MagicalState.MAGICAL_RESIDUE;
      double [] fCol = fm[i];
      double [] bCol = bm[i + 1];
      for (int s = 0; s < states.length; s++) {
        int [] ts = backwardTransitions[s];
        double [] tss = backwardTransitionScores[s];
        double p = Math.random();
        for (int tc = 0; tc < ts.length; tc++) {
          int t = ts[tc];
          double weight = states[t].getWeight(res);
          if (weight != Double.NEGATIVE_INFINITY) {
            p -= Math.exp(fCol[s] + tss[tc] + weight + bCol[t] - fs);
            if (p <= 0.0) {
              try {
                trainer.addTransitionCount((State) states[s],
                                          (State) states[t], 1.0);
              } catch (IllegalTransitionException ite) {
                throw new BioError(ite,
                  "Transition in backwardTransitions dissapeared");
              }
              break;
            }
          }
        }
      }
    }
    return fs;
  }
  
  public BaumWelchSampler(FlatModel model) {
    super(model);
  }
}
