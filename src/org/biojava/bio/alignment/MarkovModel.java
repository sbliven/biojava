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

/**
 * A markov model.
 * <P>
 * All probablities are in log space.
 * <P>
 * This interface models a subset of hidden markov models with explicit start
 * and end states. In principle, these can be combined together, so that a state
 * within one model may be an entire model in its own right, wired via
 * container->start and end->container. For the sample methods to work, the log
 * scores must be probabilities (sum to 1).
 */
public interface MarkovModel {
  /**
   * Alphabet of the query sequences.
   */
  Alphabet queryAlphabet();

  /**
   * Alphabet of the states. DP.MAGICAL_STATE is always contained within this.
   */
  Alphabet stateAlphabet();

  /**
   * Probability of the transition between from and to.
   *
   * @param from  the State currently occupied
   * @param to  the State to move to
   * @return the transition score from->to
   * @throws IllegalResidueException if either from or to are not legal states
   * @throws IllegalTransitionException if there is no transition between the states
   */
  double getTransitionScore(State from, State to)
  throws IllegalResidueException, IllegalTransitionException;

  /**
   * Returns whether a transition is possible in the model.
   *
   * @param from  the State currently occupied
   * @param to  the State to move to
   * @throws IllegalResidueException if either from or to are not legal states
   */
  boolean containsTransition(State from, State to)
  throws IllegalResidueException;

  /**
   * Makes a transition between two states legal.
   *
   * @param from  the State currently occupied
   * @param to  the State to move to
   * @throws IllegalResidueException if either from or to are not legal states
   * @throws UnsupportedOperationException if an implementation does not allow
   *         transitions to be created
   */
   void createTransition(State from, State to)
   throws IllegalResidueException, UnsupportedOperationException;
   
  /**
   * Breaks a transition between two states legal.
   *
   * @param from  the State currently occupied
   * @param to  the State to move to
   * @throws IllegalResidueException if either from or to are not legal states
   * @throws UnsupportedOperationException if an implementation does not allow
   *         transitions to be destroyed
   */
   void destroyTransition(State from, State to)
   throws IllegalResidueException, UnsupportedOperationException;

   /**
   * Set the transition score associated with a transition.
   * <P>
   * @param from  the source State
   * @param to  the destination State
   * @param score the new score for the transition
   * @throws IllegalResidueException if either from or to are not states in the
   *         model
   * @throws IllegalTransitionException if the transition does not exist in the
   *         model
   * @throws UnsupportedOperationException if an implementation does not allow
   *         transition scores to be altered
   */
  void setTransitionScore(State from, State to, double score)
  throws IllegalResidueException, IllegalTransitionException,
  UnsupportedOperationException;
  
  /**
   * Sample a transition from the distribution of transitions.
   * <P>
   * This will give eroneous results if the scores are not log-probabilities.
   *
   * @param from  the starting state
   * @return  a State sampled from all states reachable from 'from'
   * @throws  IllegalResidueException if 'from' is not a state within this model
   */
  State sampleTransition(State from) throws IllegalResidueException;
  
  /**
   * Returns a Set of all legal transitions from a state.
   *
   * @param from  the starting state
   * @return  a List of State objects
   */
  Set transitionsFrom(State from) throws IllegalResidueException;
  
  /**
   * Returns a Set of all legal transitions to a state.
   *
   * @param from  the destination state
   * @return  a List of State objects
   */
  Set transitionsTo(State to) throws IllegalResidueException;
  
  /**
   * Register this model with a trainer.
   * <P>
   * A model may be registered with multiple trainers.
   */
  void registerWithTrainer(ModelTrainer mt);
}
