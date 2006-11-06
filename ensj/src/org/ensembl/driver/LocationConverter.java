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

import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.SequenceRegion;

/**
 * Converts locations between different coordinate systems.
 */
public interface LocationConverter extends java.rmi.Remote, Adaptor {

	/**
	 * Create a new location in the target coordinate system that is equivalent
	* to the sourceLocation.
	 * @param sourceLocation
	 * @param targetCS This is where you want to convert to
	 * @param includeGaps you include or omit gaps that would be in the converted Location
	 * @param allList You may work just on the first node or on the full list
	 * @param includeSequenceRegion If true, includes sequenceRegion attribute in returned location.
	 * @return Location in _targetCS_ corresponding to _sourceLocation_
	 * @throws AdaptorException
	 */

	public Location convert(
		Location sourceLocation,
		CoordinateSystem targetCS,
		boolean includeGaps,
		boolean allList,
		boolean includeSequenceRegion)
		throws AdaptorException;

	/**
	 * Return the length of the used sequence region.
	 * 
	 * @param loc
	 * @return length by location
	 * @throws AdaptorException if problem occured retrieving information
	 */
	public int getLengthByLocation(Location loc) throws AdaptorException;

	/**
	 * Convenience function to get all ids from non gap nodes of the 
	 * location.
	 * @param l
	 * @return ids from non gap nodes of the 
	 * location
	 */
	public long[] locationToIds(Location l) throws AdaptorException;

	/**
	 * Converts location into the target coordinate system.
	 * @return new location corresponding to loc in the specified
	 * coordinate system, null if no such location exists.
	 * @throws AdaptorException
	 */
	public Location convert(Location sourceLocation, String coordinateSystemName)
		throws AdaptorException;

	/**
	 * Converts location into target coordinate system with all gaps and all nodes
	 * @param location source location.
	 * @param coordinateSystem target coordinate system.
	 * @return new location corresponding to loc in the specified
	 * coordinate system, null if no such location exists.
	 * @throws AdaptorException
	 */
	public Location convert(Location location, CoordinateSystem coordinateSystem)
		throws AdaptorException;

	/**
	 * Converts location to the specified coordinate system.
	 * @return equivalent location in coordinate system, or null if no
	 * equivalent exists.
	 */
	Location convert(
		Location location,
		String coordinateSystemName,
		boolean includeGaps,
		boolean allList,
		boolean setSequenceRegion)
		throws AdaptorException;

	/**
	 * Assemblies in dataset.
	 * @return list of zero or more assemblies.
	 */
	String[] fetchAssemblyNames() throws AdaptorException;

	/**
	 * Put given combination of things into a cache. Enable quick retrieval of internal ids 
	 * from LocationConverter.
	 * @param seqRegionName sequence region name
	 * @param cs coordinate system
	 * @param internalId sequence region internal id
	* @param regionLength sequence region length
	 */
	void cacheSeqRegion(
		String seqRegionName,
		CoordinateSystem cs,
		long internalId,
		int regionLength);

	/**
	 * Convert to an internal id, request from database if not in cache
	 * @param seqRegionName
	 * @param cs
	 * @return internal id of the sequence region.
	 */

	long nameToId(String seqRegionName, CoordinateSystem cs)
		throws AdaptorException;

	/**
	 * Convert a number of names in the same coordinate system into an array of 
	 * internal ids. Uses cache if possible.
	 * @param names
	 * @param cs
	 * @return internal ids of the sequence regions.
	 */
	long[] namesToIds(String[] names, CoordinateSystem cs)
		throws AdaptorException;

	/**
	 * Make a location from the sequence region internal id, start, end strand are passed into its 
	 * constructor
	 * @param id sequence region internalID
	 * @param start
	 * @param end
	 * @param strand
	 * @return Location on that internal id
	 * @throws AdaptorException
	 */
	Location idToLocation(long id, int start, int end, int strand)
		throws AdaptorException;

	/**
	 * Make a location from the sequence region internal id, 
	 * with start =1, end = sequence regon length and strand = 0.
	 * @param id sequence region internalID
	 * @return Location on that internal id
	 * @throws AdaptorException
	 */
	Location idToLocation(long id) throws AdaptorException;

	/**
	 * Sets the sequence region name and coordinate system on 
	 * _loc_.
	 * @param loc location to be modified. seqRegionInternalID
	 * must be set and >0.
	 * @return loc.
	 * @throws AdaptorException
	 */
	Location assignSeqRegionNameAndCoordinateSystem(Location loc) throws AdaptorException;
	
	/**
	 * @return fully specified location, or null if no such location
	 * exits. 
	 */

	/**
	 * Ensures the location is fully specified by filling in missing values
   * with data from the database.
   *  
   * A fully specified (complete) location has values for all these attributes:  
   * coordinate system name and version, 
   * sequence region name, start, end and strand. If any of these values are missing and can be 
   * extracted from the database then they will be set. 
   * 
   * If no corresponding complete location exists, e.g. the sequence region is not in the database, then 
   * null is returned.  
	 *
   * Perform an in place edit where necessary. Does nothing if the location is
   * complete or can not be made complete.
   *
	 * @param location potentially incomplete location. 
	 * @return location with CoordinateSystem, seqRegionName, 
	 * start and end set, or null if the location cannot be completed.
	 */
	Location fetchComplete(Location location) throws AdaptorException;

	/**
	 * Fetch the location representing this one in the lowest ranking coordinate system.
	 * 
	 * @param location source location to be converted
	 * @return The location with the lowest level coordinate system corresponding to 
	 * the location, or null if non exists.
	 */
	Location convertToTopLevel(Location location) throws AdaptorException;

	/** 
	 * Name of the default LocationConverteravailable from a driver. 
	 */
	final static String TYPE = "location_converter";

  /**
   * References locations into the same sequence region specified by target location.
   * 
   * Facade method that delegates to AssemblyExceptionAdaptor.
   * @throws AdaptorException
   * @see AssemblyExceptionAdaptor#rereference(Location, SequenceRegion) 
   * @see #dereference(Location)
   */
  Location rereference(Location loc, SequenceRegion seqRegion) throws AdaptorException;
  
  
  /**
   * Dereferences location into component locations.
   * 
   * Facade method that delegates to AssemblyExceptionAdaptor.
   * @see AssemblyExceptionAdaptor#dereference(Location) 
   * @see #rereference(Location, SequenceRegion) 
   */
  Location dereference(Location loc) throws AdaptorException;
  
  
}
