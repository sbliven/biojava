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

/**
 * A backpointer.
 *
 * @author Matthew Pocock
 */
public class BackPointer {
  public final State state;
  public final BackPointer back;
  public final double score;
    
  public BackPointer(State state, BackPointer back, double score) {
    this.state = state;
    this.back = back;
    this.score = score;
    if(back == null) {
      throw new NullPointerException(
        "Can't construct backpointer for state " + state.getName() +
        " with a null source"
      );
    }
  }
  
  public BackPointer(State s) {
    this.state = s;
    this.back = this;
    this.score = Double.NaN;
  }
}

