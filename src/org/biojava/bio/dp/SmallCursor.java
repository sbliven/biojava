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
 * Constant-memory implementation of single-head DP cursor.
 *
 * @author Matthew Pocock
 */
class SmallCursor extends AbstractCursor {
  private final SymbolList resList;
  private double [] currentC;
  private double [] lastC;
  
  public SymbolList resList() {
    return resList;
  }
  
  public int length() {
    return resList.length();
  }
  
  public double [] currentCol() {
    return currentC;
  }
  
  public double [] lastCol() {
    return lastC;
  }

  public void advance() {
    super.advance();
    
    double [] v = lastC;
    lastC = currentC;
    currentC = v;
  }
  
  public SmallCursor(State [] states, SymbolList resList, Iterator resIterator) {
    super(resIterator);
    this.resList = resList;
    
    this.currentC = new double[states.length];
    this.lastC = new double[states.length];
  }
}
