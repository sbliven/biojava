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

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.DNATools;

/**
 * A state in a markov process.
 * <P>
 * This implementation is optimized for DNA-AMBIGUITY. It maintains
 * probabilities for each DNA residue, and then blends them for the ambiguity
 * residues.
 */
public class AmbiguityState extends AbstractState {
  private static int[] advance = {1};

  private double [] score;
  
  {
    score = new double[16];
  }
  
  public double getWeight(Residue r) throws IllegalResidueException {
    if(r == MagicalState.MAGICAL_RESIDUE)
      return Double.NEGATIVE_INFINITY;
    alphabet().validate(r);
    return score[calcIndex(r)];
  }

  public void setWeight(Residue r, double score) throws IllegalResidueException {
    if(r == MagicalState.MAGICAL_RESIDUE)
      return;
    alphabet().validate(r);
    this.score[calcIndex(r)] = score;
  }

  private int calcIndex(Residue r) throws IllegalResidueException {
    int index = 0;
    for(Iterator i = DNATools.forAmbiguity(r).iterator(); i.hasNext();) {
      Residue ir = (Residue) i.next();
      if(ir == DNATools.a())
        index += 1;
      else if(ir == DNATools.g())
        index += 2;
      else if(ir == DNATools.c())
        index += 4;
      else if(ir == DNATools.t())
        index += 8;
    }
    return index;
  }
  
  public int [] getAdvance() {
    return advance;
  }
  
  public AmbiguityState() {
    super(DNATools.getAmbiguity());
  }
  
  public void registerWithTrainer(ModelTrainer modelTrainer) {
    Set trainerSet = modelTrainer.trainersForState(this);
    if(trainerSet.isEmpty()) {
      modelTrainer.registerTrainerForState(this, new AmbiguousStateTrainer());
    }
  }
  
  private class AmbiguousStateTrainer implements StateTrainer {
    double c [] =  new double[4];
      
    public void addCount(Residue res, double counts) throws IllegalResidueException {
      if(res instanceof MagicalState)
        return;
        
      ResidueList resList = DNATools.forAmbiguity(res);
      double size = resList.length();
      counts /= size;
      for(Iterator i = resList.iterator(); i.hasNext();) {
        c[DNATools.index((Residue) i.next())] += counts;
      }
    }
      
    public void train(EmissionState nullModel, double weight) throws IllegalResidueException {
      double sum = 0.0;
      for(int i = 0; i < c.length; i++) {
        Residue r = DNATools.forIndex(i);
        sum += c[i] += Math.exp(nullModel.getWeight(r))*weight;
      }
      for(int i = 0; i < c.length; i++) {
        Residue r = DNATools.forIndex(i);
        c[i] /= sum;
      }
      for(Iterator i = DNATools.getAmbiguity().iterator(); i.hasNext();) {
        sum = 0.0;
        Residue ir = (Residue) i.next();
        for(Iterator j = DNATools.forAmbiguity(ir).iterator(); j.hasNext();) {
          Residue jr = (Residue) j.next();
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
