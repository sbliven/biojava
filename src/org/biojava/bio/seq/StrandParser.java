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
package org.biojava.bio.seq;

public final class StrandParser {
  public static final StrandedFeature.Strand parseStrand(String strand) {
    if("+".equals(strand) || "1".equals(strand)) {
      return StrandedFeature.POSITIVE;
    } else if("-".equals(strand) || "-1".equals(strand) || "minus".equals(strand)) {
      return StrandedFeature.NEGATIVE;
    } else if(".".equals(strand) || "0".equals(strand) || "plus".equals(strand)) {
      return StrandedFeature.UNKNOWN;
    }

    throw new IllegalArgumentException("Not a legal strand: " + strand);
  }
}