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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ensembl.datamodel.InvalidLocationException;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.SequenceRegion;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.impl.BaseFeatureAdaptorImpl;
import org.ensembl.driver.impl.CoreDriverImpl;
import org.ensembl.variation.datamodel.LDFeature;
import org.ensembl.variation.datamodel.LDFeatureContainer;
import org.ensembl.variation.datamodel.VariationFeature;
import org.ensembl.variation.datamodel.impl.LDFeatureImpl;
import org.ensembl.variation.driver.LDFeatureAdaptor;
import org.ensembl.variation.driver.VariationDriver;

/**
 * Fetches LDFeatures from an ensembl database as either a simple
 * List or an LDFeatureContainer.
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public class LDFeatureAdaptorImpl extends BaseFeatureAdaptorImpl implements
		LDFeatureAdaptor {

	private VariationDriver vdriver;

	/**
	 * @param vdriver parent driver
	 */
	public LDFeatureAdaptorImpl(VariationDriver vdriver) {
		super((CoreDriverImpl)vdriver.getCoreDriver(),TYPE);
    this.vdriver = vdriver;
	}

  /**
   * @see org.ensembl.driver.impl.BaseAdaptor#getConnection()
   */
  public Connection getConnection() throws AdaptorException {
    return vdriver.getConnection();
  }

	/**
	 * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#tables()
	 */
	protected String[][] tables() {
    final String[][] tables = new String[][]{{"pairwise_ld", "pl"}};
      return tables;
	}

	/**
	 * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#columns()
	 */
	protected String[] columns() {
		final String[] columns = {"pl.variation_feature_id_1", "pl.variation_feature_id_2", "pl.sample_id",
                              "pl.seq_region_id", "pl.seq_region_start", "pl.seq_region_end", 
                              "pl.r2", "pl.d_prime", "pl.sample_count"};
		return columns;
	}

	/**
   * @return an LDFeature created from next row in rs if available, null if no more rows.
	 * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#createObject(java.sql.ResultSet)
	 */
	public Object createObject(ResultSet rs) throws AdaptorException {
    LDFeature vf = null;

    try {
      if (rs.next()) {
        Location loc =
          vdriver.getCoreDriver().getLocationConverter().idToLocation(
            rs.getLong("seq_region_id"),
            rs.getInt("seq_region_start"),
            rs.getInt("seq_region_end"),
            0); // unstranded information

        vf = new LDFeatureImpl(vdriver,
						loc,
						rs.getLong("variation_feature_id_1"),
						rs.getLong("variation_feature_id_2"),
						rs.getLong("sample_id"),
						rs.getDouble("r2"),
						rs.getDouble("d_prime"),
						rs.getInt("sample_count")
						);
        
      }

    } catch (InvalidLocationException ee) {
      throw new AdaptorException("Error when building Location", ee);
    } catch (SQLException se) {
      throw new AdaptorException("SQL error when building object", se);
    }

    return vf;
	}

	/**
	 * @see org.ensembl.variation.driver.LDFeatureAdaptor#fetch(org.ensembl.variation.datamodel.VariationFeature)
	 */
	public List fetch(VariationFeature variationFeature)
			throws AdaptorException {
		
		StringBuffer constraint = new StringBuffer();
		constraint.append("pl.variation_feature_id_1 = ").append(variationFeature.getInternalID());
		
		// Optimisation: in order to make the query faster we want to use the pairwise_ld table 
		// index which uses the seq region id and start values as keys.
		Location loc = variationFeature.getLocation(); 
		if (loc!=null) {
			
			long seqRegionID = -1;
			SequenceRegion sr = loc.getSequenceRegion();
			if (sr!=null && sr.getInternalID()>0)
				seqRegionID = sr.getInternalID();
			if (seqRegionID==-1)
				seqRegionID = driver.getLocationConverter().nameToId(loc.getSeqRegionName(), loc.getCoordinateSystem());
			if (seqRegionID>0)
				constraint.append(" AND seq_region_id = ").append(seqRegionID);
			
			long start = loc.getStart();
			if (start>0)
				constraint.append(" AND seq_region_start = ").append(start);
		}
		return fetchByNonLocationConstraint(constraint.toString());
	}

	/**
	 * @see org.ensembl.variation.driver.LDFeatureAdaptor#fetchLDFeatureContainer(org.ensembl.variation.datamodel.VariationFeature)
	 */
	public LDFeatureContainer fetchLDFeatureContainer(
			VariationFeature variationFeature) throws AdaptorException {
    List l = fetch(variationFeature);
		return new LDFeatureContainer(l);
	}

	/**
	 * @see org.ensembl.variation.driver.LDFeatureAdaptor#fetchLDFeatureContainer(org.ensembl.datamodel.Location)
	 */
	public LDFeatureContainer fetchLDFeatureContainer(Location location)
			throws AdaptorException {
    List l = fetch(location);
		return new LDFeatureContainer(l);
	}

}
