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

public interface Distribution extends Trainable {
  /**
   * The alphabet from which this spectrum emits symbols.
   *
   * @return  the Alphabet associated with this spectrum
   */
  public Alphabet getAlphabet();
    
  /**
   * Return the log probability that Symbol s is emited by this spectrum.
   * <P>
   * If the symbol is an AmbiguitySymbol, then it is the probability that one
   * of the symbols matching it was emitted. This should be calculated as <code>
   * <pre>P(A|S, Null) = [ sum_i P(a_i|S) * P(a_i|Null) ] / [ sum_i P(a_i|Null) ]</pre>
   * </code>.
   *
   * @param s the Symbol emitted
   * @return  the log probability of emitting that symbol
   * @throws IllegalSymbolException if s is not from this state's alphabet
   */
  public double getWeight(Symbol s) throws IllegalSymbolException;
  
  /**
   * Set the log probability or odds that Symbol s is emited by this state.
   *
   * @param s the Symbol emitted
   * @param w  the log-odds of emitting that symbol
   * @throws IllegalSymbolException if s is not from this state's alphabet
   * @throws IllegalOperationException if this state does not allow weights to
   *         be tampered with
   */
  public void setWeight(Symbol s, double w)
  throws IllegalSymbolException, UnsupportedOperationException;

  /**
   * Sample a symbol from this state's probability distribution.
   *
   * @return the symbol sampled
   */
  public Symbol sampleSymbol();
}
