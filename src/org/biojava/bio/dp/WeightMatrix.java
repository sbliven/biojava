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

import org.biojava.bio.seq.*;

/**
 * A log odds weight matrix.
 * <P>
 * The weight matrix uses computer-coordinates. Thus, a 10 column weight matrix
 * has columns (0 - 9). I guess that if you try to access columns outside the
 * logical range, the implementation may throw an IndexOutOfBoundsException.
 * <P>
 * A weight matrix is always over a finite alphabet of symbols, so that you can think
 * of each row of the matrix as being for a different residue.
 */
public interface WeightMatrix {
  /**
   * The alphabet for the sequences that this weight matrix models.
   *
   * @return  the Alphabet
   */
  FiniteAlphabet alphabet();
  
  /**
   * Return the weight for a given residue at a given column.
   * <P>
   * This is the log-probability or log-odds of observing a given residue
   * at a given position.
   *
   * @param res the Residue
   * @param column  the column
   * @return  the weight
   * @throws  IllegalResidueException if the residue is not part of the alphabet
   */
  double getWeight(Residue res, int column) throws IllegalResidueException;
  
  /**
   * Sets the weight for a given residue at a given column.
   * <P>
   * This is the log-probability or log-odds of observing a given residue
   * at a given position.
   *
   * @param res the Residue
   * @param column  the column
   * @param weight  the new weight
   * @throws  IllegalResidueException if the residue is not part of the alphabet
   * @throws UnsupportedOperationException if the weight-matrix is read-only
   */
  void setWeight(Residue res, int column, double weight)
         throws IllegalResidueException, UnsupportedOperationException;
  
  /**
   * The number of columns modeled by the weight matrix.
   *
   * @return the number of columns
   */
  int columns();  
}
