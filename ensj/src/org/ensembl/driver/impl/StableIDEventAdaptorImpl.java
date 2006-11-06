/*
  Copyright (C) 2002 EBI, GRL

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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ensembl.datamodel.ArchiveStableID;
import org.ensembl.datamodel.GeneSnapShot;
import org.ensembl.datamodel.MappingSession;
import org.ensembl.datamodel.StableIDEvent;
import org.ensembl.datamodel.TranscriptSnapShot;
import org.ensembl.datamodel.TranslationSnapShot;
import org.ensembl.datamodel.impl.ArchiveStableIDImpl;
import org.ensembl.datamodel.impl.GeneSnapShotImpl;
import org.ensembl.datamodel.impl.MappingSessionImpl;
import org.ensembl.datamodel.impl.StableIDEventImpl;
import org.ensembl.datamodel.impl.TranscriptSnapShotImpl;
import org.ensembl.datamodel.impl.TranslationSnapShotImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.StableIDEventAdaptor;
import org.ensembl.util.NotImplementedYetException;
import org.ensembl.util.Pair;

/**
 * Provides methods for fetching, storing and deleting entries from the database relating to 
 * events in the life of a stable id.
 * 
 * <pre>
 *      Types                     Table                 Description
 *      ------------------------------------------------------------------------------------------------
 *      StableIDEvents            stable_id_event       id created, deleted, thing it represents changed
 *      MappingSessions           mapping_session       session when ids were mapped
 *      GeneSnapShots             gene_archive          snapshot of a deleted or changed gene
 *      TranslationSnapShots      peptide_archive       snapshot of a deleted or changed peptide
 * </pre>
 * 
 * @see org.ensembl.datamodel.StableIDEvent
 * @see org.ensembl.datamodel.MappingSession
 * @see org.ensembl.datamodel.GeneSnapShot
 * @see org.ensembl.datamodel.TranslationSnapShot
 */
public class StableIDEventAdaptorImpl
	extends BaseAdaptor
	implements StableIDEventAdaptor {

	/** This variable is used during debugging; there a "trace" statements
	 * throughtout the code which show the state ov events, pairs and sets when
	 * this variable is encountered.*/
	private static final String TRACABLE_STABLE_ID = "ENSG00000142880";

	private static final Logger logger =
		Logger.getLogger(StableIDEventAdaptorImpl.class.getName());

	/** session instance to internalID in database. */
	private Map sessionToInternalIDCache = new HashMap();

	/** Cache of mapping sessions where index=internal ID in database. */
	private MappingSession[] mappingSessionsCache = null;

	public StableIDEventAdaptorImpl(CoreDriverImpl driver) {
		super(driver);
	}

	/**
	 * Stores pairs in database. Stores session in database if 
	 * session.internalID<1, a valid and unique
	 * session.internalID is automatically assigned .
	 */
	public void store(Pair[] pairs, MappingSession session)
		throws AdaptorException {

		long sessionInternalID = session.getInternalID();
		if (sessionInternalID<1) sessionInternalID = store(session);
		storePairs(pairs, sessionInternalID);
	}

	/**
	 * Stores stable id events, and there session, in database.
	 */
	public void store(StableIDEvent[] events) throws AdaptorException {

		// possible optimisation: convert directly to Pairs
		// and then store(Pairs[]).

		for (int i = 0; i < events.length; i++) {
			StableIDEvent event = events[i];
			store(event);

			if (logger.isLoggable(Level.FINE))
				logger.fine("Stored event: " + event);
			//if (i>10) break;
		}
	}

	/**
	 * If the session is new then it is added to the database. If a session
	 * with the same session.oldDatabase and session.newDatabase is already in
	 * database it's creation time is updated to the current time and it's internalID
	 * is set to match that in the database.
	 * @return internalID of session.
	 */
	public long store(MappingSession session) throws AdaptorException {

		long id = -1;
		Connection conn = null;

		try {

			id = currentSessionInternalID(session);

			conn = getConnection();

			long oldId = session.getInternalID();

			if (oldId > 0 && id != oldId)
				logger.warning(
					"Reassigning internal id for MappingSession : "
						+ briefString(session)
						+ ", "
						+ oldId
						+ " --> "
						+ id);

			if (id > 0) { // Update time stamp and synchronise internalId

				String sql =
					"UPDATE mapping_session SET created=NOW() WHERE mapping_session_id="
						+ id;
				logger.fine(sql);
				conn.createStatement().executeUpdate(sql);
				session.setInternalID(id);

			} else { // Add new session!

				String sql =
					"insert into mapping_session (old_db_name, new_db_name, created) values ("
						+ "'"
						+ session.getOldDatabase()
						+ "'"
						+ ",'"
						+ session.getNewDatabase()
						+ "'"
						+ ", NOW() "
						+ ")";
				logger.fine(sql);
				conn.createStatement().executeUpdate(sql);

				// Retrieve autogenerated id
				sql = "SELECT LAST_INSERT_ID() FROM mapping_session";
				logger.fine(sql);
				ResultSet rs = conn.createStatement().executeQuery(sql);
				if (!rs.next())
					throw new AdaptorException("Failed to retrieve internal id for mapping session.");

				id = rs.getLong(1);
				session.setInternalID(id);

			}

		} catch (Exception e) {
			throw new AdaptorException("Rethrow + stacktrace", e);
		} finally {
			close(conn);
		}

		return id;
	}

	/**
	 * @see org.ensembl.driver.StableIDEventAdaptor#fetchGeneSnapShot(java.lang.String, int)
	 */

	public GeneSnapShot fetchGeneSnapShot(String stableID, int version)
		throws AdaptorException {
		GeneSnapShot gene = null;

		Connection conn = null;
		String sql = null;
		try {
			conn = getConnection();

				sql = "SELECT " + "gene_stable_id" //1
		+", gene_version " // 2
		+", transcript_stable_id  " // 3
		+", transcript_version " // 4
		+", ga.translation_stable_id " // 5
		+", ga.translation_version " // 6
		+", old_db_name " // 7
		+", peptide_seq " // 8
	+" FROM gene_archive ga LEFT JOIN peptide_archive pa "
		+ "      ON ga.peptide_archive_id=pa.peptide_archive_id "
		+ "      , mapping_session s"
		+ " WHERE gene_stable_id=? AND gene_version=? "
		+ "      AND  s.mapping_session_id = ga.mapping_session_id ";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, stableID);
			ps.setInt(2, version);

			ResultSet rs = executeQuery(ps, sql);
			ArchiveStableID geneArchiveID = null;
			List transcripts = new ArrayList();
			while (rs.next()) {
				String oldDatabase = rs.getString(7);

				if (geneArchiveID == null)
					geneArchiveID =
						new ArchiveStableIDImpl(rs.getString(1), rs.getInt(2), oldDatabase);

				ArchiveStableID id =
					new ArchiveStableIDImpl(rs.getString(5), rs.getInt(6), oldDatabase);
				String peptide = rs.getString(8);
				TranslationSnapShot translation =
					new TranslationSnapShotImpl(id, peptide);

				id =
					new ArchiveStableIDImpl(rs.getString(3), rs.getInt(4), oldDatabase);
				transcripts.add(new TranscriptSnapShotImpl(id, translation));

			}
			if (geneArchiveID != null)
				gene =
					new GeneSnapShotImpl(
						geneArchiveID,
						TranscriptSnapShotImpl.toArray(transcripts));

		} catch (SQLException e) {
			throw new AdaptorException(
				"Failed to retrieve gene snapshot: " + stableID + "." + version,
				e);
		} finally {
			close(conn);
		}
		return gene;
	}

	/**
	 * @see org.ensembl.driver.StableIDEventAdaptor#fetchTranscriptSnapShot(java.lang.String, int)
	 */
	public TranscriptSnapShot fetchTranscriptSnapShot(
		String stableID,
		int version)
		throws AdaptorException {

		TranscriptSnapShot transcript = null;

		Connection conn = null;
		String sql = null;
		try {
			conn = getConnection();

				sql = "SELECT " + " transcript_stable_id  " // 1
		+", transcript_version " // 2
		+", ga.translation_stable_id " // 3
		+", ga.translation_version " // 4
		+", old_db_name " // 5
		+", peptide_seq " // 6
	+" FROM gene_archive ga LEFT JOIN peptide_archive pa "
		+ "      ON ga.translation_stable_id=pa.translation_stable_id AND ga.translation_version=pa.translation_version "
		+ "      , mapping_session s"
		+ " WHERE transcript_stable_id=? AND transcript_version=? "
		+ "      AND  s.mapping_session_id = ga.mapping_session_id ";
			;
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, stableID);
			ps.setInt(2, version);

			ResultSet rs = executeQuery(ps, sql);
			if (rs.next()) {

				ArchiveStableID id =
					new ArchiveStableIDImpl(
						rs.getString(3),
						rs.getInt(4),
						rs.getString(5));
				String peptide = rs.getString(6);
				TranslationSnapShot translation =
					new TranslationSnapShotImpl(id, peptide);

				id =
					new ArchiveStableIDImpl(
						rs.getString(1),
						rs.getInt(2),
						rs.getString(5));
				transcript = new TranscriptSnapShotImpl(id, translation);
			}

		} catch (SQLException e) {
			throw new AdaptorException(
				"Failed to retrieve transcript snapshot: " + stableID + "." + version,
				e);
		} finally {
			close(conn);
		}

		return transcript;
	}

	/**
	 * @see org.ensembl.driver.StableIDEventAdaptor#fetchTranslationSnapShot(java.lang.String, int)
	 */
	public TranslationSnapShot fetchTranslationSnapShot(
		String stableID,
		int version)
		throws AdaptorException {

		TranslationSnapShot translation = null;

		Connection conn = null;

		try {
			conn = getConnection();

				String sql = "SELECT old_db_name " // 1
		+", peptide_seq " // 2
	+" FROM peptide_archive pa "
		+ "  LEFT JOIN gene_archive ga "
		+ "      ON ga.translation_stable_id=pa.translation_stable_id AND ga.translation_version=pa.translation_version "
		+ "  LEFT JOIN mapping_session s "
		+ "      ON s.mapping_session_id = ga.mapping_session_id"
		+ " WHERE pa.translation_stable_id=? AND pa.translation_version=? ";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, stableID);
			ps.setInt(2, version);

			ResultSet rs = executeQuery(ps, sql);
			if (rs.next()) {

				ArchiveStableID id =
					new ArchiveStableIDImpl(stableID, version, rs.getString(1));
				String peptide = rs.getString(2);
				translation = new TranslationSnapShotImpl(id, peptide);

			}

		} catch (SQLException e) {
			throw new AdaptorException(
				"Failed to retrieve translation snapshot: " + stableID + "." + version,
				e);
		} finally {
			close(conn);
		}

		return translation;
	}

	/**
	 * Stores gene in gene_archive table of database.The
	 * archiveStableID.databaseName is overriden by session.oldDatabase.
	 * @param geneSnapShot gene snapshot to store
	 * @param session mapping session to associate with snapshot
	 */
	public void store(GeneSnapShot geneSnapShot, MappingSession session)
		throws AdaptorException {
		store(new GeneSnapShot[] { geneSnapShot }, session);
	}

	/**
	 * Stores all the geneSnapShots in the gene_archive table of the database. The
	 * archiveStableID.databaseName is overriden by session.oldDatabase.
	 * @param geneSnapShots gene snapshots to store
	 * @param session mapping session to associate with snapshots
	 */
	public void store(GeneSnapShot[] geneSnapShots, MappingSession session)
		throws AdaptorException {

		Connection geneConn = null;

		try {
			geneConn = getConnection();

				String geneSQL = "INSERT INTO gene_archive ( " + "  gene_stable_id" //1
		+", gene_version " // 2

		+", transcript_stable_id  " // 3
		+", transcript_version " // 4
		+", translation_stable_id " // 5
		+", translation_version " // 6
		+", mapping_session_id " // 7
	+") VALUES ( ?, ?, ?, ?, ?, ?, ?)";

			PreparedStatement genePS = geneConn.prepareStatement(geneSQL);

			// assume same session for ALL genes.
			long sessionID = session.getInternalID();

			int nullTranscripts = 0;
			int nonNullTranscripts = 0;
			
			for (int i = 0; i < geneSnapShots.length; i++) {
				GeneSnapShot gene = geneSnapShots[i];

				TranscriptSnapShot[] transcripts = gene.getTranscriptSnapShots();
				for (int j = 0; j < transcripts.length; j++) {
					
					TranscriptSnapShot transcript = transcripts[j];
					if (transcript != null) {
					 
					 TranslationSnapShot translation = transcript.getTranslationSnapShot();
					
					// store gene, transcript, translation snapshots
					genePS.setString(1, gene.getArchiveStableID().getStableID());
					genePS.setInt(2, gene.getArchiveStableID().getVersion());
					genePS.setString(3, transcript.getArchiveStableID().getStableID());
					genePS.setInt(4, transcript.getArchiveStableID().getVersion());
					genePS.setString(5, translation.getArchiveStableID().getStableID());
					genePS.setInt(6, translation.getArchiveStableID().getVersion());
					genePS.setLong(7, sessionID);
					executeUpdate(genePS, geneSQL);
					nonNullTranscripts++;
					} else {
						nullTranscripts++;
					}

				}
			}
			
			System.out.println("Genes with null transcripts: " + nullTranscripts + " Genes with non-null transcripts: " + nonNullTranscripts);
		} catch (SQLException e) {
			throw new AdaptorException("Failed to store geneSnapShots:", e);
		} finally {
			close(geneConn);
		}
		
	}

	/**
	 * Stores translation (peptide) in peptide_archive table of database.The
	 * archiveStableID.databaseName is overriden by session.oldDatabase.
	 * @param translationSnapShot translation to store
	 * @param session session associated with this version of the translation
	 */
	public void store(
		TranslationSnapShot translationSnapShot,
		MappingSession session)
		throws AdaptorException {
		store(new TranslationSnapShot[] { translationSnapShot }, session);
	}

	/**
	 * Stores translations (peptides) in peptide_archive table of database.The
	 * archiveStableID.databaseName is overriden by session.oldDatabase.
	 * @param translations
	 * @param session
	 */
	public void store(TranslationSnapShot[] translations, MappingSession session)
		throws AdaptorException {
		Connection peptideConn = null;

		try {
			peptideConn = getConnection();

			String peptideSQL =
				"INSERT INTO peptide_archive ( " + " translation_stable_id "
				// 1
		+", translation_version " // 2
		+", peptide_seq " // 3
	+") VALUES ( ?, ?, ?)";
			PreparedStatement peptidePS = peptideConn.prepareStatement(peptideSQL);

			for (int i = 0; i < translations.length; i++) {
				TranslationSnapShot translation = translations[i];

				// store peptide
				peptidePS.setString(1, translation.getArchiveStableID().getStableID());
				peptidePS.setInt(2, translation.getArchiveStableID().getVersion());
				peptidePS.setString(3, translation.getPeptide());
				executeUpdate(peptidePS, peptideSQL);
			}

		} catch (SQLException e) {
			throw new AdaptorException("Failed to store TranslationSnapShots:", e);
		} finally {
			close(peptideConn);
		}

	}

	/**
	 * Deletes geneSnapShot and component parts.
	 * @return whether the gene was deleted.
	 */
	public boolean delete(GeneSnapShot geneSnapShot) throws AdaptorException {

		boolean success = false;
		Connection conn = null;

		try {

			// delete peptides
			String sql =
				"DELETE FROM peptide_archive WHERE translation_stable_id=? AND translation_version=?";
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);

			TranscriptSnapShot[] transcripts = geneSnapShot.getTranscriptSnapShots();
			for (int i = 0; i < transcripts.length; i++) {
				TranscriptSnapShot transcript = transcripts[i];
				TranslationSnapShot translation = transcript.getTranslationSnapShot();

				ps.setString(1, translation.getArchiveStableID().getStableID());
				ps.setInt(2, translation.getArchiveStableID().getVersion());
				executeUpdate(ps, sql);

			}

			// delete gene, transcript, translation
			sql =
				"DELETE FROM gene_archive WHERE gene_stable_id=? AND gene_version=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, geneSnapShot.getArchiveStableID().getStableID());
			ps.setInt(2, geneSnapShot.getArchiveStableID().getVersion());
			success = executeUpdate(ps, sql) > 0;

		} catch (SQLException e) {
			throw new AdaptorException(
				"Failed to delete geneSnapShot" + geneSnapShot,
				e);
		} finally {
			close(conn);
		}

		return success;
	}

	/**
	 * Deletes all stable id events associated with the session. Leaves the
	 * session in the database.
	 * @return number of rows deleted from database. This is not the same as
	 * the number of events deleted because events are represented by multiple
	 * rows in the database.
	 */
	public int deleteEvents(MappingSession session) throws AdaptorException {

		int nDeleted = 0;
		Connection conn = null;

		try {

			conn = getConnection();
			String sql =
				"delete from stable_id_event where mapping_session_id="
					+ currentSessionInternalID(session);
			logger.fine(sql);
			nDeleted = conn.createStatement().executeUpdate(sql);

		} catch (Exception e) {
			throw new AdaptorException(
				"Failed to delete events for mapping session.",
				e);
		} finally {
			close(conn);
		}

		return nDeleted;
	}

	public boolean deleteGeneSnapShots(MappingSession session)
		throws AdaptorException {

		boolean success = false;
		Connection conn = null;

		try {

			conn = getConnection();

			String sql = null;

			sql =
				"delete from gene_archive where mapping_session_id="
					+ currentSessionInternalID(session);
			success = executeUpdate(conn, sql) > 0;

		} catch (SQLException e) {
			throw new AdaptorException(
				"Failed to delete GeneSnapShots for mapping session.",
				e);
		} finally {
			close(conn);
		}

		return success;
	}

	/**
	 * @see org.ensembl.driver.StableIDEventAdaptor#deleteTranslationSnapShots(org.ensembl.datamodel.MappingSession)
	 */
	public boolean deleteTranslationSnapShots(MappingSession session)
		throws AdaptorException {

		boolean success = false;

		// MySQL4 (fast) way of deleting rows quickly
		//		sql = "DELETE FROM peptide_archive "
		//								+" USING peptide_archive, gene_archive "
		//								+" WHERE gene_archive.mapping_session_id="+ currentSessionInternalID(session)
		//								+" AND  peptide_archive.translation_stable_id = gene_archive.translation_stable_id " 
		//								+" AND  peptide_archive.translation_version = gene_archive.translation_version " 
		//								;
		//		executeUpdate(conn, sql);

		// pre MySQL4 (slow) way of deleting peptide_archive rows 
		Connection conn = null;
		try {
			conn = getConnection();

			String sql =
				"SELECT translation_stable_id, translation_version "
					+ " FROM gene_archive "
					+ " WHERE mapping_session_id= "
					+ currentSessionInternalID(session);
			List translations = new ArrayList();
			ResultSet rs = executeQuery(conn, sql);
			while (rs.next())
				translations.add(
					new ArchiveStableIDImpl(rs.getString(1), rs.getInt(2), null));

			sql =
				"DELETE FROM peptide_archive "
					+ "WHERE translation_stable_id=? AND translation_version=?";
			PreparedStatement ps = conn.prepareStatement(sql);
			for (Iterator iter = translations.iterator(); iter.hasNext();) {
				ArchiveStableID id = (ArchiveStableID) iter.next();
				ps.setString(1, id.getStableID());
				ps.setInt(2, id.getVersion());
				if (executeUpdate(ps, sql) > 0)
					success = true;
			}
		} catch (SQLException e) {
			throw new AdaptorException("Failed to delete TranslationSnapShots.", e);
		} finally {
			close(conn);
		}

		return success;

	}

	/**
	 * @see org.ensembl.driver.StableIDEventAdaptor#delete(org.ensembl.datamodel.TranslationSnapShot)
	 */
	public boolean delete(TranslationSnapShot translation)
		throws AdaptorException {

		boolean success = false;
		String sql =
			"DELETE FROM peptide_archive "
				+ "WHERE translation_stable_id=? AND translation_version=?";
		Connection conn = null;

		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, translation.getArchiveStableID().getStableID());
			ps.setInt(2, translation.getArchiveStableID().getVersion());
			success = executeUpdate(ps, sql) > 0;

		} catch (SQLException e) {
			throw new AdaptorException(
				"Failed to delete TranslationSnapShot." + translation,
				e);
		} finally {
			close(conn);
		}
		return success;
	}

	/**
	 * Deletes session and corresponding stable id events.
	 * @return number of sessions deleted.
	 */
	public int deleteSession(MappingSession session) throws AdaptorException {

		int nDeleted = 0;
		Connection conn = null;

		try {

			deleteEvents(session);

			String sql =
				"delete from mapping_session where mapping_session_id="
					+ currentSessionInternalID(session);
			logger.fine(sql);

			conn = getConnection();
			nDeleted = conn.createStatement().executeUpdate(sql);

		} catch (Exception e) {
			throw new AdaptorException("Rethrow + stacktrace", e);
		} finally {
			close(conn);
		}

		return nDeleted;
	}

	/**
	 * @return internal id for mapping session in database, -1 if not
	 * in database.
	 */
	private long currentSessionInternalID(MappingSession session)
		throws AdaptorException, SQLException {

		Long tmp = (Long) sessionToInternalIDCache.get(session);
		if (tmp != null)
			return tmp.intValue();

		String sql =
			"SELECT mapping_session_id FROM mapping_session WHERE "
				+ "mapping_session.old_db_name = \'"
				+ session.getOldDatabase()
				+ "\' AND mapping_session.new_db_name = \'"
				+ session.getNewDatabase()
				+ "\'";
		logger.fine(sql);

		long id = -1;

		Connection conn = getConnection();
		ResultSet rs = conn.createStatement().executeQuery(sql);
		// if session already in database.
		if (rs.next()) {
			id = rs.getLong(1);
			sessionToInternalIDCache.put(session, new Long(id));
		}

		close(conn);

		return id;
	}

	private String briefString(MappingSession session) {
		return session.getOldDatabase() + ", " + session.getNewDatabase();
	}

	/**
	 * Converts string to correct format for insertion in an SQL statement.
		 * */
	private final static void toSQLFormat(String str, StringBuffer buf) {
		if (str == null)
			buf.append(" NULL ");
		else
			buf.append("'").append(str).append("'");
	}

	/**
	 * Stores pairs in database with correct session foreign key.
	 */
	private void storePairs(Pair[] pairs, long sessionInternalID)
		throws AdaptorException {

		logger.fine("Should be storing !" + pairs.length);

		StringBuffer buf = new StringBuffer();
		Connection conn = null;
		//try {
		conn = getConnection();

		String front =
			"insert into stable_id_event (old_stable_id, old_version, "
				+ "new_stable_id, new_version, mapping_session_id, type) "
				+ "values ";

		// Store pairs in database. if bufSize>1 we use MySQL non-standard SQL
		// mechanism to send multiple inserts to database in one batch.
		final int bufSize = 500;
		buf.append(front);
		for (int i = pairs.length - 1; i >= 0; i--) {

			final Pair pair = pairs[i];
			if (pair == null) {
				logger.warning("Warningskipping null pair");
				continue;
			}
			final boolean storeNow = (i == 0 || i % bufSize == 0);

			if (logger.isLoggable(Level.FINE) && TRACABLE_STABLE_ID.equals(pair.left))
				logger.info(pair.toString());

			buf.append(" (");

			toSQLFormat(pair.left, buf);
			buf.append(" , ").append(pair.leftInt).append(" , ");

			toSQLFormat(pair.right, buf);
			buf.append(" , ").append(pair.rightInt).append(" , ");

			buf.append(sessionInternalID);
			buf.append(" , '").append(pair.type).append("'");

			if (storeNow)
				buf.append(")");
			else
				buf.append(") ,");

			if (storeNow) {
				String sql = buf.toString();
				logger.fine(sql);
				executeUpdate(conn, sql);
				logger.fine("Executing insert: " + sql);
				buf.replace(0, buf.length(), front);
			}

			if ((i % 100 == 0) && logger.isLoggable(Level.FINE))
				logger.fine(
					Double.toString(i * 1.0 / pairs.length)
						+ " completed. "
						+ pairs[i].left
						+ ","
						+ pairs[i].right);
		}

		//	  } catch (SQLException e) {
		//	  //	throw new AdaptorException("Rethrow + stacktrace", e);
		//	  } finally {
		//		  close(conn);
		//	  }
		close(conn);
	}

	/**
	 * Stores event in database as multiple rows in stable_id_event table.
	 */
	public void store(StableIDEvent event) throws AdaptorException {

		logger.fine("Event to store: " + event);

		// Inefficient but simple implementation... Faster if generate SQL and exec directly.

		// Convert stableID + related to pairs then store.

		final Set related = event.getRelatedStableIDs();
		logger.fine("Num related: " + related);
		final List pairs = new ArrayList();

		final String stableID = event.getStableID();
		final int version = event.getStableIDVersion();
		final String type = event.getType();

		for (Iterator iter = related.iterator(); iter.hasNext();) {
			String relatedID = (String) iter.next();
			int[] relatedVersions = event.getRelatedVersions(relatedID);

			logger.fine("Related " + relatedID + " ==> " + relatedVersions.length);

				for (int i = 0; i < relatedVersions.length; i++) {
					int relatedVersion = relatedVersions[i];
					pairs.add(
						new Pair(stableID, version, relatedID, relatedVersion, type));
				}
		}

		logger.fine("Num pairs in event to store:" + pairs.size());

		store((Pair[]) pairs.toArray(new Pair[pairs.size()]), event.getSession());
	}

	public String getType() throws AdaptorException {
		return TYPE;
	}

	/**
	 * @param stableID ensembl stable id that might or might not relate to one
	 * or more stable ids in the latest database release.
	 * @return list of stable id strings that _stableID_ relates to in the
	 * latest release.
	 */
	public List fetchCurrent(String stableID) throws AdaptorException {

	  throw new NotImplementedYetException("This feature requires upgrading to reflect data changes since ensembl release 39.");
	}

	/**
	 * @return list of all StableIDEvents that have happened to this stableID.
	 */
	public List fetch(String stableID) throws AdaptorException {

		List events = new ArrayList();

		List sessions = fetchMappingSessions();
		final int nSessions = sessions.size();
		for (int s = 0; s < nSessions; ++s) {
			StableIDEvent event = fetch(stableID, (MappingSession) sessions.get(s));
			if (event != null)
				events.add(event);
		}

		return events;
	}

	/**
	 * @return StableIDEvent describing what happened to the specified stableID
	 * during the specified session. Returns null if no such event exists.
	 */
	public StableIDEvent fetch(String stableID, MappingSession session)
		throws AdaptorException {

		StableIDEvent event = null;
		Connection conn = null;

		try {

			final long sessionID = currentSessionInternalID(session);
			//session.getInternalID();

			String sql =
				"SELECT old_stable_id, new_stable_id, new_version, type "
					+ "FROM stable_id_event WHERE "
					+ "mapping_session_id="
					+ sessionID
					+ " AND old_stable_id=\'"
					+ stableID
					+ "\'";

			boolean newlyCreated = false;
			conn = getConnection();
			ResultSet rs = conn.createStatement().executeQuery(sql);
			boolean eventFound = rs.next();
			if (!eventFound) {
				// Maybe it was created during this session so there is only an entry
				// in the new_stable_id column
				sql =
					"SELECT old_stable_id, new_stable_id, new_version, type"
						+ " FROM stable_id_event WHERE "
						+ "mapping_session_id="
						+ sessionID
						+ " AND new_stable_id=\'"
						+ stableID
						+ "\'";

				rs = executeQuery(conn, sql);
				eventFound = rs.next();
				newlyCreated = true;
			}

			if (eventFound) {
				event = new StableIDEventImpl();
				event.setStableID(stableID);
				event.setSession(session);
				event.setType(rs.getString(4));
				if (newlyCreated)
					event.setCreated(true);
				do {
					event.addRelated(rs.getString(2), rs.getInt(3));
				} while (rs.next());
			}
		} catch (SQLException e) {
			throw new AdaptorException("Rethrow + stacktrace", e);
		} finally {
			close(conn);
		}

		return event;
	}

	/**
	 * @return All stable id events generated during the specified session.
	 */
	public List fetch(MappingSession session) throws AdaptorException {
		if (mappingSessionsCache == null) {
			mappingSessionsCache = mappingSessionsCache(fetchMappingSessions());
		}

		HashMap events = new HashMap();

		String sql =
			"SELECT old_stable_id, new_stable_id, new_version,"
				+ " mapping_session_id, type "
				+ "FROM stable_id_event WHERE mapping_session_id = "
				+ session.getInternalID()
			//      +" limit 10"
	;

		Connection conn = null;
		try {
			conn = getConnection();
			ResultSet rs = executeQuery(conn, sql);

			if (!rs.next())
				return Collections.EMPTY_LIST;

			do {

				final String oldStableID = rs.getString(1);
				final boolean oldIsSet = oldStableID != null;
				final String newStableID = rs.getString(2);
				final boolean newIsSet = newStableID != null;
				final int newVersion = rs.getInt(3);
				final int sessionInternalID = rs.getInt(4);
				final String stableID = (oldIsSet) ? oldStableID : newStableID;
				final String type = rs.getString(5);

				StableIDEvent event = (StableIDEvent) events.get(stableID);
				if (event == null) {
					event = new StableIDEventImpl();
					event.setStableID(stableID);
					event.setType(type);
					events.put(stableID, event);
					if (sessionInternalID > mappingSessionsCache.length)
						throw new AdaptorException(
							"MappingSession unavailable:"
								+ "mappingSessionInternalID = "
								+ sessionInternalID);
					event.setSession(mappingSessionsCache[sessionInternalID]);
				}

				if (newIsSet && oldIsSet)
					event.addRelated(newStableID, newVersion);

				else if (!newIsSet && oldIsSet)
					event.setDeleted(true);

				else if (newIsSet && !oldIsSet)
					event.setCreated(true);

				else
					throw new AdaptorException(
						"Invalid row in stable_id_event table: "
							+ oldStableID
							+ ","
							+ newStableID
							+ ","
							+ sessionInternalID);

			} while (rs.next());

		} catch (SQLException e) {
			throw new AdaptorException("Rethrow + stacktrace", e);
		} finally {
			close(conn);
		}

		return new ArrayList(events.values());

	}

	/**
	 * Returns list of mapping sessions ordered by time. 
	 * @return List of zero or more mapping sessions.
	 */
	public List fetchMappingSessions() throws AdaptorException {

		List sessions = new ArrayList();

		String sql =
			"SELECT mapping_session_id, old_db_name, new_db_name, created "
				+ "FROM mapping_session WHERE old_db_name!='ALL' ORDER BY created";

		Connection conn = null;
		try {
			conn = getConnection();
			ResultSet rs = executeQuery(conn, sql);

			if (!rs.next())
				return sessions;

			do {
				sessions.add(createMappingSession(rs));
			} while (rs.next());

		} catch (SQLException e) {
			throw new AdaptorException("Rethrow + stacktrace", e);
		} finally {
			close(conn);
		}

		return sessions;
	}

	/**
	 * 
	 * @param rs
	 * @return new MappingSession object constructed from current row in rs.
	 * @throws SQLException
	 */
	private MappingSession createMappingSession(ResultSet rs)
		throws SQLException {
		MappingSession session = new MappingSessionImpl();
		session.setInternalID(rs.getLong(1));
		session.setOldDatabase(rs.getString(2));
		session.setNewDatabase(rs.getString(3));
		session.setTimestamp(rs.getString(4));
		return session;
	}

	/**
	 * Returns a sparse array of mappingsessions where index = internalID;
	 */
	private MappingSession[] mappingSessionsCache(List mappingSessions) {
		MappingSession[] cache = null;

		long max = 0;
		for (int i = 0; i < mappingSessions.size(); ++i) {
			long tmp = ((MappingSession) mappingSessions.get(i)).getInternalID();
			max = (tmp > max) ? tmp : max;
		}
		// array is a cache allowing direct internal ID to mapping session lookup
		cache = new MappingSession[(int) max + 1];
		for (int i = 0; i < mappingSessions.size(); ++i) {
			MappingSession session = (MappingSession) mappingSessions.get(i);
			long tmp = session.getInternalID();
			cache[(int) tmp] = session;
		}

		return cache;
	}

}
