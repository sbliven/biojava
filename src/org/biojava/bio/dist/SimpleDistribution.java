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
 * A simple implementation of a distribution, which works with any finite alphabet.
 *
 * @author Matthew Pocock
 * @author Thomas Down
 */

public final class SimpleDistribution extends AbstractDistribution implements Serializable {
  private transient AlphabetIndex indexer;
  private double[] weights = null;
  private Distribution nullModel;
  
  public Alphabet getAlphabet() {
    return indexer.getAlphabet();
  }
  
  public Distribution getNullModel() {
    return this.nullModel;
  }
  
  protected void setNullModelImpl(Distribution nullModel)
  throws IllegalAlphabetException, ChangeVetoException {
    this.nullModel = nullModel;
  }
  
  public double getWeight(Symbol s)
  throws IllegalSymbolException {
    if(weights == null) {
      return Double.NaN;
    } else {
      if(s instanceof AtomicSymbol) {
        return weights[indexer.indexForSymbol(s)];
      } else {
        return getAmbiguityWeight(s);
      }
    }
  }

  protected void setWeightImpl(Symbol s, double w)
  throws IllegalSymbolException, ChangeVetoException {
    if(weights == null) {
      weights = new double[indexer.getAlphabet().size()];
      for(int i = 0; i < weights.length; i++) {
        weights[i] = Double.NaN;
      }
    }
    if(!(s instanceof AtomicSymbol)) {
      throw new IllegalSymbolException(
        "Can't set the weight for an ambiguity symbol " + s.getName()
      );
    }
    if(w < 0.0) {
      throw new IllegalArgumentException(
        "Can't set weight to negative score: " +
        s.getName() + " -> " + w
      );
    }
    weights[indexer.indexForSymbol(s)] = w;
  }
  
  public SimpleDistribution(FiniteAlphabet alphabet) {
    this.indexer = AlphabetManager.getAlphabetIndex(alphabet);
    indexer.addChangeListener(
      new ChangeAdapter() {
        public void preChange(ChangeEvent ce) throws ChangeVetoException {
          if(weights != null) {
            throw new ChangeVetoException(
              ce,
              "Can't allow the index to change as we have probabilities."
            );
          }
        }
      },
      AlphabetIndex.INDEX
    );
    
    try {
      setNullModel(new UniformDistribution(alphabet));
    } catch (Exception e) {
      throw new BioError(e, "This should never fail. Something is screwed!");
    }
  }

  /**
   * Register a simple trainer for this distribution.
   */

  public void registerWithTrainer(DistributionTrainerContext dtc) {
   dtc.registerTrainer(this, new Trainer());
  }
  
  private class Trainer implements DistributionTrainer {
    private final Count counts;
    
    public Trainer() {
      counts = new IndexedCount(indexer);
    }
    
    public void addCount(DistributionTrainerContext dtc, Symbol sym, double times)
    throws IllegalSymbolException {
      try {
        counts.increaseCount(sym, times);
      } catch (ChangeVetoException cve) {
        throw new BioError(
          cve, "Assertion Failure: Change to Count object vetoed"
        );
      }
    }
    
    public void clearCounts() {
      try {
        int size = ((FiniteAlphabet) counts.getAlphabet()).size();
        for(int i = 0; i < size; i++) {
          counts.zeroCounts();
        }
      } catch (ChangeVetoException cve) {
        throw new BioError(
          cve, "Assertion Failure: Change to Count object vetoed"
        );
      }
    }
    
    public void train(double weight)
    throws ChangeVetoException {
      if(changeSupport == null)  {
        trainImpl(weight);
      } else {
        synchronized(changeSupport) {
          ChangeEvent ce = new ChangeEvent(
            SimpleDistribution.this,
            Distribution.WEIGHTS
          );
          changeSupport.firePreChangeEvent(ce);
          trainImpl(weight);
          changeSupport.firePostChangeEvent(ce);
        }
      }
    }
    
    protected void trainImpl(double weight) {
      try {
        Distribution nullModel = getNullModel();
        double []total = new double[weights.length];
        double sum = 0.0;
        for(int i = 0; i < total.length; i++) {
          Symbol s = indexer.symbolForIndex(i);
          sum += total[i] = 
            counts.getCount(s) +
            nullModel.getWeight(s) * weight;
        }
        double sum_inv = 1.0 / sum;
        for(int i = 0; i < total.length; i++) {
          weights[i] = total[i] * sum_inv;
        }
      } catch (IllegalSymbolException ise) {
        throw new BioError(ise,
          "Assertion Failure: Should be impossible to mess up the symbols."
        );
      }
    }
  }
}
