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

import org.biojava.bio.BioError;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;

public class ComplementaryState implements EmissionState {
  private EmissionState other;
  private EmissionState cache; // don't know if this speeds things up
  
  public double getWeight(Residue r) throws IllegalResidueException {
    return cache.getWeight(r);
  }
  
  public void setWeight(Residue r, double score)
  throws IllegalResidueException, UnsupportedOperationException {
    other.setWeight(DNATools.complement(r), score);
    cache.setWeight(r, score);
  }
  
  public Alphabet alphabet() {
    return other.alphabet();
  }

  public Annotation getAnnotation() {
    return other.getAnnotation();
  }
  
  public char getSymbol() {
    return other.getSymbol();
  }
  
  public String getName() {
    return other.getName() + "'";
  }
  
  public Residue sampleResidue() throws SeqException {
    try {
      return DNATools.complement(other.sampleResidue());
    } catch (IllegalResidueException ire) {
      throw new SeqException(ire, "Unable to reverse-complement sample");
    }
  }
  
  public void registerWithTrainer(ModelTrainer modelTrainer) {
    other.registerWithTrainer(modelTrainer);
    for(Iterator i = modelTrainer.trainersForState(other).iterator(); i.hasNext();) {
      StateTrainer st = (StateTrainer) i.next();
      modelTrainer.registerTrainerForState(this, new ComplementaryTrainer(st));
    }
  }
  
    public int[] getAdvance() {
	return other.getAdvance();
    }

  public ComplementaryState(EmissionState other) {
    this.other = other;
    this.cache = StateFactory.createState(other.alphabet());
    for(Iterator i = other.alphabet().residues().iterator(); i.hasNext();) {
      Residue r = (Residue) i.next();
      try {
        cache.setWeight(DNATools.complement(r), other.getWeight(r));
      } catch (IllegalResidueException ire) {
        throw new BioError("Residue " + r.getName() +
          " has magicaly dissapeared from alphabet" + cache.alphabet().getName());
      }
    }
  }
  
  private class ComplementaryTrainer implements StateTrainer {
    private StateTrainer st;
    
    public void addCount(Residue r, double count)
    throws IllegalResidueException {
      st.addCount(DNATools.complement(r), count);
    }
    
    public void clearCounts() {}
    public void train(EmissionState nullModel, double weight)
    throws IllegalResidueException {
      st.train(nullModel, weight); // a hack - forces st to be trained first
      for(Iterator i = other.alphabet().residues().iterator(); i.hasNext();) {
        Residue r = (Residue) i.next();
        try {
          cache.setWeight(DNATools.complement(r), other.getWeight(r));
        } catch (IllegalResidueException ire) {
          throw new BioError("Residue " + r.getName() +
            " has magicaly dissapeared from alphabet" + cache.alphabet().getName());
        }
      }
    }

    public ComplementaryTrainer(StateTrainer st) {
      this.st = st;
    }
  }  
}
