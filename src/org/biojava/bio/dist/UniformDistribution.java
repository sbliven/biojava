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

public class UniformDistribution
extends AbstractDistribution implements Serializable {
  private static final Map singletons;
  
  static {
    singletons = new HashMap();
  }
  
  public static UniformDistribution createInstance(FiniteAlphabet alpha) {
    UniformDistribution dist = (UniformDistribution) singletons.get(alpha);
    if(dist == null) {
      singletons.put(alpha, dist = new UniformDistribution(alpha));
    }
    return dist;
  }
  
  private final FiniteAlphabet alphabet;
  private final double weight;
  private Distribution nullModel;
  
  public Alphabet getAlphabet() {
    return alphabet;
  }
  
  public Distribution getNullModel() {
    return nullModel;
  }
  
  public void setNullModel(Distribution nullModel)
  throws IllegalAlphabetException {
    if(nullModel.getAlphabet() != this.getAlphabet()) {
      throw new IllegalAlphabetException(
        "The null model has alphabet " + nullModel.getAlphabet() +
        " but it should be " + this.getAlphabet()
      );
    }
    this.nullModel = nullModel;
  }
  
  public double getWeight(Symbol s)
  throws IllegalSymbolException {
    alphabet.validate(s);
    return weight;
  }
  
  public void registerWithTrainer(DistributionTrainerContext dtc) {
    dtc.registerDistributionTrainer(this, IgnoreCountsTrainer.getInstance());
  }
  
  private UniformDistribution(FiniteAlphabet alphabet) {
    this.alphabet = alphabet;
    this.weight = 1.0 / (double) alphabet.size();
    this.nullModel = this;
  }
}
