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

import java.io.Serializable;

import org.biojava.bio.BioError;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

public class BaumWelchSampler extends AbstractTrainer implements Serializable {
  protected double singleSequenceIteration(
    ModelTrainer trainer,
    SymbolList resList
  ) throws IllegalSymbolException, IllegalTransitionException, IllegalAlphabetException {
    DP dp = getDP();
    State [] states = dp.getStates();
    int [][] forwardTransitions = dp.getForwardTransitions();
    double [][] forwardTransitionScores = dp.getForwardTransitionScores();    
    int [][] backwardTransitions = dp.getBackwardTransitions();
    double [][] backwardTransitionScores = dp.getBackwardTransitionScores();    
    
    SymbolList [] rll = { resList };
    
    System.out.println("Forward");
    SingleDPMatrix fm = (SingleDPMatrix) dp.forwardMatrix(rll);
    double fs = fm.getScore();

    System.out.println("Backward");
    SingleDPMatrix bm = (SingleDPMatrix) dp.backwardMatrix(rll);
    double bs = bm.getScore();

    Symbol gap = AlphabetManager.instance().getGapSymbol();
    
    // state trainer
    for (int i = 1; i <= resList.length(); i++) {
      Symbol res = resList.symbolAt(i);
      double p = Math.random();
      for (int s = 0; s < dp.getDotStatesIndex(); s++) {
        if (! (states[s] instanceof MagicalState)) {
          p -= Math.exp(fm.scores[i][s] + bm.scores[i][s] - fs);
          if (p <= 0.0) {
            trainer.addStateCount((EmissionState) states[s], res, 1.0);
            break;
          }
        }
      }
    }

    // transition trainer
    for (int i = 0; i <= resList.length(); i++) {
      Symbol res = (i < resList.length()) ? resList.symbolAt(i + 1) :
                   gap;
      for (int s = 0; s < states.length; s++) {
        int [] ts = backwardTransitions[s];
        double [] tss = backwardTransitionScores[s];
        double p = Math.random();
        for (int tc = 0; tc < ts.length; tc++) {
          int t = ts[tc];
          double weight = (states[t] instanceof EmissionState)
            ? ((EmissionState) states[t]).getDistribution().getWeight(res)
            : 0.0;
          if (weight != Double.NEGATIVE_INFINITY) {
            p -= Math.exp(fm.scores[i][s] + tss[tc] + weight + bm.scores[i+1][t] - fs);
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
  
  public BaumWelchSampler(DP dp) {
    super(dp);
  }
}
