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

package org.ensembl.variation.datamodel;

import java.util.List;

import org.ensembl.datamodel.Persistent;


/**
 * This is a class representing a single individual.  An individual may be
 * part of a population.  A pedigree may be constructed using the
 * father and mother attributes.
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public interface Individual extends Persistent {

  String getName();

  /**
   * Description of the individual.
   * @return Description of the individual.
   */
  String getDescription();

  /**
   * Gender.
   * @return gender.
   */
  String getGender();

  /**
   * Populations this individual belongs to.
   * @return zero or more Populations that this individual belongs to.
   */
  List getPopulations();

  /**
   * Father of this individual.
   * @return father, null if not available.
   */
  Individual getFather();

  /**
   * Mother of this individual.
   * @return mother, null if not available.
   */
  Individual getMother();
  

  /**
   * All children of this individual.
   * @return zero or more children Individuals.
   */
  List getChildren();

}
