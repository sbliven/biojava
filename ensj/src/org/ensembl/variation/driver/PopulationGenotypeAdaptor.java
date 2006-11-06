/*
	Copyright (C) 2003 EBI, GRL

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

import org.ensembl.driver.Adaptor;
import org.ensembl.driver.AdaptorException;
import org.ensembl.variation.datamodel.Population;
import org.ensembl.variation.datamodel.PopulationGenotype;

/**
 * This adaptor provides database connectivity for PopulationGenotype objects.
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public interface PopulationGenotypeAdaptor extends Adaptor {

	final String TYPE = "population_genotype";
	
  /**
   * Retrieves a population genotype via its unique internal identifier.
   * @return PopulationGenotype with matching internalID or null if none found.
   */
   PopulationGenotype fetch(long internalID) throws AdaptorException;

  /**
   * Retrieves a population genotypes which appear in the specified population.
   * @return zero or more PopulationGenotypes from the specified population.
   */
  List fetch(Population population) throws AdaptorException;
}
