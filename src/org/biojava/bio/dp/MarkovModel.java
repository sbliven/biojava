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
public interface MarkovModel {
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
   * Get a probability Distribution over the transition from 'source'. 
   *
   * @param source  the State currently occupied
   * @return the probability Distribution over the reachable states
   * @throws IllegalSymbolException if from is not a legal state
   */
  Distribution getWeights(State source)
  throws IllegalSymbolException;

  /**
   * Set the probability distribution over the transitions from 'source'.
   * <P>
   * This should throw an IllegalAlphabetException if the source alphabet in
   * 'dist' is not the same alphabet as returned by transitionsFrom(source).
   *
   * @param source  the source State
   * @param dist    the new distribution over transitions from 'source'
   * @throws IllegalSymbolException if source is not a state in this model
   * @throws IllegalAlphabetException if the distribution has the wrong source
   *         alphabet
   * @throws ModelVetoException if for any reason the distribution can't be
   *         replaced at this time
   */
  void setWeights(State source, Distribution dist)
  throws IllegalSymbolException, IllegalAlphabetException, ModelVetoException;
  
  /**
   * Returns the FiniteAlphabet of all states that have a transition from 'source'.
   *
   * @param source  the source State
   * @return  a FiniteAlphabet of State objects that can reach from 'source'
   */
  FiniteAlphabet transitionsFrom(State source) throws IllegalSymbolException;
  
  /**
   * Returns the FiniteAlphabet of all states that have a transition to 'dest'.
   *
   * @param dest  the destination state
   * @return  a FiniteAlphabet of State objects that can reach 'dest'
   */
  FiniteAlphabet transitionsTo(State dest) throws IllegalSymbolException;

  /**
   * Returns wether a transition exists or not.
   *
   * @param from the transitin source
   * @param to the transition destination
   * @return true/false depending on wether this model has the transition
   * @throws IllegalSymbolException if either from or to are not states in this
   *         model
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
