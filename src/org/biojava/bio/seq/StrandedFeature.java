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
 
import java.io.*;
import java.lang.reflect.*;

import org.biojava.bio.symbol.*;
import org.biojava.utils.*;

/**
 * Adds the concept of 'strand' to features.
 * <P>
 * Strandedness only applies to some types of sequence, such as DNA. Any
 * implementation should blow chunks to avoid being added to a sequence for
 * which strand is a foreign concept.
 *
 * @author Matthew Pocock
 */
public interface StrandedFeature extends Feature {
  /**
   * Retrieve the strand that this feature lies upon.
   * <P>
   * This will be one of StrandedFeature.POSITIVE or NEGATIVE.
   *
   * @return one of the Strand constants
   */
  Strand getStrand();
  
  /**
   * Return a list of symbols that are contained in this feature.
   * <P>
   * The symbols may not be contiguous in the original sequence, but they
   * will be concatinated together in the resulting SymbolList.
   * <P>
   * The order of the Symbols within the resulting symbol list will be 
   * according to the concept of ordering within the location object.
   * <P>
   * If the feature is on the negative strand then the SymbolList will be
   * reversecomplemented as apropreate.
   *
   * @return  a SymbolList containing each symbol of the parent sequence contained
   *          within this feature in the order they appear in the parent
   */
  SymbolList getSymbols();
  
  /**
   * flag to indicate that a feature is on the positive strand.
   */
  static final Strand POSITIVE = new Strand("POSITIVE", +1, '+');

  /**
   * flag to indicate that a feature is on the negative strand.
   */
  static final Strand NEGATIVE = new Strand("NEGATIVE", -1, '-');
  
  /**
   * flag to indicate that a feature has an unknown strand.
   */
  static final Strand UNKNOWN = new Strand("UNKNOWN", 0, '.');
  
    /**
     * Template class for parameterizing the creation of a new
     * <code>StrandedFeature</code>.
     *
     * @author Matthew Pocock
     */

  public static class Template extends Feature.Template {
    public Strand strand;
  }
  
  /**
   * Class to represent the 'strandedness' of a feature.
   * <P>
   * Strandedness may be re-used in other situations, but basicaly what it means
   * is whether the feature has directionality, and if it does, does it travel
   * from its location min to max, or max to min.
   *
   * @author Matthew Pocock
   */
  public static class Strand implements Serializable {
    private final String text;
    private final int value;
    private final char token;
    
    // Should be private. workaround for known javac 1.2 bug
    // http://developer.java.sun.com/developer/bugParade/bugs/4262105.html
    Strand(String text, int value, char token) {
      this.text = text;
      this.value = value;
      this.token = token;
    }
    public String toString() {
      return text;
    }
    public int getValue() {
      return value;
    }
    public char getToken() {
      return token;
    }
    private Object writeReplace() throws ObjectStreamException {
      try {
        return new StaticMemberPlaceHolder(StrandedFeature.class.getField(text));
      } catch (NoSuchFieldException nsfe) {
        throw new NotSerializableException(nsfe.getMessage());
      }
    }
  }
}
