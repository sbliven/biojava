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

import org.biojava.bio.symbol.*;

/**
 * Encapsulates the dynamic programmming matrix, and the context within algorithms work.
 * The cursor should put DP.MAGICAL_SYMBOL symbols at either end of the sequence.
 */
interface DPCursor {
  /**
   * The symbol list being looped over.
   */
  SymbolList resList();
  
  /**
   * The length of the sequence.
   * <P>
   * The matrix may allocate length+1 columns.
   */
  int length();
  
  /**
   * The current column of the matrix.
   */
  double [] currentCol();
  
  /**
   * The previous column.
   */
  double [] lastCol();
  
  /**
   * The current symbol.
   */
  Symbol currentRes();
  
  /**
   * The previous symbol.
   */
  Symbol lastRes();
    
  /**
   * Can we advance?
   */
  boolean canAdvance();
  
  /**
   * Advance.
   */
  void advance();
}
