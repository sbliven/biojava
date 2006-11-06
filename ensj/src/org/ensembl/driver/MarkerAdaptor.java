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

import org.ensembl.datamodel.Marker;
import org.ensembl.datamodel.MarkerFeature;

/**
 * Provides access to Markers in the datasource.
 */
public interface MarkerAdaptor extends Adaptor {




  /** 
   * Location type set to CloneFragmentLocation.
   * @return marker if present, otherwise null.
   */
  Marker fetch(long internalID) throws AdaptorException;



  /**
   * @return marker if present, otherwise null.
   * */
  Marker fetchBySynonym(String synonym) throws AdaptorException ;


  /**
   * Store the marker in the data source.
   */
  long store(Marker marker) throws  AdaptorException;

  /**
   * Deletes the marker.
   */
  void delete(Marker marker)  throws  AdaptorException;

  /**
   * Deletes the marker with the specified internalID.
   */
  void delete(long internalID)  throws  AdaptorException;

  /** 
   * Name of the default MarkerAdaptor available from a driver. 
   */
  final static String TYPE = "marker";



  /**
   * Loads MarkerFeatures into the marker.
   * @param marker to be loaded with MarkerFeatures
   */
  void fetchComplete(Marker marker) throws AdaptorException;



  /**
   * Fetch Marker corresponding to markerFeature
   * @param markerFeature markerFeature corresponding to a marker
   * @return Marker corresponding to markerFeature, or null if non found
   */
  Marker fetch(MarkerFeature markerFeature) throws AdaptorException;
}
