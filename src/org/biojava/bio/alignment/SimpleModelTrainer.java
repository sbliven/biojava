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

public class SimpleModelTrainer implements ModelTrainer {
  private Transition _tran;
  
  /**
   * state -> Set <StateTrainer>
   */
  private Map stateToTrainer;
  
  /**
   * The set of all StateTrainers.
   */
  private Set allStateTrainers;
  
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
  private EmissionState nullState;
  private double nullStateWeight;
  private double transCounts;
  private double transCountWeight;
  
  {
    _tran = new Transition(null, null);
    stateToTrainer = new HashMap();
    allStateTrainers = new HashSet();
    transitionToTrainer = new HashMap();
    transitionToCount = new HashMap();
    allTransitionTrainers = new HashSet();
    modelToTrainer = new HashMap();
  }
  
  public void addStateCount(EmissionState s, Residue r, double count)
  throws IllegalResidueException {
    Set trainerSet = (Set) stateToTrainer.get(s);
    if(trainerSet == null)
      throw new IllegalResidueException("No trainers associated with state " +
                                        s.getName());
                                        
    for(Iterator i = trainerSet.iterator(); i.hasNext();) {
      ((StateTrainer) i.next()).addCount(r, count);
    }
  }
  
  public void addTransitionCount(State from, State to, double count)
  throws IllegalResidueException, IllegalTransitionException {
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
  throws IllegalResidueException, IllegalTransitionException {
    // train all states
    for(Iterator i = getAllStateTrainers().iterator(); i.hasNext();) {
      StateTrainer st = (StateTrainer) i.next();
      st.train(nullState, nullStateWeight);
    }
    
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
    // clear all states
    for(Iterator i = getAllStateTrainers().iterator(); i.hasNext();) {
      ((StateTrainer) i.next()).clearCounts();
    }
    
    // clear all counts in transitionToCount
    for(Iterator i = transitionToCount.keySet().iterator(); i.hasNext();) {
      transitionToCount.put((Transition) i.next(), new Double(0.0));
    }
    
    // clear all transitions in TransitionTrainers
    for(Iterator i = getAllTransitionTrainers().iterator(); i.hasNext();) {
      ((TransitionTrainer) i.next()).clearCounts();
    }
  }
  
  public void registerTrainerForState(EmissionState state,
                                      StateTrainer trainer) {
    Set trainerSet = (Set) stateToTrainer.get(state);
    if(trainerSet == null) {
      trainerSet = new HashSet();
      stateToTrainer.put(state, trainerSet);
    }
    trainerSet.add(trainer);
    allStateTrainers.add(trainer);
  }
  
  public Set trainersForState(EmissionState state) {
    Set trainerSet = (Set) stateToTrainer.get(state);
    if(trainerSet == null)
      return Collections.EMPTY_SET;
    return trainerSet;
  }
  
  public Set getAllStateTrainers() {
    return allStateTrainers;
  }
  
  public void registerTrainerForTransition(State from, State to,
                                           TransitionTrainer trainer,
                                           State source, State destination)
  throws SeqException {
    if(trainer == null)
      throw new NullPointerException("Attempted to use a null trainer");
    if(!getAllTransitionTrainers().contains(trainer))
      throw new SeqException("Trainer not registered.");
      
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
  throws SeqException {
    if(allTransitionTrainers.contains(model))
      throw new SeqException("Trainer already associated with the model");
    modelToTrainer.put(model, trainer);
    allTransitionTrainers.add(trainer);
  }
  
  public SimpleModelTrainer(MarkovModel model,
                            EmissionState nullState, double nullStateWeight,
                            double transCounts, double transCountWeight) {
    this.model = model;
    this.nullState = nullState;
    this.nullStateWeight = nullStateWeight;
    this.transCounts = transCounts;
    this.transCountWeight = transCountWeight;
    model.registerWithTrainer(this);
  }
}
