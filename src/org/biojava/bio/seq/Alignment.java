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

import java.util.*;

import org.biojava.bio.seq.*;

/**
 * An alignment containing multiple residue lists.
 * <P>
 * The alignment can be thought of a rectangular array of residues. Each
 * row is indexed by a label and each column is indexed by residue offset.
 * <P>
 * Alternatively, it can be thought of as a residue list where each residue is
 * a list of residues in that column - modeled as a Column object.
 * <P>
 * To create gapped alignments, use ResidueLists with gaps. The most flexible
 * way to do this will be to leverage GappedResidueList objects.
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
  List getLabels();
  
  /**
   * Retrieve a residue by label and column.
   *
   * @param res the ResidueList to retrieve from
   * @param column  the index of the column to retrieve
   * @return  the residue in the residue list associated with the label at the given column
   * @throws NoSuchElementException if there is no row for 'label'
   */
  Residue residueAt(Object label, int column)
  throws NoSuchElementException;
  
  /**
   * Retrieve a single row of the alignment by label.
   *
   * @return  a ResidueList that contains each token in a row of the alignment
   * @throws NoSuchElementException if there is no row for 'label'
   */
  ResidueList residueListForLabel(Object label)
  throws NoSuchElementException;
  
  /**
   * Make a view onto this alignment.
   *
   * @param residueLists the set of sequences to include
   * @param loc the Location to include
   * @return  a sub Alignment
   * @throws  NoSuchElementException if labels contains any item that is not a label
   */
  Alignment subAlignment(List labels, Location loc)
  throws NoSuchElementException;
  
  /**
   * Defines the particular type of CrossProductResidues that can be a column
   * in an alignment.
   *
   * @author Matthew Pocock
   */
  interface Column extends CrossProductResidue {
    /**
     * Return the same list as the alignment uses to label each ResidueList.
     * <P>
     * In this case, it labels each residue.
     *
     * @return  a List of labels for the residues
     */
    List getLabels();
  }
}

