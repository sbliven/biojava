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

package org.ensembl.driver;

import java.util.List;

import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.MiscFeature;
import org.ensembl.datamodel.MiscSet;

/**
 * Adaptor for retrieving MiscFeatures.
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public interface MiscFeatureAdaptor extends FeatureAdaptor {

  final static String TYPE = "misc_feature"; 
  
  /**
   * @return A MiscFeature matching the internalID, or null if non found.
   */
  MiscFeature fetch(long internalID) throws AdaptorException;
  
  
  /**
   * Fetch all MiscFeatures lying in the location and corresponding to the miscSet
   * @param location location to filter against.
   * @param miscSet MiscSet to filter against.
   * @return zero or more MiscFeatures.
   * @throws AdaptorException
   */
  List fetch(Location location, MiscSet miscSet) throws AdaptorException;
  
  /**
   * Fetch all MiscFeatures corresponding to the miscSet.
   * @param miscSet MiscSet to filter against.
   * @return zero or more MiscFeatures.
   * @throws AdaptorException
   */
  List fetch(MiscSet miscSet) throws AdaptorException;
  
  /**
   * Fetch all MiscFeatures corresponding to the type.
   * @param type type to filter against.
   * @throws AdaptorException
   */
  List fetchByAttributeType(String type)  throws AdaptorException;
    
  /**
   * Fetch all MiscFeatures corresponding to the type and value.
   * @param type type to filter against.
   * @param value value to filter against.
   * @throws AdaptorException
   */
    List fetchByAttributeTypeAndValue(String type, String value)  throws AdaptorException;
  
  
}
