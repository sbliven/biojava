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

package org.ensembl.driver.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ensembl.datamodel.InvalidLocationException;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Marker;
import org.ensembl.datamodel.MarkerFeature;
import org.ensembl.datamodel.impl.MarkerFeatureImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.LocationConverter;
import org.ensembl.driver.MarkerFeatureAdaptor;

/**
 * Implementation fo the MarkerFeatureAdaptor that works with
 * standard Ensembl databases.
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public class MarkerFeatureAdaptorImpl
  extends BaseFeatureAdaptorImpl
  implements MarkerFeatureAdaptor {

  public MarkerFeatureAdaptorImpl(CoreDriverImpl driver) {
    super(driver, TYPE);
  }

  /**
   * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#tables()
   */
  protected String[][] tables() {
    String[][] tables = { { "marker_feature", "mf" }
      //, 
      //      {"marker", "m"},
      //      {"marker_synonym", "ms"}
    };
    return tables;
  }

  /**
   * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#columns()
   */
  protected String[] columns() {
    String[] columns =
      {
        "mf.marker_feature_id",
        "mf.marker_id",
        "mf.seq_region_id",
        "mf.seq_region_start",
        "mf.seq_region_end",
        "mf.analysis_id",
        "mf.map_weight"
      //      ,
      //      "m.left_primer", 
      //      "m.right_primer", 
      //      "m.min_primer_dist",
      //      "m.max_primer_dist", 
      //      "m.priority", 
      //      "m.type", 
      //      "ms.marker_synonym_id",
      //      "ms.name", 
      //      "ms.source"
    };
    return columns;
  }

  //  public String[][] leftJoin() {
  //    String[][] leftJoin = {
  //      {"marker_synonym",
  //       "m.display_marker_synonym_id = ms.marker_synonym_id" }
  //    };
  //    return leftJoin;
  //  }
  //
  //
  //  public String finalWhereClause() {
  //    return "mf.marker_id = m.marker_id";
  //  }

  /**
   * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#createObject(java.sql.ResultSet)
   */
  public Object createObject(ResultSet rs) throws AdaptorException {
    MarkerFeature mf = null;

    try {
      LocationConverter locationConverter = driver.getLocationConverter();
      if (rs.next()) {

        Location loc =
          locationConverter.idToLocation(
            rs.getLong("seq_region_id"),
            rs.getInt("seq_region_start"),
            rs.getInt("seq_region_end"),
            0);
        // marker features are unstranded

        mf = new MarkerFeatureImpl();
        mf.setDriver(getDriver());
        mf.setLocation(loc);
        mf.setInternalID(rs.getLong("marker_feature_id"));
        mf.setMapWeight(rs.getInt("map_weight"));
        mf.setAnalysisID(rs.getLong("analysis_id"));
        // TODO mf.setDisplayID(rs.getString());

      }

    } catch (InvalidLocationException e) {
      throw new AdaptorException("Error when building Location", e);
    } catch (SQLException e) {
      throw new AdaptorException("SQL error when building object", e);
    }

    return mf;

  }

  /**
   * @see org.ensembl.driver.MarkerFeatureAdaptor#fetch(long)
   */
  public MarkerFeature fetch(long internalID) throws AdaptorException {
    return (MarkerFeature) super.fetchByInternalID(internalID);
  }

  /**
   * @see org.ensembl.driver.MarkerFeatureAdaptor#fetch(org.ensembl.datamodel.Location, int)
   */
  public List fetch(Location loc, int minWeight)
    throws AdaptorException {

     return fetchAllByConstraint( loc," mf.map_weight>"+minWeight);
  }

  /**
   * @see org.ensembl.driver.MarkerFeatureAdaptor#fetch(org.ensembl.datamodel.Marker)
   */
  public List fetch(Marker marker) throws AdaptorException {
    
    if (marker.getInternalID()<1) 
      throw new AdaptorException("Failed to load MarkerFeatures for Marker because marker.internalID is invalid : "+ marker);
    
    // Get matching marker features
    String sql = "SELECT marker_feature_id FROM marker_feature WHERE marker_id="+marker.getInternalID();
    String[] constraints = createConstraintBatches("mf.marker_feature_id", sql);
    List mfs = genericFetch(constraints);
    
    // Bind the marker and marker features together
    for (int i = 0, n = mfs.size(); i < n; i++) {
      MarkerFeature mf = (MarkerFeature) mfs.get(i);
      mf.setMarker(marker);
    }
    marker.setMarkerFeatures( mfs );    
    
    return mfs;
  }

}
