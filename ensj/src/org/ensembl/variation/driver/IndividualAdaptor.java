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
import org.ensembl.variation.datamodel.Individual;
import org.ensembl.variation.datamodel.Population;

/**
 * This adaptor provides database connectivity for Individual objects.
 * Individuals may be retrieved from the ensembl variation database by several
 * means using this module.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 */
public interface IndividualAdaptor extends Adaptor {

	final static String TYPE = "individual";

	/**
	 * Retrieves an individual via its internal identifier.
	 * 
	 * @return Individual with specified internalID or null if none found.
	 */
	Individual fetch(long internalID) throws AdaptorException;
	
  /**
   * Fetch Individuals by internal ids.
   * @param internalIDs internal IDs of individuals.
   * @return zero or more populations corresponding to _internalIDs.
   */
  List fetch(long[] internalIDs) throws AdaptorException;

	/**
	 * Retrieves all individuals with the specified name. Individual names may
	 * be non-unique.
	 * 
	 * @return zero or more Individuals with specified name.
	 */
	List fetch(String name) throws AdaptorException;

	/**
	 * Retrieves all individuals from a specified population
	 * 
	 * @return zero or more Individuals from the specified population.
	 */
	List fetch(Population population) throws AdaptorException;

	/**
	 * Retrieves all individuals which are children of a provided parent
	 * individual. This function operates under the assumptions that Male
	 * individuals can only be fathers, Female individuals can only be mothers
	 * and Unknown individuals can only be one or the other - not both.
	 * 
	 * @return zero or more children of the specified parent.
	 */
	List fetch(Individual parent) throws AdaptorException;

	/**
	 * Fetch the populations that the individual belongs to.
	 * 
	 * @param individual individual to fetch populations
	 * @return zero or more Populations that _individual_ belongs to.
	 */
	List fetchPopulations(Individual individual) throws AdaptorException;

}
