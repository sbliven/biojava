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
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.DNATools;

/**
 * A thing that can make states.
 * <P>
 * This decouples programs from needing to know what implementation of EmissionState
 * to instantiate for a given alphabet. It also lets you parameterise model creation
 * for things like profile HMMs.
 *
 * @author Matthew Pocock
 */
public interface StateFactory {
  /**
   * Generate a new EmissionState as requested.
   *
   * @param alpha  the emission alphabet for the state
   * @param advance the advance array
   * @param name  the state name
   * @throws IllegalAlphabetException if the factory is unable to generate a
   *         state for the required alphabet
   */
  EmissionState createState(Alphabet alpha, int [] advance, String name)
  throws IllegalAlphabetException;
  
  /**
   * The default state factory object.
   * <P>
   * You may wish to alias this within your scripts with something like:
   * StateFactory sFact = StateFactory.DEFAULT; sFact.createState(...);
   */
  static StateFactory DEFAULT = new DefaultStateFactory();
  
  /**
   * The default state factory implementation.
   * <P>
   * It knows about hand-optimized implementations for some alphabets (like DNA)
   * without the optimized classes needing to be exposed from the DP package.
   *
   * @author Matthew Pocock
   */
  class DefaultStateFactory implements StateFactory {
    public EmissionState createState(Alphabet alpha, int [] advance, String name)
    throws IllegalAlphabetException {
      AbstractState state;
      if(! (alpha instanceof FiniteAlphabet) ) {
        throw new IllegalAlphabetException(
          "The default StateFactory implementation can only produce states over " +
          "finite alphabets, not " + alpha.getName()
        );
      }
      FiniteAlphabet fa = (FiniteAlphabet) alpha;
      
      if(
        fa == DNATools.getAlphabet() &&
        advance.length == 1 &&
        advance[0] == 1
      ) {
        state = new DNAState();
      } else if(
        fa == DNATools.getAmbiguity() &&
        advance.length == 1 &&
        advance[0] == 1
      ) {
        state = new AmbiguityState();
      } else {
        state = new SimpleState(fa, advance);
      }
      state.setName(name);
    
      return state;
    }
  };
}


