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

import java.util.Map;
import org.biojava.bio.seq.*;

/**
 * Extends the Alignment interface so that it is explicitly used to represent
 * a state path through an HMM, and the associated emitted sequence and
 * likelyhoods.
 */
public interface StatePath extends Alignment {
  /**
   * Alignment label for the emitted sequence.
   */
  public static final Object SEQUENCE = "SEQUENCE";

  /**
   * Alignment label for the state path.
   */
  public static final Object STATES   = "STATES";

  /**
   * Alignment label for the likelyhood at each step.
   */
  public static final Object SCORES   = "SCORES";
  
  /**
   * Return the overall score for this state-path and it's emissions.
   *
   * @return the score
   */
  public double getScore();
}
