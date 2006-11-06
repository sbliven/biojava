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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ensembl.datamodel.OligoFeature;
import org.ensembl.datamodel.OligoProbe;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.impl.OligoFeatureImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.OligoFeatureAdaptor;

/**
 * The point of this class is....
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 */
public class OligoFeatureAdaptorImpl extends BaseFeatureAdaptorImpl implements
		OligoFeatureAdaptor {

	/**
	 * Creates an OligoFeatureAdaptor associated with the specified driver.
	 * @param driver parent driver.
	 */
	public OligoFeatureAdaptorImpl(CoreDriverImpl driver) {
		super(driver, TYPE);
	}

	/**
	 * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#tables()
	 */
	protected String[][] tables() {
		final String[][] tables = { { "oligo_feature", "af" }};
		return tables;
	}

	/**
	 * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#columns()
	 */
	protected String[] columns() {
		final String[] columns = { "af.oligo_feature_id", "af.seq_region_id",
				"af.seq_region_start", "af.seq_region_end",
				"af.seq_region_strand", "af.mismatches", "af.oligo_probe_id",
				"af.analysis_id" };
		return columns;
	}


	/**
	 * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#createObject(java.sql.ResultSet)
	 */
	public Object createObject(ResultSet rs) throws AdaptorException {
		try {
			if (!rs.next())
				return null;

			Location loc = driver.getLocationConverter()
					.idToLocation(rs.getLong("seq_region_id"), rs
							.getInt("seq_region_start"), rs
							.getInt("seq_region_end"), rs
							.getInt("seq_region_strand"));

			return new OligoFeatureImpl(driver, rs.getLong(1), loc, null
          , rs.getLong(7), rs.getInt(6));

		} catch (SQLException e) {
			throw new AdaptorException("Failed to create oligoFeature.", e);
		}
	}

	/**
	 * @throws AdaptorException
	 * @see org.ensembl.driver.OligoFeatureAdaptor#fetch(org.ensembl.datamodel.oligoProbe)
	 */
	public List fetch(OligoProbe probe) throws AdaptorException {
		return fetchByNonLocationConstraint("af.oligo_probe_id = "
				+ probe.getInternalID());
	}



	/**
	 * @see org.ensembl.driver.OligoFeatureAdaptor#fetch(long)
	 */
	public OligoFeature fetch(long internalID) throws AdaptorException {
    // disable failIfMoreThanOneRowReturned because some probes appear in multiple arrays
    // and we join to the oligo_probe table. In practice this means that we only use
    // the first row returned to construct the OligoFeature.
		return (OligoFeature) fetchByInternalID(internalID, false);
	}


}
