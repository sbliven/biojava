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

package org.biojava.bio.program.phred;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.utils.*;
import org.biojava.bio.symbol.*;

/**
 * Title:        PhredSequence.java
 * Description:  An extension of SimpleSequence that implements Qualitative to hold Phred quality scores
 *               Consideration was made of using a CrossProduct alphabet to hold both the quality and DNA
 *               within the same sequence, however the CrossProduct alphabets cannot use an Infinite Alphabet
 *               such as the IntegerAlphabet.
 * Copyright:    Copyright (c) 2001
 * Company:      AgResearch
 * @author Mark Schreiber
 * @since 1.1
 */

public class PhredSequence extends SimpleSequence implements Qualitative{
  private SymbolList seq;
  private SymbolList qual;

  /**
   * Constructs a new PhredSequence. All inherited functions from SimpleSequence operate on the <b>sequence</b>
   * SymbolList, not the <b>quality</b> SymbolList
   * @param sequence - The biological polymer (Normally DNA)
   * @param quality - The quality data (Normally from an Integer Alphabet. NOTE: no checking is performed
   * to ensure sequence and quality are of the same length.
   * @param name - the name for the sequence.
   * @param urn - the URN for the sequence.
   * @param anno - the Annotation object for the sequence.
   */
  public PhredSequence(SymbolList sequence, SymbolList quality, String name, String urn, Annotation anno) {
    super(sequence,urn,name,anno);
    this.seq = sequence;
    this.qual = quality;
  }

  public SymbolList getQuality(){
   return qual;
  }
  public Symbol getQualityAt(int index) throws IndexOutOfBoundsException{
    return qual.symbolAt(index);
  }
}