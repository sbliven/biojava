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

package org.ensembl.variation.driver;

import java.util.List;

import org.ensembl.datamodel.Transcript;
import org.ensembl.driver.Adaptor;
import org.ensembl.driver.AdaptorException;
import org.ensembl.variation.datamodel.TranscriptVariation;
import org.ensembl.variation.datamodel.VariationFeature;


/**
 * This adaptor provides database connectivity for TranscriptVariation
 * objects.  
 *
 * TranscriptVariations which represent an association between a
 * variation and a Transcript may be retrieved from the Ensembl variation
 * database via several means using this class.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public interface TranscriptVariationAdaptor extends Adaptor {

	final static String TYPE = "transcript_variation";
	
  TranscriptVariation fetch(long internalID) throws AdaptorException;

  /**
   * Retrieves all TranscriptVariation objects associated with
   * provided Ensembl variation features. Attaches them to the given variation
   * features.
   * @return zero or more TranscriptVariations associated with the
   * variationFeature.
   */
  List fetch(VariationFeature variationFeature) throws AdaptorException;

  /**
   * Retrieves all TranscriptVariation objects associated with
   * provided transcript. Attaches them to the given variation
   * features.
   * @return zero or more TranscriptVariations associated with the
   * transcript.
   */
  List fetch(Transcript transcript) throws AdaptorException;

}
