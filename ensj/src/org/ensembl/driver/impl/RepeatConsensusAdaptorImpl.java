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
import java.util.logging.Logger;

import org.ensembl.datamodel.Persistent;
import org.ensembl.datamodel.RepeatConsensus;
import org.ensembl.datamodel.Sequence;
import org.ensembl.datamodel.impl.RepeatConsensusImpl;
import org.ensembl.datamodel.impl.SequenceImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.RepeatConsensusAdaptor;

public class RepeatConsensusAdaptorImpl
  extends BaseAdaptor
  implements RepeatConsensusAdaptor {
  private static final Logger logger =
    Logger.getLogger(RepeatConsensusAdaptorImpl.class.getName());

  /**
   * creates adaptor with a cache.
   */
  public RepeatConsensusAdaptorImpl(CoreDriverImpl driver) {
    super(driver, 2000);
  }

  public String getType() {
    return TYPE;
  }

  public RepeatConsensus fetch(long internalID) throws AdaptorException {

    Persistent tmp = fetchFromCache(internalID);

    if (tmp != null) {
      return (RepeatConsensus) tmp;
    }

    String sql =
      " SELECT "
        + " repeat_name "
        + " ,repeat_class "
        + " ,repeat_consensus "
        + " FROM "
        + " repeat_consensus "
        + " WHERE "
        + " repeat_consensus_id = ?";

    logger.fine(sql);

    RepeatConsensus consensus = null;
    Connection conn = null;
    try {
      conn = getConnection();
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setLong(1, internalID);
      ResultSet rs = executeQuery(ps, sql);

      if (rs.next()) 
        consensus = add(internalID, rs.getString(1), rs.getString(2), rs.getString(3));

    } catch (SQLException e) {
      throw new AdaptorException(sql.toString(), e);
    } finally {
      close(conn);
    }

    return consensus;
  }

  public long store(RepeatConsensus consensus) throws AdaptorException {

    String sql =
      " INSERT INTO repeat_consensus ( "
        + " repeat_name "
        + " ,repeat_class "
        + " ,repeat_consensus "
        + " ) VALUES ( ?, ?, ? ) ";

    long internalID = 0;
    Connection conn = null;
    try {
      conn = getConnection();
      PreparedStatement ps = conn.prepareStatement(sql);

      ps.setString(1, consensus.getName());
      ps.setString(2, consensus.getType());
      String seqStr = "NULL";
      if (consensus.getSequence() != null)
        seqStr = consensus.getSequence().getString();
      ps.setString(3, seqStr);

      internalID = executeAutoInsert(ps, sql);
      consensus.setInternalID(internalID);

    } catch (SQLException e) {
      throw new AdaptorException(sql.toString(), e);
    } finally {
      close(conn);
    }

    addToCache(consensus);

    return internalID;
  }

  public void delete(long internalID) throws AdaptorException {

    if (internalID < 1)
      return;

    deleteFromCache(internalID);

    Connection conn = null;
    try {

      conn = getConnection();
      conn.setAutoCommit(false);

      delete(conn, internalID);

      conn.commit();
    } catch (Exception e) {
      rollback(conn);
      throw new AdaptorException(
        "Failed to delete repeat consensus: " + internalID,
        e);
    } finally {
      close(conn);
    }
  }

  public void delete(RepeatConsensus consensus) throws AdaptorException {
    if (consensus == null)
      return;
    delete(consensus.getInternalID());
    consensus.setInternalID(0);
  }

  /**
   * Executes sql to delete row from repeat_feature table.
   */
  void delete(Connection conn, long internalID) throws AdaptorException {

    executeUpdate(
      conn,
      "delete from repeat_consensus where repeat_consensus_id = " + internalID);

  }


  /**
   * Creates an instance of RepeatConsensus using the supplied parameters and adds it to
   * the cache.
   * @param internalID
   * @param name
   * @param classification
   * @param consensusString
   * @return created RepeatConsensus
   */
  RepeatConsensus add(
    long internalID,
    String name,
    String classification,
    String consensusString) {
      
    RepeatConsensus rc = new RepeatConsensusImpl(getDriver());
    rc.setInternalID(internalID);
    rc.setName(name);
    rc.setType(classification);
    Sequence seq = null;
    if (consensusString != null) 
      seq = new SequenceImpl(consensusString);

    rc.setSequence(seq);
    addToCache(rc);
    
    return rc;
  }


}
