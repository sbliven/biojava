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

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.DNATools;

public class ComplementaryState implements EmissionState {
  private EmissionState other;
  private EmissionState cache; // don't know if this speeds things up
  
  public double getWeight(Symbol r) throws IllegalSymbolException {
    return cache.getWeight(r);
  }
  
  public void setWeight(Symbol r, double score)
  throws IllegalSymbolException, UnsupportedOperationException {
    other.setWeight(DNATools.complement(r), score);
    cache.setWeight(r, score);
  }
  
  public Alphabet alphabet() {
    return other.alphabet();
  }

  public Annotation getAnnotation() {
    return other.getAnnotation();
  }
  
  public char getToken() {
    return other.getToken();
  }
  
  public String getName() {
    return other.getName() + "'";
  }
  
  public Symbol sampleSymbol() {
    try {
      return DNATools.complement(other.sampleSymbol());
    } catch (IllegalSymbolException ire) {
      throw new BioError(ire, "Unable to reverse-complement sample. This is screwey!");
    }
  }
  
  public void registerWithTrainer(ModelTrainer modelTrainer)
  throws BioException {
    other.registerWithTrainer(modelTrainer);
    for(Iterator i = modelTrainer.trainersForState(other).iterator(); i.hasNext();) {
      StateTrainer st = (StateTrainer) i.next();
      modelTrainer.registerTrainerForState(this, new ComplementaryTrainer(st));
    }
  }
  
  public int [] getAdvance() {
    return other.getAdvance();
  }
  
  public ComplementaryState(EmissionState other)
  throws IllegalAlphabetException {
    Alphabet oa = other.alphabet();
    if(! (oa instanceof FiniteAlphabet) ) {
      throw new IllegalAlphabetException(
        "Can't create a ComplementaryState for state " + other.getName() +
        " as it has a non-finite alphabet " + oa.getName()
      );
    }
    FiniteAlphabet foa = (FiniteAlphabet) oa;
    this.other = other;
    this.cache = StateFactory.DEFAULT.createState(oa, other.getAdvance(), other.getName() + "-c");
    for(Iterator i = foa.iterator(); i.hasNext();) {
      Symbol r = (Symbol) i.next();
      try {
        cache.setWeight(DNATools.complement(r), other.getWeight(r));
      } catch (IllegalSymbolException ire) {
        throw new BioError("Symbol " + r.getName() +
          " has magicaly dissapeared from alphabet" + cache.alphabet().getName());
      }
    }
  }
  
  private class ComplementaryTrainer implements StateTrainer {
    private StateTrainer st;
    
    public void addCount(Symbol r, double count)
    throws IllegalSymbolException {
      st.addCount(DNATools.complement(r), count);
    }
    
    public void clearCounts() {}
    public void train(EmissionState nullModel, double weight)
    throws IllegalSymbolException {
      st.train(nullModel, weight); // a hack - forces st to be trained first
      for(
        Iterator i = ((FiniteAlphabet) other.alphabet()).iterator();
        i.hasNext();
      ) {
        Symbol r = (Symbol) i.next();
        try {
          cache.setWeight(DNATools.complement(r), other.getWeight(r));
        } catch (IllegalSymbolException ire) {
          throw new BioError("Symbol " + r.getName() +
            " has magicaly dissapeared from alphabet" + cache.alphabet().getName());
        }
      }
    }

    public ComplementaryTrainer(StateTrainer st) {
      this.st = st;
    }
  }  
}
