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
 * Transcript. 
 */
public interface Transcript extends Accessioned, Feature {

  List getExons();

  void setExons(List exons);

  /**
   * Concatenation of all exon locations.
   * @return list location containing a contatenation of all the exon locations.
   */
  Location getCDNALocation();

  /**
   * Returns combined length of all exons.
   */
  int getLength();

  /**
  * @return dispaly name if available, otherwise null
  */
  String getDisplayName();

  void setDisplayName(String displayName);

  /**
   * @return list of UTR locations at end of translation, or null if not available.
   */
  List getThreePrimeUTR();

  /**
   * @return list of UTR locations before translation, or null if not available.
   */
  List getFivePrimeUTR();

  Gene getGene();

  /**
   * Sets gene and geneInternalID.
   */
  void setGene(Gene gene);

  /**
   * External refs for this transcript and it's translation
   * if is has one. This is the same as getExternalRefs(true).
   * Use getExternalRefs(false) if you only want
   * external refs for the transcript and not the translation.
   * @return list of ExternalRef objects, an empty list if none available.
   */
  public List getExternalRefs();

  /**
   * External refs for this transcript and optionally
   * those of it's translation (if the transcript has one.)
   * @param includeTranslation whether to include external refs
   * for the translation.
   * @return list of ExternalRef objects, an empty list if none available.
   */
  public List getExternalRefs(boolean includeTranslation);

  /**
   * Returns gene internal id. This is the same
   * as gene.internalID if gene is available.
   *
   * @return internalID of the gene.
   */
  long getGeneInternalID();

  /**
   * Sets gene internal id, also sets the
   * gene.internalID if gene is available.
   */
  void setGeneInternalID(long geneInternalID);

  /**
   * Returns translation internal id. This is the same
   * as translation.internalID if translation is available.
   *
   * @return internalID of the translation.
   */
  long getTranslationInternalID();

  /**
   * Sets translation internal id, also sets the
   * translation.internalID if translation is available.
   */
  void setTranslationInternalID(long translationInternalID);

  Translation getTranslation();

  /**
   * Sets translation and translationInternalID.
   */
  void setTranslation(Translation newTranslation);

  /*
   * Returns the transcript sequence with or without 
   * sequence edits taken into account depending on the 
   * state of the transcript.
   * 
   * If sequence edits are disabled then return the 
   * concatenation of all the exon sequences.
   * 
   * Otherwise if sequence edits are enabled and one 
   * or sequence edits exist then these are applied to
   * the concated exon sequence.
   * 
   * @return sequence if available, otherwise null.
   */
  Sequence getSequence();

  void setSequence(Sequence sequence);

  /**
   * Whether sequence edits are enabled.
   * 
   * If sequenceEditsEnabled=false then getSequence()
   * returns the concatenated exon sequence.
   * 
   * If sequenceEditsEnabled=true and sequence edits
   * exit for this transcritp then getSequence()
   * returns the concatenated exon sequence with the edits
   * applied.
   * 
   * @return whether sequence edits are enabled. True by default.
   * @see #getSequence()
   * @see #getSequenceEdits()
   */
  boolean isSequenceEditsEnabled();

  /**
   * Set whether sequence edits should be applied to sequence.
   * @param sequenceEditsEnabled whether sequence edits are enabled.
   * @see #getSequence()
   */
  void setSequenceEditsEnabled(boolean sequenceEditsEnabled);

  /**
   * Sequence edits for this transcript.
   * 
   * There may be no sequence edits available for 
   * the transcripts.
   * @return list of zero or more SequenceEdits.
   * @see SequenceEdit
   */
  List getSequenceEdits();

  /**
   * Return all attributes associated witht this transcript.
   * @return zero or more Attributes associated witht this transcript.
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
   * Whether this is a known transcript. Transcript are known if their
   * corresponding translation is known.
   * @return value of known.
   */
  boolean isKnown();

  /**
   * Interpro IDs associated with this transcript.
   * @return array of zero or more interpro IDs.
   * @throws AdaptorException if a problem occurs retrieving the IDs from the database.
   */
  String[] getInterproIDs() throws AdaptorException;

  
  /**
   * Returns the status of the transcript.
   * 
   * Delegates to getStatus(). 
   * @deprecated since version 34.1. Use getStatus() instead.
   * @see #getStatus()
   */
  String getConfidence();
  
  /**
   * The status of the transcript.
   * 
   * Normally be one of these Strings: 
   * 'KNOWN','NOVEL','PUTATIVE','PREDICTED'.
   * @return status of the transcript.
   */
	String getStatus();
	
	/**
   * Set the status of the transcript.
   * @param status status of the transcript.
   */
	void setStatus(String status);

	/**
	 * Supporting features used to build this transcript.
	 * 
	 * Supporting features are a mixture of DnaDnaAlignment
	 * and DnaProteinAlignment.
	 * @return zero or more supporting features.
	 * @see DnaDnaAlignment
	 * @see DnaProteinAlignment
	 */
	List getSupportingFeatures();
	
	
	/**
	 * Set the biological type of the gene.
	 * @param type biological type.
	 */
	void setBioType(String type);
	
	/**
	 * Return the biological type of the gene.
	 * @return biological type.
	 */
	String getBioType();
	
}
