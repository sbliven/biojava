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
import org.biojava.bio.seq.tools.*;

/**
 * A thing that can make states.
 * <P>
 * This decouples programs from needing to know what implementation of EmissionState
 * to instantiate for a given alphabet. It also lets you parameterise model creation
 * for things like profile HMMs.
 */
public interface StateFactory {
  EmissionState createState(Alphabet alpha, int [] advance, String name);
  
  static StateFactory DEFAULT = new DefaultStateFactory();
  
  class DefaultStateFactory implements StateFactory {
    public EmissionState createState(Alphabet alpha, int [] advance, String name) {
      AbstractState state;
    
      if(alpha == DNATools.getAlphabet()) {
        state = new DNAState();
      } else if(alpha == DNATools.getAmbiguity()) {
        state = new AmbiguityState();
      } else {
        state = new SimpleState(alpha, advance);
      }
      state.setName(name);
    
      return state;
    }
  };
}


