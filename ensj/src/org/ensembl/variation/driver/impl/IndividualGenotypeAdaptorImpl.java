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
import org.ensembl.variation.datamodel.Individual;
import org.ensembl.variation.datamodel.IndividualGenotype;
import org.ensembl.variation.datamodel.impl.IndividualGenotypeImpl;
import org.ensembl.variation.driver.IndividualGenotypeAdaptor;

/**
 * Implementation of the IndividualGenotypeAdaptor that works
 * with ensembl databases.
 *
 * Uses a cache.
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public class IndividualGenotypeAdaptorImpl extends BasePersistentAdaptor
		implements IndividualGenotypeAdaptor {

	private final static String BASE_QUERY = "SELECT variation_id, allele_1, allele_2, sample_id FROM  TABLE_NAME"; 
	
	public IndividualGenotypeAdaptorImpl(VariationDriverImpl vdriver) {
		super(vdriver,1000);
	}

	/**
	 * @see org.ensembl.variation.driver.impl.BasePersistentAdaptor#createObject(java.sql.ResultSet)
	 */
	protected Object createObject(ResultSet rs) throws SQLException,
			AdaptorException {
		
		if (rs.isAfterLast()) return null;
		
		IndividualGenotype ig = new IndividualGenotypeImpl(vdriver, 
                                                       rs.getString("allele_1"), 
                                                       rs.getString("allele_2"),
                                                       rs.getLong("variation_id"),
                                                       rs.getLong("sample_id"));
		cache.put(ig, ig.getInternalID());

    rs.next();

		return ig;
	}

	/**
	 * @see org.ensembl.variation.driver.IndividualGenotypeAdaptor#fetch(org.ensembl.variation.datamodel.Individual)
	 */
	public List fetch(Individual individual) throws AdaptorException {
		
	  // Individual genotypes are stored in 2 tables to minimize storage space.
	  // We therefor need to execute 2 queries and combine the results.
	  
	  String sql = BASE_QUERY + " WHERE  sample_id = " + individual.getInternalID();
		
	  List singleTableResult = fetchListByQuery(sql.replaceAll("TABLE_NAME", "individual_genotype_single_bp"));
		List multipleTableResult = fetchListByQuery(sql.replaceAll("TABLE_NAME", "individual_genotype_multiple_bp"));
		
		if (singleTableResult.size()==0)
		  singleTableResult = multipleTableResult;
		else
		  singleTableResult.addAll(multipleTableResult);
		
		return singleTableResult;
	}

	/**
	 * @see org.ensembl.driver.Adaptor#getType()
	 */
	public String getType() throws AdaptorException {
		return TYPE;
	}

}
