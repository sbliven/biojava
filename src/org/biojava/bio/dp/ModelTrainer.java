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

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;

/**
 * Encapsulates the training of an entire model.
 */
public interface ModelTrainer {
  /**
   * Adds some counts to a state for a given symbol.
   */
  void addStateCount(EmissionState s, Symbol r, double count)
  throws IllegalSymbolException;
  
  /**
   * Adds some counts to the transition between two states.
   */
  void addTransitionCount(State from, State to, double count)
  throws IllegalSymbolException, IllegalTransitionException;

  /**
   * Trains up the transitions in this model with the counts so far.
   * <P>
   * After training, the counts are all reset to zero.
   */
  void train()
  throws IllegalSymbolException, IllegalTransitionException;

  /**
   * Clears all of the counts, ready for re-training.
   */
  void clearCounts();
  
  /**
   * Register a StateTrainer as being associated with a state.
   * <P>
   * More than one trainer may be associated with the same state, in which
   * case any counts will be added to each trainer.
   * More than one state may be associated with the same trainer, in which
   * case the trainer is responsible for each state.
   *
   * @param state the EmissionState to associate with a trainer
   * @param trainer the trainer to add to the list of trainers for the state
   */
  void registerTrainerForState(EmissionState state, StateTrainer trainer);
  
  /**
   * Retrieves a Set of StateTrainers associated with an emission state.
   * <P>
   * Do not change the Set returned.
   *
   * @param state the state for which to retrieve trainers
   * @return  a Set, possibly empty, of StateTrainers that will be informed of
   *          counts for the state
   */
  Set trainersForState(EmissionState state);
  
  /**
   * Return a Set of all the StateTrainer objects used by this ModelTrainer.
   */
  Set getAllStateTrainers();
  
  /**
   * Register a TransitionTrainer, a source and a destination state with a
   * pair of states.
   * <P>
   * More than one TransitionTrainer and source, destination pairs may be
   * associated with a pair of states, in which case counts will be added to
   * each trainer and source, destination.
   * <P>
   * The TransitionTrainer must have been added previously using
   * registerTrainerForModel.
   *
   * @param from  the source state of the observed transition
   * @param to  the destination state of the observed transition
   * @param trainer the TransitionTrainer to add to the list of trainers
   * @param source  the source state for the trainer
   * @param destination the destination state for the trainer
   * @throws BioException if the trainer is not yet registered
   * @throws NullPointerException if any of the arguments are null
   */
  void registerTrainerForTransition(State from, State to,
                                    TransitionTrainer trainer,
                                    State source, State destination)
  throws BioException;

  /**
   * Retrieves a Set of TransitionTrainers associated with a transition between
   * a pair of states.
   * <P>
   * Do not change the fields of the TrainerTransition objects in the returned
   * Set, and do not change the Set.
   *
   * @param from the source state
   * @param to  the destination state
   * @return  a Set, possibly empty, of TrainerTransition objects
   */
  Set trainersForTransition(State from, State to);
  
  /**
   * Return a set of all TransitionTrainers used by this ModelTrainer.
   */
  Set getAllTransitionTrainers();
  
  /**
   * Retrieve the TransitionTrainer associated with a model, or null if
   * none is registered.
   */
  TransitionTrainer getTrainerForModel(MarkovModel model);
  
  /**
   * Register a trainer as being for a model.
   *
   * @param model the MarkovModel to train
   * @param trainer the trainer that will train the model
   * @throws BioException if the model already has a trainer associated
   */
  void registerTrainerForModel(MarkovModel model, TransitionTrainer trainer)
  throws BioException;
}
