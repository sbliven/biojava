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
 * Concrete implementation of a CrossProductSymbol that represents a single
 * point in a CrossProduct space.
 * <P>
 * You should not normaly have to instantiate one of these directly - rather
 * you would retrieve a CrossProductAlphabet from the AlphabetManager and use
 * the CrossProductSymbol objects that it creates.
 * <P>
 * You will need to use this class if you write your own implementation of
 * CrossProductAlphabet and don't wish to use
 * AlphabetManager.generateCrossProductSymbol().
 *
 * @author Matthew Pocock
 */

class AtomicCrossProductSymbol
extends SimpleCrossProductSymbol
implements AtomicSymbol {
  /**
   * Construct a new AtomicCrossProductSymbol instance.
   *
   * @param token the token for this symbol
   * @param name the name for this symbol
   * @param symList a List of AtomicSymbol instances
   */
  public AtomicCrossProductSymbol(char token, List symList) {
    super(token, symList);
  }
}
