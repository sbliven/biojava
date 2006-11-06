/*

    Copyright (C) 2001 EBI, GRL

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.ensembl.datamodel;

import java.util.List;

import org.ensembl.driver.AdaptorException;

/**
 * Translation. 
 */
public interface Translation extends Accessioned {

  Exon getStartExon();
  void setStartExon(Exon exon);

  /**
   * Returns startExon internal id. This is the same
   * as startExon.internalID if startExon is available.
   *
   * @return internalID of the startExon.
   */
  long getStartExonInternalID();

  /**
   * Sets startExon internal id, also sets the
   * startExon.internalID if startExon is available.
   */
  void setStartExonInternalID(long internalID);

  int getPositionInStartExon();
  void setPositionInStartExon(int position);

  Exon getEndExon();
  void setEndExon(Exon exon);

  /**
   * Returns endExon internal id. This is the same
   * as endExon.internalID if endExon is available.
   *
   * @return internalID of the endExon.
   */
  void setEndExonInternalID(long internalID);

  /**
   * Returns endExon internal id. This is the same
   * as endExon.internalID if endExon is available.
   *
   * @return internalID of the endExon.
   */
  long getEndExonInternalID();

  int getPositionInEndExon();
  void setPositionInEndExon(int position);

  void setTranscript(Transcript transcript);
  Transcript getTranscript();

  /**
   * Returns transcript internal id. This is the same
   * as transcript.internalID if transcript is available.
   *
   * @return internalID of the transcript.
   */
  long getTranscriptInternalID();

  /**
   * Sets transcript internal id, also sets the
   * transcript.internalID if transcript is available.
   */
  void setTranscriptInternalID(long internalID);

  List getSimilarityFeatures();

  /**
   * References to external databases
   * @return list of ExternalRef objects
   */
  List getExternalRefs();

  void setExternalRefs(List externalRefs);

  /**
   * Concatenatation of all exon sequences excluding the UTRs. 
   *
   * <p>In the case of "monkey exons", where the phases of consecutive exons
   * are incompatible, Ns are inserted. This padding ensures the previous
   * exon ends with a complete codon and the next exon begins with a complete codon.
   * The table below shows the cases where padding is added and the number of
   * Ns inserted. key: 'p' is a base in the previous exon, 'n' is a
   * base in the next exon.
   *
   * <pre>
   * end_phase phase padding
   *
   * 0         0     
   * 0         1     N
   * 0         2     NN
   * 0         -1
   *
   * 1         0     NN
   * 1         1     
   * 1         2     NNNN
   * 1         -1
   *
   * 2         0     N
   * 2         1     NN
   * 2         2     
   * 2         -1
   *
   * -1         0
   * -1         1
   * -1         2
   * -1         -1
  
   * </pre>
   *
   * @return sequence if available, otherwise null.  */
  Sequence getSequence();

  void setSequence(Sequence sequence);

  /**
   * Returns a list of coding locations where each corresponds to the coding part of a coding exon. 
   * Like getCodingLocation() but returns a list of locations where each element corresponds to an exon.
   * @see #getCodingLocation()
   * @return list of locations where each location corresponds to the coding part of an exon.  */
  List getCodingLocations();

  /**
   * Concation of the coding part of the coding exons. Like getCodingLocations() but returns a 
   * single, concatenated, location.
   * @see #getCodingLocations()
   * @return single location consisting of a concatenation of the coding parts of the coding exons.
   */
  Location getCodingLocation();

  /**
   * @return list of UTR locations at end of translation, or null if not available.
   */
  List getThreePrimeUTR();

  /**
   * @return list of UTR locations before translation, list is empty if unavailable.
   */
  List getFivePrimeUTR();

  Location getAminoAcidStart(int aminoAcidPosition);

  /**
   * Peptide sequence corresponding to the dna sequence returned 
   * by getSequence() with or without sequence edits applied.
   * 
   * Applies edits if transcript.isSequenceEditsEnabled().
   * 
   * @see Transcript#isSequenceEditsEnabled() 
   */
  String getPeptide();

  void setPeptide(String peptide);

  /**
   * Sequence edits for this translation.
   * 
   * Edits do not exist for all translations.
   * @return list of zero or more SequenceEdits.
   * @see SequenceEdit
   */
  List getSequenceEdits();

  /**
   * Return all attributes associated witht this translation.
   * @return zero or more Attributes associated with this translation.
   * @see Attribute
   */
  List getAttributes();

  /**
   * Return all Attributes where attribute.code==code.
   * @param code code to filter attributes against.
   * @return zero or more attributes.
   * @see Attribute
   */
  List getAttributes(String code);
  
  /**
   * Add attribute.
   * @param attribute attribute to add to attributes.
   */
  void addAttribute(Attribute attribute);

  /**
   * Remove attribute if present in attributes.
   * @param attribute attribute to be removed.
   * @return true if attribute was removed, otherwise false.
   */
  boolean removeAttribute(Attribute attribute);

  /**
   * Sets whether this is a known translation.
   * @param v Value to assign to known.  */
  void setKnown(boolean v);

  /**
   * Whether this is a known translation.
   * @return value of known.
   */
  boolean isKnown();

  /**
   * Interpro IDs associated with this translation.
   * @return array of zero or more interpro IDs.
   * @throws AdaptorException if a problem occurs retrieving the IDs from the database.
   */
  String[] getInterproIDs() throws AdaptorException;

  /**
   *  Interpro IDs associated with this translation.
   * @param strings Interpro IDs associated with this translation, empty array if
   * none are associated.
   */
  void setInterproIDs(String[] strings);
  
  /**
   * Get ProteinFeatures associated with this translation.
   * @return zero or more ProteinFeatures associated with this translation.
   * @see org.ensembl.datamodel.ProteinFeature
   */
  List getProteinFeatures();
}
