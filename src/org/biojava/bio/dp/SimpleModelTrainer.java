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

import java.util.*;
import java.io.Serializable;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.dist.*;

public class SimpleModelTrainer
extends SimpleDistributionTrainerContext
implements ModelTrainer, Serializable {
  private Transition _tran;
  
  /**
   * Transition -> Set <TrainerTransition>
   */  
  private Map transitionToTrainer;
  
  /**
   * Transition -> Double count
   * <P>
   * Optimization so that transition counts only need be distributed to all
   * listeners just prior to training.
   */
  private Map transitionToCount;
  
  /**
   * The set of all TransitionTrainers.
   */
  private Set allTransitionTrainers;
  
  /**
   * MarkovModel -> TransitionTrainer
   */
  private Map modelToTrainer;

  private MarkovModel model;
  private Distribution nullModel;
  private double nullModelWeight;
  private double transCounts;
  private double transCountWeight;
  
  {
    _tran = new Transition(null, null);
    transitionToTrainer = new HashMap();
    transitionToCount = new HashMap();
    allTransitionTrainers = new HashSet();
    modelToTrainer = new HashMap();
  }
  
  public void addTransitionCount(State from, State to, double count)
  throws IllegalSymbolException, IllegalTransitionException {
    _tran.from = from;
    _tran.to = to;
    Double countD = (Double) transitionToCount.get(_tran);
    if(countD == null)
      throw new IllegalTransitionException(
        from, to,
        "No trainers associated with transition"
      );
    transitionToCount.put(_tran, new Double(countD.doubleValue() + count));
  }

  public void train()
  throws IllegalSymbolException, IllegalTransitionException {
    trainDistributions();
    
    // dispurse all transition counts
    for(Iterator i = transitionToTrainer.keySet().iterator(); i.hasNext();) {
      Transition trans = (Transition) i.next();
      Set trainerSet = (Set) transitionToTrainer.get(trans);
      if(trainerSet == null) {
        throw new IllegalTransitionException(
          trans.from, trans.to,
          "No trainers associated with transition"
          );
      }
      double count = ((Double) transitionToCount.get(trans)).doubleValue();
      for(Iterator j = trainerSet.iterator(); j.hasNext();) {
        TrainerTransition tt = (TrainerTransition) j.next();
        tt.trainer.addCount(tt.from, tt.to, count);
      }
    }
    
    // train all transitions
    for(Iterator i = getAllTransitionTrainers().iterator(); i.hasNext();) {
      TransitionTrainer tt = (TransitionTrainer) i.next();
      tt.train(transCounts, transCountWeight);
    }
  }

  public void clearCounts() {
    super.clearDistributionCounts();
    
    // clear all counts in transitionToCount
    for(Iterator i = transitionToCount.keySet().iterator(); i.hasNext();) {
      transitionToCount.put((Transition) i.next(), new Double(0.0));
    }
    
    // clear all transitions in TransitionTrainers
    for(Iterator i = getAllTransitionTrainers().iterator(); i.hasNext();) {
      ((TransitionTrainer) i.next()).clearCounts();
    }
  }
  
  public void registerTrainerForTransition(
    State from, State to,
    TransitionTrainer trainer,
    State source,
    State destination
  ) throws BioException {
    if(trainer == null) {
      throw new NullPointerException("Attempted to register a null trainer");
    }
    
    if(!getAllTransitionTrainers().contains(trainer)) {
      throw new BioException("Trainer not registered.");
    }
      
    _tran.from = from;
    _tran.to = to;
    Set trainerSet = (Set) transitionToTrainer.get(_tran);
    if(trainerSet == null) {
      Transition trans = new Transition(from, to);
      trainerSet = new HashSet();
      transitionToTrainer.put(trans, trainerSet);
      transitionToCount.put(trans, new Double(0.0));
    }
    trainerSet.add(new TrainerTransition(trainer, source, destination));
  }

  public Set trainersForTransition(State from, State to) {
    _tran.from = from;
    _tran.to = to;
    Set trainersSet = (Set) transitionToTrainer.get(_tran);
    if(trainersSet == null)
      return Collections.EMPTY_SET;
    return trainersSet;
  }
  
  public Set getAllTransitionTrainers() {
    return allTransitionTrainers;
  }
  
  public TransitionTrainer getTrainerForModel(MarkovModel model) {
    return (TransitionTrainer) modelToTrainer.get(model);
  }
  
  public void registerTrainerForModel(MarkovModel model,
                                      TransitionTrainer trainer)
  throws BioException {
    if(allTransitionTrainers.contains(model))
      throw new BioException("Trainer already associated with the model");
    modelToTrainer.put(model, trainer);
    allTransitionTrainers.add(trainer);
  }
  
  /**
   * Create a model trainer for a particular model.
   *
   * @param model the MarkovModel to train
   * @param nullModel  the null state model - possibly null
   * @param nullModelWeight how many times to add the null model
   * @param transCounts the counts to add to each transition
   * @param transCountWeight how many times to add them
   * @throws BioException  if for any reason the trainer could not be built
   */
  public SimpleModelTrainer(
    MarkovModel model,
    Distribution nullModel, double nullModelWeight,
    double transCounts, double transCountWeight
  ) throws BioException {
    this.model = model;
    setNullModel(nullModel);
    setNullModelWeight(nullModelWeight);
    this.transCounts = transCounts;
    this.transCountWeight = transCountWeight;
    model.registerWithTrainer(this);
  }
}
