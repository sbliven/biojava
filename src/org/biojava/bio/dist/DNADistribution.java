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
import org.biojava.bio.seq.DNATools;

/**
 * A distribution optimized for DNA.
 * <P>
 * This implementation is optimized for DNA.
 */
public final class DNADistribution
extends AbstractDistribution implements Serializable {
  private double [] scores;
  private Distribution nullModel;
  private ChangeListener nullModelListener; 
  
  {
    scores = new double[4];
    nullModelListener = new Distribution.NullModelForwarder(this, changeSupport);
  }
  
  public Alphabet getAlphabet() {
    return DNATools.getDNA();
  }

  public Distribution getNullModel() {
    return this.nullModel;
  }
  
  public void setNullModel(Distribution nullModel)
  throws IllegalAlphabetException, ChangeVetoException {
    if(nullModel.getAlphabet() != getAlphabet()) {
      throw new IllegalAlphabetException(
        "Could not use distribution " + nullModel +
        " as its alphabet is " + nullModel.getAlphabet().getName() +
        " and this distribution's alphabet is " + getAlphabet().getName()
      );
    }
    if(changeSupport == null) {
      // if there are no listners yet, don't g through the overhead of
      // synchronized regions or of trying to inform them.
      this.nullModel.removeChangeListener(nullModelListener);
      this.nullModel = nullModel;
      nullModel.addChangeListener(nullModelListener);
    } else {
      // OK - so somebody is intereted in me. Do it properly this time.
      ChangeEvent ce = new ChangeEvent(
        this,
        Distribution.NULL_MODEL,
        nullModel,
        this.nullModel
      );
      if(this.nullModel != null) {
        this.nullModel.removeChangeListener(nullModelListener);
      }
      synchronized(changeSupport) {
        changeSupport.firePreChangeEvent(ce);
        this.nullModel = nullModel;
        nullModel.addChangeListener(nullModelListener);
        changeSupport.firePostChangeEvent(ce);
      }
    }
  }
  
  public double getWeight(Symbol s)
  throws IllegalSymbolException {
    if(s == DNATools.a()) {
      return scores[0];
    } else if (s == DNATools.g()) {
      return scores[1];
    } else if (s == DNATools.c()) {
      return scores[2];
    } else if (s == DNATools.t()) {
      return scores[3];
    } else {
      getAlphabet().validate(s);
      return getAmbiguityWeight(s);
    }
  }

  public void setWeight(Symbol s, double score)
  throws IllegalSymbolException, ChangeVetoException {
    int si;
    if(s == DNATools.a()) {
      si = 0;
    } else if (s == DNATools.g()) {
      si = 1;
    } else if (s == DNATools.c()) {
      si = 2;
    } else if (s == DNATools.t()) {
      si = 3;
    } else {
      getAlphabet().validate(s);
      throw new IllegalSymbolException(
        "Unable to set weight associated with symbol " + s.getName() +
        ". Either this is a cock-up, or it is an ambiguous symbol."
      );
    }
    
    if(changeSupport == null) {
      // if there are no listners yet, don't g through the overhead of
      // synchronized regions or of trying to inform them.
      scores[si] = score;
    } else {
      // OK - so somebody is intereted in me. Do it properly this time.
      ChangeEvent ce = new ChangeEvent(
        this,
        Distribution.WEIGHTS,
        new Object[] {s, new Double(score)},
        new Object[] {s, new Double(scores[si])}
      );
      synchronized(changeSupport) {
        changeSupport.firePreChangeEvent(ce);
        scores[si] = score;
        changeSupport.firePostChangeEvent(ce);
      }
    }
  }
    
  public DNADistribution() {
    try {
      setNullModel(new UniformDistribution(DNATools.getDNA()));
    } catch (IllegalAlphabetException iae) {
      throw new BioError(iae, "This should never fail. Something is screwed with alpahbets!");
    } catch (ChangeVetoException cve) {
      throw new BioError(cve, "This should never fail. Something is screwed with change listeners");
    }
  }
  
  public void registerWithTrainer(DistributionTrainerContext dtc) {
    dtc.registerTrainer(this, new DNADistributionTrainer());
  }

  private class DNADistributionTrainer
  implements DistributionTrainer, Serializable {
    double c [] = new double[4];
      
    public void addCount(
      DistributionTrainerContext dtc, Symbol sym, double counts
    ) throws IllegalSymbolException {
      c[DNATools.index(sym)] += counts;
    }
      
    public void train(double weight)
    throws ChangeVetoException {
      if(changeSupport == null) {
        // if there are no listners yet, don't g through the overhead of
        // synchronized regions or of trying to inform them.
        realTrain(weight);
      } else {
        // OK - so somebody is intereted in me. Do it properly this time.
        ChangeEvent ce = new ChangeEvent(
          DNADistribution.this,
          Distribution.WEIGHTS
        );
        synchronized(changeSupport) {
          changeSupport.firePreChangeEvent(ce);
          realTrain(weight);
          changeSupport.firePostChangeEvent(ce);
        }
      }
    }
    
    private void realTrain(double weight)
    throws ChangeVetoException {
      // this accesses the state of DNADistribution directly. We should be
      // protected from nasties as long as the change event stuff is being used
      // properly.
      System.out.println("Training " + this);
      System.out.println("Was:");
      for(int i = 0; i < c.length; i++) {
        Symbol s = DNATools.forIndex(i);
        System.out.println("\t" + s.getName() + "->" + scores[i]);
      }

      try {
        Distribution nullModel = getNullModel();
        double sum = 0.0;
        for(int i = 0; i < c.length; i++) {
          Symbol s = DNATools.forIndex(i);
          sum += c[i] + nullModel.getWeight(s)*weight;
        }
        
        System.out.println("Sum: " + sum);
        
        for(int i = 0; i < c.length; i++) {
          Symbol s = DNATools.forIndex(i);
          scores[i] = (c[i] + nullModel.getWeight(s)*weight) / sum;
        }
        System.out.println("Is:");
        for(int i = 0; i < c.length; i++) {
          Symbol s = DNATools.forIndex(i);
          System.out.println("\t" + s.getName() + "->" + scores[i]);
        }
      } catch (IllegalSymbolException ise) {
        throw new BioError(ise, "Nullmodel alphabet incompatible with this distribution");
      }
    }
      
    public void clearCounts() {
      System.out.println("Clearing counts for " + this);
      for(int i = 0; i < c.length; i++) {
        c[i] = 0;
      }
    }
  }
}
