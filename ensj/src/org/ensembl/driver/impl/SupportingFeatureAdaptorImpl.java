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
import java.util.List;
import java.util.logging.Logger;

import org.ensembl.datamodel.DnaDnaAlignment;
import org.ensembl.datamodel.DnaProteinAlignment;
import org.ensembl.datamodel.Exon;
import org.ensembl.datamodel.FeaturePair;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.SupportingFeatureAdaptor;


public class SupportingFeatureAdaptorImpl
  extends BaseAdaptor
  implements SupportingFeatureAdaptor {
  private static final Logger logger =
    Logger.getLogger(SupportingFeatureAdaptorImpl.class.getName());

  public SupportingFeatureAdaptorImpl(CoreDriverImpl driver) {
    super(driver);
  }

  /**
   *  Gets the type attribute of the SupportingFeatureAdaptorImpl object
   *@return                       The type value
   *@exception  AdaptorException  Description of Exception
   */
  public String getType() throws AdaptorException {
    return TYPE;
  }

  /**
   * Stores links between exon and supporting features. The features must
   * already be stored in the database.  */
  public void store(Exon exon, List features) throws AdaptorException {

    Connection conn = null;
    try {
      conn = getConnection();
      conn.setAutoCommit(false);

      store(conn, exon, features);

      conn.commit();
      exon.setDriver(driver);

    } catch (Exception e) {
      rollback(conn);
      throw new AdaptorException(
        "Failed to store supporting features " + "for exon: " + exon,
        e);
    } finally {
      close(conn);
    }

  }

  /**
   * Actually does the work of storing.
   */
  void store(Connection conn, Exon exon, List features)
    throws AdaptorException, SQLException {

    final int nFeatures = features.size();
    final long exonID = exon.getInternalID();

    String sql =
      "INSERT INTO supporting_feature "
        + " ( exon_id, feature_type, feature_id) "
        + " VALUES ( ?, ?, ?) ";

    PreparedStatement ps = conn.prepareStatement(sql);
    ps.setLong(1, exonID);

    for (int i = 0; i < nFeatures; ++i) {

      FeaturePair fp = (FeaturePair) features.get(i);
      final long fpID = fp.getInternalID();
      if (fpID < 1)
        throw new AdaptorException(
          "Can not store supporting evidence link"
            + " because feature has invalid internalID"
            + " it should be >0: "
            + fp);
      String type = null;
      if (fp instanceof DnaProteinAlignment)
        type = "protein_align_feature";
      else if (fp instanceof DnaDnaAlignment)
        type = "dna_align_feature";
      else
        throw new AdaptorException(
          "Unsupported type of supporting evidence:" + fp);

      // store exon->feature link
      ps.setString(2, type);
      ps.setLong(3, fpID);
      executeUpdate(ps, sql);
    }

  }

  /**
   * Deletes all links between exon and it's supporting evidence.
   */
  public void delete(Exon exon) throws AdaptorException {

    Connection conn = null;
    try {

      conn = getConnection();
      conn.setAutoCommit(false);

      delete(conn, exon);
      conn.commit();
    } catch (SQLException e) {
      rollback(conn);
      throw new AdaptorException(
        "Failed to delete supporting evidence for exon " + exon,
        e);
    } finally {
      close(conn);
    }

  }

  void delete(Connection conn, Exon exon)
    throws SQLException, AdaptorException {

    executeUpdate(
      conn,
      "DELETE FROM supporting_feature WHERE exon_id=" + exon.getInternalID());
  }

  public List fetch(Exon exon) throws AdaptorException {

    List l = new ArrayList();
    
    // get all links
    Connection conn = null;
    try {
      conn = getConnection();

      ResultSet rs =
        executeQuery(
          conn,
          "SELECT sf.feature_type, sf.feature_id "
            + "FROM   supporting_feature sf "
            + "WHERE  exon_id = "
            + exon.getInternalID());
      
      while( rs.next() ) {

        // follow link to get supporting features
        String type = rs.getString(1);
        if ( "dna_align_feature".equals(type))
          l.add(driver.getDnaDnaAlignmentAdaptor().fetch(rs.getLong(2)));
        else if  ("protein_align_feature".equals(type))      
          l.add(driver.getDnaProteinAlignmentAdaptor().fetch(rs.getLong(2)));

      }
      
    } catch (SQLException e) {
      rollback(conn);
      throw new AdaptorException(
        "Failed to delete supporting evidence for exon " + exon,
        e);
    } finally {
      close(conn);
    }

  return l;
  }

}
