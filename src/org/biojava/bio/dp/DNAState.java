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

import java.util.*;
import java.io.Serializable;

import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.DNATools;

/**
 * A state in a markov process.
 * <P>
 * This implementation is optimized for DNA.
 */
public class DNAState extends AbstractState implements Serializable {
  private final static int[] advance = { 1 };
  private final double [] scores;

  {
    scores = new double[4];
  }
  
  public int [] getAdvance() {
    return advance;
  }

  public double getWeight(Symbol r)
  throws IllegalSymbolException {
    if(r == MagicalState.MAGICAL_SYMBOL)
      return Double.NEGATIVE_INFINITY;
    alphabet().validate(r);
    return scores[DNATools.index(r)];
  }

  public void setWeight(Symbol r, double score)
  throws IllegalSymbolException {
    if(r == MagicalState.MAGICAL_SYMBOL)
      return;
    alphabet().validate(r);
    scores[DNATools.index(r)] = score;
  }
  
  public DNAState() {
    super(DNATools.getAlphabet());
  }
  
  public void registerWithTrainer(ModelTrainer modelTrainer) {
    Set trainerSet = modelTrainer.trainersForState(this);
    if(trainerSet.isEmpty()) {
      modelTrainer.registerTrainerForState(this, new DNAStateTrainer());
    }
  }

  private class DNAStateTrainer implements StateTrainer, Serializable {
    double c [] = new double[4];
      
    public void addCount(Symbol res, double counts)
    throws IllegalSymbolException {
      if(res instanceof MagicalState)
        return;
      // System.out.println("Added count " + name() + " -> " + res.getToken() + " = "
      //                    + counts + ", " + c[DNATools.index(res)]);
      c[DNATools.index(res)] += counts;
    }
      
    public void train(EmissionState nullModel, double weight)
    throws IllegalSymbolException {
      double sum = 0.0;
      for(int i = 0; i < c.length; i++) {
        Symbol r = DNATools.forIndex(i);
        sum += c[i] += (nullModel == null) ? 0.0 : Math.exp(nullModel.getWeight(r))*weight;
      }
      for(int i = 0; i < c.length; i++) {
        Symbol r = DNATools.forIndex(i);
        setWeight(r, Math.log(c[i] / sum) );
      }
    }
      
    public void clearCounts() {
      for(int i = 0; i < c.length; i++)
        c[i] = 0;
    }
  }
}
