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

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.DNATools;

public class ComplementaryDistribution
implements Distribution, Serializable {
  private Distribution other;
  private Distribution cache;
  
  public double getWeight(Symbol r) throws IllegalSymbolException {
    return cache.getWeight(r);
  }
  
  public void setWeight(Symbol r, double score)
  throws IllegalSymbolException, UnsupportedOperationException {
    other.setWeight(DNATools.complement(r), score);
    cache.setWeight(r, score);
  }
  
  public Alphabet getAlphabet() {
    return cache.getAlphabet();
  }
  
  public Symbol sampleSymbol() {
    return cache.sampleSymbol();
  }
  
  public void registerWithTrainer(ModelTrainer modelTrainer)
  throws BioException {
/*    other.registerWithTrainer(modelTrainer);
    for(Iterator i = modelTrainer.trainersForState(other).iterator(); i.hasNext();) {
      StateTrainer st = (StateTrainer) i.next();
      modelTrainer.registerTrainerForState(this, new ComplementaryTrainer(st));
    }*/
  }
  
  public ComplementaryDistribution(Distribution other)
  throws IllegalAlphabetException {
    Alphabet oa = other.getAlphabet();
    if(! (oa instanceof FiniteAlphabet) ) {
      throw new IllegalAlphabetException(
        "Can't create a ComplementaryState for alphabet " +
        oa.getName() + " as it is not finite"
      );
    }
    if(! (oa == DNATools.getDNA()) ) {
      throw new IllegalAlphabetException(
        "Can't create a ComplementaryState for alphabet " + oa.getName() +
        " as I can only complement DNA"
      );
    }
    FiniteAlphabet foa = (FiniteAlphabet) oa;
    this.other = other;
    this.cache = DistributionFactory.DEFAULT.createDistribution(oa);
    for(Iterator i = foa.iterator(); i.hasNext();) {
      Symbol s = (Symbol) i.next();
      try {
        cache.setWeight(DNATools.complement(s), other.getWeight(s));
      } catch (IllegalSymbolException ise) {
        throw new BioError(
          ise,
          "Symbol " + s.getName() +
          " has magicaly dissapeared from alphabet" +
          cache.getAlphabet().getName()
        );
      }
    }
  }
  
  private class ComplementaryTrainer
  implements DistributionTrainer, Serializable {
    private DistributionTrainer dt;
    
    public void addCount(Symbol r, double count)
    throws IllegalSymbolException {
      dt.addCount(DNATools.complement(r), count);
    }
    
    public void clearCounts() {}
    
    public void train(Distribution nullModel, double weight)
    throws IllegalSymbolException {
      dt.train(nullModel, weight); // a hack - forces st to be trained first
      for(
        Iterator i = ((FiniteAlphabet) other.getAlphabet()).iterator();
        i.hasNext();
      ) {
        Symbol r = (Symbol) i.next();
        try {
          cache.setWeight(DNATools.complement(r), other.getWeight(r));
        } catch (IllegalSymbolException ire) {
          throw new BioError("Symbol " + r.getName() +
            " has magicaly dissapeared from alphabet" + cache.getAlphabet().getName());
        }
      }
    }

    public ComplementaryTrainer(DistributionTrainer dt) {
      this.dt = dt;
    }
  }  
}
