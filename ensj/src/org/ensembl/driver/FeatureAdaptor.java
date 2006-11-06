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

import java.util.Iterator;
import java.util.List;

import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.Location;

/**
 * Provides access to a datatype that is Locatable so can be fetched by location.
 */
public interface FeatureAdaptor extends Adaptor {

  /**
   * Converts all feature.location to coordinateSystem.
   * @param features zero or more features
   * @param coordinateSystem target coordinate system.
   * @return features
   * @throws AdaptorException
   */
  List convertLocations(List features, CoordinateSystem coordinateSystem) throws AdaptorException;
  
  /**
   * Fetch zero or more Features with the specified internalIDs. Any internalIDs that do
   * not correspond to Features in the database will be ignored.
   * 
   * @param internalIDs internal IDs. The ids should be unique.
   * @return zero or more Features with internalIDs specified in internalIDs and in the same order.
   * @throws AdaptorException
   */
  List fetch(long[] internalIDs) throws AdaptorException;

  /**
   * Fetch zero or more Features with the specified internalIDs. Any internalIDs that do
   * not correspond to Features in the database will be ignored.
   * 
   * @param internalIDs internal IDs. The ids should be unique.
   * @param loadChildren hint to prefetch child data.
   * @return zero or more Features with internalIDs specified in internalIDs and in the same order.
   * @throws AdaptorException
   */
  List fetch(long[] internalIDs, boolean loadChildren)
    throws AdaptorException;

  /**
   * Returns a low memory iterator over Features matching the internalIDs.  
   * 
   * Any internalIDs that do
   * not correspond to Features in the database will be ignored.
   * 
   * @param internalIDs internal IDs. The ids should be unique.
   * @return iterator over zero or more Features with internalIDs specified in internalIDs, order is unspecified.
   * @throws AdaptorException
   */
  Iterator fetchIterator(long[] internalIDs) throws AdaptorException;

  /**
   * Returns a low memory iterator over Features matching the internalIDs. 
   * 
   * Any internalIDs that do
   * not correspond to Features in the database will be ignored.
   * 
   * @param internalIDs internal IDs. The ids should be unique.
   * @param loadChildren hint to prefetch child data.
   * @return iterator over zero or more Features with internalIDs specified in internalIDs, order is unspecified.
   * @throws AdaptorException
   */
  Iterator fetchIterator(long[] internalIDs, boolean loadChildren)
    throws AdaptorException;

  /**
   * Returns a low memory iterator over Features matching the internalIDs. 
   * 
   * Any internalIDs that do
   * not correspond to Features in the database will be ignored.
   * 
   * @param internalIDs internal IDs. The ids should be unique.
   * @param loadChildren hint to prefetch child data.
   * @param bufferSize maximum number of fatures loaded by the iterator at any moment.
   * @return iterator over zero or more Features with internalIDs specified in internalIDs, order is unspecified.
   * @throws AdaptorException
   */
  Iterator fetchIterator(long[] internalIDs, boolean loadChildren, int bufferSize)
    throws AdaptorException;
  
  /**
   * Fetches features that overlap with location.
   * @param location location filter.
   * @return List containing zero or more items overlaping the location.
   * @see #fetch(Location,boolean) if you want to include child data.
   */
  List fetch(Location location) throws AdaptorException;

  /**
   * Fetches features, optionally including child data, that overlap with location.
   *
   * Including child data may be more efficient than having
   * the data lazy loaded later.
   * @param location location filter.
   * @param loadChildren hint to prefetch child data.
   * @return List containing zero or more items overlaping the location.
   */
  List fetch(Location location, boolean loadChildren)
    throws AdaptorException;

  /**
   * Returns a low memory footprint iterator over all Features of this type in the database.
   * 
   * @return iterator over all Features of this type in the database.
   * @see #fetchAll() an alternative way of getting all the features.
   * @throws AdaptorException
   */
  Iterator fetchIterator() throws AdaptorException;


  /**
   * Returns a low memory footprint iterator over all Features of this type in the database.
   * 
   * @param loadChildren hint to preload child data if supported.
   * @return iterator over all Features of this type in the database.
   * @see #fetchAll() an alternative way of getting all the features.
   * @throws AdaptorException
   */
  Iterator fetchIterator(boolean loadChildren) throws AdaptorException;

  /**
   * Returns a low memory footprint iterator over all Features of this type in the database.
   * 
   * @param loadChildren hint to preload child data if supported.
   * @param bufferSize maximum number of features loaded by the iterator at any time.
   * @return iterator over all Features of this type in the database.
   * @see #fetchAll() an alternative way of getting all the features.
   * @throws AdaptorException
   */
  Iterator fetchIterator(boolean loadChildren, int bufferSize) throws AdaptorException;

  /**
   * Returns a low memory footprint iterator over all Features in the specified location.
   * 
   * @param location location filter.
   * @return iterator over all Features that fall in the specified location.
   * @see #fetch(Location) an alternative way of getting all the features.
   * @see #fetchIterator(Location,boolean) for a possibly more efficient retrieval mechanism.
   * @throws AdaptorException
   */
  Iterator fetchIterator(Location location) throws AdaptorException;

  /**
   * Returns a low memory footprint iterator over all Features in the specified location
   * with or without child data preloaded.
   * 
   * @param location location filter.
   * @param loadChildren whether to include children. Setting to true
   * may produce faster overall retrieval if child data is also accessed.
   * @return iterator over all Features that fall in the specified location.
   * @see #fetch(Location,boolean) an alternative way of getting all the features.
   * @throws AdaptorException
   */
  Iterator fetchIterator(Location location, boolean loadChildren)
    throws AdaptorException;

  /**
   * Returns a low memory footprint iterator over all Features in the specified location
   * with or without child data preloaded.
   * 
   * @param location location filter.
   * @param loadChildren whether to include children. Setting to true
   * may produce faster overall retrieval if child data is also accessed.
   * @param chunkSize maximum length of a "chunk" of sequence from which features
   * are retrieved.
   * 
   * @return iterator over all Features that fall in the specified location.
   * @see #fetch(Location,boolean) an alternative way of getting all the features.
   * @throws AdaptorException
   */
  Iterator fetchIterator(Location location, boolean loadChildren, int chunkSize)
    throws AdaptorException;

  /**
   * Retrieves all features from the datasase.
   * @return all features in the database.
   * @see #fetchIterator() alternative way of getting all features using less memory.
   * @throws AdaptorException
   */
  List fetchAll() throws AdaptorException;

  /**
   * Retrieves all features from the datasase and if includeChildren
   * is true it might preload it's child data.
   * @param includeChildren hint to preload child data.
   * @return all features in the database.
   * @see #fetchIterator(boolean) alternative way of getting all features using less memory.
   * @throws AdaptorException
   */
  List fetchAll(boolean includeChildren) throws AdaptorException;
  
  /**
   * Fetch the internalIDs for all features of this type in the database. 
   * @return zero or more internal ids for this type
   */
  long[] fetchInternalIDs() throws AdaptorException;

  /**
   * Fetch internalIDs for features in the specified
   * location.
   * @return zero or more internal ids for this type
   * @throws AdaptorException
   * @param location location filter.
   */
  long[] fetchInternalIDs(Location location) throws AdaptorException;

  /**
   * Fetch the number of features in the database.
   * 
   * @return number of features in the database, >=0.
   * @throws AdaptorException
   */
  long fetchCount() throws AdaptorException;

}
