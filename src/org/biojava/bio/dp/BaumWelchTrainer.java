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

public class BaumWelchTrainer extends AbstractTrainer {
  protected double singleSequenceIteration(
    DP dp, ModelTrainer trainer,
    ResidueList resList
  ) throws IllegalResidueException, IllegalTransitionException, IllegalAlphabetException {
    System.out.println("Training");
    State [] states = dp.getStates();
    int [][] forwardTransitions = dp.getForwardTransitions();
    double [][] forwardTransitionScores = dp.getForwardTransitionScores();    
    int [][] backwardTransitions = dp.getBackwardTransitions();
    double [][] backwardTransitionScores = dp.getBackwardTransitionScores();    
    ResidueList [] rll = { resList };
    System.out.println("Forward");
    SingleDPMatrix fm = (SingleDPMatrix) dp.forwardMatrix(rll);
    double fs = fm.getScore();

    System.out.println("Backward");
    SingleDPMatrix bm = (SingleDPMatrix) dp.backwardMatrix(rll);
    double bs = bm.getScore();

    System.out.println("State training");
    // state trainer
    for (int i = 1; i <= resList.length(); i++) {
      Residue res = resList.residueAt(i);
      for (int s = 0; s < dp.getDotStatesIndex(); s++) {
        if (! (states[s] instanceof MagicalState)) {
          trainer.addStateCount(
            (EmissionState) states[s],
            res,
            Math.exp(fm.scores[i][s] + bm.scores[i][s] - fs)
          );
        }
      }
    }

    System.out.println("Transition training");
    // transition trainer
    for (int i = 0; i <= resList.length(); i++) {
      Residue res = (i < resList.length()) ? resList.residueAt(i + 1) :
                    MagicalState.MAGICAL_RESIDUE;
      for (int s = 0; s < states.length; s++) {  // any -> emission transitions
        int [] ts = backwardTransitions[s];
        double [] tss = backwardTransitionScores[s];
        for (int tc = 0; tc < ts.length; tc++) {
          int t = ts[tc];
          if(t >= dp.getDotStatesIndex()) {
            break;
          }
          double weight = ((EmissionState) states[t]).getWeight(res);
          if (weight != Double.NEGATIVE_INFINITY) {
            try {
              trainer.addTransitionCount(
                states[s], states[t],
                Math.exp(fm.scores[i][s] + tss[tc] + weight + bm.scores[i+1][t] - fs)
              );
            } catch (IllegalTransitionException ite) {
              throw new BioError(
                ite,
                "Transition in backwardTransitions[][] dissapeared"
              );
            }
          }
        }
      }
      double [] mo = new double[states.length - dp.getDotStatesIndex()];
      for(int s = 0; s < mo.length; s++) {
        int ss = s + dp.getDotStatesIndex();
        int [] ts = backwardTransitions[s];
        double [] tss = backwardTransitionScores[s];
        mo[s] = fm.scores[i][ss];
        for(int tc = 0; tc < ts.length; tc++) {
          int t = ts[tc];
          if(t < dp.getDotStatesIndex()) {
            continue;
          }
          if(t >= ss) {
            break;
          }
          mo[s] -= (fm.scores[i][ss] + tss[t]);
        }
      }
      for (int s = dp.getDotStatesIndex(); s < states.length; s++) {  // emission -> emission transitions
        int [] ts = backwardTransitions[s];
        double [] tss = backwardTransitionScores[s];
        for (int tc = 0; tc < ts.length; tc++) {
          int t = ts[tc];
          if(t >= dp.getDotStatesIndex()) {
            break;
          }
          try {
            trainer.addTransitionCount(
              states[s], states[t],
              Math.exp(mo[s + dp.getDotStatesIndex()] + tss[tc] + bm.scores[i][t] - fs)
            );
          } catch (IllegalTransitionException ite) {
            throw new BioError(
              ite,
              "Transition in backwardTransitions[][] dissapeared"
            );
          }
        }
      }
    }
    
    System.out.println("Done");
    return fs;
  }
  
  public BaumWelchTrainer(FlatModel model) {
    super(model);
  }
}
