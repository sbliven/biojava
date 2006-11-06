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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.ensembl.datamodel.Analysis;
import org.ensembl.datamodel.impl.AnalysisImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.AnalysisAdaptor;

/**
 * Analysis adaptor. Lazy loads all analyses and caches them. 
 * 
 * Class is thread safe.
 * @author frans
 * @author craig
 */

public class AnalysisAdaptorImpl
  extends BaseAdaptor
  implements AnalysisAdaptor {
  private final static Logger logger =
    Logger.getLogger(AnalysisAdaptorImpl.class.getName());


  /**
   *  Constructor for the AnalysisAdaptorImpl object
   *@param  driver  Description of Parameter
   */
  public AnalysisAdaptorImpl(CoreDriverImpl driver) {
    //  big cache limit so we don't keep going to the db
    super(driver, Integer.MAX_VALUE);
  }

  /**
   * Add item to cache with two keys: internalID and 
   * logical name.
   * @param analysis object to be stored in cache
   */
  protected void addToCache(Analysis analysis) {
    if (cache != null && analysis != null) {
      cache.put(analysis, new Long(analysis.getInternalID()));
      cache.put(analysis, analysis.getLogicalName());
    }

  }

  /**
   *  Gets the type attribute of the AnalysisAdaptorImpl object
   *@return                       The type value
   *@exception  AdaptorException  Description of Exception
   */
  public String getType() throws AdaptorException {
    return TYPE;
  }


  /**
   * Loads cache if cache is empty, otherwise does nothing.
   * Items added to the database after the cache is created
   * will not be available unless the cache is cleared via
   * clearCache().
   * @throws AdaptorException
   */
  private synchronized void lazyLoadFullCache()  throws AdaptorException {
    
    if (cache.getSize()>0) return;
    
    Connection conn = null;

    String sql = null;

    try {

        sql = "SELECT" + " created " // 1
    +", logic_name " // 2
    +", db " // 3
    +", db_version " // 4
    +", db_file " // 5
    +", program " // 6
    +", program_version " // 7
    +", program_file " // 8
    +", parameters " // 9
    +", module " // 10
    +", module_version " // 11
    +", gff_source " // 12
    +", gff_feature " // 13
    +", analysis_id " // 14
  +" FROM " + " analysis";

      logger.fine(sql);

      conn = getConnection();
      ResultSet rs = executeQuery(conn, sql);

      while (rs.next()) {
        Analysis analysis = extractAnalysis(rs);
        addToCache(analysis);
      }
      
    } catch (SQLException e) {
      cache.clear();
      throw new AdaptorException(sql, e);
    } finally {
      close(conn);
    }


  }

  public List fetch() throws AdaptorException {
    
    lazyLoadFullCache();
    
    return new ArrayList(cache.values());
    

  }

  /**
   * Fetch Analysis with specified internalID.
   * @param  internalID            internalID of analysis
   * @param  useCache              ignored
   * @return                       analysis if found, otherwise null
   * @exception  AdaptorException
   * @deprecated since 23.1 use fetch(long)
   * @see #fetch(long)
   */
  public Analysis fetch(long internalID, boolean useCache)
    throws AdaptorException {
    return fetch(internalID);
  }

  /**
   * Fetch Analysis with specified internalID.
   * @param  internalID            internalID of analysis
   * @return                       analysis if found, otherwise null
   * @exception  AdaptorException
   */
  public Analysis fetch(long internalID) throws AdaptorException {

    lazyLoadFullCache();
    return (Analysis) cache.get(new Long(internalID));
 }

  /**
   * Fetch all analyses with specified gff feature.
   * @return zero or more analyses.
   * @exception  AdaptorException  
   */
  public List fetchByGffFeature(String gffFeature) throws AdaptorException {
    List list = new ArrayList();
    Connection conn = null;

    lazyLoadFullCache();

    // find matching analyses in cache
    for (Iterator iterator = cache.values().iterator(); iterator.hasNext();) {
      Analysis a = (Analysis) iterator.next();
      String gff = a.getGFFFeature();
      if (gff!=null && gff.equals(gffFeature))
        list.add(a);
    }

    return list;
  }

  /**
   *@param  logicalName           Description of Parameter
   *@return                       An Analysis object matching the logicalName, or null if non found.
   *@exception  AdaptorException  Description of Exception
   */
  public Analysis fetchByLogicalName(String logicalName)
    throws AdaptorException {
    return fetchByLogicalName(logicalName, true);
  }

  public Analysis fetchByLogicalName(String logicalName, boolean useCache)
    throws AdaptorException {

    lazyLoadFullCache();

    return (Analysis) cache.get(logicalName);
  }

  /**
   * Extracts column values from rs and loads into an Analysis object.
   */
  private Analysis extractAnalysis(ResultSet rs) throws SQLException {

    Analysis analysis = new AnalysisImpl();

    try {
      analysis.setCreated(rs.getTimestamp(1));
    } catch (SQLException e) {
      // exception if timestamp is "0000-00-00 00:00:00"
      // and mysql driver version >=3.1.18
      analysis.setCreated(null);
    }
   
    analysis.setLogicalName(rs.getString(2));

    analysis.setSourceDatabase(rs.getString(3));
    analysis.setSourceDatabaseVersion(rs.getString(4));
    analysis.setSourceDatabaseFile(rs.getString(5));

    analysis.setProgram(rs.getString(6));
    analysis.setProgramVersion(rs.getString(7));
    analysis.setProgramFile(rs.getString(8));

    analysis.setParameters(rs.getString(9));

    analysis.setRunnable(rs.getString(10));
    analysis.setRunnableVersion(rs.getString(11));

    analysis.setGFFSource(rs.getString(12));
    analysis.setGFFFeature(rs.getString(13));

    analysis.setInternalID(rs.getLong(14));

    return analysis;
  }

  /**
   * Stores analysis.
   * @param  analysis analysis to be stored.
   * @exception  AdaptorException If store fails.
   */
  public long store(Analysis analysis) throws AdaptorException {

      String sql = "INSERT INTO analysis (" + " created " // 1
    +", logic_name " // 2
    +", db " // 3
    +", db_version " // 4
    +", db_file " // 5
    +", program " //6 
    +", program_version " // 7
    +", program_file " // 8
    +", parameters " // 9
    +", module " // 10
    +", module_version " // 11
    +", gff_source " // 12
    +", gff_feature " // 13
  +" ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";

    long internalID = 0;
    Connection conn = null;
    try {

      conn = getConnection();
      conn.setAutoCommit(false);

      Timestamp created = analysis.getCreated();
      if (created == null)
        created = new Timestamp(System.currentTimeMillis());

      PreparedStatement ps = conn.prepareStatement(sql);

      ps.setTimestamp(1, created);
      ps.setString(2, analysis.getLogicalName());
      ps.setString(3, analysis.getSourceDatabase());
      ps.setString(4, analysis.getSourceDatabaseVersion());
      ps.setString(5, analysis.getSourceDatabaseFile());
      ps.setString(6, analysis.getProgram());
      ps.setString(7, analysis.getProgramVersion());
      ps.setString(8, analysis.getProgramFile());
      ps.setString(9, analysis.getParameters());
      ps.setString(10, analysis.getRunnable());
      ps.setString(11, analysis.getRunnableVersion());
      ps.setString(12, analysis.getGFFSource());
      ps.setString(13, analysis.getGFFFeature());

      internalID = executeAutoInsert(ps, sql);

      conn.commit();

      analysis.setDriver(driver);
      analysis.setInternalID(internalID);

    } catch (Exception e) {
      rollback(conn);
      throw new AdaptorException("Failed to store analysis: " + analysis, e);
    } finally {
      close(conn);
    }

    addToCache(analysis);

    return internalID;
  }

  /**
   * @param internalID internalID of analysis to delete.
   * @throws AdaptorException if an adaptor error occurs
   */
  public void delete(long internalID) throws AdaptorException {

    if (internalID < 1)
      return;

    cache.removeValueByKey(new Long(internalID));

    Connection conn = null;
    try {

      conn = getConnection();
      conn.setAutoCommit(false);

      delete(conn, internalID);

      conn.commit();
    } catch (Exception e) {
      rollback(conn);
      throw new AdaptorException("Failed to delete analysis: " + internalID, e);
    } finally {
      close(conn);
    }
    
  }

  /**
   * @param analysis analysis to delete.
   * @throws AdaptorException if an adaptor error occurs
   */
  public void delete(Analysis analysis) throws AdaptorException {
    if (analysis == null)
      return;
    delete(analysis.getInternalID());
    analysis.setInternalID(0);
  }

  /**
   * Executes sql to delete row from analysis table.
   */
  void delete(Connection conn, long internalID) throws AdaptorException {

    executeUpdate(
      conn,
      "delete from analysis where analysis_id = " + internalID);

  }

}
