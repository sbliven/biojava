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

package org.biojava.bio.symbol;

import java.util.*;
import java.io.*;

import org.biojava.bio.*;

/**
 * Concrete implementation of an ambiguous CrossProductSymbol, as returned
 * by a SimpleCrossProductAlphabet and InfiniteCrossProductAlphabet.
 *
 * @author Matthew Pocock
 */
public class AmbiguityCrossProductSymbol
extends SimpleCrossProductSymbol
implements AmbiguitySymbol {
  private CrossProductAlphabet matching;
  
  public Alphabet getMatchingAlphabet() {
    return matching;
  }
  
  public AmbiguityCrossProductSymbol(
    List l,
    char token,
    CrossProductAlphabet parent
  ) {
    super(l, token);
    List al = new ArrayList();
    for(Iterator i = l.iterator(); i.hasNext(); ) {
      Symbol s = (Symbol) i.next();
      if(s instanceof AmbiguitySymbol) {
        al.add(((AmbiguitySymbol) s).getMatchingAlphabet());
      } else {
        al.add(new SimpleAlphabet(Collections.singleton(s)));
      }
    }
    try {
      this.matching = new SimpleCrossProductAlphabet(al, parent);
    } catch (IllegalAlphabetException iae) {
      throw new BioError(iae, "Couldn't build alphabet");
    }
  }
}
