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

package org.ensembl.variation.driver;

import java.util.List;

import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Transcript;
import org.ensembl.driver.Adaptor;
import org.ensembl.driver.AdaptorException;

/**
 * Used to retrieve AlleleFeatures.
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public interface AlleleFeatureAdaptor extends Adaptor {

  /**
   * Return AlleleFeatures overlapping the location.
   * @param location location filter.
   * @return zero or more AlleleFeatures overlapping the location.
   */
  List fetch(Location location) throws AdaptorException;
  
  /**
   * Return all the AlleleFeatures hitting the transcript and it's
   * 5prime and 3prime flanking regions.
   * 
   * We use the transcripts location as defined by the start
   * of it's first exon to the end of it's last exon (including introns).
   * 
   * @param transcript transcript of interest.
   * @param flankSize flanking region size.
   * @return zero or more AlleleFeatures hitting the transcript or it's 
   * flanking regions.
   */
  List fetch(Transcript transcript, int flankSize) throws AdaptorException;
  
  final String TYPE = "allele_feature";
}
