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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ensembl.driver.AdaptorException;
import org.ensembl.util.StringUtil;
import org.ensembl.variation.datamodel.Individual;
import org.ensembl.variation.datamodel.Population;
import org.ensembl.variation.datamodel.impl.IndividualImpl;
import org.ensembl.variation.driver.IndividualAdaptor;
import org.ensembl.variation.driver.VariationDriver;

/**
 * Implementation of IndividualAdaptor that fetches Individuals from an ensembl
 * database.
 * 
 * Uses a cache to improve performance.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 *  
 */
public class IndividualAdaptorImpl extends BasePersistentAdaptor implements
		IndividualAdaptor {

	private final static String BASE_QUERY = "SELECT i.sample_id, s.name, s.description, i.gender, i.father_individual_sample_id, i.mother_individual_sample_id FROM   individual i, sample s";

	/**
	 *  
	 */
	public IndividualAdaptorImpl(VariationDriver vdriver) {
		super(vdriver, 1000);
	}

	/**
	 * @see org.ensembl.variation.driver.IndividualAdaptor#fetch(long)
	 */
	public Individual fetch(long internalID) throws AdaptorException {

		Object o = cache.get(internalID);
		if (o != null)
			return (Individual) o;

		String sql = BASE_QUERY + " WHERE  i.sample_id = " + internalID + " AND i.sample_id=s.sample_id ";
		return (Individual) fetchByQuery(sql);
	}

	/**
	 * @see org.ensembl.variation.driver.IndividualAdaptor#fetch(java.lang.String)
	 */
	public List fetch(String name) throws AdaptorException {
		// can't use cache because name is potentially 1 to many
		// and even if cache has >=1 etry matching name there might
		// be others in the db that are not in the cache.
		String sql = BASE_QUERY + " WHERE  s.name = '" + name + "'" + " AND i.sample_id=s.sample_id ";
		return fetchListByQuery(sql);
	}

	/**
	 * @see org.ensembl.variation.driver.IndividualAdaptor#fetch(org.ensembl.variation.datamodel.Population)
	 */
	public List fetch(Population population) throws AdaptorException {
		long[] ids = fetchInternalIDsBySQL("SELECT individual_sample_id "
				+ "FROM individual_population " + "WHERE population_sample_id="
				+ population.getInternalID());
		return fetch(ids);
	}

	/**
	 * @see org.ensembl.variation.driver.IndividualAdaptor#TYPE
	 * @see org.ensembl.driver.Adaptor#getType()
	 */
	public String getType() throws AdaptorException {
		return TYPE;
	}

	protected Object createObject(ResultSet rs) throws SQLException,
			AdaptorException {
		if (rs.isAfterLast())
			return null;

		final long internalID = rs.getLong("i.sample_id");
		Individual individual = new IndividualImpl(vdriver, rs
				.getString("name"), rs.getString("description"), rs
				.getString("gender"), rs.getLong("father_individual_sample_id"),
				rs.getLong("mother_individual_sample_id"));
		individual.setInternalID(internalID);

		cache.put(individual, individual.getInternalID());

		rs.next();

		return individual;

	}

	/**
	 * Fetches children of this individual.
	 * 
	 * If gender is set then look for children with this individual as father or
	 * mother as appropriatte. If gender is unset then look for children where
	 * this individual is first considered as a mother. If there no children are
	 * found the repeat the query but time treating this individual as a father.
	 * 
	 * @see org.ensembl.variation.driver.IndividualAdaptor#fetch(org.ensembl.variation.datamodel.Individual)
	 */
	public List fetch(Individual parent) throws AdaptorException {

		if (parent.getGender() != null)
			return fetchByParent(parent, parent.getGender());

		List r = fetchByParent(parent, "Female");
		if (r.size() == 0)
			r = fetchByParent(parent, "Male");
		return r;

	}

	/**
	 * @see org.ensembl.variation.driver.IndividualAdaptor#fetchPopulations(org.ensembl.variation.datamodel.Individual)
	 */
	public List fetchPopulations(Individual individual) throws AdaptorException {

		long[] popIDs = fetchInternalIDsBySQL("SELECT population_sample_id FROM individual_population WHERE individual_sample_id="
				+ individual.getInternalID());
		return vdriver.getPopulationAdaptor().fetch(popIDs);
	}

	private List fetchByParent(Individual parent, String gender)
			throws AdaptorException {
		String sql = BASE_QUERY
				+ " WHERE "
				+ (gender.equals("Male") ? " i.father_individual_sample_id = "
						: " i.mother_individual_sample_id = ")
				+ parent.getInternalID()
				+ " AND i.sample_id=s.sample_id ";
		return fetchListByQuery(sql);
	}

	/**
	 * @see org.ensembl.variation.driver.IndividualAdaptor#fetch(long[])
	 */
	public List fetch(long[] internalIDs) throws AdaptorException {
		return fetchListByQuery(BASE_QUERY+" WHERE i.sample_id IN (" + StringUtil.toString(internalIDs)+ ") AND i.sample_id=s.sample_id ");
	}
}
