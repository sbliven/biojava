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

/**
 * A state in a markov process that has an emission spectrum.
 * <P>
 * These states emit symbols with a probability distribution over an alphabet.
 * They are the states that actualy make your observed sequence. They also must supply
 * training behaviour to set the emission spectrum up.
 */
public interface EmissionState extends State, Trainable {
  /**
   * The alphabet from which this state emits symbols.
   *
   * @return  the Alphabet associated with this state
   */
  public Alphabet alphabet();
  
  /**
   * Return the log probability or odds that Symbol r is emited by this state.
   *
   * @param r the Symbol emitted
   * @return  the log-odds of emitting that symbol
   * @throws IllegalSymbolException if r is not from this state's alphabet
   */
  public double getWeight(Symbol r) throws IllegalSymbolException;
  
  /**
   * Set the log probability or odds that Symbol r is emited by this state.
   *
   * @param r the Symbol emitted
   * @param w  the log-odds of emitting that symbol
   * @throws IllegalSymbolException if r is not from this state's alphabet
   * @throws IllegalOperationException if this state does not allow weights to be tampered with
   */
  public void setWeight(Symbol r, double w)
  throws IllegalSymbolException, UnsupportedOperationException;

  /**
   * Sample a symbol from this state's probability distribution.
   *
   * @return the symbol sampled
   */
  public Symbol sampleSymbol();

    /**
     * Determine the number of symbols this state advances along
     * one or more symbol lists.  In the simple case, this method
     * should almost always return {1} if it is a true `emmision'
     * state, or {0} if it is a dot state which only emits a gap
     * character.  For pairwise HMMs, it will normally return {1, 1}
     * for match state, and {0, 1} or {1, 0} for a gap state.  Under
     * some circumstances it may be valid to return values other
     * than 1 or 0, but you should consider the consequences for
     * HMM architecture very carefully.
     *
     * <P> 
     * Note that the int array returned by this method should
     * <em>never</em> be modified.
     * </P>
     */

    public int[] getAdvance();
}
