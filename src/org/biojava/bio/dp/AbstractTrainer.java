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

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.dist.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;

public abstract class AbstractTrainer implements TrainingAlgorithm {
  private DP dp;
  
  private double lastScore = -Double.NEGATIVE_INFINITY;
  private double currentScore = -Double.NEGATIVE_INFINITY;
  private int cycle;
  
  public double getLastScore() {
    return lastScore;
  }
  
  public double getCurrentScore() {
    return currentScore;
  }
  
  public int getCycle() {
    return cycle;
  }
  
  public DP getDP() {
    return dp;
  }
  
  protected abstract double singleSequenceIteration(ModelTrainer trainer,
                                                    SymbolList resList)
  throws IllegalSymbolException, IllegalTransitionException, IllegalAlphabetException;
  
  /**
   * Trains the sequences in db until stopper says to finnish.
   */
  public void train(SequenceDB db,
                    double nullWeight, StoppingCriteria stopper)
  throws IllegalSymbolException, BioException {
    try {
      ModelTrainer trainer = new SimpleModelTrainer();
      trainer.setNullModelWeight(5.0);
      trainer.registerModel(dp.getModel());
        
      do {
        cycle++;
        lastScore = currentScore;
        currentScore = 0.0;
        for(SequenceIterator si = db.sequenceIterator(); si.hasNext(); ) {
          Sequence seq = si.nextSequence();
          currentScore += singleSequenceIteration(trainer, seq);
        }
        trainer.train();
        trainer.clearCounts();
      } while(!stopper.isTrainingComplete(this));
    } catch (Exception e) {
      throw new BioException(e, "Unable to train");
    }
  }
  
  public AbstractTrainer(DP dp) {
    this.dp = dp;
  }
  
  protected AbstractTrainer() {}
}
