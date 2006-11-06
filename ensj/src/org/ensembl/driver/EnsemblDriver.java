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
package org.ensembl.driver;

import java.sql.Connection;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;



/**
 * An ensembl driver provides access to an ensembl database
 * through it's adaptors.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public interface EnsemblDriver {
  
  
  /**
   * Initialises the driver and it's adaptor with the 
   * configuration. 
   * @param configuration parameters specifying the database
   * and any additional datasource specific settings. 
   * @throws ConfigurationException
   * @throws AdaptorException
   */
  void initialise(Properties configuration) throws ConfigurationException, AdaptorException;

  /**
   * 
   * @return configuration provided to initialise(Properties) plus
   * any locally made modifications.
   * @see #initialise(Properties)
   */
  Properties getConfiguration();


  /**
   * @return Adaptor of the specified type, or
   * null if no such driver available.
   */
  Adaptor getAdaptor(String type) throws AdaptorException;


  /**
   * @return array of zero or more Adaptors.
   */
  Adaptor[] getAdaptors() throws AdaptorException;


  /**
   * Checks whether a connection can be made to the database.
   * 
   * Useful for checking if a driver has been correctly configured.
   * 
   * Note: This method might leave an open connection in a connection pool
   * so you should call closeAllConnections() if you want to ensure all connections
   * are closed. 
   * 
   * @return true if driver can connect to it's persistent store, otherwise
   *         false.
   * @see #closeAllConnections()
   */
  boolean testConnection();
  
  /**
   * Checks whether a connection can be made to the database.
   * 
   * Useful for checking if a driver has been correctly configured.
   * 
   * Note: This method might leave an open connection in a connection pool
   * so you should call closeAllConnections() if you want to ensure all connections
   * are closed. 
   * 
   * @return  true if driver can connect to it's persistent store, otherwise
   *         false.
   * @deprecated Since ensj 30.3. Use testConnection() instead.
   * @see #closeAllConnections()
   */
  boolean isConnected();

  
  /**
   * Lists databases available on this server.
   * 
   * This method does not require the database or 
   * database_prefix to specified in the configuration. 
   * This means
   * that you can connect to a database server and retrieve the 
   * database names without having to first know one of them.
   * 
   * @return zero or more database names available on the same server as this driver instance. 
   */
  String[] fetchDatabaseNames() throws AdaptorException;

  
  /**
   * Attempts to connects to the database if not already connected.
   *
   * Useful for checking if a driver has been correctly configured.
   * 
   * @return true if driver can connect to it's persistent store, otherwise
   *         false.
   */
  Adaptor addAdaptor(Adaptor adaptor) throws AdaptorException;
  
  void removeAdaptor(Adaptor adaptor) throws AdaptorException;
  
  void removeAdaptor(String type);
  
  void removeAllAdaptors() throws AdaptorException;

  Connection getConnection() throws AdaptorException;
  
  /**
   * Clears all caches.
   * 
   * @throws AdaptorException if a problem occurs closing the connections.
   */
  void clearAllCaches() throws AdaptorException;

  /**
   * Closes all connections opened by the driver and it's adapotors.
   * 
   * @throws AdaptorException
   */
  void closeAllConnections() throws AdaptorException;

  /**
   * Datasource this driver gets connections from.
   * @return datasource this driver gets connections from.
   */
  DataSource getDatasource() throws AdaptorException;
  
  /**
   * If two EnsemblDrivers wrap databases on the same
   * database server then setting the same ServerDriverFactory
   * on both will enable them to share connections.
   * @return factory if set,otherwise null.
   */
  ServerDriverFactory getServerFactory();
  
  
  /**
   * If two EnsemblDrivers wrap databases on the same
   * database server then setting the same ServerDriverFactory
   * on both will enable them to share connections.
   */
  void setServerDriverFactory(ServerDriverFactory cacheManager);
  
	/**
	 * Return list of database tables excluding backup tables.
	 * 
	 * Calls fetchTableNames(false).
	 * 
	 * @return all table names in associated database excluding backup 
	 * tables.
	 * @throws AdaptorException
	 * @see #fetchTableNames(boolean)
	 */
	String[] fetchTableNames() throws AdaptorException;
	
	/**
	 * Returns list of tables in database with or without backup tables.
	 * 
	 * @param includeBackups whether to include backup tables.
	 * @return zero or more table names.
	 * @throws AdaptorException
	 */
	String[] fetchTableNames(boolean includeBackups) throws AdaptorException;
	
	/**
	 * Backup all tables returned by fetchTableNames().
	 * 
	 * Calls backup(String) for each table returned by fetchTableNames().
	 * 
	 * Note: this method might not work with all database servers, it has
	 * only been tested with MySQL.
	 * 
	 * @see #fetchTableNames()
	 * @see #backupTable(String)
	 * @throws AdaptorException
	 */
	void backupTables() throws AdaptorException;
	
	/**
	 * Backup the table.
	 * 
	 * Note: this method might not work with all database servers, it has
	 * only been tested with MySQL.
	 * 
	 * @param tableName name of table to be backed up.
	 * @throws AdaptorException
	 */
	void backupTable(String tableName) throws AdaptorException;
	
	/**
	 * Backup all the tableNames.
	 * 
	 * Note: this method might not work with all database servers, it has
	 * only been tested with MySQL.
	 * 
	 * @param tableNames tables to be backed up.
	 * @throws AdaptorException
	 */
	void backupTables(String[] tableNames) throws AdaptorException;

	/**
	 * Clears all tables returned by fetchTableNames();
	 *
	 * Note: this method might not work with all database servers, it has
	 * only been tested with MySQL.
	 * 
	 * Calls clear(String) for all tables returned by fetchTableNames().
	 * @throws AdaptorException
	 */
	void clearTables() throws AdaptorException;
	
	/**
	 * Empties the table.
	 * 
	 * Note: this method might not work with all database servers, it has
	 * only been tested with MySQL.
	 * @param tableName table name.
	 * @throws AdaptorException
	 */
	void clearTable(String tableName) throws AdaptorException;
	
	/**
	 * Empties all the tables.
	 * 
	 * Note: this method might not work with all database servers, it has
	 * only been tested with MySQL.
	 * 
	 * Calls clear(String) for all tableNames.
	 * @param tableNames tables to to cleared.
	 * @throws AdaptorException
	 */
	void clearTables(String[] tableNames) throws AdaptorException;	
	
	/**
	 * Backs up and clears all tables returned by fetchTableNames().
	 * 
	 * Calls backupAndClear(String) for all tables returned by fetchTableNames().
	 * 
	 * Note: this method might not work with all database servers, it has
	 * only been tested with MySQL.
	 * 
	 * @throws AdaptorException
	 * @see #fetchTableNames()
	 */
	void backupAndClearTables() throws AdaptorException;
	
	/**
	 * Make a backup copy of the table and empty the original table.
	 * Calls backup(tableName) and clear(tableName).
	 * 
	 * Note: this method might not work with all database servers, it has
	 * only been tested with MySQL.
	 * 
	 * @param tableName table to be backed up and cleared.
	 * @throws AdaptorException
	 */
	void backupAndClearTable(String tableName) throws AdaptorException;
	
	/**
	 * Backup the tables and empty the originals.
	 * 
	 * Note: this method might not work with all database servers, it has
	 * only been tested with MySQL.
	 * 
	 * @param tableNames tables to be backed up and cleared.
	 * @throws AdaptorException
	 * @see #backupAndClearTable(String)
	 */
	void backupAndClearTables(String[] tableNames) throws AdaptorException;
	
	/**
	 * Restores all the tables returned by fetchTableNames(). 
	 * that have backup copies.
	 * 
	 * Calls restore(String) for each table.
	 * 
	 * Note: this method might not work with all database servers, it has
	 * only been tested with MySQL.
	 * 
	 * @throws AdaptorException
	 * @see #restoreTable(String)
	 */
	void restoreTables() throws AdaptorException;
	
	/**
	 * Restores the table from a backup table.
	 * Restoring means copying the rows from the backup table into
	 * the table, replacing any original data.
	 * 
	 * 
	 * @param tableName table to be restored.
	 * @throws AdaptorException
	 */
	void restoreTable(String tableName) throws AdaptorException;
	
	/**
	 * Restores all the tables from their respective backups.
	 * 
	 * Calls restore(String) for each table.
	 * 
	 * Note: this method might not work with all database servers, it has
	 * only been tested with MySQL.
	 * 
	 * @param tableNames tables to be restored.
	 * @throws AdaptorException
	 */
	void restoreTables(String[] tableNames) throws AdaptorException;
	
	/**
	 * Delete the database this driver wraps.
	 * 
	 * Uses getConfiguration.getProperty("database") to determine 
	 * the database name. Does nothing if it is null. 
	 * 
	 * Note: this method might not work with all database servers, it has
	 * only been tested with MySQL.
	 * 
	 * @throws AdaptorException
	 */
  void deleteDatabase() throws AdaptorException;
  
  /**
   * Returns the schema version for the database this driver wraps.
   * 
   * The schema version is determined by these values, in this order:
   * <ol>
   * <li>If available the attribute driver.getConfiguration().getProperty("schema_version")
   * is used.
   * <li>If this driver wraps a specific database name derived from the penultimate "number" in the database name. 
   * e.g. in homo_sapiens_core_34_35g the schema version is 34.
   * </ol>
   * 
   * @return the schema version for the database this driver wraps, null
   * if it can not be determined.
   * @throws AdaptorException
   */
  String fetchDatabaseSchemaVersion() throws AdaptorException;
  
  
  /**
   * Returns zero or more values from the meta table matching the specified meta_key.
   * @param metaKey meta_key filter.
   * @return zero or more meta attributes as Strings.
   * @throws AdaptorException
   */
  List fetchMetaValues(String metaKey) throws AdaptorException;
  
  
  /**
   * Returns the database name.
   * 
   * @return name of the database this driver wraps, null if it does not
   * wrap a specific database. 
   * @throws AdaptorException
   */
  String fetchDatabaseName() throws AdaptorException;
  
}