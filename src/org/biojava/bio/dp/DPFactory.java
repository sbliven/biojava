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

import org.biojava.bio.*;

public class DPFactory {
  public static DP createDP(MarkovModel model)
  throws IllegalArgumentException, BioException {
    int heads = model.heads();
    MarkovModel flat = DP.flatView(model);
    if(heads == 1) {
      return new SingleDP(flat);
    } else if(heads == 2) {
      return new PairwiseDP(flat);
    } else {
      throw new IllegalArgumentException(
        "Can't create DPFactory for models with " + heads + " heads"
      );
    }
  }
}
