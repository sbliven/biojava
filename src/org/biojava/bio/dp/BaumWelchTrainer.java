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
import org.biojava.bio.dist.*;
import org.biojava.bio.seq.*;

public class BaumWelchTrainer extends AbstractTrainer implements Serializable {
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
    MarkovModel model = dp.getModel();
    
    SymbolList [] rll = { resList };
    
    SingleDPMatrix fm = (SingleDPMatrix) dp.forwardMatrix(rll);
    double fs = fm.getScore();
    
    SingleDPMatrix bm = (SingleDPMatrix) dp.backwardMatrix(rll);
    double bs = bm.getScore();

    Symbol gap = AlphabetManager.getGapSymbol();
    
    // state trainer
    for (int i = 1; i <= resList.length(); i++) {
      Symbol res = resList.symbolAt(i);
      for (int s = 0; s < dp.getDotStatesIndex(); s++) {
        double [] fsc = fm.scores[i];
        double [] bsc = bm.scores[i];
        if (! (states[s] instanceof MagicalState)) {
          trainer.addCount(
            ((EmissionState) states[s]).getDistribution(),
            res,
            Math.exp(fsc[s] + bsc[s] - fs)
          );
        }
      }
    }

    // transition trainer
    for (int i = 0; i <= resList.length(); i++) {
      Symbol res = (i < resList.length()) ? resList.symbolAt(i + 1) :
                    gap;
      double [] fsc = fm.scores[i];
      double [] bsc = bm.scores[i+1];
      for (int s = 0; s < states.length; s++) {  // any -> emission transitions
        int [] ts = backwardTransitions[s];
        double [] tss = backwardTransitionScores[s];
        Distribution dist = model.getWeights(states[s]);
        for (int tc = 0; tc < ts.length; tc++) {
          int t = ts[tc];
          double weight = (states[t] instanceof EmissionState)
            ? ((EmissionState) states[t]).getDistribution().getWeight(res)
            : 0.0;
          if (weight != Double.NEGATIVE_INFINITY) {
            try {
              trainer.addCount(
                dist, states[t],
                Math.exp(
                  fsc[s] + tss[tc] + weight + bsc[t]
                  -
                  fs
                )
              );
            } catch (IllegalSymbolException ise) {
              throw new BioError(
                ise,
                "Transition in backwardTransitions[][] dissapeared"
              );
            }
          }
        }
      }
    }
    
    return fs;
  }
  
  public BaumWelchTrainer(DP dp) {
    super(dp);
  }
}
