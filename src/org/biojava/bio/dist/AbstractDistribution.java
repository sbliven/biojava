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

import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;

/**
 * An abstract implementation of Distribution.
 * <P>
 * You will need to over-ride <code>getWeight()</code> for a simple
 * implementation. You may also wish to over-ride the other methods if the
 * default implementation is not suitable.
 *
 * @author Matthew Pocock
 */
public abstract class AbstractDistribution implements Distribution {
  public void setWeight(Symbol sym, double weight)
  throws IllegalSymbolException, UnsupportedOperationException {
    throw new UnsupportedOperationException(
      "There is no way to set the emission weight for symbol " + sym.getName()
    );
  }
  
  /**
   * Performs the standard munge to handle ambiguity symbols.
   *
   * @param amb the AmbiguitySymbol to find the probability of
   * @return the probability that one of the symbols matching amb was emitted
   * @throws IllegalSymbolException if for any reason the symbols within amb
   *         are not recognized by this state
   */
  protected double getAmbiguityWeight(Symbol amb)
  throws IllegalSymbolException {
    if(amb instanceof AtomicSymbol) {
      throw new IllegalSymbolException(
        "Can't calculate ambiguity weight for atomic symbol " + amb.getName()
      );
    }
    Alphabet ambA = amb.getMatches();
    if(ambA instanceof FiniteAlphabet) {
      FiniteAlphabet fa = (FiniteAlphabet) ambA;
      double sum = 0.0;
      double div = 0.0;
      for(Iterator i = fa.iterator(); i.hasNext(); ) {
        Symbol sym = (Symbol) i.next();
        double nm = getNullModel().getWeight(sym);
        sum += getWeight(sym) * nm;
        div += nm;
      }
      return (sum == 0.0)
        ? 0.0
        : sum / div;
    } else {
      throw new IllegalSymbolException(
        "Can't find weight for infinite set of symbols matched by " +
        amb.getName()
      );
    }
  }
  
  /**
   * Retrieve the null model Distribution that this Distribution recognizes.
   *
   * @return  the apropreate null model
   */
  public abstract Distribution getNullModel();
  
  public Symbol sampleSymbol()
  throws BioError {
    double p = Math.random();
    try {
      for(Iterator i = ((FiniteAlphabet) getAlphabet()).iterator(); i.hasNext(); ) {
        AtomicSymbol s = (AtomicSymbol) i.next();
        p -= getWeight(s);
        if( p <= 0) {
          return s;
        }
      }
    
      StringBuffer sb = new StringBuffer();
      for(Iterator i = ((FiniteAlphabet) this.getAlphabet()).iterator(); i.hasNext(); ) {
        AtomicSymbol s = (AtomicSymbol) i.next();
        double w = getWeight(s);
        sb.append("\t" + s.getName() + " -> " + w + "\n");
      }
      throw new BioError(
        "Could not find a symbol to emit from alphabet " + getAlphabet() +
        ". Do the probabilities sum to 1?" + "\np=" + p + "\n" + sb.toString()
      );
    } catch (IllegalSymbolException ire) {
      throw new BioError(
        ire,
        "Unable to itterate over all symbols in alphabet - " +
        "things changed beneath me!"
      );
    }
  }
  
  public void registerWithTrainer(DistributionTrainerContext dtc) {
    dtc.registerDistributionTrainer(this, IgnoreCountsTrainer.getInstance());
  }
}
