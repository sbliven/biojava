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
 * <p>
 * Encapsulates an edit operation on a SymbolList.
 * </p>
 *
 * <p>
 * All edits can be broken down into a series of operations that change
 * contiguous blocks of the sequence. This represent a one of those operations.
 * </p>
 * <p>
 * When applied, this Edit will replace 'length' number of symbols starting a
 * position 'pos' by the SymbolList 'replacement'. This allow to do insertions
 * (length=0), deletions (replacement=SymbolList.EMPTY_LIST) and replacements
 * (length>=1 and replacement.length()>=1).
 * </p>
 *
 *<p>
 * The pos and pos+length should always be valid positions on the SymbolList to
 * be edited (between 0 and symL.length()+1).<br>
 * To append to a sequence, pos=symL.length()+1, pos=0.<br>
 * To insert something at the beginning of the sequence, set pos=1 and
 * length=0.
 * </p>
 *
 * @author Matthew Pocock
 * @author Francois Pepin (docs)
 */
public final class Edit implements Serializable {
  public final int pos;
  public final int length;
  public final SymbolList replacement;

  /**
   * Create a new Edit.
   *
   * @param pos the start of the edit
   * @param length the length of the edit
   * @param replacement a SymbolList representing the symbols that replace those from pos to
   *        pos + length-1 inclusive
   */
  public Edit(int pos, int length, SymbolList replacement) {
    this.pos = pos;
    this.length = length;
    this.replacement = replacement;
  }
}
