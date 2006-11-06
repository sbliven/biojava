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

import java.util.List;

import org.ensembl.datamodel.Persistent;
import org.ensembl.driver.AdaptorException;

/**
 * A group of individuals.
 * 
 * Populations include phenotypic groups (e.g. people with
 * diabetes), ethnic groups (e.g. caucasians), individuals used in an assay
 * (e.g. subjects in experiment X), etc.
 *
 * Populations may be arranged into an arbitrary hierarchy of sub and super
 * populations.
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public interface Population extends Persistent {

  /**
   * Retrieves all populations which are conceptually a sub set of this
   * population.
   * @return zero or more sub populations.
   */
  List getSubPopulations() throws AdaptorException; 
  
  /**
   * Retrieves all populations which this population is a part of.
   * @return zero or more super populations.
   */
  List getSuperPopulations() throws AdaptorException; 

  /**
   * Population description.
   * @return description of this population.
   */
  String getDescription();

  /**
   * Population name.
   * @return name of the population.
   */
  String getName();

  /**
   * Population size.
   * @return population size, 0 if size is unknown.
   */
  int getSize();

  /**
   */
  /**
   * Set the population description.
   * @param description description of this population.
   */
  void setDescription(String description);

  /**
   * Set the population name.
   * @param name population name.
   */
  void setName(String name);

  /**
   * Set the population size.
   * @param size population size.
   */
  void setSize(int size);

}
