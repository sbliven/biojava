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

abstract public class AbstractCursor implements DPCursor {
  private Iterator resIterator;
  
  private Residue currentRes;
  private Residue lastRes;
  
  public Residue currentRes() {
    return currentRes;
  }
  
  public Residue lastRes() {
    return lastRes;
  }
  
  public boolean canAdvance() {
    return resIterator.hasNext() || currentRes != DP.MAGICAL_RESIDUE;
  }
  
  public void advance() {
    lastRes = currentRes;
    currentRes = (resIterator.hasNext()) ? (Residue) resIterator.next()
                                         : DP.MAGICAL_RESIDUE;
  }
  
  public AbstractCursor(Iterator resIterator) {
    this.resIterator = resIterator;
    this.currentRes = DP.MAGICAL_RESIDUE;
    this.lastRes = DP.MAGICAL_RESIDUE;
  }
}
