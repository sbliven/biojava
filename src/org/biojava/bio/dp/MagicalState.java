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
import java.io.*;
import java.lang.reflect.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.dist.*;
import org.biojava.utils.*;

/**
 * Start/end state for HMMs.
 * <P>
 * All MagicalState objects emit over MAGICAL_ALPHABET, which only contains
 * MAGICAL_STATE.
 *
 * @author Matthew Pocock
 */
public final class MagicalState extends SimpleEmissionState {
  /**
   * A cache of magical state objects so that we avoid making the same
   * thing twice.
   */
  private static final Map stateCache;
  private static final Map symbolCache;
  static {
    stateCache = new HashMap();
    symbolCache = new HashMap();
  }
  
  public static MagicalState getMagicalState(Alphabet alphabet, int heads) {
    AlphaHeads ah = new AlphaHeads(alphabet, heads);
    MagicalState ms = (MagicalState) stateCache.get(ah);
    if(ms == null) {
      ms = new MagicalState(alphabet, heads);
      stateCache.put(ah, ms);
    }
    return ms;
  }

  private MagicalState(Alphabet alpha, int heads) {
    super(
      "!-" + heads,
      Annotation.EMPTY_ANNOTATION,
      new int[heads],
      new MagicalDistribution(alpha)
    );
    int [] advance = getAdvance();
    for(int i = 0; i < heads; i++) {
      advance[i] = 1;
    }
  }

  private Object writeReplace() throws ObjectStreamException {
    return new PlaceHolder(getDistribution().getAlphabet(), getAdvance().length);
  }
  
  private static class AlphaHeads {
    public Alphabet alpha;
    public int heads;
    
    public AlphaHeads(Alphabet alpha, int heads) {
      this.alpha = alpha;
      this.heads = heads;
    }
    
    public boolean equals(Object o) {
      AlphaHeads ah = (AlphaHeads) o;
      return this.alpha == ah.alpha && this.heads == ah.heads;
    }
    
    public int hashCode() {
      return alpha.hashCode() ^ heads;
    }
  }
  
  private static class MagicalDistribution implements Distribution {
    private final Alphabet alpha;
    
    public double getWeight(Symbol sym) throws IllegalSymbolException {
      return sym == AlphabetManager.instance().getGapSymbol()
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
      return AlphabetManager.instance().getGapSymbol();
    }

    public void registerWithTrainer(DistributionTrainerContext dtc) {
      dtc.registerDistributionTrainer(this, IgnoreCountsTrainer.getInstance());
    }
    
    public MagicalDistribution(Alphabet alpha) {
      this.alpha = alpha;
    }
  }
  
  private static class PlaceHolder implements Serializable {
    private Alphabet alpha;
    private int heads;
    
    public PlaceHolder(Alphabet alpha, int heads) {
      this.alpha = alpha;
      this.heads = heads;
    }
    
    private Object readReplace() throws ObjectStreamException {
      return MagicalState.getMagicalState(alpha, heads);
    }
  }
}

