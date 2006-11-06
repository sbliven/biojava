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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ensembl.datamodel.Marker;
import org.ensembl.datamodel.MarkerFeature;
import org.ensembl.datamodel.impl.MarkerImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.MarkerAdaptor;
import org.ensembl.util.NotImplementedYetException;

/**
 * Fetches, stores and deletes markers from database. Relevant tables are
 * marker, marker_feature and marker_synonym.
 */

// TODO Implement fetch for new schema/API
// TODO Implement store/delete

public class MarkerAdaptorImpl extends BaseAdaptor implements MarkerAdaptor {
  private static final Logger logger =
    Logger.getLogger(MarkerAdaptorImpl.class.getName());

  public MarkerAdaptorImpl(CoreDriverImpl driver) {
    super(driver);
  }

  public String getType() throws AdaptorException {
    return TYPE;
  }

  public Marker fetch(long internalID) throws AdaptorException {

    Marker m = null;
    Connection conn = getConnection();
    ResultSet rs =
      executeQuery(
        conn,
        "SELECT m.marker_id, "
          + "m.display_marker_synonym_id, "
          + "m.priority, "
          + "m.left_primer, "
          + "m.right_primer, "
          + "m.type, "
          + "m.min_primer_dist, "
          + "m.max_primer_dist,  "
          + "ms.marker_synonym_id, "
          + "ms.source, "
          + "ms.name "
          + "FROM marker m, marker_synonym ms "
          + "WHERE ms.marker_id = m.marker_id AND m.marker_id = "
          + internalID);

    try {
      if (rs.next()) {
        m = new MarkerImpl(getDriver());
        m.setInternalID(rs.getLong("marker_id"));
        m.setMaxPrimerDistance(rs.getInt("max_primer_dist"));
        m.setType(rs.getString("type"));
        m.setMinPrimerDistance(rs.getInt("min_primer_dist"));
        m.setMaxPrimerDistance(rs.getInt("max_primer_dist"));
        m.setSeqLeft("left_primer");
        m.setSeqRight("right_primer");
        List synonyms = new ArrayList();
        m.setSynonyms(synonyms);
        long displaySynonymID = rs.getLong("display_marker_synonym_id");
        do {
          String synonym = rs.getString("name");
          synonyms.add(synonym);
          if ( displaySynonymID==rs.getLong("marker_synonym_id"))
            m.setDisplayName(synonym);
        } while (rs.next());
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      close(conn);
    }

    return m;

  }

  /**
   * Fetches marker with specified synonym. 
   *
   * marker.location is in assembly coordinates.
   * @return marker if found, otherwise null.
   */
  public Marker fetchBySynonym(String synonym) throws AdaptorException {

    Marker marker = null;
    Connection conn = null;
    String sql = null;
    ResultSet rs = null;

    try {
      conn = getConnection(); // open connection to core db

      sql = "SELECT marker_id FROM marker_synonym WHERE name = ?";
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, synonym);

      rs = executeQuery(ps, sql);
      if (rs.next()) {
        long id = rs.getLong(1);
        if (logger.isLoggable(Level.FINE))
          logger.fine("id = " + id);
        marker = fetch(id);
      }

    } catch (SQLException e) {
      throw new AdaptorException(
        "Failed to read marker:" + sql + "(" + conn + ")",
        e);
    } finally {
      close(conn);
    }

    return marker;
  }

  /**
   * @param markerIDs
   * @return list of markers where each corresponds to a markerID
   */
  private List fetch(Set markerIDs) throws AdaptorException {
    List r = new ArrayList(markerIDs.size());
    Iterator i = markerIDs.iterator();
    while (i.hasNext())
      r.add(fetch(((Long) i.next()).longValue()));
    return r;
  }

  /**
   * Stores marker.
   * @param  marker marker to be stored.
   * @exception  AdaptorException If store fails.
   */
  public long store(Marker marker) throws AdaptorException {

    throw new NotImplementedYetException("Not yet implemented in new API");

  }

  /**
   * @param internalID internalID of marker to delete.
   * @throws AdaptorException if an adaptor error occurs
   */
  public void delete(long internalID) throws AdaptorException {

    throw new NotImplementedYetException("Not yet implemented in new API");

  }

  /**
   * @param marker marker to delete.
   * @throws AdaptorException if an adaptor error occurs
   */
  public void delete(Marker marker) throws AdaptorException {
    delete(marker.getInternalID());
    marker.setInternalID(0);
  }

  /* *
   * @see org.ensembl.driver.MarkerAdaptor#fetchComplete(org.ensembl.datamodel.Marker)
   */
  public void fetchComplete(Marker marker) throws AdaptorException {

    // A side effect of calling this method (in this implementation) is to bind the
    // marker and marker features together.
    driver.getMarkerFeatureAdaptor().fetch(marker);
  }

  /**
   * @see org.ensembl.driver.MarkerAdaptor#fetch(org.ensembl.datamodel.MarkerFeature)
   */
  public Marker fetch(MarkerFeature markerFeature) throws AdaptorException {

    long mfID = markerFeature.getInternalID();
    if (mfID < 1)
      throw new AdaptorException(
        "Failed to fetch Marker for markerFeature because it's intnernalID is invalid:"
          + markerFeature);
    Marker m = null;
    Connection conn = getConnection();
    ResultSet rs =
      executeQuery(
        conn,
        "SELECT marker_id FROM marker_feature WHERE marker_feature_id=" + mfID);
    try {
      if (rs.next())
        m = fetch(rs.getLong(1));
    } catch (SQLException e) {
      throw new AdaptorException("",e);
    }
    close(conn);
    return m;
  }

}
