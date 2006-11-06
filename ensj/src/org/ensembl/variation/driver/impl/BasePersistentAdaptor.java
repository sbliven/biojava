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
package org.ensembl.variation.driver.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.impl.BaseAdaptor;
import org.ensembl.driver.impl.CoreDriverImpl;
import org.ensembl.util.LongList;
import org.ensembl.util.LruCache;
import org.ensembl.variation.driver.VariationDriver;

/**
 * Base class for non-location adaptors.
 */
public abstract class BasePersistentAdaptor {

	protected LruCache cache;

	public BasePersistentAdaptor(VariationDriver vdriver) {
		this.vdriver = vdriver;
	}

	public BasePersistentAdaptor(VariationDriver vdriver, int cacheSize) {
		this.vdriver = vdriver;
		cache = new LruCache(cacheSize);
	}

	protected List fetchListByQuery(String sql) throws AdaptorException {
		List r = new ArrayList();

		Connection conn = null;
		try {
			conn = vdriver.getConnection();
			ResultSet rs = conn.createStatement().executeQuery(sql);
			if (rs.next()) {
				Object o = null;
				while ((o = createObject(rs)) != null)
					r.add(o);
			}
		} catch (SQLException e) {
			throw new AdaptorException("Failed to fetch items with query: "
					+ sql, e);
		} finally {
			CoreDriverImpl.close(conn);
		}

		return r;

	}

	
	protected Object fetchByQuery(String sql) throws AdaptorException {
		List r = fetchListByQuery(sql);
		return r.size() > 0 ? r.get(0) : null;
	}

	
	/**
	 * Convenience method that returns an array of internal IDs created by
	 * executing the SQL.
	 * 
	 * @param sql
	 *            sql statement with internal Ids in the first column of the
	 *            select statement.
	 * @return iterator with zero or more elements.
	 * @throws AdaptorException
	 */
	protected long[] fetchInternalIDsBySQL(String sql) throws AdaptorException {

		LongList buf = new LongList();

		Connection conn = null;
		try {
			conn = vdriver.getConnection();
			ResultSet rs = BaseAdaptor.executeQuery(conn, sql);
			while (rs.next())
				buf.add(rs.getLong(1));

		} catch (SQLException e) {
			throw new AdaptorException("Failed to load internal ids from sql: "
					+ sql, e);
		}finally {
			CoreDriverImpl.close(conn);
		}

		return buf.toArray();
	}
	
	
	
	/**
	 * @see org.ensembl.driver.Adaptor#clearCache()
	 */
	public void clearCache() throws AdaptorException {
		if (cache != null)
			cache.clear();
	}

	/**
	 * Does nothing by default, derived classes that manage there own connections
	 * should override this method to close their connections.
	 * 
	 * @see org.ensembl.driver.Adaptor#closeAllConnections()
	 */
	public void closeAllConnections() throws AdaptorException {
	}

	protected VariationDriver vdriver;

	/**
	 * Returns an object of the correct type for the implementing adaptor or
	 * null if there are no more in the result set.
	 * 
	 * @param rs
	 * @return object of the correct type, or null.
	 * @throws SQLException
	 * @throws AdaptorException
	 */
	protected abstract Object createObject(ResultSet rs) throws SQLException,
			AdaptorException;

}
