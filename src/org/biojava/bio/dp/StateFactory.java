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


package org.biojava.bio.alignment;

import java.util.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;

public class StateFactory {
  public static AbstractState createState(Alphabet alpha) {
    if(alpha == DNATools.getAlphabet())
      return new DNAState();
    else if(alpha == DNATools.getAmbiguity())
      return new AmbiguityState();
    return new SimpleState(alpha);
  }
}
  

