/*
 Copyright (C) 2001 EBI, GRL

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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.sql.DataSource;

import org.ensembl.driver.Adaptor;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.ConfigurationException;
import org.ensembl.driver.EnsemblDriver;
import org.ensembl.driver.ServerDriver;
import org.ensembl.driver.ServerDriverFactory;
import org.ensembl.util.ConnectionPoolDataSource;
import org.ensembl.util.JDBCUtil;
import org.ensembl.util.Version;

/**
 * Base class for all Ensembl Drivers providing database connection support and
 * adaptor management.
 * 
 * Derived classes should implement loadAdaptors().
 * 
 * Note that clearTable(...), backupTable(...), restoreTable(...) and
 * backupAndClearTable(...) methods might use SQL that incompatible with
 * database servers other than MySQL version 4.1 or higher. If you need these
 * methods and they don't work against your database server then you should
 * implement a derived class and override clear(String), restore(String) and
 * backup(String) using compatible SQL.
 * 
 * <b>Autoloading compressed databases for testing. </b>
 * 
 * We want to automatically load and delete 'compressed' test databases once and
 * on demand during test 'sessions'. The mechanism implemented here should work
 * irrespective of how many tests are run and how they are run e.g. via an ant
 * junit task, junit.TextTestRunner or from inside eclispe. For this to work all
 * the driver should stay in scope between the tests. This is acheived in the
 * ensj unit test framework by org.ensembl.Base holding a <code>static</code>
 * Registry instance which contains the drivers. This approach requires that ALL
 * tests run in the same JVM.
 * 
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 * @see #org.ensembl.test.Base
 * @see #org.ensembl.registry.Registry
 */
public class EnsemblDriverImpl implements EnsemblDriver {

  private final static Logger logger = Logger.getLogger(EnsemblDriverImpl.class
      .getName());

  private final static String BACKUP_TABLE_EXTENSION = "_backup";

  private HashMap adaptors = new HashMap();

  protected Configuration configuration;

  private String[] databaseNames = null;
  private String databaseName = null;
  private String databaseSchemaVersion = null;

  /**
   * Whether the driver should attempt to autoload the database before using it.
   */
  private boolean autoload = false;

  /**
   * Creates a drive initialised with the specified configuration.
   * 
   * @param configuration
   *          this should be a Properties instance.
   * @throws AdaptorException
   * @see #initialise(java.util.Properties) for configuration parameters.
   */
  public EnsemblDriverImpl(Properties configuration) throws AdaptorException {
    initialise(configuration);
  }

  /**
   * Constructs a driver pointing at the specified database. Assumes no password
   * and port = 3306.
   * 
   * @param host
   *          computer hosting mysqld database
   * @param database
   *          database name
   * @param user
   *          user name
   * @param databaseIsPrefix
   *          true is database is to be used as a prefix or false if it is to be
   *          used unmodified as a database name.
   */
  public EnsemblDriverImpl(String host, String database, String user,
      boolean databaseIsPrefix) throws AdaptorException {
    this(host, database, user, null, null, databaseIsPrefix);
  }

  /**
   * Constructs a driver pointing at the specified database. Assumes port =
   * 3306.
   * 
   * @param host
   *          computer hosting mysqld database
   * @param database
   *          database name
   * @param user
   *          user name
   * @param password
   *          password
   * @param databaseIsPrefix
   *          true is database is to be used as a prefix or false if it is to be
   *          used unmodified as a database name.
   */
  public EnsemblDriverImpl(String host, String database, String user,
      String password, boolean databaseIsPrefix) throws AdaptorException {
    this(host, database, user, null, null, databaseIsPrefix);
  }

  /**
   * Creates an unitialised driver with no adaptor.
   * 
   * Call initialise(Properties) to initialise this driver.
   * 
   * @see #initialise(Properties)
   */
  public EnsemblDriverImpl() {
  }

  /**
   * Constructs a driver pointing at the specified database.
   * 
   * @param host
   *          computer hosting mysqld database
   * @param database
   *          database name
   * @param user
   *          user name
   * @param password
   *          password
   * @param port
   *          port on host computer that mysqld is running on
   */
  public EnsemblDriverImpl(String host, String database, String user,
      String password, String port, boolean databaseIsPrefix)
      throws AdaptorException {
    Properties p = new Properties();

    if (host == null)
      throw new AdaptorException("host can not be null");
    else
      p.setProperty("host", host);

    if (user == null)
      throw new AdaptorException("user can not be null");
    else
      p.setProperty("user", user);

    if (password != null && !"".equals(password))
      p.setProperty("password", password);

    if (port != null)
      p.setProperty("port", port);

    if (database != null) {
      if (databaseIsPrefix)
        p.setProperty("database_prefix", database);
      else
        p.setProperty("database", database);
    }

    try {
      initialise(p);
    } catch (ConfigurationException e) {
      throw new AdaptorException("Failed to configure driver with.", e);
    }

  }

  /**
   * Creates Driver and optionally checks for the presence of the ' database' or
   * 'database_prefix' properties.
   * 
   * @param configuration
   *          driver configuration.
   * @param requiresDatabase
   *          if true then configuration must contain 'database' or
   *          'database_prefix' properties.
   * @throws AdaptorException
   *           if driver cannot be constucted or configuration is invalid.
   */
  public EnsemblDriverImpl(Properties configuration, boolean requiresDatabase)
      throws AdaptorException {
    this(configuration);
    if (requiresDatabase && !configuration.containsKey("database")
        && !configuration.containsKey("database_prefix"))
      throw new AdaptorException(
          "Configuration requires 'database' or 'database_prefix' property.");
  }

  public synchronized boolean testConnection() {

    boolean connected = false;
    Connection conn = null;
    try {
      conn = getConnection();
    } catch (AdaptorException e) {
      logger.warning(e.getMessage());
    } finally {
      if (conn != null) {
        connected = true;
        close(conn);
      }
    }
    return connected;
  }

  public synchronized boolean isConnected() {
    return testConnection();
  }

  public Adaptor addAdaptor(Adaptor adaptor) throws AdaptorException {
    return (Adaptor) adaptors.put(adaptor.getType(), adaptor);
  }

  public synchronized void removeAdaptor(Adaptor adaptor)
      throws AdaptorException {
    removeAdaptor(adaptor.getType());
  }

  public synchronized void removeAdaptor(String type) {
    BaseAdaptor adaptor = (BaseAdaptor) adaptors.remove(type);
    if (adaptor != null) {
      adaptor.driver = null;
      logger.fine("Removed " + adaptor.getClass().getName()
          + " from CoreDriver");
    }
  }

  public synchronized void removeAllAdaptors() throws AdaptorException {
    for (Iterator iter = adaptors.values().iterator(); iter.hasNext();)
      removeAdaptor((Adaptor) iter.next());

  }

  public synchronized Connection getConnection() throws AdaptorException {

    autoload();

    DataSource ds = getDatasource();
    if (ds == null)
      return null;

    Connection conn = null;
    try {

      logger.fine("Getting connection ... ");
      conn = ds.getConnection();
      logger.fine("Got connection.");
    } catch (Exception e) {
      throw new AdaptorException(
          "Failed to initialise database connection pool : ", e);
    }

    return conn;
  }

  /**
   * Closes any open datasource connections if the underlying datasource is a
   * ConnectionPoolDataSource (which it is by default). Prints a warning if the
   * underlying connection is not a ConnectionPoolDataSource.
   * 
   * @throws AdaptorException
   *           if a problem occurs closing the connections.
   */
  public synchronized void closeAllConnections() throws AdaptorException {
    for (Iterator iter = dataSource.values().iterator(); iter.hasNext();)
      ConnectionPoolDataSource.closeAllConnections((DataSource) iter.next());
    dataSource.clear();

    // each adaptor might have it's own datasource so we need
    // to make sure those are closed as well.
    Adaptor[] adaptors = getAdaptors();
    for (int i = 0; i < adaptors.length; i++)
      adaptors[i].closeAllConnections();

  }

  /**
   * Clears all caches.
   * 
   * @throws AdaptorException
   *           if a problem occurs closing the connections.
   */
  public synchronized void clearAllCaches() throws AdaptorException {

    // each adaptor might have it's own datasource so we need
    // to make sure those are closed as well.
    Adaptor[] adaptors = getAdaptors();
    for (int i = 0; i < adaptors.length; i++)
      adaptors[i].clearCache();

    databaseNames = null;
    databaseName = null;
    databaseSchemaVersion = null;
  }

  /**
   * Convenience method for closing a connection. Also sets
   * conn.setAutoCommit(true) before closing, this is useful if conn is returned
   * to a connection pool. Prints logger.warninging if exception occurs.
   */
  public static void close(Connection conn) {

    JDBCUtil.close(conn);
  }

  /**
   * Convenience method for closing a result set. It can not setAutoCommit(true)
   * before closing. Prints logger.warninging if exception occurs.
   */
  public static void close(ResultSet rs) {

    JDBCUtil.close(rs);
  }

  /**
   * Convenience method for closing a JDBC statement.
   * 
   * Prints logger.warninging if exception occurs.
   */
  public static void close(Statement s) {

    JDBCUtil.close(s);
  }

  /**
   * Creates a connection pool datasource pointing at the database specified in
   * properties. If there are adaptor specific settings then these override the
   * default (driver) settings.
   * 
   * @param type
   *          adaptor.getType() is used to identify adaptor specific
   *          datasources.
   * @return new connection pool.
   */
  protected DataSource createDataSource(String type) throws AdaptorException {

    Configuration c = configuration.deriveConfiguration(type);
    
    // Using the cacheManger if set means that we can share connections
    // with other EnsemblDrivers.
    if (serverDriverFactory==null) 
      serverDriverFactory = new ServerDriverFactory();
    
    ServerDriver cache = serverDriverFactory.get(c);
    String database = c.getDatabase();
    if (database==null && c.getDatabasePrefix()!=null)
      database = resolveDatabaseName(cache.getDataSource(), c.getDatabasePrefix());
    return (database==null) ? cache.getDataSource() : cache.getDataSource(database);  
  }


  public synchronized Adaptor getAdaptor(String type) throws AdaptorException {

    Adaptor a = (Adaptor) adaptors.get(type);
    autoload();
    return a;
  }

  /**
   * @throws AdaptorException
   *  
   */
  private void autoload() throws AdaptorException {

    if (!autoload)
      return;
    autoload = false;

    try {
      
      Properties config = getConfiguration();

      // Allow external programmer to set a shared cache manager
      // in order to reduce required db connections. Only create one
      // here if one hasn't been provided.
      if (serverDriverFactory == null)
        serverDriverFactory = new ServerDriverFactory();
      ServerDriver cache = serverDriverFactory.get(getConfiguration());

      // Delete temporary database when JVM shuts down.
      //  - Add a cleanup thread that will be run during
      // the JVM's normal shutsdown process.
      // NOTE that this cleanup thread *might not be called*
      // if the JVM does not shutdown properly. See the Javadocs for
      // Runtime.getRuntime().addShutdownHook(...) for more
      // information. In these cases the user must manually delete the
      // databases.
      //  - We add the cleanup handler before attempting to
      // load the database so that even if the database exists
      // it will be deleted if it is explicitly marked for deletion.
      String permanent = config.getProperty("autoload.permanent");
      if (permanent==null)
        permanent = "true";
      else
        permanent = permanent.toLowerCase();
      if ("false".equals(permanent))
        Runtime.getRuntime().addShutdownHook(new Thread() {
          public void run() {
            try {
              deleteDatabase();
            } catch (AdaptorException e) {
              e.printStackTrace();
            }
          }
        });

      String database = config.getProperty("database");
      if (database == null) {
        logger.warning("Can not autoload driver "
            + "because 'database' parameter missing: " + config);
        return;
      }

      // Do not load database if it already exists.
      if (cache.contains(database))
        return;

      String autoloadSource = config.getProperty("autoload.source");
      if (autoloadSource == null) {
        logger.warning("Can not autoload driver "
            + "because 'autoload.source' (zip) parameter missing : " + config);
        return;
      }

      File f = new File(autoloadSource);
      if (!f.exists()) {
        logger
            .warning("Can not autoload driver "
                + "because 'autoload.source' (zip) file does not exist : "
                + config);
        return;
      }

      uploadDatabase(cache, database, f);

    } catch (RuntimeException e) {
      // do this to simplify debugging as it makes stack traces easier to
      // read.
      throw e;
    } catch (Exception e) {
      throw new AdaptorException(e);
    }
  }

  public synchronized Adaptor[] getAdaptors() throws AdaptorException {
    int len = adaptors.size();
    Adaptor[] adaptorArray = new Adaptor[len];
    adaptors.values().toArray(adaptorArray);
    return adaptorArray;
  }

  /**
   * Initialises the driver using the parameters in the configuration.
   * 
   * If "database_prefix" is specified then this is used to specify the latest
   * version of the database beginning with the prefix. In this case "database"
   * should not be specified. For example "database_prefix"="homo_sapiens_core"
   * will be resolved to "database"="homo_sapiens_core_HIGHEST_VERSION".
   * 
   * All caches are cleared, connections closed and adaptors removed before
   * calling <code>processConfiguration(Properties)</code> and then
   * <code>loadAdaptors()</code>.
   * 
   * Derived classes can implement there own processConfiguration(Configuration)
   * if they want to modify the configuration before <code>loadAdaptors()</code>
   * is called.
   * 
   * @param config
   *          configuration parameters.
   * @throws ConfigurationException
   * @throws AdaptorException
   */
  public synchronized void initialise(Properties config)
      throws ConfigurationException, AdaptorException {

    clearAllCaches();
    closeAllConnections();
    removeAllAdaptors();

    // copy to avoid modifying the parameter passed in
    configuration = new Configuration(config);
    logger.fine("Initial driver configuration : " + configuration);

    // amend the configuration, should be overridden by
    // derived classes for special behaviour
    processConfiguration(configuration);
    logger.fine("Derived driver configuration : " + configuration);

    if (getConfiguration().getProperty("autoload") != null)
      autoload = true;

    // load the adaptors, should be overridden by implementing
    // classes/
    loadAdaptors();

  }

  /**
   * Modifies properties if needed and validates it.
   * 
   * Prints warnings or throws an exception if the configuration is invalid.
   * 
   * Derived classes should override this method if they want different
   * behaviour.
   * 
   * <ul>
   * Default properties inserted if the key is missing:
   * <li>"jdbc_driver" : "org.gjt.mm.mysql.CoreDriver"
   * <li>"connection_pool_size" : "10"
   * <li>"connection_string" : "jdbc:mysql://" + host + <port>:
   * <li>"password" : ""
   * <li>"database" : database
   * </ul>
   * 
   * @param config
   *          object to be modified if necessary.
   * @throws ConfigurationException
   */
  protected void processConfiguration(Configuration config)
      throws ConfigurationException {

//    String databaseNamePrefix = config.getProperty("database_prefix");
//
//    String databaseName = config.getPropertyAndPutDefaultValueIfNecessary(null,
//        "database", "");
//
//    String user = config.getProperty("user");
//
//    String host = config.getProperty("host");
//
//    String port = config.getProperty("port");
//
//    // need original values for these for tests below
//    String connStr = config.getProperty("connection_string");
//    String connURL = config.getProperty("connection_url");
//
//    String connParams = config.getPropertyAndPutDefaultValueIfNecessary(null,
//        "connection_parameters",
//        "?autoReconnect=true&zeroDateTimeBehavior=round");
//
//    config.getPropertyAndPutDefaultValueIfNecessary(null, "jdbc_driver",
//        "com.mysql.jdbc.Driver");
//
//    config.getPropertyAndPutDefaultValueIfNecessary(null,
//        "connection_pool_size", "10");
//
//    config.getPropertyAndPutDefaultValueIfNecessary(null, "password", "");
//
//    if (user == null)
//      throw new ConfigurationException("user is not set: " + config);
//    if (host == null && connStr == null && connURL == null)
//      throw new ConfigurationException("host is not set: " + config);
//
//    // set default value AFTER previous test
//    connStr = config.getPropertyAndPutDefaultValueIfNecessary(null,
//        "connection_string", "jdbc:mysql://" + host
//            + ((port != null) ? (":" + port) : ""));
//
//    if (!databaseName.equals("") && databaseNamePrefix != null)
//      logger.warning("Ignoring 'database' because 'database_prefix' is set. "
//          + "Remove one of these parameters from the configuration.");
//
//    // name can be used by a DriverManager instance to identify
//    // this driver instance.
//    if (!config.containsKey("name"))
//      if (databaseNamePrefix != null)
//        config.put("name", databaseNamePrefix);
//      else
//        config.put("name", databaseName);

  }

  /**
   * This method is called by initialise(Object) and should be overridden by
   * implementing classes if the class should load adpators at initialisation.
   * 
   * @see #initialise(Properties)
   */
  protected synchronized void loadAdaptors() throws AdaptorException,
      ConfigurationException {

  }

  public synchronized Properties getConfiguration() {
    return configuration;
  }

  /**
   * Lists databases available on this server (if is a database server).
   * 
   * @return names of zero or more databases on the same server as this driver
   *         instance.
   */
  public synchronized String[] fetchDatabaseNames() throws AdaptorException {
    return fetchDatabaseNames(getDatasource());
  }

  private synchronized String[] fetchDatabaseNames(DataSource ds)
      throws AdaptorException {

    if (databaseNames == null) 
      try {
       databaseNames = JDBCUtil.databaseNames(ds);
      } catch (SQLException e) { 
        throw new AdaptorException("Failed to fetch database names", e);
      }

    return databaseNames;
  }

  /**
   * Returns the default datasource.
   * 
   * @return default datasource.
   */
  public synchronized DataSource getDatasource() throws AdaptorException {
    return getDatasource("default");
  }

  /**
   * Returns the datasource for the adaptor type. This could be the default
   * datasource or one specific to the adaptor.
   * 
   * @param adaptorType
   *          adaptor type.
   * @return the datasource for the adaptor.
   * @throws AdaptorException
   */
  protected synchronized DataSource getDatasource(String adaptorType)
      throws AdaptorException {

    if (adaptorType == null)
      throw new NullPointerException("adaptorType can not be null");

    DataSource ds = (DataSource) dataSource.get(adaptorType);
    if (ds == null) {
      if (configuration.containsKeyWithPrefix(adaptorType)) {
        ds = createDataSource(adaptorType);
      } else {
        ds = (DataSource) dataSource.get("default");
        if (ds == null) {
          ds = createDataSource("default");
          dataSource.put("default", ds);
        }
      }

      dataSource.put(adaptorType, ds);
    }
    return ds;
  }

  /**
   * Returns the name of the "latest" database that begins with prefix.
   * 
   * The latest database name has the pattern prefix + "_" + DIGITS + chars. If
   * more than one database name match this pattern then the one with the
   * highest DIGITS is chosen. e.g. converts "homo_sapiens_core" into
   * "homo_sapiens_core_24_34e" if 24_34e is the latest version of the database.
   * 
   * @param ds
   *          datasource to retrieve data from.
   * @return real database name begining with prefix or null if no database name
   *         begins with prefix.
   */
  private String resolveDatabaseName(DataSource ds, String databaseNamePrefix)
      throws AdaptorException {

    String[] all = fetchDatabaseNames(ds);

    // filter irrelevant database names:
    // we are only interested in databases that match
    // prefix + "_" + digits + other chars.
    List dbs = new ArrayList();
    Pattern p = Pattern.compile("^" + databaseNamePrefix + "_\\d+.*");
    for (int i = 0; i < all.length; i++)
      if (p.matcher(all[i]).find())
        dbs.add(all[i]);

    Collections.sort(dbs);
    String realDatabaseName = (String) (dbs.size() == 0 ? null : dbs.get(dbs
        .size() - 1));

    return realDatabaseName;

  }

  public String toString() {
    String s = null;
    try {
      Connection conn = getConnection();
      s = conn.getMetaData().getURL();
      conn.close();
    } catch (Exception e) {
      s = "ERROR: " + e.getMessage();
    }
    return s;
  }

  protected Map dataSource = new HashMap();

  private ServerDriverFactory serverDriverFactory;

  /**
   * @throws AdaptorException
   * @see org.ensembl.driver.EnsemblDriver#fetchTableNames()
   */
  public String[] fetchTableNames() throws AdaptorException {
    return fetchTableNames(false);
  }

  /**
   * @throws AdaptorException
   * @see org.ensembl.driver.EnsemblDriver#backupTables()
   */
  public void backupTables() throws AdaptorException {
    backupTables(fetchTableNames());

  }

  /**
   * @throws AdaptorException
   * @see org.ensembl.driver.EnsemblDriver#backupTables(java.lang.String[])
   */
  public void backupTables(String[] tableNames) throws AdaptorException {
    for (int i = 0; i < tableNames.length; i++)
      backupTable(tableNames[i]);

  }

  /**
   * @throws AdaptorException
   * @see org.ensembl.driver.EnsemblDriver#clearTables()
   */
  public void clearTables() throws AdaptorException {
    clearTables(fetchTableNames());
  }

  /**
   * @throws AdaptorException
   * @see org.ensembl.driver.EnsemblDriver#clearTables(java.lang.String[])
   */
  public void clearTables(String[] tableNames) throws AdaptorException {
    for (int i = 0; i < tableNames.length; i++)
      clearTable(tableNames[i]);
  }

  /**
   * @throws AdaptorException
   * @see org.ensembl.driver.EnsemblDriver#backupAndClearTables()
   */
  public void backupAndClearTables() throws AdaptorException {
    backupAndClearTables(fetchTableNames());
  }

  /**
   * @throws AdaptorException
   * @see org.ensembl.driver.EnsemblDriver#backupAndClearTable(java.lang.String)
   */
  public void backupAndClearTable(String tableName) throws AdaptorException {
    backupTable(tableName);
    clearTable(tableName);
  }

  /**
   * @throws AdaptorException
   * @see org.ensembl.driver.EnsemblDriver#backupAndClearTables(java.lang.String[])
   */
  public void backupAndClearTables(String[] tableNames) throws AdaptorException {
    for (int i = 0; i < tableNames.length; i++)
      backupAndClearTable(tableNames[i]);
  }

  /**
   * @throws AdaptorException
   * @see org.ensembl.driver.EnsemblDriver#restoreTables()
   */
  public void restoreTables() throws AdaptorException {
    restoreTables(fetchTableNames());
  }

  /**
   * @throws AdaptorException
   * @see org.ensembl.driver.EnsemblDriver#restoreTables(java.lang.String[])
   */
  public void restoreTables(String[] tableNames) throws AdaptorException {
    for (int i = 0; i < tableNames.length; i++)
      restoreTable(tableNames[i]);
  }

  /**
   * SQL (this might not work with all database servers, initially tested with
   * MySQL v4.1):
   * 
   * <pre><code>
   * 
   *  
   *   
   *    
   *     
   *      
   *       
   *        
   *         
   *          
   *           
   *            
   *             
   *               
   *                 delete from tableName; 
   *                 insert into tableName select * from tableName_backup;
   *               
   *              
   *             
   *            
   *           
   *          
   *         
   *        
   *       
   *      
   *     
   *    
   *   
   *  
   * </code></pre>
   * 
   * @throws AdaptorException
   * 
   * @see org.ensembl.driver.EnsemblDriver#restoreTable(java.lang.String)
   */
  public void restoreTable(String tableName) throws AdaptorException {
    String backupTableName = tableName + BACKUP_TABLE_EXTENSION;
    Connection conn = null;
    Statement s = null;
    try {
      conn = getConnection();
      s = conn.createStatement();
      s.execute("delete from " + tableName);
      s.execute("insert into " + tableName + " select * from "
          + backupTableName);
    } catch (SQLException e) {
      throw new AdaptorException("Problem deleting table: " + tableName, e);
    } finally {
      close(s);
      close(conn);
    }
  }

  /**
   * SQL (this might not work with all database servers, tested with MySQL):
   * 
   * <pre><code>
   * 
   *  
   *   
   *    
   *     
   *      
   *       
   *        
   *         
   *          
   *           
   *            
   *             
   *              
   *               delete from tableName;
   *               
   *              
   *             
   *            
   *           
   *          
   *         
   *        
   *       
   *      
   *     
   *    
   *   
   *  
   * </code></pre>
   * 
   * @throws AdaptorException
   * 
   * @see org.ensembl.driver.EnsemblDriver#clearTable(java.lang.String)
   */
  public void clearTable(String tableName) throws AdaptorException {
    Connection conn = null;
    Statement s = null;
    try {
      conn = getConnection();
      s = conn.createStatement();
      s.execute("delete from " + tableName);
    } catch (SQLException e) {
      throw new AdaptorException("Problem deleting table: " + tableName, e);
    } finally {
      close(s);
      close(conn);
    }
  }

  /**
   * SQL (this might not work with all database servers, tested with MySQL):
   * 
   * <pre><code>
   * 
   *  
   *   
   *    
   *     
   *      
   *       
   *        
   *         
   *          
   *           
   *            
   *             
   *              
   *                 create tableName_backup if not exists like tableName;
   *                 delete from tableName_backup;
   *                 insert into tableName_backup select * from tableName;
   *               
   *              
   *             
   *            
   *           
   *          
   *         
   *        
   *       
   *      
   *     
   *    
   *   
   *  
   * </code></pre>
   * 
   * @throws AdaptorException
   * 
   * @see org.ensembl.driver.EnsemblDriver#backupTable(java.lang.String)
   */
  public void backupTable(String tableName) throws AdaptorException {
    String backupTableName = tableName + BACKUP_TABLE_EXTENSION;
    Connection conn = null;
    Statement s = null;
    try {
      conn = getConnection();
      s = conn.createStatement();
      s.execute("create table if not exists " + backupTableName + " like "
          + tableName);
      s.execute("delete from " + backupTableName);
      s.execute("insert into " + backupTableName + " select * from "
          + tableName);
    } catch (SQLException e) {
      throw new AdaptorException("Problem deleting table: " + tableName, e);
    } finally {
      close(s);
      close(conn);
    }

  }

  /**
   * @throws AdaptorException
   * @see org.ensembl.driver.EnsemblDriver#fetchTableNames(boolean)
   */
  public String[] fetchTableNames(boolean includeBackups)
      throws AdaptorException {
    String[] names = null;
    Connection conn = null;
    ResultSet rs = null;
    try {
      conn = getConnection();
      ArrayList buf = new ArrayList();

      try {
        rs = conn.getMetaData().getTables(null, null, "", null);
        while (rs.next()) {
          String tableName = rs.getString(3);
          if (includeBackups || tableName.indexOf(BACKUP_TABLE_EXTENSION) == -1)
            buf.add(tableName);
        }
        names = (String[]) buf.toArray(new String[buf.size()]);

      } catch (SQLException e) {
        throw new AdaptorException("Problem reading table names for database",
            e);
      }
    } finally {
      close(rs);
      close(conn);

    }
    return names;
  }

  /**
   * Potentially MySQL specific way of deleting database.
   * 
   * @see org.ensembl.driver.EnsemblDriver#deleteDatabase()
   */
  public void deleteDatabase() throws AdaptorException {

    String database = getConfiguration().getProperty("database");
    logger.fine("Delete database: " + database + "\tfor driver:  "
        + getConfiguration());
    if (database == null)
      return;

    Connection conn = null;
    Statement s = null;
    try {
      if (testConnection()) {
        conn = getConnection();
        s = conn.createStatement();
        s.execute("drop database " + database);
      }

    } catch (SQLException e) {
      throw new AdaptorException("Failed to delete database: " + database, e);
    } finally {
      close(s);
      close(conn);
      closeAllConnections();
      clearAllCaches();
    }
  }

  /**
   * @see org.ensembl.driver.EnsemblDriver#getServerFactory()
   */
  public ServerDriverFactory getServerFactory() {
    return serverDriverFactory;
  }

  /**
   * @see org.ensembl.driver.EnsemblDriver#setServerDriverFactory(org.ensembl.registry.DatabaseNameCacheManager)
   */
  public void setServerDriverFactory(ServerDriverFactory cacheManager) {
    this.serverDriverFactory = cacheManager;
  }

  /**
   * Upload database.
   * 
   * Creates database and uploads relevant table contents from source file.
   * 
   * @return whether a temporary db was created for the driver.
   * @throws IOException
   * @throws ZipException
   * @throws IOException
   * @throws SQLException
   * @throws AdaptorException
   * @throws SQLException
   */
  private void uploadDatabase(ServerDriver cache, String database, File f)
      throws ZipException, IOException, AdaptorException, SQLException {

    Connection conn = null;

    try {

      // need a JDBC connection that doesn't include
      // database name because the database doesn't exist yet.
      conn = cache.getEnsemblDriver().getConnection();
      execSQL(conn, "create database " + database);
      close(conn);

      // create tables and upload data from files.
      conn = getConnection();
      Pattern sqlRegexp = Pattern.compile("^(.+/(.+))\\.sql$");
      ZipFile zf = new ZipFile(getConfiguration()
          .getProperty("autoload.source"));
      for (Enumeration zes = zf.entries(); zes.hasMoreElements();) {

        ZipEntry ze = (ZipEntry) zes.nextElement();
        String filename = ze.getName();
        Matcher m = sqlRegexp.matcher(filename);
        if (m.matches()) {

          // each table has an SQL table creation statement in one file and
          // ,optionally, a corresponding data file.

          // create table.
          execSQL(conn, inputStreamToString(zf.getInputStream(ze)));

          // import data from corresponding compressed file if available.
          String datafile = m.group(1) + ".txt";
          ZipEntry dataEntry = zf.getEntry(datafile);
          if (dataEntry != null) {
            String tablename = m.group(2);
            InputStream is = zf.getInputStream(dataEntry);
            File tmpFile = inputStreamToFile(is, tablename, "txt");
            is.close();
            execSQL(conn, "load data local infile '"
                + tmpFile.getAbsolutePath() + "' into table " + tablename);
            tmpFile.delete();
          }
        }
      }
    } finally {
      close(conn);
    }

  }

  /**
   * Extract contents of inputstream to temporary file containing prefix and
   * suffix in name.
   * 
   * @param inputStream
   *          source of data.
   * @param prefix
   *          prefix for temporary file name.
   * @param suffix
   *          suffix for temporary file name.
   * @return temporary file.
   * @throws IOException
   */
  private File inputStreamToFile(InputStream inputStream, String prefix,
      String suffix) throws IOException {

    File tmp = File.createTempFile(prefix, suffix);
    final int BUFFER_SIZE = 2048;
    final byte[] data = new byte[BUFFER_SIZE];
    BufferedInputStream in = new BufferedInputStream(inputStream, BUFFER_SIZE);
    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(
        tmp), BUFFER_SIZE);
    int n;
    while ((n = in.read(data, 0, BUFFER_SIZE)) != -1)
      out.write(data, 0, n);
    out.flush();
    out.close();
    return tmp;
  }

  private String inputStreamToString(InputStream is) throws IOException {
    StringBuffer sql = new StringBuffer();
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    String line;
    while ((line = br.readLine()) != null)
      if (!line.startsWith("#"))
        sql.append(line);
    br.close();

    return sql.toString();
  }

  private void execSQL(Connection conn, String sql) throws AdaptorException,
      SQLException {
    Statement s = null;
    try {
      logger.info(sql);
      s = conn.createStatement();
      s.execute(sql.toString());
    } finally {
      JDBCUtil.close(s);
    }
  }

  /**
   * @see org.ensembl.driver.EnsemblDriver#fetchDatabaseSchemaVersion()
   */
  public String fetchDatabaseSchemaVersion() throws AdaptorException {
    
    if (databaseSchemaVersion == null) {
      
      // attempt to lazy load schema version from these sources in this order:
      // 1 - configuration
      // 2 - database meta table
      // 3 - derive from database name.

      // try to get version from configuration.
      databaseSchemaVersion = getConfiguration().getProperty("schema_version");
      if (databaseSchemaVersion != null)
        logger.fine("Loaded databaseSchemaVersion from configuration: "
            + databaseSchemaVersion);
      
      // try to get version from database.
      if (databaseSchemaVersion == null) {
        List atts = fetchMetaValues("schema_version");
        if (atts.size()>0)
          databaseSchemaVersion = (String) atts.get(0);
      }
      
      // try to derive schema version from db name 
      if (databaseSchemaVersion == null) { 
        String dbname = fetchDatabaseName();
        System.out.println(dbname);
        if (dbname != null) {
          Pattern p = Pattern.compile("^[a-z]+_[a-z]+_[a-z]+_(\\d+)_.*");
          Matcher m = p.matcher(dbname);
          if (m.matches())
            databaseSchemaVersion = m.group(1);
          logger.fine("Derived databaseSchemaVersion from database name: "
              + databaseSchemaVersion);
        }
      }
      
      if (databaseSchemaVersion == null) {
        databaseSchemaVersion = Version.buildVersion();
        if ( databaseSchemaVersion!=null)
          logger.warning("Database Schema version unkown: defaulting to schema version = ensj build version. " +
              "\nYou should either specify schema_version in the driver configuration or rename " +
            	" \nthe database to match ensembl naming convention.");
      }

      if (databaseSchemaVersion==null)
        throw new AdaptorException("Cannot determine schema version.");
    }
    return databaseSchemaVersion;
  }

  
  public List fetchMetaValues(String metaKey) throws AdaptorException {
    List atts = new ArrayList();
    Connection conn = null;
    try {
      conn = getConnection();
      ResultSet rs = BaseAdaptor.executeQuery(conn, "select meta_value from meta where meta_key='"+metaKey+"'");
      if (rs.next())
        atts.add(rs.getString(1));
    } catch (SQLException e) {
      throw new AdaptorException(e);
    } finally {
      close(conn);
    }
    
    return atts;
  }
  
  /**
   * @see org.ensembl.driver.EnsemblDriver#fetchDatabaseName()
   */
  public String fetchDatabaseName() throws AdaptorException {
    if (databaseName==null) {
      Connection conn = null;
    	try {
        conn = getConnection(); 
        databaseName = conn.getCatalog();
      } catch (SQLException e) {
        throw new AdaptorException(e);
      } finally {
        close(conn);
      }
        
    }
    return databaseName;
  }

}