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
import org.biojava.utils.*;

/**
 * An encapsulation of a probability distribution over the Symbols within an
 * alphabet.
 * <P>
 * A distribution is effectively a map from symbol to probability. You may
 * choose to store something else instead, but it is not guaranteed that
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
public interface Distribution extends Changeable {
  /**
   * Whenever a distribution changes the values that would be returned by
   * getWeight, they should fire a ChangeEvent with this object as the type.
   * <P>
   * If the whole distribution changes, then the change and previous fields of
   * the ChangeEvent should be left null. If only a single weight is modified,
   * then change should be of the form Object[] { symbol, new Double(newVal) }
   * and previous should be of the form Object[] { symbol, new Double(oldVal) }
   */
  public static final ChangeType WEIGHTS = new ChangeType(
    "distribution weights changed",
    "org.biojava.bio.dist.Distribution",
    "WEIGHTS"
  );

  /**
   * Whenever the null model distribution changes the values that would be
   * returned by getWeight, either by being edited or by being replaced, a
   * ChangeEvent with this object as the type should be thrown.
   * <P>
   * If the null model has changed its weights, then the ChangeEvent should
   * refer back to the ChangeEvent from the null model. 
   */
  public static final ChangeType NULL_MODEL = new ChangeType(
    "distribution null model changed",
    "org.biojava.bio.dist.Distribution",
    "NULL_MODEL"
  );
  
  /**
   * The alphabet from which this spectrum emits symbols.
   *
   * @return  the Alphabet associated with this spectrum
   */
  Alphabet getAlphabet();
    
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
  double getWeight(Symbol s) throws IllegalSymbolException;
  
  /**
   * Set the probability or odds that Symbol s is emited by this state.
   *
   * @param s the Symbol emitted
   * @param w  the probability of emitting that symbol
   * @throws IllegalSymbolException if s is not from this state's alphabet, or
   *         if it is an ambiguity symbol and the implementation can't handle
   *         this case
   * @throws ChangeVetoException if this state does not allow weights
   *         to be tampered with, or if one of the listeners vetoed this change
   */
  void setWeight(Symbol s, double w)
  throws IllegalSymbolException, ChangeVetoException;

  /**
   * Sample a symbol from this state's probability distribution.
   *
   * @return the symbol sampled
   */
  Symbol sampleSymbol();
  
  /**
   * Retrieve the null model Distribution that this Distribution recognizes.
   *
   * @return  the apropriate null model
   */
  Distribution getNullModel();
  
  /**
   * Set the null model Distribution that this Distribution recognizes.
   *
   * @param nullDist  the new null model Distribution
   * @throws IllegalAlphabetException if the null model has the wrong alphabet
   * @throws ChangeVetoException  if this Distirbution doesn't support setting
   *         the null model, or if one of its listeners objects
   */
  void setNullModel(Distribution nullDist)
  throws IllegalAlphabetException, ChangeVetoException;
  
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
  
  /**
   * This listens to the null model distribution events and converts them into
   * NULL_MODEL events.
   *
   * @author Matthew Pocock
   * @since 1.1
   */
  public class NullModelForwarder extends ChangeAdapter implements java.io.Serializable {
    public NullModelForwarder(Object source, ChangeSupport cs) {
      super(source, cs);
    }
    
    protected ChangeEvent generateEvent(ChangeEvent ce) {
      if(ce.getType() == WEIGHTS) {
        return new ChangeEvent(
          getSource(),
          NULL_MODEL,
          null, null,
          ce
        );
      }
      return null;
    }
  }
}
