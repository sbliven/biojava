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

package org.biojava.bio.symbol;

import java.io.Serializable;

/**
 * Encapsulates an edit operation on a SymbolList.
 * <P>
 * All edits can be brokend down into a series of operations that change
 * contiguous blocks of the sequence. The edit replaces getLength symbols
 * starting with that at getPos with the symbols in getReplacement. This
 * representation allows symbols to be replaced, inserted or deleted,
 * depending on the relative lengths of getLength and
 * getReplacement().lenght().
 * <P>
 * The position of the offset should be between 1 and length+1. An edit at
 * 1 will either insert, change or delete symbols at the begining of the
 * list. An edit at length+1 should have a length of 0, and can be used to
 * extend the list. pos + length should always be <= (symL.length+1).
 *
 * @author Matthew Pocock
 */
public final class Edit implements Serializable {
  public final int pos;
  public final int length;
  public final SymbolList replacement;
  
  public Edit(int pos, int length, SymbolList replacement) {
    this.pos = pos;
    this.length = length;
    this.replacement = replacement;
  }
}
