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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.ensembl.datamodel.Analysis;
import org.ensembl.datamodel.ProteinFeature;
import org.ensembl.datamodel.Translation;
import org.ensembl.datamodel.impl.ProteinFeatureImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.AnalysisAdaptor;
import org.ensembl.driver.ProteinFeatureAdaptor;
import org.ensembl.driver.TranslationAdaptor;

/**
 * Peptide feature.
 */
public class ProteinFeatureAdaptorImpl extends BaseAdaptor implements
		ProteinFeatureAdaptor {

	private static final Logger logger = Logger
			.getLogger(ProteinFeatureAdaptorImpl.class.getName());

	private final String BASE_SQL = "SELECT " + " f.protein_feature_id " // 1
			+ " ,f.translation_id " // 2
			+ " ,f.analysis_id " // 3
			+ " ,f.seq_start " // 4
			+ " ,f.seq_end " // 5
			+ " ,f.hit_start " // 6
			+ " ,f.hit_end " // 7
			+ " ,f.hit_id " // 8
			+ " ,f.score " // 9
			+ " ,f.evalue " // 10
			+ " ,f.perc_ident " // 11
			+ ", x.description " // 12
			+ ", i.interpro_ac " // 13
			+ ", x.display_label " // 14
			+ " FROM " + " protein_feature f "
			+ " LEFT JOIN interpro AS i ON f.hit_id = i.id "
			+ " LEFT JOIN xref AS x ON x.dbprimary_acc = i.interpro_ac ";

	public final boolean CLIP = false;

	private TranslationAdaptor translationAdaptor = null;

	private String analysisIDCondition = null;

	public ProteinFeatureAdaptorImpl(CoreDriverImpl driver) {
		super(driver);
	}

	public String getType() {
		return TYPE;
	}

	/**
	 * @return List of SimplePeptideFeature corresponding to the the specified
	 *         translation.
	 */
	public List fetch(Translation translation) throws AdaptorException {

		List result = new ArrayList();

		Connection conn = null;
		try {

			conn = getConnection();
			String sql = BASE_SQL + " WHERE f.translation_id="
					+ translation.getInternalID();
			ResultSet rs = executeQuery(conn, sql);
			while (rs.next()) {

				ProteinFeature pf = createProteinFeature(rs);
				pf.setTranslation(translation);
				result.add(pf);

			}
		} catch (Exception e) {
			throw new AdaptorException(e);
		} finally {
			close(conn);
		}

		return result;
	}

	public ProteinFeature fetch(long internalID) throws AdaptorException {

		ProteinFeature pf = null;
		Connection conn = null;
		try {
			conn = getConnection();
			String sql = BASE_SQL + " WHERE f.protein_feature_id=" + internalID
					+ " ";
			ResultSet rs = executeQuery(conn, sql);
			if (rs.next())
				pf = createProteinFeature(rs);

		} catch (SQLException e) {
			throw new AdaptorException(e);
		} finally {
			close(conn);
		}

		// Note: this will be potentially slow unless translation adaptor
		// caches
		if (pf != null)
			pf.setTranslation(driver.getTranslationAdaptor().fetch(
					pf.getTranslationInternalID()));

		return pf;

	}

	/**
	 * Creates a ProteinFeature from the current _rs_ row.
	 * @param rs result set with cursor set to a row that contains
	 * "select" values defined in BASE_SQL.
	 * @return new ProteinFeature built from current _rs_ row.
	 * @throws AdaptorException
	 * @throws SQLException
	 */
	private ProteinFeature createProteinFeature(ResultSet rs)
			throws AdaptorException, SQLException {

		ProteinFeature pf = new ProteinFeatureImpl(driver);

		AnalysisAdaptor analysisAdaptor = driver.getAnalysisAdaptor();
		Analysis a = analysisAdaptor.fetch(rs.getLong(3));
		pf.setAnalysis(a);
		pf.setDisplayName(rs.getString(8)); // hit_id
		pf.setInternalID(rs.getLong(1));
		pf.setPeptideStart(rs.getInt(4));
		pf.setPeptideEnd(rs.getInt(5));

		final long translationID = rs.getLong(2);
		pf.setTranslationInternalID(translationID);

		pf.setScore(rs.getFloat(9));
		pf.setEvalue(rs.getDouble(10));
		pf.setPercentageIdentity(rs.getInt(11));

		pf.setInterproDescription(rs.getString(12));
		pf.setInterproAccession(rs.getString(13));
		pf.setInterproDisplayName(rs.getString(14));
		
		pf.setDriver(driver);

		return pf;
	}

	/**
	 * @return internalID assigned to feature in database.
	 */
	public long store(ProteinFeature feature) throws AdaptorException {
		// We ignore hit_start and hit_end
		String sql = "INSERT INTO protein_feature (" + " translation_id " // 1
				+ ",  seq_start " // 2
				+ ",  seq_end " // 3
				+ ",  analysis_id " // 4
				+ ",  hit_id " // 5
				+ ",  score " // 6
				+ ",  evalue " // 7
				+ ",  perc_ident " // 8
				+ " ) VALUES (?, ?, ?, ?, ?, ?, ?, ? ) ";

		long internalID = 0;
		Connection conn = null;
		try {

			conn = getConnection();
			conn.setAutoCommit(false);

			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setLong(1, feature.getTranslationInternalID());
			ps.setInt(2, feature.getPeptideStart());
			ps.setInt(3, feature.getPeptideEnd());
			ps.setLong(4, feature.getAnalysis().getInternalID());
			ps.setString(5, feature.getDisplayName());
			ps.setDouble(6, feature.getScore());
			ps.setDouble(7, feature.getEvalue());
			ps.setDouble(8, feature.getPercentageIdentity());

			internalID = executeAutoInsert(ps, sql);

			conn.commit();
			feature.setDriver(driver);
			feature.setInternalID(internalID);
		} catch (Exception e) {
			rollback(conn);
			throw new AdaptorException("Failed to store SimplePeptideFeature: "
					+ feature, e);
		} finally {
			close(conn);
		}

		addToCache(feature);

		return internalID;
	}

	/**
	 * @param internalID
	 *            internalID of feature to be deleted from database.
	 */
	public void delete(long internalID) throws AdaptorException {

		if (internalID < 1)
			return;

		deleteFromCache(internalID);

		Connection conn = null;
		try {

			conn = getConnection();
			conn.setAutoCommit(false);

			delete(conn, internalID);

			conn.commit();
		} catch (Exception e) {
			rollback(conn);
			throw new AdaptorException(
					"Failed to delete SimplePeptideFeature: " + internalID, e);
		} finally {
			close(conn);
		}

	}

	/**
	 * @param feature
	 *            feature to delete.
	 */
	public void delete(ProteinFeature feature) throws AdaptorException {
		delete(feature.getInternalID());
		feature.setInternalID(0);
	}

	/**
	 * Executes sql to delete row from protein_feature table.
	 */
	void delete(Connection conn, long internalID) throws AdaptorException {

		executeUpdate(conn,
				"delete from protein_feature where protein_feature_id = "
						+ internalID);

	}
}
