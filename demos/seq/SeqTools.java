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
import java.util.*;
 
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;

/**
 * Common stuff that the demos rely on.
 *
 * @author Matthew Pocock
 */
public class SeqTools {
  /**
   * Creates a random DNA sequence.
   *
   * @param length  the number of residues in the sequence
   * @return the generated sequence
   */
  public static ResidueList createResidueList(int length)
  throws IllegalResidueException {
    List l = new ArrayList(length);
    for(int i = 0; i < length; i++) {
      l.add(DNATools.forIndex((int) (4.0*Math.random())));
    }
    return new SimpleResidueList(DNATools.getAlphabet(), l);
  }
}
