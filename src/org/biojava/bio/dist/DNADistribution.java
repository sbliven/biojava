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
import org.biojava.bio.seq.DNATools;

/**
 * A state in a markov process.
 * <P>
 * This implementation is optimized for DNA.
 */
public final class DNADistribution
extends AbstractDistribution implements Serializable {
  private final double [] scores;
  private Distribution nullModel;

  {
    scores = new double[4];
  }
  
  public Alphabet getAlphabet() {
    return DNATools.getDNA();
  }

  public Distribution getNullModel() {
    return this.nullModel;
  }
  
  public void setNullModel(Distribution nullModel)
  throws IllegalAlphabetException {
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
      AmbiguitySymbol as = (AmbiguitySymbol) s;
      return getAmbiguityWeight(as);
    }
  }

  public void setWeight(Symbol s, double score)
  throws IllegalSymbolException {
    if(s == DNATools.a()) {
      scores[0] = score;
    } else if (s == DNATools.g()) {
      scores[1] = score;
    } else if (s == DNATools.c()) {
      scores[2] = score;
    } else if (s == DNATools.t()) {
      scores[3] = score;
    } else {
      getAlphabet().validate(s);
      throw new IllegalSymbolException(
        "Unable to set weight associated with symbol " + s.getName() +
        ". Either this is a cock-up, or it is an isntance of AmbiguitySymbol."
      );
    }
  }
  
  public DNADistribution() {
    try {
      setNullModel(UniformDistribution.createInstance(DNATools.getDNA()));
    } catch (IllegalAlphabetException iae) {
      throw new BioError(iae, "This should never fail. Something is screwed!");
    }
  }
  
  public void registerWithTrainer(DistributionTrainerContext dtc) {
    dtc.registerDistributionTrainer(this, new DNADistributionTrainer());
  }

  private class DNADistributionTrainer implements DistributionTrainer, Serializable {
    double c [] = new double[4];
      
    public void addCount(
      DistributionTrainerContext dtc, Symbol sym, double counts
    ) throws IllegalSymbolException {
      c[DNATools.index(sym)] += counts;
    }
      
    public void train(Distribution nullModel, double weight)
    throws IllegalSymbolException {
      double sum = 0.0;
      for(int i = 0; i < c.length; i++) {
        Symbol s = DNATools.forIndex(i);
        sum += c[i] + nullModel.getWeight(s)*weight;
      }
      for(int i = 0; i < c.length; i++) {
        Symbol s = DNATools.forIndex(i);
        setWeight(s, (c[i] + nullModel.getWeight(s)*weight) / sum );
      }
    }
      
    public void clearCounts() {
      for(int i = 0; i < c.length; i++)
        c[i] = 0;
    }
  }
}
