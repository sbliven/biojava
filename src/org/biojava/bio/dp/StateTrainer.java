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

import org.biojava.bio.seq.*;

/**
 * An object that can be used to train a state up.
 * <P>
 * This lets the state implementation handle counts or distributions
 * in the best way possible.
 */
public interface StateTrainer {
  /**
   * Registers that res was counted in this state.
   * <P>
   * This method may be called multiple times with the same residue. In this
   * case, the times should be summed.
   *
   * @param res the Residue seen
   * @param times the number of times to add
   */
  void addCount(Residue res, double times) throws IllegalResidueException;
  
  /**
   * Trains the state, given a null model.
   * <P>
   * This will use the information collected with multiple addCount calls, and
   * the null model to generate the new distribution for the state.
   *
   * @param nullModel the nullModel to use
   * @param weight  how many lots of the null model to add
   */
  void train(EmissionState nullModel, double weight) throws IllegalResidueException;
  
  /**
   * Clears all of the counts to zero.
   */
  void clearCounts();
}
