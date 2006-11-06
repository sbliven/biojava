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
import org.ensembl.datamodel.Marker;
import org.ensembl.datamodel.MarkerFeature;

/**
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public interface MarkerFeatureAdaptor {

  final static String TYPE = "marker_feature";

  /**
   * MarkerFeatures in the specified location matching meeting the other
   * criteria as well.
   *
   * @param loc location filter.
   * @param minWeight minimum weight
   * @return list of zero or more MarkerFeatures.
   * @throws AdaptorException
   */
  List fetch(Location loc, int minWeight) throws AdaptorException;

  /**
   * Fetch MarkerFeatures with the specified Marker.
   * @param marker marker associated with MarkerFeatures.
   * @return zero or more MarkerFeatures corresponding to this marker.
   */
  List fetch(Marker marker) throws AdaptorException;

  /**
   * Fetch MarkerFeature by internalID
   * @param internalID id of MarkerFeature to retrieve
   * @return marker feature with specified interalID, or null if none found
   */
  MarkerFeature fetch(long internalID)  throws AdaptorException ;

  /**
   * Fetch MarkerFeatures by location.
   * @param location location on a sequence
   * @return zero or more MarkerFeatures from the specified location.
   */
  List fetch(Location location)   throws AdaptorException;

  
}
