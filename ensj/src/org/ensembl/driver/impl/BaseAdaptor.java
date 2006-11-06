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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.ensembl.datamodel.CloneFragmentLocation;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Persistent;
import org.ensembl.driver.Adaptor;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.RuntimeAdaptorException;
import org.ensembl.util.ConnectionPoolDataSource;
import org.ensembl.util.LongList;
import org.ensembl.util.LruCache;
import org.ensembl.util.Warnings;

abstract public class BaseAdaptor implements Adaptor {
	private static final Logger logger = Logger.getLogger(BaseAdaptor.class
			.getName());

	final String NULL = "NULL";

	protected LruCache cache;

	protected CoreDriverImpl driver;

	protected DataSource dataSource = null;

	protected BaseAdaptor(CoreDriverImpl driver) {
		this.driver = driver;
	}

	/**
	 * Creates driver member with specified driver attached and an
	 * internalID->Persistent cache. See Cache for meaning of cache parameters.
	 * 
	 * @see LruCache org.ensembl.util.LruCache
	 */
	protected BaseAdaptor(CoreDriverImpl driver, int cachemaxCapacity) {
		this(driver);
		if (cachemaxCapacity > 0)
			cache = new LruCache(cachemaxCapacity);
	}

	/**
	 * Closes any open datasource connections if the underlying datasource is a
	 * ConnectionPoolDataSource (which it is by default). Prints a warning if
	 * the underlying connection is not a ConnectionPoolDataSource.
	 * 
	 * Each adaptor can have a separate datasource so it is necessary to ensure
	 * all connections are closed.
	 * 
	 * @throws AdaptorException
	 *             if a problem occurs closing the connections.
	 */
	public void closeAllConnections() throws AdaptorException {
		ConnectionPoolDataSource.closeAllConnections(dataSource);
	}

	/**
	 * Clears cache if cache is used. Does nothing otherwise.
	 *  
	 */
	public void clearCache() {
		if (cache != null)
			cache.clear();
	}

	public final org.ensembl.driver.CoreDriver getDriver() {
		return driver;
	}

	public final boolean supportsMap(String map) {
		return false;
	}

	/**
	 * Convenience method for closing a connection. Also sets
	 * conn.setAutoCommit(true) before closing, this is useful if conn is
	 * returned to a connection pool. Prints logger.warninging if exception
	 * occurs.
	 */
	public static void close(Connection conn) {

		CoreDriverImpl.close(conn);
	}

	/**
	 * Convenience method for closing a connection on a ResultSet. Also sets
	 * conn.setAutoCommit(true) before closing, this is useful if conn is
	 * returned to a connection pool. Prints logger.warninging if exception
	 * occurs.
	 */
	public static void close(ResultSet rs) {

		CoreDriverImpl.close(rs);
	}

	public static void rollback(Connection conn) {
		try {
			if (conn != null) {
				conn.rollback();
			}
		} catch (SQLException e) {
			logger.warning("Failed to rollback transaction. " + e.getMessage());
		}
	}

	/**
	 * Convenience method which wraps conn.createStatement().executeUpdate(sql).
	 * Sql is included in exception.message if exception thrown.
	 * 
	 * @return number of rows affected.
	 */
	public static int executeUpdate(Connection conn, String sql)
			throws AdaptorException {
		try {
			return conn.createStatement().executeUpdate(sql);
		} catch (SQLException e) {
			throw createAdaptorException(conn, sql, e);
		}
	}

	/**
	 * Convenience method which wraps ps.executeUpdate() in try/catch and
	 * includes SQL in thrown exception.
	 * 
	 * @return number of rows affected.
	 */
	public static int executeUpdate(PreparedStatement ps, String sql)
			throws AdaptorException {
		try {
			return ps.executeUpdate();
		} catch (SQLException e) {
			throw createAdaptorException(ps, sql, e);
		}
	}

	/**
	 * Convenience method that executes the SQL using the connection with or
	 * without batching results.
	 * 
	 * @param conn
	 *            connection on which to execute the query
	 * @param sql
	 *            SQL query to execute
	 * @param batchResultsHint
	 *            a hint to the JDBC driver to batch results.
	 * @return ResultSet generated by executing the query.
	 * @throws AdaptorException
	 *             Wraps an SQLException and includes the SQL parameter in the
	 *             error message.
	 */
	public static ResultSet executeQuery(Connection conn, String sql,
			boolean batchResultsHint) throws AdaptorException {
		try {

			Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY);

			// TYPE_FORWARD_ONLY + CONCUR_READ_ONLY +
			// stmt.setFetchSize(Integer.MIN_VALUE)
			// cause streaming one-row-at-a-time. Value must be
			// Integer.MIN_VALUE.

			// Ideally we would use stmt.setFetchSize(1000) to fetch batches of
			// up
			// to 1000 rows but this won't be supported for mysql until
			// connectorJ >=3.2
			// and MySQL >=5.0.

			if (batchResultsHint && conn.getMetaData().getDriverName().toLowerCase().matches(".*mysql.*"))
				stmt.setFetchSize(Integer.MIN_VALUE);
			// Replaced next line because it breaks methods
			// (e.g. MySQLQTLAdaptor.createObject()) which call
			// resultSet.previous()
			//stmt.setFetchSize(1000);

			return stmt.executeQuery(sql);
		} catch (SQLException e) {
			throw createAdaptorException(conn, sql, e);
		}
	}
	
	private static AdaptorException createAdaptorException(Connection conn, String sql, SQLException e) {
		String database = null;
		if (conn!=null)
			try {
				database = conn.getCatalog();
			} catch (SQLException e1) {
				// do nothing
			}
		StringBuffer msg = new StringBuffer();
		msg.append("Failed to execute sql");
		if (database!=null)
			msg.append(" against database " + database);
		msg.append(":");
		msg.append(sql);
		return new AdaptorException(msg.toString(), e);
	}

	private static AdaptorException createAdaptorException(PreparedStatement ps, String sql, SQLException e) {
		Connection conn = null;
		try {
			conn = ps.getConnection();
		} catch (SQLException e1) {
			// do nothing
		}
		return createAdaptorException(conn, sql, e);
	}
	
	/**
	 * Convenience method that executes the SQL using the connection without
	 * batching results.
	 * 
	 * Delegates call to executeQuery(conn, sql, false).
	 * 
	 * @param conn
	 *            connection on which to execute the query
	 * @param sql
	 *            SQL query to execute
	 * @return ResultSet generated by executing the query.
	 * @throws AdaptorException
	 *             Wraps an SQLException and includes the SQL parameter in the
	 *             error message.
	 * @see #executeQuery(Connection, String, boolean)
	 */
	public static ResultSet executeQuery(Connection conn, String sql)
			throws AdaptorException {
		return executeQuery(conn, sql, false);
	}

	/**
	 * Convenience method which wraps ps.executeQuery(). Sql is included in
	 * exception.message if exception thrown.
	 * 
	 * @return ResultSet generated by executing the query.
	 */
	public static ResultSet executeQuery(PreparedStatement ps, String sql)
			throws AdaptorException {
		try {
			return ps.executeQuery();
		} catch (SQLException e) {
			String database = null;
			if (ps!=null)
				try {
					database = ps.getConnection().getCatalog();
				} catch (SQLException e1) {
					// do nothing
				}
			StringBuffer msg = new StringBuffer();
			msg.append("Failed to execute sql");
			if (database!=null)
				msg.append(" on database " + database);
			msg.append(":");
			msg.append(sql);
			
			throw createAdaptorException(ps, sql, e);
		}
	}

	/**
	 * Executes the sql which should include an autoincrement element.
	 * 
	 * @return internalID internalID auto generated by database.
	 */
	public static long executeAutoInsert(Connection conn, String sql)
			throws AdaptorException {

		long internalID = 0;
		String sql2 = sql;

		try {

			int nRows = conn.createStatement().executeUpdate(sql2);
			if (nRows != 1)
				throw new AdaptorException("Failed to insert to database: "
						+ sql2);

			sql2 = "select last_insert_id()";
			ResultSet rs = conn.createStatement().executeQuery(sql2);
			rs.next();
			internalID = rs.getLong(1);
			if (internalID <= 0)
				throw new AdaptorException(
						"Auto increment generated an unacceptable internalID: "
								+ internalID + " : " + sql2);
		} catch (SQLException e) {
			throw new AdaptorException("Failed to execute sql:" + sql2, e);
		}

		return internalID;
	}

	/**
	 * Executes the PreparedStatement which should include an autoincrement
	 * element.
	 * 
	 * @return internalID auto generated by database (last autoincremented
	 *         value).
	 */
	public static long executeAutoInsert(PreparedStatement ps, String sql)
			throws AdaptorException {

		long internalID = 0;
		String sql2 = sql;

		try {

			int nRows = ps.executeUpdate();
			if (nRows != 1)
				throw new AdaptorException("Failed to insert to database: "
						+ sql2);

			sql2 = "select last_insert_id()";
			ResultSet rs = ps.getConnection().createStatement().executeQuery(
					sql2);
			rs.next();
			internalID = rs.getLong(1);
			if (internalID <= 0)
				throw new AdaptorException(
						"Auto increment generated an unacceptable internalID: "
								+ internalID + " : " + sql2);
		} catch (SQLException e) {
			throw new AdaptorException("Failed to execute sql:" + sql2, e);
		}

		return internalID;
	}

	/**
	 * @return location as a clonefragmentLocation, if location is an
	 *         AssemblyLocation it is converted to a CloneFragmentLocation
	 *         without gaps.
	 */
	protected CloneFragmentLocation getAsCloneFragmentLocation(Location loc)
			throws AdaptorException {

		Warnings
				.deprecated("CloneFrangmentLocations no longer supported - returning null");

		return null;
	}

	/**
	 * Convenience clears buffer.
	 */
	final void clear(StringBuffer buf) {
		buf.delete(0, Integer.MAX_VALUE);
	}

	/**
	 * Add item to cache keyed on it's internalID and alternative key.
	 * 
	 * @param persistent
	 *            object to be stored in cache
	 */
	protected void addToCache(Persistent persistent) {
		if (cache != null && persistent != null)
			cache.put(persistent, new Long(persistent.getInternalID()));
	}

	/**
	 * Add item to cache keyed on it's internalID and alternative key.
	 * 
	 * @param persistent
	 *            object to be stored in cache
	 * @param alternativeKey
	 *            optional second key to persistent object, ignored if null.
	 */
	protected void addToCache(Persistent persistent, Object alternativeKey) {
		if (cache != null && persistent != null)
			if (alternativeKey == null)
				cache.put(persistent, new Long(persistent.getInternalID()));
			else
				cache.put(persistent, new Long(persistent.getInternalID()),
						alternativeKey);
	}

	/**
	 * @return object from cache with specified internalID if present, otherwise
	 *         null
	 */
	protected Persistent fetchFromCache(long internalID) {

		if (cache == null || internalID < 1)
			return null;
		else
			return (Persistent) cache.get(new Long(internalID));
	}

	/**
	 * Retrives cached item by alternative key.
	 * 
	 * @return object from cache with specified key if present, otherwise null
	 */
	protected Persistent fetchFromCache(Object alternativeKey) {

		if (cache == null || alternativeKey == null)
			return null;
		else
			return (Persistent) cache.get(alternativeKey);

	}

	protected Persistent deleteFromCache(long internalID) {
		if (cache == null || internalID < 1)
			return null;
		else {
			return (Persistent) cache.removeValueByKey(new Long(internalID));
		}
	}

	/**
	 * Returns a connection to either the generic (driver) database or a
	 * database specified for this adaptor. The specific database is only used
	 * if one is specified in the driver configuration.
	 * 
	 * @return connection that is relevant to this adaptor.
	 */
	public Connection getConnection() throws AdaptorException {

		Connection conn = null;
		try {
		  conn = getDataSource().getConnection();
		} catch (SQLException e) {
			throw new AdaptorException("", e);
		}

		return conn;
	}

	/**
	 * Returns the datasource for this adaptor.
	 * 
	 * @return datasource that is relevant to this adaptor.
	 * @throws AdaptorException
	 */
	public DataSource getDataSource() throws AdaptorException {

		if (dataSource == null)
			dataSource = driver.getDatasource(getType());
 
		if (dataSource == null)
	    throw new AdaptorException("No datasource available for [type="+getType()+", driver=" + driver.getConfiguration() + "]");
		
		return dataSource;
	}

	/**
	 * Gets items matching _internalIDs_ from _cache_ and puts unmatched IDs in
	 * _outUncachedIDs_.
	 * 
	 * @param internalIDs
	 *            internal IDs of items to retrieve from cache.
	 * @param outUncachedIDs
	 *            IDs that weren't in the cache.
	 * @param cache
	 *            cache of items.
	 * 
	 * @return 0 to internalIDs.lenght items from cache matching internalIDs.
	 */
	public static List fromCache(long[] internalIDs, LongList outUncachedIDs,
			LruCache cache) {

		if (cache==null || cache.listSize()==0) {
			outUncachedIDs.add(internalIDs);
			// don't return Collections.EMPTY_LIST because calling code might 
			// try to add to returned list.
			return new ArrayList(); 
		}
		
		List outList = new ArrayList(internalIDs.length);
		for (int i = 0; i < internalIDs.length; i++) {
			long id = internalIDs[i];
			Object o = cache.get(id);
			if (o != null)
				outList.add(o);
			else
				outUncachedIDs.add(id);
		}
		return outList;
	}

	/**
	 * Gets items matching _internalIDs_ from cache and puts unmatched IDs in
	 * _outUncachedIDs_.
	 * 
	 * @param internalIDs
	 *            internal IDs of items to retrieve from cache.
	 * @param outUncachedIDs
	 *            IDs that weren't in the cache.
	 * 
	 * @return 0 to internalIDs.lenght items from cache matching internalIDs.
	 */
	public List fromCache(long[] internalIDs, LongList outUncachedIDs) {
		return fromCache(internalIDs, outUncachedIDs, cache);
	}
  
  
  /** 
   * Convenience method that gets the schema version from the driver.
   * 
   * wraps try/catch block.
   */
  protected String getSchemaVersion() {
    try {
      return driver.fetchDatabaseSchemaVersion();
    } catch (AdaptorException e) {
      throw new RuntimeAdaptorException(e);
    }
  }
  
  protected int getSchemaVersionAsInt() {
    return Integer.parseInt(getSchemaVersion());
  }
  
  /**
   * Convenience method for choosing an SQL column specification based on the 
   * current schema and when the column name changed.
   *
   * @param columnDefault default column.
   * @param schemaVersionA schema when column changed.
   * @param columnA column since schemaVersionA.
   * 
   * @return columnDefault if 0 < version < schemaVersionA,
   * columnA if schemaVersionA <= version.
   */
  protected String schemaSpecificColumn(String columnDefault, int schemaVersionA, String columnA) {
    if (getSchemaVersionAsInt()<schemaVersionA)
      return columnDefault;
    else
      return columnA;
  }
  
  /**
   * Convenience method for choosing an SQL column specification based on the 
   * current schema and when the column name changed.
   * 
   * Expect schemaVersionA<schemaVersionB.
   * 
   * @param columnDefault default column.
   * @param schemaVersionA schema when column changed first time.
   * @param columnA new column name since schemaVersionA.
   * @param schemaVersionB schema when column changed second time.
   * @param columnB column since schemaVersionB.
   * @return columnDefault if 0 < version < schemaVersionA,
   * columnA if schemaVersionA <= version < schemaVersionB,
   * columnB if schemaVersionB <= version. 
   */
  protected String schemaSpecificColumn(String columnDefault, int schemaVersionA, String columnA, int schemaVersionB,  String columnB) {
    
    if (schemaVersionA>=schemaVersionB)
      throw new RuntimeException("Expected schemaVersionA < schemaVersionB");
    
    int version = getSchemaVersionAsInt();
    if (version<schemaVersionA)
      return columnDefault;
    else if (version<schemaVersionB)
      return columnA;
    else
      return columnB;
  }
  
}
