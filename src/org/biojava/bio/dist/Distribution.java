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


package org.biojava.bio.dist;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;

/**
 * An encapsulation of a probability distribution over the Symbols within an
 * alphabet.
 * <P>
 * A distribution is effectively a map from symbol to probability. You may
 * choose to store odds instead ( p(x) / p(H_0) ), but it is not guaranteed that
 * all algorithms will work correctly.
 * <P>
 * This interface should handle the case of emitting an ambiguity symbol. In
 * classical statistics, this would be just the sum of the probabiltiy of
 * emitting each matching symbol. However, in our domain, only one symbol can
 * actualy be produced, and the ambiguity symbol means 'one of these', not
 * 'each of these', you should take a weighted average:<code>
 * <pre>P(A|S, Null) = [ sum_i P(a_i|S) * P(a_i|Null) ] / [ sum_i P(a_i|Null) ]</pre>
 * </code>
 *
 * @author Matthew Pocock
 */
public interface Distribution {
  /**
   * The alphabet from which this spectrum emits symbols.
   *
   * @return  the Alphabet associated with this spectrum
   */
  public Alphabet getAlphabet();
    
  /**
   * Return the probability that Symbol s is emited by this spectrum.
   * <P>
   * If the symbol is an AmbiguitySymbol, then it is the probability that
   * exactly one of the symbols matching it was emitted.
   *
   * @param s the Symbol emitted
   * @return  the log probability of emitting that symbol
   * @throws IllegalSymbolException if s is not from this state's alphabet
   */
  public double getWeight(Symbol s) throws IllegalSymbolException;
  
  /**
   * Set the probability or odds that Symbol s is emited by this state.
   *
   * @param s the Symbol emitted
   * @param w  the probability of emitting that symbol
   * @throws IllegalSymbolException if s is not from this state's alphabet, or
   *         if it is an ambiguity symbol and the implementation can't handle
   *         this case
   * @throws UnsupportedOperationException if this state does not allow weights
   *         to be tampered with
   */
  public void setWeight(Symbol s, double w)
  throws IllegalSymbolException, UnsupportedOperationException;

  /**
   * Sample a symbol from this state's probability distribution.
   *
   * @return the symbol sampled
   */
  public Symbol sampleSymbol();
  
  /**
   * Register this distribution with a training context.
   * <P>
   * This should be invoked from within dtc.addDistribution(). This method
   * is responsible for constructing a suitable DistributionTrainer instance
   * and registering it by calling
   * dtc.registerDistributionTrainer(this, trainer). If the distribution is a
   * view onto another distribution, it can force the other to be registered by
   * calling dtc.addDistribution(other), and can then get on with registering
   * it's own trainer.
   *
   * @param dtc the DistributionTrainerContext with witch to register a trainer
   */
  void registerWithTrainer(DistributionTrainerContext dtc);
}
