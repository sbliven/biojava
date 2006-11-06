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

/**
 * MiscSets represent classsifications or groupings of MiscFeatures.
 * Features are classified into sets essentially to define what they are and
 * how they may be used.  Generally MiscFeatures are retrieved on the basis
 * of their associated sets.
 *
 * @see org.ensembl.driver.MiscSetAdaptor
 * @see org.ensembl.datamodel.MiscFeature
 * @see org.ensembl.driver.MiscFeatureAdaptor
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public interface MiscSet extends Persistent {

  /**
   * Name of this MiscSet
   * @return name of the set.
   */
  String getName();


  /**
   * Name of this MiscSet
   * @param name of the set.
   */
  void setName(String name);
  
  /**
   * Description of this MiscSet.
   * @return description of this set
   */
  String getDescription();

  /**
   * Description of this MiscSet.
   * @param description description of this set
   */
  void setDescription(String description);
  
  /**
   * Code for this MiscSet.
   * @return unique code for this set.
   */
  String getCode();

  /**
   * Code for this MiscSet.
   * @param code unique code for this set.
   */
  void setCode(String code);
  
  /**
   * Maximum length of features in this set
   * @return Maximum length of features in this set
   */
  int getMaxFeatureLength();

  /**
   * Maximum length of features in this set
   * @param maxFeatures Maximum length of features in this set
   */
  void setMaxFeatureLength(int maxFeatures);

}
