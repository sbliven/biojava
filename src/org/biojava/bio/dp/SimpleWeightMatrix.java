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
import java.io.Serializable;

import org.biojava.bio.symbol.*;

public class SimpleWeightMatrix implements WeightMatrix, Serializable {
  private EmissionState [] columns;
  private Alphabet alpha;

  public Alphabet alphabet() {
    return alpha;
  }

  public int columns() {
    return this.columns.length;
  }
  
  public EmissionState getColumn(int column) {
    return columns[column];
  }

  public SimpleWeightMatrix(Alphabet alpha, int columns, StateFactory sFact)
  throws IllegalAlphabetException {
    this.alpha = alpha;
    this.columns = new EmissionState[columns];
    int [] advance = { 1 };
    for(int i = 0; i < columns; i++) {
      this.columns[i] = sFact.createState(alpha, advance, i + "");
    }
  }
}
