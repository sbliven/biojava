/*
 Copyright (C) 2005 EBI, GRL

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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.ensembl.driver.impl.Configuration;
import org.ensembl.driver.impl.EnsemblDriverImpl;
import org.ensembl.util.ConnectionPoolDataSource;
import org.ensembl.util.JDBCUtil;

/**
 * Driver providing access to an ensembl database server.
 * 
 * Provides a database connection pool specific to the server
 * and a cached list of database names. Sharing instances
 * of ServerDriver amongst EnsemblDrivers can reduce
 * the number of connections and queries made to a database
 * server.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 * @see org.ensembl.driver.ServerDriverFactory
 */
public class ServerDriver {

  private String[] databaseNames = null;

  private Configuration config;

  private ConnectionPoolDataSource pool = null;

  private EnsemblDriver ensemblDriver = null;

  /**
   * Lazy loading cache of database names.
   * 
   * Ignores "database" parameter so we can connect to a database server without
   * a specific database.
   */
  public ServerDriver(Properties config) {
    this.config = new Configuration(config);
    this.config.remove("database");
  }

  public DataSource getDataSource() throws AdaptorException {
    return getPool();
  }

  public DataSource getDataSource(String catalog) throws AdaptorException {
    return getPool().createDataSourceProxy(catalog);
  }

  public Connection getConnection() throws AdaptorException, SQLException {
    return getPool().getConnection();
  }

  public Connection getConnection(String catalog) throws AdaptorException,
      SQLException {
    return getPool().getConnection(catalog);
  }

  /**
   * Return lazily instantiated connection pool.
   * 
   * @return connection pool for this server.
   * @throws AdaptorException
   */
  private ConnectionPoolDataSource getPool() throws AdaptorException {
    if (pool == null) {

      try {
        pool = config.createPool();
      } catch (IllegalArgumentException e) {
        throw new AdaptorException("Config = " + config, e);
      } catch (SQLException e) {
        throw new AdaptorException("", e);
      } catch (ClassNotFoundException e) {
        throw new AdaptorException("", e);
      }

    }
    return pool;
  }

  /**
   * Clears the cache.
   */
  public void clear() {
    databaseNames = null;
  }

  /**
   * Returns whether databaseName is in getDatabaseNames().
   * 
   * @param databaseName
   *          name of database to find in getDatabaseNames().
   * @return true if databaseName is in getDatabaseNames().
   * @throws AdaptorException
   */
  public boolean contains(String databaseName) throws AdaptorException {

    if (databaseName == null)
      return false;

    databaseNames(); // lazy load if necesary

    for (int i = 0; i < databaseNames.length; i++)
      if (databaseName.equals(databaseNames[i]))
        return true;

    return false;
  }

  /**
   * Return database names matching the filter string.
   * 
   * @param filter
   *          String regexp specifiying which database names , if any, should be
   *          returned.
   * @return sorted array of zero or more database names matching the filter.
   * @throws AdaptorException
   */
  public String[] filterDatabaseNames(String filter) throws AdaptorException {

    databaseNames(); // lazy load if necesary

    List buf = new ArrayList();
    for (int i = 0; i < databaseNames.length; i++) {
      String dbName = databaseNames[i];
      if (dbName.matches(filter))
        buf.add(dbName);
    }

    String[] r = (String[]) buf.toArray(new String[buf.size()]);
    Arrays.sort(r);
    return r;
  }

  /**
   * Same as _filterDatabaseNames(String)_ but returns reverse sorted array.
   * 
   * This convenience method puts the higest versioned database name at the
   * start of the array. e.g.
   * <code>String latestDB = filterDatabaseNamesAndReversSort("homo_sapiens_core.*");</code>
   * 
   * @param filter
   *          String regexp specifiying which database names , if any, should be
   *          returned.
   * @throws AdaptorException
   * @see #filterDatabaseNames(String)
   */
  public String[] filterDatabaseNamesAndReverseSort(String filter)
      throws AdaptorException {
    String[] r = filterDatabaseNames(filter);
    Arrays.sort(r, Collections.reverseOrder());
    return r;
  }

  /**
   * Searches the database names in the cache for the one that begins with
   * _prefix_ and has the highest version.
   * 
   * @param prefix
   *          database name prefix.
   * @return database name from cache beginning with _prefix_ and with the
   *         highest version, or null if none begin with _prefix_.
   * @throws AdaptorException
   */
  public String highestVersionedDatabaseName(String prefix)
      throws AdaptorException {
    String[] dbNames = filterDatabaseNamesAndReverseSort("^" + prefix + "_?\\d.*");
    return (dbNames.length > 0) ? dbNames[0] : null;
  }

  /**
   * Determines if at least one database beginning with the _prefix_ is included
   * in _databaseNames()_.
   * 
   * @param prefix
   *          beginning of a database name.
   * @return true if _databaseNames()_ contains a database that begins with the
   *         prefix, otherwise false.
   * @throws AdaptorException
   * @see #databaseNames()
   */
  public boolean containsPrefix(String prefix) throws AdaptorException {
    if (prefix == null)
      return false;
    else
      return filterDatabaseNames("^" + prefix + ".*").length > 0;
  }

  /**
   * @return Returns the ensemblDriver.
   * @throws AdaptorException
   */
  public EnsemblDriver getEnsemblDriver() throws AdaptorException {
    if (ensemblDriver == null)
      ensemblDriver = new EnsemblDriverImpl(config);

    return ensemblDriver;
  }

  /**
   * 
   * @return zero or more databases available on the db server.
   * @throws AdaptorException
   */
  public String[] databaseNames() throws AdaptorException {

    if (databaseNames == null)
      try {
        databaseNames = JDBCUtil.databaseNames(getPool());
      } catch (SQLException e) {
        throw new AdaptorException("Failed to fetch database names", e);
      }

    return databaseNames;
  }

}