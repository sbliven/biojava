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

/**
 * An abstract instance of a single-head DP cursor.
 *
 * @author Matthew Pocock
 */
abstract class AbstractCursor implements DPCursor {
  private Iterator resIterator;
  
  private Symbol currentRes;
  private Symbol lastRes;
  
  public Symbol currentRes() {
    return currentRes;
  }
  
  public Symbol lastRes() {
    return lastRes;
  }
  
  public boolean canAdvance() {
    return resIterator.hasNext() || currentRes != MagicalState.MAGICAL_RESIDUE;
  }
  
  public void advance() {
    lastRes = currentRes;
    currentRes = (resIterator.hasNext()) ? (Symbol) resIterator.next()
                                         : MagicalState.MAGICAL_RESIDUE;
  }
  
  public AbstractCursor(Iterator resIterator) {
    this.resIterator = resIterator;
    this.currentRes = MagicalState.MAGICAL_RESIDUE;
    this.lastRes = MagicalState.MAGICAL_RESIDUE;
  }
}
