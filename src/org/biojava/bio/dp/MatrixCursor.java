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
  private final SingleDPMatrix matrix;
  private final int dir;

  private int index;
  
  public int length() {
    return matrix.scores[0].length - 1;
  }
  
  public ResidueList resList() {
    return matrix.resList()[0];
  }
  
  public double [] currentCol() {
    return matrix.scores[index];
  }
  
  public double [] lastCol() {
    return matrix.scores[index-dir];
  }

  public void advance() {
    super.advance();
    index += dir;    
  }
  
  public MatrixCursor(
    SingleDPMatrix matrix,
    Iterator resIterator,
    int dir
  ) throws IllegalArgumentException {
    super(resIterator);
    
    this.matrix = matrix;
    this.dir = dir;
    this.index = (dir == 1) ?
                    0     :
                    length() + 2;
  }
}
