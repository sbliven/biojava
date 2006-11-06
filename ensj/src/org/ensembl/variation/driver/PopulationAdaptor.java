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

import org.ensembl.driver.Adaptor;
import org.ensembl.driver.AdaptorException;
import org.ensembl.variation.datamodel.Population;

/**
 * Adaptor for retrieving variation populations. 
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public interface PopulationAdaptor extends Adaptor {

  final static String TYPE = "population";

  /**
   * Fetch population by internal id.
   * @param internalID internal ID of the population.
   * @return population with specified internal id or null if none found.
   */
  Population fetch(long internalID) throws AdaptorException;

  /**
   * Fetch populations by internal id.
   * @param internalIDs internal IDs of populations.
   * @return zero or more populations corresponding to _internalIDs.
   */
  List fetch(long[] internalIDs) throws AdaptorException;
  
  /**
   * Fetch population by name.
   * @param name name of the population.
   * @return population with specified name or null if none found.
   */
  Population fetch(String name) throws AdaptorException;

  /**
   * Retrieve all super populations for a given sub population.
   * @param subPopulation sub population.
   * @return zero or more super populations.
   * @throws AdaptorException
   */
  List fetchSuperPopulations(Population subPopulation) throws AdaptorException;
  
  /**
   * Retrieve all sub populations for a given super population.
   * @param superPopulation super population.
   * @return zero or sub populations.
   * @throws AdaptorException
   */
  List fetchSubPopulations(Population superPopulation) throws AdaptorException;

}
