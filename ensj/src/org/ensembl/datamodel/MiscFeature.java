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

package org.ensembl.datamodel;

import java.util.List;

/**
 * MiscFeatures are extremely general features with a location and an
 * arbitrary group of attributes.  They are grouped with other features of
 * the same 'type' through the use of MiscSets.  Attributes are attached in
 * the fom of Attribute objects.  See MiscFeatureAdaptor for ways to fetch
 * MiscFeatures.
 *
 * @see org.ensembl.driver.MiscFeatureAdaptor
 * @see org.ensembl.datamodel.Attribute
 * @see org.ensembl.datamodel.MiscSet
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public interface MiscFeature extends Feature {

  /**
   * Attributes associatted with this feature.
   * @return attributes associatted with this feature, empty list if non exist
   */
  List getAttributes();

  /**
   * Get attributes with the specified code
   * @param code code to filter attributes against
   * @return zero or more attributes.
   */
  List getAttributes(String code);

  /**
   * Adds attribute.
   * @param attribute
   */
  void add(Attribute attribute);

  /**
   * MiscSets associated with this feature.
   * @return MiscSets associated with this feature, or empty list if non available.
   */
  List getMiscSets();

  /**
   * Adds MiscSet.
   * @param miscSet a MiscSet this feature belongs to.
   */
  void add(MiscSet miscSet);
  
  

}
