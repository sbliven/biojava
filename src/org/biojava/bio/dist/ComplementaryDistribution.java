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

public class ComplementaryDistribution
implements Distribution, Serializable {
  private Distribution other;
  
  public double getWeight(Symbol s) throws IllegalSymbolException {
    return other.getWeight(DNATools.complement(s));
  }
  
  public void setWeight(Symbol s, double score)
  throws IllegalSymbolException, UnsupportedOperationException {
    other.setWeight(DNATools.complement(s), score);
  }
  
  public Alphabet getAlphabet() {
    return other.getAlphabet();
  }
  
  public Symbol sampleSymbol() {
    try {
      return DNATools.complement(other.sampleSymbol());
    } catch (IllegalSymbolException ise) {
      throw new BioError(
        ise,
        "Somehow, I have been unable to complement a symbol"
      );
    }
  }
  
  public void registerWithTrainer(DistributionTrainerContext dtc) {
    dtc.registerDistribution(other);
    dtc.registerDistributionTrainer(this, new IgnoreCountsTrainer() {
      public void addCount(
        DistributionTrainerContext dtc,
        Symbol s,
        double count
      ) throws IllegalSymbolException {
        dtc.addCount(other, DNATools.complement(s), count);
      }
    });
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
    this.other = other;
  }  
}
