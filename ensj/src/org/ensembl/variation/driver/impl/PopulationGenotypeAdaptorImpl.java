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

package org.ensembl.variation.driver.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ensembl.driver.AdaptorException;
import org.ensembl.variation.datamodel.Population;
import org.ensembl.variation.datamodel.PopulationGenotype;
import org.ensembl.variation.datamodel.impl.PopulationGenotypeImpl;
import org.ensembl.variation.driver.PopulationGenotypeAdaptor;

/**
 * Implementation of PopulationGenotypeAdaptor.
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public class PopulationGenotypeAdaptorImpl extends BasePersistentAdaptor
		implements PopulationGenotypeAdaptor {

	
	private final static String BASE_QUERY = "SELECT population_genotype_id, variation_id, allele_1, allele_2, frequency, sample_id FROM   population_genotype";
	
	/**
	 * Create a PopulationGenotypeAdaptorImpl associated with the specified 
	 * variation driver.
	 * @param vdriver parent variation driver. 
	 */
	public PopulationGenotypeAdaptorImpl(VariationDriverImpl vdriver) {
		super(vdriver,1000);
	}

	/**
	 * @see org.ensembl.variation.driver.impl.BasePersistentAdaptor#createObject(java.sql.ResultSet)
	 */
	protected Object createObject(ResultSet rs) throws SQLException,
			AdaptorException {
	
		if (rs.isAfterLast())
			return null;
		
		PopulationGenotype pg = new PopulationGenotypeImpl(vdriver, 
        rs.getString("allele_1"), 
        rs.getString("allele_2"),
        rs.getLong("variation_id"),
        rs.getLong("sample_id"),
				rs.getDouble("frequency"));
		pg.setInternalID(rs.getLong("population_genotype_id"));
		
		cache.put(pg, pg.getInternalID());
		
		rs.next();
		
		return pg;
	}

	/**
	 * @see org.ensembl.variation.driver.PopulationGenotypeAdaptor#fetch(long)
	 */
	public PopulationGenotype fetch(long internalID) throws AdaptorException {
		Object o = cache.get(internalID);
		if (o!=null)
			return (PopulationGenotype) o;
		
		String sql = BASE_QUERY + " WHERE  population_genotype_id = " + internalID;
		return (PopulationGenotype) fetchByQuery(sql);
	}

	/**
	 * @see org.ensembl.variation.driver.PopulationGenotypeAdaptor#fetch(org.ensembl.variation.datamodel.Population)
	 */
	public List fetch(Population population) throws AdaptorException {
		String sql = BASE_QUERY + " WHERE  sample_id = " + population.getInternalID();
		return fetchListByQuery(sql);
	}

	/**
	 * @see org.ensembl.driver.Adaptor#getType()
	 */
	public String getType() throws AdaptorException {
		return TYPE;
	}

}
