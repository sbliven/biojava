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
 * A Dot state that you can make and use.
 * <P>
 * Dot states emit no sequence. They are there purely to make the wireing
 * of the model look neater, and to cut down the number of combinatorial
 * transitions that can so easily swamp models.
 */
public class SimpleDotState extends SimpleResidue implements DotState {
  public SimpleDotState(char symbol, String name, Annotation annotation) {
    super(symbol, name, annotation);
  }
  
  public SimpleDotState(String name) {
    super(name.charAt(0), name, Annotation.EMPTY_ANNOTATION);
  }
}
