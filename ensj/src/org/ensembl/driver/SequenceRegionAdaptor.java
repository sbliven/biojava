/*
 * Copyright (C) 2002 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.driver;

import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.SequenceRegion;

/**
 * Adaptor for SequenceRegion objects.
 */
public interface SequenceRegionAdaptor {

	/**
	 * Fetch a SequenceRegion by its internalID.
	 * 
	 * @return A SequenceRegion matching the internalID, or null if non found.
	 */
	SequenceRegion fetch(long internalID) throws AdaptorException;

	/**
	 * Fetch a SequenceRegion by name and co-ordinate system.
	 * 
	 * @return The sequence region object represented by name and cs, or null.
	 */
	SequenceRegion fetch(String name, CoordinateSystem cs) throws AdaptorException;

	/**
		 * Fetch a SequenceRegion corresponding to a Location.
		 * 
		 * @return A SequenceRegion matching the Location, or null if non found.
		 */
	SequenceRegion fetch(Location loc) throws AdaptorException;

	/**
	 * Fetch all the sequence regions for a particular co-ordinate system.
	 * 
	 * @return An array of SequenceRegion objects representing all the sequence
	 *         regions in coordinate system cs. Use with caution!
	 */
	SequenceRegion[] fetchAllByCoordinateSystem(CoordinateSystem cs) throws AdaptorException;

	/**
	 * Get all the sequence regions that have an attribute with a particular code set,
	 * irrespective of what the value of the attribute is.
	 * @param code The attribute code to look for.
	 * @return The SequenceRegions with the attrbiute set.
	 * @throws AdaptorException
	 */
	SequenceRegion[] fetchAllByAttributeCode(String code) throws AdaptorException;

	/**
	 * Fetch all the sequence regions that have an attribute set to a certain value.
	 * @param code The code of the attribute in question.
	 * @param value The value that attribute [code] should have.
	 * @return All SequenceRegions with arrtibute [code] set to [value].
	 * @throws AdaptorException
	 */
	SequenceRegion[] fetchAllByAttributeValue(String code, String value) throws AdaptorException;


  /**
   * Sets the attributes on the seqRegion.
   * @param seqRegion seqRegion to set attributes on.
   * @throws AdaptorException
   */
  void fetchComplete(SequenceRegion seqRegion) throws AdaptorException;

	/**
	 * Name of the default SequenceRegionAdaptor available from a driver.
	 */
	final static String TYPE = "SequenceRegion";

}
