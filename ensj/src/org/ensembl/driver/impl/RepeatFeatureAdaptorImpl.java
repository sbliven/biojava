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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.InvalidLocationException;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.RepeatConsensus;
import org.ensembl.datamodel.RepeatFeature;
import org.ensembl.datamodel.impl.RepeatFeatureImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.AnalysisAdaptor;
import org.ensembl.driver.RepeatFeatureAdaptor;
import org.ensembl.util.NotImplementedYetException;

//TODO Implement store/delete

/**
 * Adaptor for fetching RepeatFeature objects from
 * ensembl databases.
 * 
 * <p>
 * Works in conjunction with the it's peer RepeatConsensusAdaptorImpl.
 * </p>
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public class RepeatFeatureAdaptorImpl
  extends BaseFeatureAdaptorImpl
  implements RepeatFeatureAdaptor {

  private static final Logger logger =
    Logger.getLogger(RepeatFeatureAdaptorImpl.class.getName());

  private CoordinateSystem repeatCoordSys = new CoordinateSystem("repeat_cs");

  /**
   * Fetch by the internalID of the repeat feature.
  */
  public RepeatFeature fetch(long internalID) throws AdaptorException {

    return (RepeatFeature) super.fetchByInternalID(internalID);

  } //end fetch

  /** fetch by location */
  public List fetch(Location location) throws AdaptorException {

    return super.fetch(location);

  }

  /**
   * @return internalID assigned to repeat feature in database.
   * @throws AdaptorException if an adaptor error occurs
   */
  public long store(RepeatFeature feature) throws AdaptorException {

    throw new NotImplementedYetException("Not yet implemented in new API");

  }

  /**
   * @param internalID internalID of repeat feature to be deleted from database.
   * @throws AdaptorException if an adaptor error occurs
   */
  public void delete(long internalID) throws AdaptorException {

    if (internalID < 1)
      return;

    Connection conn = null;
    try {

      conn = getConnection();
      conn.setAutoCommit(false);

      delete(conn, internalID);

      conn.commit();
    } catch (Exception e) {
      rollback(conn);
      throw new AdaptorException(
        "Failed to delete repeat feature: " + internalID,
        e);
    } finally {
      close(conn);
    }
  }

  /**
   * @param feature repeat feature to be delete.
   * @throws AdaptorException if an adaptor error occurs
   */
  public void delete(RepeatFeature feature) throws AdaptorException {
    if (feature == null)
      return;
    delete(feature.getInternalID());
    feature.setInternalID(0);
  }

  /**
   * Executes sql to delete row from external_db table.
   */
  void delete(Connection conn, long internalID) throws AdaptorException {

    executeUpdate(
      conn,
      "delete from repeat_feature where repeat_feature_id = " + internalID);

  }

  // ---------------------------------------------------------------------

  protected String[] columns() {

    String[] cols =
      {
        "r.repeat_feature_id",
        "r.seq_region_id",
        "r.seq_region_start",
        "r.seq_region_end",
        "r.seq_region_strand",
        "r.repeat_consensus_id",
        "r.repeat_start",
        "r.repeat_end",
        "r.analysis_id",
        "r.score",
        "rc.repeat_name",
        "rc.repeat_class",
        "rc.repeat_consensus" };

    return cols;

  } // columns

  // ------------------------------------------------------------------

  public Object createObject(ResultSet rs) throws AdaptorException {

    RepeatFeature f = null;

    try {
      AnalysisAdaptor analysisAdaptor = driver.getAnalysisAdaptor();
      if (rs.next()) {

        Location loc = new Location(
            rs.getLong("seq_region_id"),
            rs.getInt("seq_region_start"),
            rs.getInt("seq_region_end"),
            rs.getInt("seq_region_strand"));
        
        // max/min is a work around to handle cases in the database where 
        // where repeat_start>repeat_end
        final int a = rs.getInt("repeat_start");
        final int b = rs.getInt("repeat_end");  
        int start = Math.min(a,b);
        if (start<1) start=1;
        int end = Math.max(a,b);
        if (end<1) end = 1;
        Location hitLoc =
          new Location(
            repeatCoordSys,
            rs.getString("repeat_name"),
            start,
            end,
            0);

        f =
          new RepeatFeatureImpl(rs.getLong("repeat_feature_id"), loc, hitLoc);
        f.setDisplayName("not set");
        f.setDescription("not set");
        f.setSequence(null);
        f.setScore(rs.getDouble("score"));
        f.setHitDisplayName(rs.getString("repeat_name"));
        f.setAnalysisID(rs.getLong("analysis_id"));

        long rcID = rs.getLong("repeat_consensus_id");
        f.setRepeatConsensusInternalID(rcID);
        
        RepeatConsensusAdaptorImpl rca = (RepeatConsensusAdaptorImpl) driver.getRepeatConsensusAdaptor();
        // use the cache in the peer repeat consensus adaptor  
        RepeatConsensus rc = (RepeatConsensus)rca.fetchFromCache(rcID);
        if ( rc==null ) 
          rc = rca.add(rcID, 
                        rs.getString("repeat_name"), 
                        rs.getString("repeat_class"), 
                        rs.getString("repeat_consensus")); 
        f.setRepeatConsensus( rc );

        f.setDriver(getDriver());
      }

    } catch (InvalidLocationException e) {
      throw new AdaptorException("Error when building Location", e);
    } catch (SQLException e) {
      throw new AdaptorException("SQL error when building object", e);
    }

    return f;
  }

  protected String[][] tables() {

    String[][] tables = { { "repeat_feature", "r" }, {
        "repeat_consensus", "rc" }
    };

    return tables;

  }

  public RepeatFeatureAdaptorImpl(CoreDriverImpl driver) {
    super(driver, TYPE);
  }

  public RepeatFeatureAdaptorImpl(
    CoreDriverImpl driver,
    String logicName,
    String type) {
    super(driver, logicName, type);
  }

  /* (non-Javadoc)
   * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#finalWhereClause()
   */
  public String finalWhereClause() {
    return "r.repeat_consensus_id = rc.repeat_consensus_id";
  }
}
