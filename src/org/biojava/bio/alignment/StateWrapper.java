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

/**
 * A state that wraps up another state within a model so that it can appear in
 * another model.
 */
public interface StateWrapper extends EmissionState {
  /**
   * The state that is wrapped up.
   */
  EmissionState getWrappedState();
  /**
   * The state that conained the model that contained this state
   */
  ModelInState getContainingState();
}

