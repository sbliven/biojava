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
 * A markov model.
 * <P>
 * All probablities are in log space.
 * <P>
 * This interface models a subset of hidden markov models with an explicit start
 * and end state. In principle, these can be combined together, so that a state
 * within one model may be an entire model in its own right, wired via
 * container->start and end->container. For the sample methods to work, the log
 * scores must be probabilities (sum to 1).
 */
public interface MarkovModel extends Trainable {
  /**
   * Alphabet that is emitted by the emission states.
   */
  Alphabet emissionAlphabet();

  /**
   * FiniteAlphabet of the states.
   * <P>
   * We are modeling a finite-state-machine, so there will be a finite set of
   * states.
   * <P>
   * The MagicalState returned by getMagicalState is always contained
   * within this as the start/end state.
   *
   * @return the alphabet over states
   */
  FiniteAlphabet stateAlphabet();
  
  /**
   * The MagicalState for this model.
   */
  MagicalState magicalState();
  
  /**
   * The number of heads on this model.
   * <P>
   * Each head consumes a single SymbolList. A single-head model just consumes/
   * emits a single sequence. A two-head model performs alignment between two
   * sequences (e.g. smith-waterman). Models with more heads do more interesting
   * things.
   */
  int heads();
  
  /**
   * Probability of the transition between from and to.
   *
   * @param from  the State currently occupied
   * @param to  the State to move to
   * @return the transition score from->to
   * @throws IllegalSymbolException if either from or to are not legal states
   * @throws IllegalTransitionException if there is no transition between the states
   */
  double getTransitionScore(State from, State to)
  throws IllegalSymbolException, IllegalTransitionException;

  /**
   * Returns whether a transition is possible in the model.
   *
   * @param from  the State currently occupied
   * @param to  the State to move to
   * @throws IllegalSymbolException if either from or to are not legal states
   */
  boolean containsTransition(State from, State to)
  throws IllegalSymbolException;

  /**
   * Makes a transition between two states legal.
   * <P>
   * This should inform each TransitionListener that a transition is to be
   * created using preCreateTransition, and if none of the listeners fire a
   * ModelVetoException, it should create the transition, and then inform each
   * TransitionListener with postCreateTransition.
   *
   * @param from  the State currently occupied
   * @param to  the State to move to
   * @throws IllegalSymbolException if either from or to are not legal states
   * @throws UnsupportedOperationException if an implementation does not allow
   *         transitions to be created
   * @throws ModelVetoException if creating the transition is vetoed
   */
   void createTransition(State from, State to)
   throws IllegalSymbolException, UnsupportedOperationException,
   ModelVetoException;
   
  /**
   * Breaks a transition between two states legal.
   * <P>
   * This should inform each TransitionListener that a transition is to be
   * broken using preDestroyTransition, and if none of the listeners fire a
   * ModelVetoException, it should break the transition, and then inform each
   * TransitionListener with postDestroyTransition.
   *
   * @param from  the State currently occupied
   * @param to  the State to move to
   * @throws IllegalSymbolException if either from or to are not legal states
   * @throws UnsupportedOperationException if an implementation does not allow
   *         transitions to be destroyed
   * @throws ModelVetoException if breaking the transition is vetoed
   */
   void destroyTransition(State from, State to)
   throws IllegalSymbolException, UnsupportedOperationException,
   ModelVetoException;

   /**
   * Set the transition score associated with a transition.
   * <P>
   * This method should inform each TransitionListener that the score is to be
   * changed by calling preChangeTransitionScore, and if the change is not
   * vetoed, it should update the score and then call postChangeTransitionScore
   * on each listener.
   *
   * @param from  the source State
   * @param to  the destination State
   * @param score the new score for the transition
   * @throws IllegalSymbolException if either from or to are not states in the
   *         model
   * @throws IllegalTransitionException if the transition does not exist in the
   *         model
   * @throws UnsupportedOperationException if an implementation does not allow
   *         transition scores to be altered
   * @throws ModelVetoException if the new score is vetoed
   */
  void setTransitionScore(State from, State to, double score)
  throws IllegalSymbolException, IllegalTransitionException,
  UnsupportedOperationException, ModelVetoException;
  
  /**
   * Sample a transition from the distribution of transitions.
   * <P>
   * This will give eroneous results if the scores are not log-probabilities.
   *
   * @param from  the starting state
   * @return  a State sampled from all states reachable from 'from'
   * @throws  IllegalSymbolException if 'from' is not a state within this model
   */
  State sampleTransition(State from) throws IllegalSymbolException;
  
  /**
   * Returns a Set of all legal transitions from a state.
   *
   * @param from  the starting state
   * @return  a List of State objects
   */
  Set transitionsFrom(State from) throws IllegalSymbolException;
  
  /**
   * Returns a Set of all legal transitions to a state.
   *
   * @param from  the destination state
   * @return  a List of State objects
   */
  Set transitionsTo(State to) throws IllegalSymbolException;
  
  /**
   * Adds a state to the model.
   *
   * @param newState  the state to add
   * @throws UnsupportedOperationException if this MarkovModel doesn't allow
   *         states to be added
   * @throws IllegalSymbolException if the state is not valid or is a MagicalState
   */
  void addState(State newState)
  throws UnsupportedOperationException, IllegalSymbolException,
  ModelVetoException;

  /**
   * Remove a state from the model.
   * <P>
   * States should not be removed untill they are involved in no transitions.
   * This is to avoid producing corrupted models by accident.
   *
   * @param toGo  the state to remove
   * @throws UnsupportedOperationException if the MarkovModel doesn't allow
   *         states to be removed
   * @throws IllegalSymbolException if the symbol is not part of this model
   *         or a MagicalState
   * @throws IllegalTransitionException if the state is currently involved in
   *         any transitions
   */
  void removeState(State toGo)
  throws UnsupportedOperationException, IllegalTransitionException,
  IllegalSymbolException, ModelVetoException;
    
  /**
   * Register a TransitionListener with the model.
   *
   * @param tl   a TransitionListener to notify when transitions are created,
   *             destroyed or the probabilities changed
   */
  void addTransitionListener(TransitionListener tl);
  
  /**
   * Unregister a TransitionListener with the model.
   *
   * @param tl   a TransitionListener to no longer notify when transitions are
   *             created, destroyed or the probabilities changed
   */
  void removeTransitionListener(TransitionListener tl);
}
