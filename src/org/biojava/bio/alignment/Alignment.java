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
 * Alternatively, it can be thought of as a residue list where each residue is
 * a list of residues in that column - modeled as a CrossProductResidue.
 * <P>
 * To create gapped alignments, use ResidueLists with gaps. The most flexible
 * way to do this will be to leverage GappedResidueList objects.
 * <P>
 * This object uses bio-numbers, that is 1-length (a-b is a and b inclusive).
 */
public interface Alignment extends ResidueList {
  /**
   * The list of ResidueLists in the alignment.
   * <P>
   * The index in the list is the same as the index in the alignment.
   * Each ResidueList object will only be in the alignment once. However, a
   * single underlying ResidueList may have more than one view within an
   * alignment, each represented by a different GappedResidueList.
   *
   * @return  the List of all ResidueLists in the alignment
   */
  List getResidueLists();
  
  /**
   * Retrieve a residue by ResidueList and column.
   *
   * @param res the ResidueList to retrieve from
   * @param column  the index of the column to retrieve
   */
  Residue getResidue(ResidueList res, int column);
  
  /**
   * Make a view onto this alignment.
   *
   * @param residueLists the set of sequences to include
   * @param loc the Location to include
   * @return  a sub Alignment
   */
  Alignment subAlignment(List residueLists, Location loc);
}
