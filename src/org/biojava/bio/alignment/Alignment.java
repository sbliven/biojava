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


package org.biojava.bio.alignment;

import java.util.*;

import org.biojava.bio.seq.*;

/**
 * An alignment containing multiple sequences.
 * <P>
 * The alignment can be thought of a rectangular array of residues. Each
 * row is indexed by sequence and each column is indexed by residue offset.
 * <P>
 * This object uses bio-numbers, that is 1-length (a-b is a and b inclusive).
 */
public interface Alignment extends Annotatable {
  /**
   * The length of the alignment.
   *
   * @return the number of columns or -1 if the alignment is empty
   */
  int length();
  
  /**
   * The set of sequences in the alignment. A sequence can only be in once.
   *
   * @return  the Set of all sequences in the alignment
   */
  Set getSequences();
  
  /**
   * Retrieve a residue by sequence and column.
   * <p>
   * There is no concept of row index, only of sequence.
   *
   * @param seq the ResidueList to retrieve from
   * @param column  the index of the column to retrieve
   */
  Residue getResidue(ResidueList seq, int column);
  
  /**
   * Retrieve a single column of the alignment.
   * <P>
   * The keys of the map will be the sequences. The values will be the residues
   * at the specified column.
   *
   * @param column  the column index
   * @return  a Map of sequence to residue at that column
   */
  Map getColumn(int column);
  
  /**
   * Make a view onto this alignment.
   *
   * @param sequences the set of sequences to include
   * @param loc the Location to include
   * @return  a sub Alignment
   */
  Alignment subAlignment(Set sequences, Location loc);
}
