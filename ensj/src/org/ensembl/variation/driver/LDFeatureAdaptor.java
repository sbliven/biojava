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

import org.ensembl.datamodel.Location;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.FeatureAdaptor;
import org.ensembl.variation.datamodel.LDFeatureContainer;
import org.ensembl.variation.datamodel.VariationFeature;

/**
 * Provides database access for retrieving LDFeatures as
 * either a simple List or wrapped in an optimised 
 * LDFeaturesContainer.
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public interface LDFeatureAdaptor extends FeatureAdaptor {

	final static String TYPE = "ld_feature";
	
	
	/**
	 * Fetches LDFeatures that overlap with location.
	 * @param location location to filter on.
	 * @return zero or more VariationFeatures.
	 * @throws AdaptorException
	 * @see #fetchLDFeatureContainer(Location) for an alternative optimised way of
	 * using the retrieved data.
	 */
	List fetch(Location location) throws AdaptorException;
	
	
	/**
	 * Fetches LDFeatures that include the VariationFeature.
	 * @param variationFeature variation feature to search for.
	 * @return zero or more VariationFeatures.
	 * @throws AdaptorException
	 * @see #fetchLDFeatureContainer(VariationFeature) for an alternative optimised way of
	 * using the retrieved data.
	 */
	List fetch(VariationFeature variationFeature) throws AdaptorException;
	
	/**
	 * Retrieves an LDFeatureContainer that contains all the 
	 * LDFeatures that
	 * refer to the specified VariationFeature.
	 * 
	 * It provides convenient
	 * and optimised methods for accessing the data in several ways.
	 * @param variationFeature
	 * @return an LDFeatureContainer that contains zero or more VariationFeatures.
	 * @throws AdaptorException
	 */
	LDFeatureContainer fetchLDFeatureContainer(VariationFeature variationFeature)  throws AdaptorException;
	
	/**
	 * Retrieves an LDFeatureContainer that contains all the 
	 * LDFeatures that overlap with the specified location.
	 * 
	 * It provides convenient
	 * and optimised methods for accessing the data in several ways.
	 * @param location location filter
	 * @return an LDFeatureContainer that contains zero or more VariationFeatures.
	 * @throws AdaptorException
	 */
	LDFeatureContainer fetchLDFeatureContainer(Location location)  throws AdaptorException;
}
