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

package org.ensembl.variation.datamodel;

import org.ensembl.datamodel.Persistent;
import org.ensembl.datamodel.Transcript;

/**
 * A TranscriptVariation object represents a variation feature which is in close
 * proximity to an Ensembl transcript.  
 * 
 * A TranscriptVariation object has several
 * attributes which define the relationship of the variation to the transcript.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public interface TranscriptVariation extends Persistent {

  /**
   * Transcript that is affected by this TranscriptVariation.
   * @return Transcript that is affected by this TranscriptVariation.
   */
	Transcript getTranscript();
	
  /**
   * VariationFeature associated with this transcript variation.
   * @return VariationFeature associated with this transcript variation.
   */
	VariationFeature getVariationFeature();
	
  /**
   * The pep allele string is a '/' delimited string of amino acid codes
   * representing the change to the peptide made by this variation.
   * A '-' represents one half of a insertion/deletion.  The
   * reference allele (the one found in the ensembl peptide) should
   * be first.
   * @return peptide allele string.
   */
	String getPeptideAlleleString();
	
  /**
   * The start position of this variation on the transcript in CDNA
   * coordinates.
   * @return the start position of this variation on the transcript in CDNA
   * coordinates.
   */
	int getCDNAstart();
	
  /**
   * The end position of this variation on the transcript in cdna
   * coordinates.
   * @return end position of this variation on the transcript in cdna
   * coordinates.
   */
	int getCDNAend();
	
  /**
   * the start position of this variation on the translation of the
   * associated transcript in peptide coordinates.  
   * @return start position of this variation on the translation of the
   * associated transcript in peptide coordinates.  
   */
  int getTranslationStart();
	
  /**
   * The end position of this variation on the translation of the associated
   * transcript in peptide coordinates.
   * @return end position of this variation on the translation of the associated
   * transcript in peptide coordinates.
   */
	int getTranslationEnd();
	
  /**
   * Consequence type of this transcript variation.
   * @return Consequence type of this transcript variation.
   */
	String getConsequenceType();
}
