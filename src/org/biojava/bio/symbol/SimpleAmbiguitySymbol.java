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

import java.io.Serializable;
import org.biojava.bio.*;

/**
 * A no-frills implementation of AmbiguitySymbol.
 * <P>
 * This implementation is backed by an alphabet.
 *
 * @author Matthew Pocock
 */
class SimpleAmbiguitySymbol
extends SimpleSymbol
implements AmbiguitySymbol, Serializable {
  private Alphabet matching;

  public Alphabet getMatchingAlphabet() {
    return this.matching;
  }
  
  public SimpleAmbiguitySymbol(
    char token,
    String name,
    Annotation ann,
    Alphabet matching
  ) {
    super(token, name, ann);
    this.matching = matching;
  }
}
