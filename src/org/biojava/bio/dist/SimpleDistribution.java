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

public class SimpleDistribution
extends AbstractDistribution
implements Serializable {
  private FiniteAlphabet alphabet;
  private transient AlphabetIndex indexer;
  private transient ChangeListener indexerListener;
  private double[] weights = null;
  private Distribution nullModel;
  
    private AlphabetIndex getIndexer() {
	if (indexer == null) {
	    this.indexer = AlphabetManager.getAlphabetIndex(alphabet);
	    this.indexerListener = new ChangeAdapter() {
		public void preChange(ChangeEvent ce) throws ChangeVetoException {
		    if(hasWeights()) {
			throw new ChangeVetoException(
						      ce,
						      "Can't allow the index to change as we have probabilities."
						      );
		    }
		}
	    } ;
	    indexer.addChangeListener(indexerListener, AlphabetIndex.INDEX);
	}

	return indexer;
    }

    public Alphabet getAlphabet() {
	return alphabet;
    }
  
  public Distribution getNullModel() {
    return this.nullModel;
  }
  
  protected void setNullModelImpl(Distribution nullModel)
  throws IllegalAlphabetException, ChangeVetoException {
    this.nullModel = nullModel;
  }
  
  protected boolean hasWeights() {
    return weights != null;
  }
  
  protected double[] getWeights() {
    if(weights == null) {
      weights = new double[getIndexer().getAlphabet().size()];
      for(int i = 0; i < weights.length; i++) {
        weights[i] = Double.NaN;
      }
	}
	
	return weights;
  }
  
  public double getWeightImpl(AtomicSymbol s)
  throws IllegalSymbolException {
    if(!hasWeights()) {
      return Double.NaN;
    } else {
      return weights[getIndexer().indexForSymbol(s)];
    }
  }

  protected void setWeightImpl(AtomicSymbol s, double w)
  throws IllegalSymbolException, ChangeVetoException {
    double[] weights = getWeights();
    if(w < 0.0) {
      throw new IllegalArgumentException(
        "Can't set weight to negative score: " +
        s.getName() + " -> " + w
      );
    }
    weights[getIndexer().indexForSymbol(s)] = w;
  }
  
  public SimpleDistribution(FiniteAlphabet alphabet) {
      this.alphabet = alphabet;
    
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
  
  protected class Trainer implements DistributionTrainer {
    private final Count counts;
    
    public Trainer() {
      counts = new IndexedCount(getIndexer());
    }
    
    public void addCount(DistributionTrainerContext dtc, AtomicSymbol sym, double times)
    throws IllegalSymbolException {
      try {
          counts.increaseCount(sym, times);
      } catch (ChangeVetoException cve) {
        throw new BioError(
          cve, "Assertion Failure: Change to Count object vetoed"
        );
      }
    }
    
    public double getCount(DistributionTrainerContext dtc, AtomicSymbol sym)
    throws IllegalSymbolException {
      return counts.getCount(sym);
    }
    
    public void clearCounts(DistributionTrainerContext dtc) {
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
    
    public void train(DistributionTrainerContext dtc, double weight)
    throws ChangeVetoException {
      if(changeSupport == null)  {
        trainImpl(dtc, weight);
      } else {
        synchronized(changeSupport) {
          ChangeEvent ce = new ChangeEvent(
            SimpleDistribution.this,
            Distribution.WEIGHTS
          );
          changeSupport.firePreChangeEvent(ce);
          trainImpl(dtc, weight);
          changeSupport.firePostChangeEvent(ce);
        }
      }
    }
    
    protected void trainImpl(DistributionTrainerContext dtc, double weight) {
      try {
        Distribution nullModel = getNullModel();
		double[] weights = getWeights();
        double []total = new double[weights.length];
        double sum = 0.0;
        for(int i = 0; i < total.length; i++) {
          AtomicSymbol s = (AtomicSymbol) getIndexer().symbolForIndex(i);
          sum += total[i] = 
            getCount(dtc, s) +
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
