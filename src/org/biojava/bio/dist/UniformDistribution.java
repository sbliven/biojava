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

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;

/**
* An implementation of an uniform distribution
*/

public class UniformDistribution
extends AbstractDistribution implements Serializable {
  private final FiniteAlphabet alphabet;
  private Distribution nullModel;
  
  public Alphabet getAlphabet() {
    return alphabet;
  }
  
  public Distribution getNullModel() {
    return nullModel;
  }
   /**
  *Assign a background distribution
  *@param nullModel the background distribution to assign
  */  
  protected void setNullModelImpl(Distribution nullModel)
  throws IllegalAlphabetException {
    this.nullModel = nullModel;
  }
  
  public double getWeight(Symbol s)
  throws IllegalSymbolException {
    alphabet.validate(s);
    if(s instanceof AtomicSymbol) {
      return 1.0 / (double) alphabet.size();
    } else {
      return getAmbiguityWeight(s);
    }
  }
  
  protected void setWeightImpl(Symbol sym, double weight)
  throws ChangeVetoException {
    throw new ChangeVetoException(
      "Can't change the weights in a UniformDistribution"
    );
  }
  
  public void registerWithTrainer(DistributionTrainerContext dtc) {
    dtc.registerTrainer(this, IgnoreCountsTrainer.getInstance());
  }
  
  public UniformDistribution(FiniteAlphabet alphabet) {
    this.alphabet = alphabet;
    this.nullModel = this;
  }
}
