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

public class PairDistribution
extends AbstractDistribution implements Serializable {
  private Distribution first;
  private Distribution second;
  private CrossProductAlphabet alphabet;
  
  private Distribution nullModel;
  
  public Alphabet getAlphabet() {
    return alphabet;
  }
  
  public Distribution getNullModel() {
    return nullModel;
  }
  
  public void setNullModel(Distribution nullModel)
  throws IllegalAlphabetException {
    if(nullModel.getAlphabet() != this.getAlphabet()) {
      throw new IllegalAlphabetException(
        "The null model has alphabet " + nullModel.getAlphabet() +
        " but it should be " + this.getAlphabet()
      );
    }
    this.nullModel = nullModel;
  }
  
  public void registerWithTrainer(org.biojava.bio.dp.ModelTrainer trainer) {
    trainer.registerDistribution(first);
    trainer.registerDistribution(second);
    
    trainer.registerDistributionTrainer(this, new PairTrainer());
  }

  public double getWeight(Symbol sym) throws IllegalSymbolException {
    getAlphabet().validate(sym);

    CrossProductSymbol cps = (CrossProductSymbol) sym;
    List symL = cps.getSymbols();
    Symbol f = (Symbol) symL.get(0);
    Symbol s = (Symbol) symL.get(1);

    return first.getWeight(f) * second.getWeight(s);      
  }
  
  public PairDistribution(Distribution first, Distribution second) {
    this.first = first;
    this.second = second;
    this.alphabet = AlphabetManager.getCrossProductAlphabet(
      Arrays.asList(new Alphabet[] {
        first.getAlphabet(), second.getAlphabet()
      })
    );
  }
  
  private class PairTrainer
  extends IgnoreCountsTrainer
  implements Serializable {
    public void addCount(
      DistributionTrainerContext dtc, Symbol sym, double times
    ) throws IllegalSymbolException {
      getAlphabet().validate(sym);
      if(!(sym instanceof AtomicSymbol)) {
        throw new IllegalSymbolException(
          "Can't add counts for ambiguity symbols. Got: " +
          sym.getName()
        );
      }
      CrossProductSymbol cps = (CrossProductSymbol) sym;
      List symL = cps.getSymbols();
      Symbol f = (Symbol) symL.get(0);
      Symbol s = (Symbol) symL.get(1);
      
      dtc.addCount(first, f, times);
      dtc.addCount(second, s, times);
    }
  }
}
