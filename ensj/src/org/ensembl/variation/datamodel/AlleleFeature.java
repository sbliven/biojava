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

import org.ensembl.datamodel.Feature;
import org.ensembl.driver.AdaptorException;

/**
 * An allele located on a genome.
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public interface AlleleFeature extends Feature {

  /**
   * Return the allele string.
   * @return allele as a string.
   */
  String getAlleleString();
  
  /**
   * Variation this allele belongs to.
   * @return internal ID of the variation this allele belongs to.
   */
  long getVariationInternalID();
  
  /**
   * Variation this allele belongs to.
   * @return variation this allele belongs to.
   */
  Variation getVariation() throws AdaptorException;
  
}
