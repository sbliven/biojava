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
import java.io.Serializable;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
/**
*A simple implementation of a distribution
*/

public final class SimpleDistribution extends AbstractDistribution implements Serializable {
  private final FiniteAlphabet alphabet;
  private final Map weight;
  private Distribution nullModel;
  
  public Alphabet getAlphabet() {
    return alphabet;
  }
  
  public Distribution getNullModel() {
    return this.nullModel;
  }
  
  /**
  *Assign a distribution to be the background null model.
  *@param nullModel the distribution to act as the null model.
  */
  
  public void setNullModel(Distribution nullModel)
  throws IllegalAlphabetException {
    if(nullModel == null) {
      throw new NullPointerException(
        "The null model must not be null." +
        " The apropreate null-model is a UniformDistribution instance."
      );
    }
    if(nullModel.getAlphabet() != getAlphabet()) {
      throw new IllegalAlphabetException(
        "Could not use distribution " + nullModel +
        " as its alphabet is " + nullModel.getAlphabet().getName() +
        " and this distribution's alphabet is " + getAlphabet().getName()
      );
    }
    this.nullModel = nullModel;   
  }
  
  public double getWeight(Symbol s)
  throws IllegalSymbolException {
    Double d = (Double) weight.get(s);
    if(d != null) {
      return d.doubleValue();
    } else {
      alphabet.validate(s);
      if(!(s instanceof AtomicSymbol)) {
        return getAmbiguityWeight(s);
      } else {
        throw new BioError(
          "Requested weight for " + s.getName() +
          " but something is odd & I can't find its weight"
        );
      }
    }
  }
  
  public void setWeight(Symbol s, double w)
  throws IllegalSymbolException {
    alphabet.validate(s);
    if(!(s instanceof AtomicSymbol)) {
      throw new IllegalSymbolException(
        "Can't set the weight for an ambiguity symbol " + s.getName()
      );
    }
    Double d = new Double(w);
    weight.put(s, d);
  }
  
  public SimpleDistribution(FiniteAlphabet alphabet) {
    this.alphabet = alphabet;
    this.weight = new HashMap();
    for(Iterator i = alphabet.iterator(); i.hasNext(); ) {
      weight.put(i.next(), new Double(Double.NaN));
    }
    try {
      setNullModel(new UniformDistribution(alphabet));
    } catch (Exception e) {
      throw new BioError(e, "This should never fail. Something is screwed!");
    }
  }
}
