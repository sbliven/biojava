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

package org.ensembl.util;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.ensembl.driver.AdaptorException;

/**
 * Connection pooling implementation of a datasource.
 * 
 * Checks every 5 minutes for unused connections and closes them.
 * 
 * If you want to connect to several databases on the same server
 * then it is possible to reduce the number of connections used by
 * pooling connections this usually reduces the load on both the client and 
 * server. See createDataSourceProxy(String defaultCatalog)
 * and getConnection(String defaultCatalog).
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 * @see #createDataSourceProxy(String)
 * @see #getConnection(String) 
 */
public class ConnectionPoolDataSource implements DataSource {

  /**
   * Peridically closes unused connections if necessary.
   */
  private class ConnectionCloser extends Thread {

    /**
     * Close unused connections after 5 minutes.
     */
    private final int CONNECTION_POOL_CLOSE_TIMEOUT = 300000;

    private long timeStamp = 0;

    private long connectionActionTimestamp = 0;

    private ConnectionCloser() {
      setDaemon(true);
      setName("ConnectionPoolCloser");
    }

    /**
     * Runs every CONNECTION_POOL_CLOSE_TIMEOUT ms and closes all pooled
     * connections if connectionEvent() has not been called since the last time
     * it ran.
     */
    public void run() {
      while (true) {
        try {

          timeStamp = System.currentTimeMillis();
          Thread.sleep(CONNECTION_POOL_CLOSE_TIMEOUT);
          if (connectionActionTimestamp < timeStamp)
            closeAllConnections(pool);

        } catch (InterruptedException e) {
          e.printStackTrace();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }

    /**
     * Marks the time of a connection event.
     */
    public void connectionEvent() {
      connectionActionTimestamp = System.currentTimeMillis();
    }
  }

  
  private static final Logger logger = Logger
      .getLogger(ConnectionPoolDataSource.class.getName());

  private String fullConnectionString;

  private int maxPoolSize;

  private Driver driver;

  private Properties config = new Properties();

  private List active = new Vector();

  private List pool = new Vector();

  private ConnectionCloser connectionCloser = new ConnectionCloser();

  /**
   * Creates new instance of the connection pool.
   * 
   * @param jdbcDriverName
   *          name of the jdbc driver
   * @param fullConnectionString
   *          connection string
   * @param user
   *          user name
   * @param password
   *          password, can be null
   * @param maxPoolSize
   *          maximum connection pool size, must be >0
   * @throws ClassNotFoundException
   *           if the jdbc driver can be found
   * @throws SQLException
   *           if a problem occurs connecting to the database
   * @throws IllegalArgumentException
   *           if the parameters are illegal
   */
  public ConnectionPoolDataSource(String jdbcDriverName,
      String fullConnectionString, String user, String password, int maxPoolSize)
      throws ClassNotFoundException, SQLException {

    if (maxPoolSize < 1)
      throw new IllegalArgumentException("maxPoolSize should be >0 but is: "
          + maxPoolSize);
    if (fullConnectionString == null)
      throw new IllegalArgumentException("fullConnectionString is Null");
    if (user == null)
      throw new IllegalArgumentException("user is Null");

    this.maxPoolSize = maxPoolSize;
    this.fullConnectionString = fullConnectionString;
    config.put("user", user);
    if (password != null)
      config.put("password", password);

    Class.forName(jdbcDriverName);
    this.driver = DriverManager.getDriver(fullConnectionString);

    connectionCloser.start();

  }

  /**
   * Creates an unconfigured connection pool.
   */
  public ConnectionPoolDataSource() {
  }
  
  /**
   * Creates a proxy for this pool which returns a Datasource
   * whose getConnection() method returns a connection
   * pointing at _defaultConnection_.
   * 
   * Optimisation: This method is used to create multiple DataSources backed by a
   * single pool which enables different users to access multiple databases on the 
   * same server using the same connections.
   * Doing this usually reduces the number of database connections 
   * required for a server. 
   * 
   * Also, <code>createDataSource("my_db").getConnection().getCatalog().equals("my_db").</code>
   * 
   * @param defaultCatalog
   * @return catalog specific datasource backed by this pool.
   * @see #getConnection()
   * @see #getConnection(String)
   */
  public DataSource createDataSourceProxy(String defaultCatalog) {
    return new CatalogSwitchingDataSourceProxy(this, defaultCatalog);
  }
  
  /**
   * Gets a connection, either from the pool or a new one is created. If
   * creating a new connection would exceed the pool size then an SQLException
   * is thrown.
   * 
   * @return connection from pool if one is available, otherwise a new one if
   *         the creation of the connection does not exceed the maxPoolSize.
   * @see javax.sql.DataSource#getConnection()
   * @throws SQLException
   *           if a problem occurs connecting to db. This includes if the number
   *           of connections will be exceeded if another is created.
   */
  public synchronized Connection getConnection() throws SQLException {

    Connection conn = getPooledConnection(0);

    if (conn == null)
      if (active.size() >= maxPoolSize)
        throw new SQLException("Connection pool limit of " + maxPoolSize
            + " exceeded");
      else
        conn = createNewConnection();

    connectionCloser.connectionEvent();

    return conn;
  }

  /**
   * Returns a connection to the specified catalog from the pool or by creating
   * a new one.
   * 
   * If creating a new connection would exceed the pool size then an
   * SQLException is thrown.
   * 
   * @return connection to specified catalog from pool if one is available,
   *         otherwise a new one if the creation of the connection does not
   *         exceed the maxPoolSize.
   * @see javax.sql.DataSource#getConnection()
   * @throws SQLException
   *           if a problem occurs connecting to db. This includes if the number
   *           of connections will be exceeded if another is created.
   * @see #createDataSourceProxy(String) An alternative to calling this method is to 
   * create a datasource backed by this pool. 
   */
  public synchronized Connection getConnection(String catalog)
      throws SQLException {

    Connection conn = null;

    int n = pool.size();
    if (n > 0) {
      
      // TODO test whether this is faster or slower.
      // if one already exists get a pooled connection that already points to the catalog
      for (int j = 0; j < n; j++) {
        Connection c = (Connection) pool.get(j);
        String cat = c.getCatalog();
        if (catalog == cat || (catalog != null && catalog.equals(cat))) {
          conn = getPooledConnection(j);
          break;
        }
      }
      
      if (conn==null) {
        // get the first pooled connection and set it's catalog
        conn = getPooledConnection(0);
        conn.setCatalog(catalog);
      }
    }
    
    if (conn==null) 
      if (conn == null)
        if (active.size() >= maxPoolSize)
          throw new SQLException("Connection pool limit of " + maxPoolSize
              + "exceeded");
        else
          conn = createNewConnection(catalog);
    
    connectionCloser.connectionEvent();
    
    return conn;
  }

  /**
   * Returns the connection to the pool. Does nothing if this connection was not
   * created by this instance.
   * 
   * @param conn
   *          connection to be returned to pool.
   */
  public synchronized void release(Connection conn) {
    if (active.contains(conn)) {
      active.remove(conn);
      pool.add(conn);
      logger.fine("Released connection: " + conn);
      logger.fine(toString());
    } else {
      logger.fine("Failed to release connection: " + conn);
      logger.fine(toString());
    }

    connectionCloser.connectionEvent();
  }

  private synchronized Connection createNewConnection(String catalog)
      throws SQLException {
    Connection conn = createNewConnection();
    conn.setCatalog(catalog);
    return conn;
  }

  private synchronized Connection createNewConnection() throws SQLException {

    //  It is useful to see where we connect to and with what parameters,
    // we now get this printed if environment variable "ensj.debug" is set
    // or logger begug level>=DEBUG.
    if (System.getProperty("ensj.debug") != null
        || logger.isLoggable(Level.FINE)) {

      String connectionConfirmation = "Connecting to :"
          + "\nconnection_string = " + fullConnectionString + "\nuser= "
          + config.getProperty("user") + "\n password = "
          + config.getProperty("password");

      if (logger.isLoggable(Level.FINE))
        logger.fine(connectionConfirmation);
      else
        logger.info(connectionConfirmation);
    }

    
    Connection rawConn;
    try {
      rawConn = driver.connect(
          fullConnectionString, config);
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Failed to create connection: " + fullConnectionString +"\t"+config);
      throw e;
    }
    Connection conn = new ConnectionWrapper(rawConn, this);

    active.add(conn);
    logger.fine("Created new connection: " + conn);
    logger.fine(toString());

    return conn;

  }

  private synchronized Connection getPooledConnection(int poolIndex) {
    Connection conn = null;
    if (pool.size() > 0) {
      conn = (Connection) pool.remove(poolIndex);
      active.add(conn);
      logger.fine("Got pooled connection: " + conn);
      logger.fine(toString());
    } else {
      logger.fine("Failed to retrieve connection from pool");
      logger.fine(toString());
    }
    return conn;
  }

  /**
   * @throws NotImplementedYetException
   */
  public Connection getConnection(String arg0, String arg1) throws SQLException {
    throw new NotImplementedYetException();
  }

  /**
   * @throws NotImplementedYetException
   */
  public PrintWriter getLogWriter() throws SQLException {
    throw new NotImplementedYetException();
  }

  /**
   * @throws NotImplementedYetException
   */
  public int getLoginTimeout() throws SQLException {
    throw new NotImplementedYetException();
  }

  /**
   * @throws NotImplementedYetException
   */
  public void setLogWriter(PrintWriter arg0) throws SQLException {
    throw new NotImplementedYetException();
  }

  /**
   * @throws NotImplementedYetException
   */
  public void setLoginTimeout(int arg0) throws SQLException {
    throw new NotImplementedYetException();
  }

  /**
   * @return maximum pool size
   */
  public int getMaxPoolSize() {
    return maxPoolSize;
  }

  /**
   * 
   * @return current number of connections in the pool.
   */
  public int poolSize() {
    return pool.size();
  }

  /**
   * 
   * @return current number of connections that have been given out through
   *         getConnection().
   */
  public int activeSize() {
    return active.size();
  }

  /**
   * Close the underlying database connections.
   * 
   * @throws SQLException
   */
  public synchronized void closeAllConnections() throws SQLException {
    closeAllConnections(pool);
    closeAllConnections(active);
  }

  private synchronized void closeAllConnections(List connections)
      throws SQLException {
    while (connections.size() > 0) {
      ConnectionWrapper conn = (ConnectionWrapper) connections.remove(0);
      conn.closeWrappedConnection();
    }
  }

  /**
   * Convenience method for closing connections on instances of this class,
   * handles the fact that the datasource is null and prints a warning if it is
   * not a ConnectionPoolDataSource.
   * 
   * @param datasource
   * @throws SQLException
   */
  public static void closeAllConnections(DataSource datasource)
      throws AdaptorException {
    if (datasource == null) {

      return;

    } else if (datasource instanceof ConnectionPoolDataSource) {

      try {
        ((ConnectionPoolDataSource) datasource).closeAllConnections();
      } catch (SQLException e) {
        throw new AdaptorException("Failed to close database connections.", e);
      }

    } else {

      logger
          .warning("Can not close connections because unsupported Datasource: "
              + datasource);

    }
  }

  /**
   * @return string representation of this pool.
   */
  public String toString() {
    StringBuffer buf = new StringBuffer();

    buf.append("[");
    buf.append("maxPoolSize=").append(maxPoolSize);
    buf.append(", activeSize=").append(activeSize());
    buf.append(", poolSize=").append(poolSize());
    buf.append(", fullConnectionString=").append(fullConnectionString);
    buf.append(", user=").append(config.getProperty("user"));
    buf.append("]");

    return buf.toString();
  }

}