/*
	Copyright (C) 2005 EBI, GRL

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

import java.util.List;

import org.ensembl.datamodel.Transcript;
import org.ensembl.driver.Adaptor;
import org.ensembl.driver.AdaptorException;

/**
 * Adaptor for retrievning and computing AlleleConsequences.
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public interface AlleleConsequenceAdaptor extends Adaptor {
  
  /**
   * Creates the AlleleTranscriptConsequences that result from applying the
   * AlleleFeature to the transcript.
   * 
   * An AlleleFeature may cause zero, one or more consequences on the transcript
   * depending on whether and where it hits the transcript.
   * 
   * @param transcript
   *          transcript of interest.
   * @param alleleFeatures
   *          zero or more AlleleFeatures to apply to the transcript.
   * @return list of zero or more AlleleTranscriptConsequences representing the
   *         consequences of the alleles on the transcript.
   * @throws AdaptorException
   */
  List fetch(Transcript transcript,
      List alleleFeatures) throws AdaptorException;

  /**
   * Return the AlleleConsequences for the transcript.
   * 
   * A transcript may have zero or more AlleleConsequences. 
   * 
   * All AlleleFeatures that overlap the transcript or it's flanking
   * regions (5000 bases up and downstream) are used.
   * 
   * @param transcript
   *          transcript of interest.
   * @return list of zero or more AlleleConsequences representing the
   *         consequences of relevant alleles on the transcript.
   * @throws AdaptorException
   */
  List fetch(Transcript transcript) throws AdaptorException;
  
  final static String TYPE = "allele_consequence"; 
}
