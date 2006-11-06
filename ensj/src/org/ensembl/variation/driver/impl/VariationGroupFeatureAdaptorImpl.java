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

import org.ensembl.datamodel.InvalidLocationException;
import org.ensembl.datamodel.Location;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.impl.BaseFeatureAdaptorImpl;
import org.ensembl.driver.impl.CoreDriverImpl;
import org.ensembl.variation.datamodel.VariationGroupFeature;
import org.ensembl.variation.datamodel.impl.VariationGroupFeatureImpl;
import org.ensembl.variation.driver.VariationDriver;
import org.ensembl.variation.driver.VariationGroupFeatureAdaptor;

/**
 * This adaptor provides database connectivity for VariationGroupFeature
 * objects. Genomic locations of VariationGroups can be obtained from the
 * variation database using this adaptor.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 * @see org.ensembl.variation.datamodel.VariationGroupFeature
 */
public class VariationGroupFeatureAdaptorImpl extends BaseFeatureAdaptorImpl
		implements VariationGroupFeatureAdaptor {

	// TODO use vdriver.getCoreDriver() to get on demand because it can't be
	// passed
	// in via the generic DriverManager.loadVariationAdaptor() mechanism.

	private VariationDriver vdriver;

	public VariationGroupFeatureAdaptorImpl(VariationDriver vdriver) {
		// TODO consider changing baseadaptor to handle CoreDriver instead of
		// CoreDriverImpl
		super((CoreDriverImpl) vdriver.getCoreDriver(), TYPE);
		this.vdriver = vdriver;
	}

	/**
	 * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#tables()
	 */
	protected String[][] tables() {
		String[][] tables = { { "variation_group_feature", "vgf" } };

		return tables;

	}

	/**
	 * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#columns()
	 */
	protected String[] columns() {
		String[] cols = { "vgf.variation_group_feature_id",
				"vgf.seq_region_id", "vgf.seq_region_start",
				"vgf.seq_region_end", "vgf.seq_region_strand",
				"vgf.variation_group_id", "vgf.variation_group_name" };
		return cols;
	}

	/**
	 * Creates a VariationFeature using the next row from the result set.
	 * 
	 * @return a VariationFeature if there is another row, otherwise null.
	 * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#createObject(java.sql.ResultSet)
	 */
	public Object createObject(ResultSet rs) throws AdaptorException {
		
		VariationGroupFeature vgf = null;
		
		try {
			if (rs.next()) {
				Location loc = vdriver.getCoreDriver().getLocationConverter()
						.idToLocation(rs.getLong("seq_region_id"),
								rs.getInt("seq_region_start"),
								rs.getInt("seq_region_end"),
								rs.getInt("seq_region_strand"));

				vgf = new VariationGroupFeatureImpl(vdriver,
						loc, rs.getLong("variation_group_id"), rs
								.getString("vgf.variation_group_name"));
				vgf.setInternalID(rs.getLong("variation_group_feature_id"));

			}

		} catch (InvalidLocationException ee) {
			throw new AdaptorException("Error when building Location", ee);
		} catch (SQLException se) {
			throw new AdaptorException("SQL error when building object", se);
		}

		return vgf;

	}

	/**
	 * @see org.ensembl.driver.impl.BaseAdaptor#getConnection()
	 */
	public Connection getConnection() throws AdaptorException {
		return vdriver.getConnection();
	}

	/**
	 * @see org.ensembl.variation.driver.VariationGroupFeatureAdaptor#fetch(long)
	 */
	public VariationGroupFeature fetch(long internalID) throws AdaptorException {
		return (VariationGroupFeature) fetchByInternalID(internalID);
	}

}
