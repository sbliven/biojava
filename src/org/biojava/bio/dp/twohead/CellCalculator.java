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

package org.biojava.bio.dp.twohead;

import org.biojava.bio.symbol.*;
import org.biojava.bio.dp.*;

/**
 * The interface for all functions that can calculate the 'scores' array for
 * a given cell.
 *
 * @author Matthew Pocock
 */
public interface CellCalculator {
  /**
   * Initialize the cell at [0][0] to the recursion initial parameters.
   */
  public void initialize(Cell [][] cells)
  throws
    IllegalSymbolException,
    IllegalAlphabetException,
    IllegalTransitionException;

  /**
   * Calculate the 'scores' array in the cell at cells[0][0].
   * <P>
   * These objects implement the actual cell-by-cell recursions, such as
   * forwards or viterbi.
   *
   * @param cells the array of cells to read from, with the cell to update
   *        at 0,0
   */
  public void calcCell(Cell [][] cells)
  throws
    IllegalSymbolException,
    IllegalAlphabetException,
    IllegalTransitionException;
}
