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

public class MatrixCursor extends AbstractCursor {
  private double [][] matrix;
  private int index;
  private int dir;
  
  public int length() {
    return matrix[0].length - 1;
  }
  
  public double [] currentCol() {
    return matrix[index];
  }
  
  public double [] lastCol() {
    return matrix[index-dir];
  }

  public void advance() {
    super.advance();
    index += dir;    
  }
  
  public MatrixCursor(EmissionState [] states, ResidueList resList,
                      Iterator resIterator,
                      double [][] matrix, int dir) throws IllegalArgumentException {
    super(resIterator);
    
    if(matrix.length != (resList.length() + 2) ||
       matrix[0].length != states.length)
      throw new IllegalArgumentException("Incorrectly sized matrix");
    if(dir != -1 && dir != 1)
      throw new IllegalArgumentException("dir must be -1 or +1, not '" + dir + "'");
      
    this.matrix = matrix;
    this.dir = dir;
    this.index = (dir == 1) ?
                    0     :
                    resList.length() + 1;
  }
}
