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

/**
 * Start/end state for HMMs.
 * <P>
 * All MagicalState objects emit over MAGICAL_ALPHABET, which only contains
 * MAGICAL_RESIDUE.
 *
 * @author Matthew Pocock
 */
public class MagicalState implements EmissionState {
  /**
   * The symbol that implicitly exists at the beginning and end of every
   * SymbolList (index 0 and length+1).
   */
  public static final Symbol MAGICAL_RESIDUE;

  /**
   * The alphabet that contains only MAGICAL_RESIDUE.
   */
  public static final Alphabet MAGICAL_ALPHABET;

  /**
   * A cache of magical state objects so that we avoid making the same
   * thing twice.
   */
  protected static final Map stateCache;
  
  static {
    MAGICAL_RESIDUE = new SimpleSymbol('!', "mMagical", null);
    MAGICAL_ALPHABET = new SimpleAlphabet();

    try {
      ((SimpleAlphabet) MAGICAL_ALPHABET).addSymbol(MAGICAL_RESIDUE);
      ((SimpleAlphabet) MAGICAL_ALPHABET).setName("Magical Alphabet");
    } catch (IllegalSymbolException ire) {
      throw new BioError(
        ire,
        "Could not complete static intialization of MagicalState"
      );
    }
    
    stateCache = new HashMap();
  }
  
  public static MagicalState getMagicalState(int heads) {
    Integer headsI = new Integer(heads);
    MagicalState ms = (MagicalState) stateCache.get(headsI);
    if(ms == null) {
      ms = new MagicalState(heads);
      stateCache.put(headsI, ms);
    }
    return ms;
  }
  
  private final int[] advance;
  
  private MagicalState(int heads) {
    advance = new int[heads];
    for(int i = 0; i < heads; i++) {
      advance[i] = 1;
    }
  }

  public char getToken() {
    return '!';
  }

  public String getName() {
    return "!-" + advance.length;
  }

  public Annotation getAnnotation() {
    return Annotation.EMPTY_ANNOTATION;
  }

  public Alphabet alphabet() {
    return MAGICAL_ALPHABET;
  }

  public double getWeight(Symbol r) throws IllegalSymbolException {
    if (r != MAGICAL_RESIDUE)
      return Double.NEGATIVE_INFINITY;
    return 0.0;
  }

  public void setWeight(Symbol r, double w) throws IllegalSymbolException,
  UnsupportedOperationException {
    alphabet().validate(r);
    throw new UnsupportedOperationException(
      "The weights are immutable: " + r.getName() + " -> " + w);
  }

  public Symbol sampleSymbol() {
    return MAGICAL_RESIDUE;
  }

  public void registerWithTrainer(ModelTrainer modelTrainer) {
  }

  public int[] getAdvance() {
    return advance;
  }
}
