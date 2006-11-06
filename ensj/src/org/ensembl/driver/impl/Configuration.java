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

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;

import org.ensembl.driver.ConfigurationException;
import org.ensembl.util.ConnectionPoolDataSource;
import org.ensembl.util.PropertiesUtil;

/**
 * Properties object augmented with convenience methods to support both
 * key=value and prefix.key=value pairs.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 */
public class Configuration extends Properties {

  private static final long serialVersionUID = 1L;

  public Configuration() {
  }

  public Configuration(Properties properties) {
    putAll(properties);
  }

  public boolean containsKeyWithPrefix(String prefix) {
    for (Enumeration keysEnum = keys(); keysEnum.hasMoreElements();) {
      String key = (String) keysEnum.nextElement();
      if (key.startsWith(prefix))
        return true;
    }
    return false;
  }

  public void putProperty(String prefix, String rawKey, String value) {
    put(compositeKey(prefix, rawKey), value);
  }

  public String getProperty(String prefix, String rawKey) {
    String value = getProperty(compositeKey(prefix, rawKey));
    if (value == null)
      value = getProperty(rawKey);
    return value;
  }

  /**
   * Returns a value for key and adds property if _key_ is not present.
   * 
   * @param prefix
   *          key prefix, ignored if null.
   * @param rawKey
   *          raw key.
   * @param defaultValue
   *          default value to use and insert into Configuration if key not
   *          present.
   * @return value if present, otherwise _defaultValue_.
   */
  public String getPropertyAndPutDefaultValueIfNecessary(String prefix,
      String rawKey, String defaultValue) {
    String value = getProperty(prefix, rawKey);
    if (value == null)
      put(compositeKey(prefix, rawKey), defaultValue);
    return defaultValue;
  }

  private String compositeKey(String prefix, String rawKey) {
    return (prefix == null) ? rawKey : prefix + "." + rawKey;
  }

  /**
   * Return the JDBC driver specified by the "jdbc_driver" parameter or
   * "com.mysql.jdbc.Driver" by default.
   * 
   * @return JDBC driver specified by the "jdbc_driver" parameter or
   *         "com.mysql.jdbc.Driver" by default.
   */
  public String getJdbcDriver() {
    String s = getProperty("jdbc_driver");
    return (s == null) ? "com.mysql.jdbc.Driver" : s;
  }

  public String getHost() {
    return getProperty("host");
  }

  public int getPort() {
    String s = getProperty("port");
    return (s == null) ? 3306 : Integer.parseInt(s);
  }

  public String getDatabase() {
    return getProperty("database");
  }

  public String getDatabasePrefix() {
    return getProperty("database_prefix");
  }

  public String getUser() {
    return getProperty("user");
  }

  public String getPassword() {
    return getProperty("password");
  }

  /**
   * Returns the JDBC connection url defined by the properties.
   * 
   * Returns getProperty("connection_url") if not null, otherwise default to
   * "jdbc:mysql://HOST:PORT/&lt;DATABASE&gt;?autoReconnect=true&zeroDateTimeBehavior=round";
   * 
   * @return JDBC connection url defined by the properties.
   */
  public String getConnectionURL() {

    String url = getProperty("connection_url");

    if (url == null) {

      String database = getDatabase();
      // derive default_connection_url from individual parameters
      StringBuffer buf = new StringBuffer();
      buf.append("jdbc:mysql://").append(getHost()).append(":").append(
          getPort()).append("/");
      if (database != null)
        buf.append(database);
      String connectionParams = getProperty("connection_parameters");
      if (connectionParams==null)
        connectionParams="autoReconnect=true&zeroDateTimeBehavior=round";
      buf.append("?").append(connectionParams);
      url = buf.toString();
    }

    return url;
  }

  /**
   * Return connection pool size defined by the "connection_pool_size"
   * parameter.
   * 
   * XXX NOTE: the default will change to 1 in future releases.
   * 
   * @return connection pool size, 4 if not set.
   */
  public int getConnectionPoolSize() {
    // XXX Change default size to 1 once adaptors optimised
    String s = getProperty("connection_pool_size");
    return (s == null) ? 4 : Integer.parseInt(s);
  }

  /**
   * Derives a new Configuration by removing _prefix_ from the beginning of any
   * keys and, in the process, replaces any other properties with the same key.
   * 
   * e.g. if config = {"core.host"-> "athena", "host"->"zeus"} then
   * config.deriveConfiguration("core") = {"host"->"athena"}
   * 
   * @param prefix
   *          prefix of property keys to update.
   * @return new Configuration with possibly updated properties.
   */
  public Configuration deriveConfiguration(String prefix) {
    return new Configuration(PropertiesUtil.removePrefixFromKeys(this, prefix));
  }

  /**
   * Checks whether the connection is fully specified.
   * 
   * @return null if the connection is fully specified, or an error message
   *         otherwise.
   */
  public String validateConnectionConfiguration(boolean requireDatabase) {
    StringBuffer err = new StringBuffer();

    if (getConnectionURL() != null)
      return null;

    if (getHost() == null)
      err.append("host, ");

    if (getUser() == null)
      err.append("user, ");

    if (requireDatabase)
      if (getDatabase() == null && getDatabasePrefix() == null)
        err.append("database or database_prefix, ");

    if (getUser() == null)
      err.append("user, ");

    return (err.length() == 0) ? null : "Invalid connection configuration"
        + " because missing parameters: " + err.toString();
  }

  /**
   * @throws ConfigurationException
   * @throws SQLException
   * @throws ClassNotFoundException
   * @throws
   *  
   */
  public ConnectionPoolDataSource createPool() throws ConfigurationException,
      ClassNotFoundException, SQLException {
    String err = validateConnectionConfiguration(false);
    if (err != null)
      throw new ConfigurationException(err);

    return new ConnectionPoolDataSource(getJdbcDriver(), getConnectionURL(),
        getUser(), getPassword(), getConnectionPoolSize());

  }

}