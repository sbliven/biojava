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
import org.biojava.bio.dist.*;

/**
 * Encapsulates the training of an entire model.
 */
public interface ModelTrainer extends DistributionTrainerContext {
  /**
   * Adds some counts to the transition between two states.
   *
   * @param from  the source of the transition
   * @param to  the destination of the transition
   * @param counts how many counts to add
   * @throws IllegalSymbolException if either from or to are not in the model
   * @throws IllegalTransitionException if the model doesn't contain a
   *         transition between the states
   */
  void addTransitionCount(State from, State to, double count)
  throws IllegalSymbolException, IllegalTransitionException;

  /**
   * Trains up the transitions in this model with the counts so far.
   * <P>
   * This method should not throw any exceptions. I am considering making this
   * much tighter now that we have the vetoable mutator methods.
   *
   * @throws IllegalSymbolException if something went wrong with locating
   *         symbols in an emission spectrum or if a state couldn't be located
   * @throws IllegalTransitionException if an attempt was made to train a
   *         transition that couldn't be located
   */
  void train()
  throws IllegalSymbolException, IllegalTransitionException;

  /**
   * Clears all of the counts, ready for re-training.
   */
  void clearCounts();
  
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
