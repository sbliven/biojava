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

import org.biojava.bio.seq.*;

public abstract class AbstractTrainer implements TrainingAlgorithm {
  private FlatModel model;
  
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
  
  public FlatModel getModel() {
    return model;
  }
  
  protected abstract double singleSequenceIteration(DP dp, ModelTrainer trainer,
                                                    ResidueList resList)
  throws IllegalResidueException, IllegalTransitionException, IllegalAlphabetException;
  
  /**
   * Trains the sequences in db until stopper says to finnish.
   */
  public void train(SequenceDB db, EmissionState nullModel,
                    double nullWeight, StoppingCriteria stopper)
  throws IllegalResidueException, SeqException {
    try {
      ModelTrainer trainer =
        new SimpleModelTrainer(model, nullModel, nullWeight, 0.000001, 1.0);
      do {
        cycle++;
        DP dp = DPFactory.createDP(model);
        lastScore = currentScore;
        currentScore = 0.0;
        for(SequenceIterator si = db.sequenceIterator(); si.hasNext(); ) {
          Sequence seq = si.nextSequence();
          currentScore += singleSequenceIteration(dp, trainer, seq);
        }
        trainer.train();
        trainer.clearCounts();
      } while(!stopper.isTrainingComplete(this));
    } catch (Exception e) {
      throw new SeqException(e, "Unable to train");
    }
  }
  
  public AbstractTrainer(FlatModel model) {
    this.model = model;
  }
}
