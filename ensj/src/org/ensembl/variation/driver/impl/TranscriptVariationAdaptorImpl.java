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

import org.ensembl.datamodel.Transcript;
import org.ensembl.driver.AdaptorException;
import org.ensembl.variation.datamodel.TranscriptVariation;
import org.ensembl.variation.datamodel.VariationFeature;
import org.ensembl.variation.datamodel.impl.TranscriptVariationImpl;
import org.ensembl.variation.driver.TranscriptVariationAdaptor;
import org.ensembl.variation.driver.VariationDriver;

/**
 * Provides access to TranscritpVariations stored in ensembl databases.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 */
public class TranscriptVariationAdaptorImpl extends BasePersistentAdaptor
		implements TranscriptVariationAdaptor {

	private final static String BASE_QUERY = "SELECT tv.transcript_variation_id, tv.transcript_id,"
			+ " tv.variation_feature_id, tv.cdna_start, tv.cdna_end, "
			+ " tv.translation_start, tv.translation_end, "
			+ " tv.peptide_allele_string, tv.consequence_type "
			+ " FROM   transcript_variation tv ";

	public TranscriptVariationAdaptorImpl(VariationDriver vdriver) {
		super(vdriver, 1000);
	}

	/**
	 * @see org.ensembl.variation.driver.impl.BasePersistentAdaptor#createObject(java.sql.ResultSet)
	 */
	protected Object createObject(ResultSet rs) throws SQLException,
			AdaptorException {

		if (rs.isAfterLast())
			return null;

		TranscriptVariation tv = new TranscriptVariationImpl(vdriver, rs
				.getLong("transcript_id"),
				rs.getInt("translation_start"), rs
						.getInt("translation_end"), rs
						.getLong("variation_feature_id"), rs
						.getInt("cdna_start"), rs.getInt("tv.cdna_end"), rs
						.getString("peptide_allele_string"), rs.getString("consequence_type"));
		tv.setInternalID(rs.getLong("transcript_variation_id"));

		cache.put(tv, tv.getInternalID());

		rs.next();

		return tv;
	}

	/**
	 * @see org.ensembl.variation.driver.TranscriptVariationAdaptor#fetch(long)
	 */
	public TranscriptVariation fetch(long internalID) throws AdaptorException {
		String sql = BASE_QUERY + " WHERE tv.transcript_variation_id = " + internalID;
		return (TranscriptVariation) fetchByQuery(sql);
	}

	/**
	 * @see org.ensembl.variation.driver.TranscriptVariationAdaptor#fetch(org.ensembl.variation.datamodel.VariationFeature)
	 */
	public List fetch(VariationFeature variationFeature)
			throws AdaptorException {
		String sql = BASE_QUERY + " WHERE tv.variation_feature_id = "
				+ variationFeature.getInternalID();
		return fetchListByQuery(sql);
	}

	/**
	 * @see org.ensembl.driver.Adaptor#getType()
	 */
	public String getType() throws AdaptorException {
		return TYPE;
	}

	/**
	 * @see org.ensembl.variation.driver.TranscriptVariationAdaptor#fetch(org.ensembl.datamodel.Transcript)
	 */
	public List fetch(Transcript transcript) throws AdaptorException {
		String sql = BASE_QUERY + " WHERE tv.transcript_id = "
				+ transcript.getInternalID();
		return fetchListByQuery(sql);
	}

}
