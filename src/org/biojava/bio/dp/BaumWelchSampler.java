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
import org.biojava.bio.dp.onehead.SingleDPMatrix;

public class BaumWelchSampler extends AbstractTrainer implements Serializable {
  protected double singleSequenceIteration(
    ModelTrainer trainer,
    SymbolList symList
  ) throws IllegalSymbolException, IllegalTransitionException, IllegalAlphabetException {
    ScoreType scoreType = ScoreType.PROBABILITY;
    DP dp = getDP();
    State [] states = dp.getStates();
    int [][] forwardTransitions = dp.getForwardTransitions();
    double [][] forwardTransitionScores = dp.getForwardTransitionScores(scoreType);    
    int [][] backwardTransitions = dp.getBackwardTransitions();
    double [][] backwardTransitionScores = dp.getBackwardTransitionScores(scoreType);
    MarkovModel model = dp.getModel(); 
    
    SymbolList [] rll = { symList };
    
    System.out.println("Forward");
    SingleDPMatrix fm = (SingleDPMatrix) dp.forwardMatrix(rll, scoreType);
    double fs = fm.getScore();

    System.out.println("Backward");
    SingleDPMatrix bm = (SingleDPMatrix) dp.backwardMatrix(rll, scoreType);
    double bs = bm.getScore();

    Symbol gap = AlphabetManager.getGapSymbol();
    
    // state trainer
    for (int i = 1; i <= symList.length(); i++) {
      Symbol sym = symList.symbolAt(i);
      double p = Math.random();
      for (int s = 0; s < dp.getDotStatesIndex(); s++) {
        if (! (states[s] instanceof MagicalState)) {
          p -= Math.exp(fm.scores[i][s] + bm.scores[i][s] - fs);
          if (p <= 0.0) {
            trainer.addCount(
              ((EmissionState) states[s]).getDistribution(),
              sym, 1.0
            );
            break;
          }
        }
      }
    }

    // transition trainer
    for (int i = 0; i <= symList.length(); i++) {
      Symbol sym = (i < symList.length()) ? symList.symbolAt(i + 1) :
                   gap;
      for (int s = 0; s < states.length; s++) {
        int [] ts = backwardTransitions[s];
        double [] tss = backwardTransitionScores[s];
        double p = Math.random();
        Distribution dist = model.getWeights(states[s]);
        for (int tc = 0; tc < ts.length; tc++) {
          int t = ts[tc];
          double weight = (states[t] instanceof EmissionState)
            ? ((EmissionState) states[t]).getDistribution().getWeight(sym)
            : 1.0;
          if (weight != 0.0) {
            p -= Math.exp(fm.scores[i][s] + tss[tc] + bm.scores[i+1][t] - fs) * weight;
            if (p <= 0.0) {
              try {
                trainer.addCount(dist, (State) states[t], 1.0);
              } catch (IllegalSymbolException ise) {
                throw new BioError(ise,
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
