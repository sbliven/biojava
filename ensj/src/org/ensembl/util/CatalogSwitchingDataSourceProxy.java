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

package org.ensembl.util;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.ensembl.driver.AdaptorException;


/**
 * Proxy for a connection pool that ensures getConnection() returns a connection
 * with it's catalog set to _defaultCatalog_.
 */
class CatalogSwitchingDataSourceProxy extends ConnectionPoolDataSource {
  
  private final String defaultCatalog;
  private final ConnectionPoolDataSource sourceDataSource;
  
  public CatalogSwitchingDataSourceProxy(ConnectionPoolDataSource sourceDataSource, String defaultCatalog) {
    this.sourceDataSource = sourceDataSource;
    this.defaultCatalog = defaultCatalog;
  }
  
  public static void closeAllConnections(DataSource datasource)
      throws AdaptorException {
    ConnectionPoolDataSource.closeAllConnections(datasource);
  }
  
  public int activeSize() {
    return sourceDataSource.activeSize();
  }
  
  public void closeAllConnections() throws SQLException {
    sourceDataSource.closeAllConnections();
  }
  
  public boolean equals(Object obj) {
    return sourceDataSource.equals(obj);
  }

  /**
   * Gets connection from pool and sets it's catalog to 
   * _defaultCatalog_.
   */
  public Connection getConnection() throws SQLException {
    return sourceDataSource.getConnection(defaultCatalog);
  }
  

  public Connection getConnection(String username, String password)
      throws SQLException {
    return sourceDataSource.getConnection(username, password);
  }
  
  public Connection getConnection(String catalog) throws SQLException {
    return sourceDataSource.getConnection(catalog);
  }
  
  public int getLoginTimeout() throws SQLException {
    return sourceDataSource.getLoginTimeout();
  }
  
  public PrintWriter getLogWriter() throws SQLException {
    return sourceDataSource.getLogWriter();
  }
  

  public int getMaxPoolSize() {
    return sourceDataSource.getMaxPoolSize();
  }
  

  public int hashCode() {
    return sourceDataSource.hashCode();
  }
  
  public int poolSize() {
    return sourceDataSource.poolSize();
  }
  
  public void release(Connection conn) {
    sourceDataSource.release(conn);
  }
  
  public void setLoginTimeout(int seconds) throws SQLException {
    sourceDataSource.setLoginTimeout(seconds);
  }
  
  public void setLogWriter(PrintWriter out) throws SQLException {
    sourceDataSource.setLogWriter(out);
  }
  
 public String toString() {
  StringBuffer buf = new StringBuffer();

  buf.append("[");
  buf.append("defaultCatalog=").append(defaultCatalog);
  buf.append(", sourceDataSource=").append(sourceDataSource.toString());
  buf.append("]");

  return buf.toString();
}
}