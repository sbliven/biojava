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
 * A state in a markov process that has an emission spectrum.
 * <P>
 * These states emit residues with a probability distribution over an alphabet.
 * They are the states that actualy make your observed sequence.
 */
public interface EmissionState extends State {
  /**
   * The alphabet from which this state emits residues.
   *
   * @return  the Alphabet associated with this state
   */
  public Alphabet alphabet();
  
  /**
   * Return the log probability or odds that Residue r is emited by this state.
   *
   * @param r the Residue emitted
   * @return  the log-odds of emitting that residue
   * @throws IllegalResidueException if r is not from this state's alphabet
   */
  public double getWeight(Residue r) throws IllegalResidueException;
  
  /**
   * Set the log probability or odds that Residue r is emited by this state.
   *
   * @param r the Residue emitted
   * @param w  the log-odds of emitting that residue
   * @throws IllegalResidueException if r is not from this state's alphabet
   * @throws IllegalOperationException if this state does not allow weights to be tampered with
   */
  public void setWeight(Residue r, double w)
  throws IllegalResidueException, UnsupportedOperationException;

  /**
   * Sample a residue from this state's probability distribution.
   *
   * @return the residue sampled
   */
  public Residue sampleResidue() throws SeqException;
}
