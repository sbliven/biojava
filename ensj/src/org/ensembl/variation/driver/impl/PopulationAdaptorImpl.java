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
import org.ensembl.variation.datamodel.Population;
import org.ensembl.variation.datamodel.impl.PopulationImpl;
import org.ensembl.variation.driver.PopulationAdaptor;
import org.ensembl.variation.driver.VariationDriver;

/**
 * Implementation of Population adaptor.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 *  
 */
public class PopulationAdaptorImpl implements PopulationAdaptor {

	private VariationDriver vdriver;

	/**
	 * LRU cache is used to store populations and thereby reduce database
	 * retrievals.
	 */
	private LruCache cache = new LruCache(1000);

	/**
	 * Creates a population instance associated with the specified driver.
	 * 
	 * @param vdriver
	 *            variation driver this population came from.
	 */
	public PopulationAdaptorImpl(VariationDriver vdriver) {
		this.vdriver = vdriver;
	}

	/**
	 * @see org.ensembl.variation.driver.PopulationAdaptor#fetch(long)
	 */
	public Population fetch(long internalID) throws AdaptorException {

		Population p = (Population) cache.get(internalID);
		if (p == null)
			p = fetchByConstraint("p.sample_id = " + internalID);
		return p;
	}

	/**
	 * @return PopulationAdaptor.TYPE
	 * @see org.ensembl.driver.Adaptor#getType()
	 * @see org.ensembl.variation.driver.PopulationAdaptor#TYPE
	 */
	public String getType() throws AdaptorException {
		return TYPE;
	}

	/**
	 * Does nothing because we use the connections from the driver.
	 * 
	 * @see org.ensembl.driver.Adaptor#closeAllConnections()
	 */
	public void closeAllConnections() throws AdaptorException {
	}

	/**
	 * @see org.ensembl.driver.Adaptor#clearCache()
	 */
	public void clearCache() throws AdaptorException {
		cache.clear();
	}

	/**
	 * @see org.ensembl.variation.driver.PopulationAdaptor#fetch(java.lang.String)
	 */
	public Population fetch(String name) throws AdaptorException {

		Population p = (Population) cache.get(name);
		if (p == null)
			p = fetchByConstraint("name = '" + name + "'");
		return p;
	}

	/**
	 * @see org.ensembl.variation.driver.PopulationAdaptor#fetchSuperPopulations(org.ensembl.variation.datamodel.Population)
	 */
	public List fetchSuperPopulations(Population subPopulation)
			throws AdaptorException {

		String sql = "SELECT p.sample_id, s.name, s.size,  s.description"
				+ " FROM   population p, population_structure ps, sample s"
				+ " WHERE  p.sample_id = ps.super_population_sample_id"
				+ " AND s.sample_id = p.sample_id "
				+ " AND ps.sub_population_sample_id = "
				+ subPopulation.getInternalID();

		return fetchListByQuery(sql);
	}

	/**
	 * @see org.ensembl.variation.driver.PopulationAdaptor#fetchSubPopulations(org.ensembl.variation.datamodel.Population)
	 */
	public List fetchSubPopulations(Population superPopulation)
			throws AdaptorException {

		String sql = "SELECT p.sample_id, s.name, s.size,  s.description"
				+ " FROM   population p, population_structure ps, sample s"
				+ " WHERE  p.sample_id = ps.super_population_sample_id"
				+ " AND s.sample_id = p.sample_id "
				+ " AND ps.super_population_sample_id = "
				+ superPopulation.getInternalID();

		return fetchListByQuery(sql);
	}

	private List fetchListByConstraint(String constraint)
			throws AdaptorException {
		String sql = "SELECT p.sample_id, s.name, s.size, s.description"
				+ " FROM   population p, sample s" 
				+ " WHERE s.sample_id = p.sample_id AND " 
				+ constraint;
		return fetchListByQuery(sql);
	}

	private List fetchListByQuery(String sql) throws AdaptorException {
		List r = new ArrayList();

		Connection conn = null;
		try {
			conn = vdriver.getConnection();
			ResultSet rs = conn.createStatement().executeQuery(sql);
			if (rs.next()) {
				Population p = null;
				while ((p = createObject(rs)) != null)
					r.add(p);
			}
		} catch (SQLException e) {
			throw new AdaptorException(
					"Failed to fetch populations with query: " + sql, e);
		} finally {
			CoreDriverImpl.close(conn);
		}

		return r;
	}

	private Population fetchByConstraint(String constraint)
			throws AdaptorException {

		List r = fetchListByConstraint(constraint);
		return (Population) (r.size() > 0 ? r.get(0) : null);

	}

	/**
	 * Creates a population object from the current and next N rows.
	 * 
	 * @param rs
	 *            resultset with next() called at least once.
	 * @return a population or null if after last row in rs.
	 */

	private Population createObject(ResultSet rs) throws AdaptorException,
			SQLException {

		if (rs.isAfterLast())
			return null;

		final long internalID = rs.getLong("sample_id");
		Population p = new PopulationImpl(vdriver);

		do {
			p.setInternalID(internalID);
			p.setName(rs.getString("name"));
			p.setDescription(rs.getString("description"));
			p.setSize(rs.getInt("size"));
		} while (rs.next() && rs.getLong("sample_id") != internalID);

		cache.put(p, new Long(p.getInternalID()), p.getName());

		return p;
	}

	/**
	 * @see org.ensembl.variation.driver.PopulationAdaptor#fetch(long[])
	 */
	public List fetch(long[] internalIDs) throws AdaptorException {

		LongList uncachedIDs = new LongList();
		List r = BaseAdaptor.fromCache(internalIDs, uncachedIDs, cache);

		if (uncachedIDs.size() > 0)
			r.addAll(fetchListByConstraint("p.sample_id IN ("
					+ uncachedIDs.toCommaSeparatedString() + ")"));
		return r;
	}
	
	
	
}
