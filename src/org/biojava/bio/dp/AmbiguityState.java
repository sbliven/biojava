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
 * This implementation is optimized for DNA-AMBIGUITY. It maintains
 * probabilities for each DNA symbol, and then blends them for the ambiguity
 * symbols.
 */
public class AmbiguityState extends AbstractState implements Serializable {
  static final long serialVersionUID = -6063745195859460723L;
  private static int[] advance = {1};

  private final double [] score;
  
  {
    score = new double[16];
  }
  
  public double getWeight(Symbol r) throws IllegalSymbolException {
    if(r == MagicalState.MAGICAL_SYMBOL)
      return Double.NEGATIVE_INFINITY;
    alphabet().validate(r);
    return score[calcIndex(r)];
  }

  public void setWeight(Symbol r, double score) throws IllegalSymbolException {
    if(r == MagicalState.MAGICAL_SYMBOL)
      return;
    alphabet().validate(r);
    this.score[calcIndex(r)] = score;
  }

  private int calcIndex(Symbol r) throws IllegalSymbolException {
    if(r == DNATools.a()) {
      return 1;
    } else if(r == DNATools.g()) {
      return 2;
    } else if(r == DNATools.c()) {
      return 4;
    } else if(r == DNATools.t()) {
      return 8;
    } else {
      int index = 0;
      for(Iterator i = DNATools.forAmbiguity(r).iterator(); i.hasNext();) {
        Symbol ir = (Symbol) i.next();
        if(ir == DNATools.a()) {
          index += 1;
        } else if(ir == DNATools.g()) {
          index += 2;
        } else if(ir == DNATools.c()) {
          index += 4;
        } else if(ir == DNATools.t()) {
          index += 8;
        }
      }
      return index;
    }
  }
  
  public int [] getAdvance() {
    return advance;
  }
  
  public AmbiguityState() {
    super(DNATools.getAmbiguity());
  }
  
  public Alphabet alphabet() {
    return DNATools.getAmbiguity();
  }
  
  public void registerWithTrainer(ModelTrainer modelTrainer) {
    Set trainerSet = modelTrainer.trainersForState(this);
    if(trainerSet.isEmpty()) {
      modelTrainer.registerTrainerForState(this, new AmbiguousStateTrainer());
    }
  }
  
  private class AmbiguousStateTrainer implements StateTrainer, Serializable {
    double c [] =  new double[4];
      
    public void addCount(Symbol res, double counts) throws IllegalSymbolException {
      if(res instanceof MagicalState)
        return;
        
      SymbolList resList = DNATools.forAmbiguity(res);
      double size = resList.length();
      counts /= size;
      for(Iterator i = resList.iterator(); i.hasNext();) {
        c[DNATools.index((Symbol) i.next())] += counts;
      }
    }
      
    public void train(EmissionState nullModel, double weight) throws IllegalSymbolException {
      double sum = 0.0;
      for(int i = 0; i < c.length; i++) {
        Symbol r = DNATools.forIndex(i);
        sum += c[i] += Math.exp(nullModel.getWeight(r))*weight;
      }
      for(int i = 0; i < c.length; i++) {
        Symbol r = DNATools.forIndex(i);
        c[i] /= sum;
      }
      for(Iterator i = DNATools.getAmbiguity().iterator(); i.hasNext();) {
        sum = 0.0;
        Symbol ir = (Symbol) i.next();
        for(Iterator j = DNATools.forAmbiguity(ir).iterator(); j.hasNext();) {
          Symbol jr = (Symbol) j.next();
          sum += c[DNATools.index(jr)];
        }
        setWeight(ir, Math.log(sum));
      }
    }

    public void clearCounts() {
      for(int i = 0; i < c.length; i++)
        c[i] = 0;
    }
  }
}
