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

/**
 * This distribution emits only the gap symbol.
 * <P>
 * It is a useful thing to have around for pair-wise alignment, as you can
 * build a PairDistribution that emits gaps in one sequence and Symbols in the
 * other.
 *
 * @author Matthew Pocock
 */
public class GapDistribution implements Distribution {
  private final Alphabet alpha;
    
  public double getWeight(Symbol sym) throws IllegalSymbolException {
    return sym == AlphabetManager.getGapSymbol()
      ? 1.0
      : 0.0;
  }

  public void setWeight(Symbol s, double w) throws IllegalSymbolException,
  UnsupportedOperationException {
    getAlphabet().validate(s);
    throw new UnsupportedOperationException(
      "The weights are immutable: " + s.getName() + " -> " + w
    );
  }

  public Alphabet getAlphabet() {
    return alpha;
  }
    
  public Symbol sampleSymbol() {
    return AlphabetManager.getGapSymbol();
  }

  public void registerWithTrainer(DistributionTrainerContext dtc) {
    dtc.registerDistributionTrainer(this, IgnoreCountsTrainer.getInstance());
  }
    
  public GapDistribution(Alphabet alpha) {
    this.alpha = alpha;
  }
}
  

