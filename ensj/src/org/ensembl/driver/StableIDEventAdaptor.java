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



package org.ensembl.driver;
import java.util.List;

import org.ensembl.datamodel.GeneSnapShot;
import org.ensembl.datamodel.MappingSession;
import org.ensembl.datamodel.StableIDEvent;
import org.ensembl.datamodel.TranscriptSnapShot;
import org.ensembl.datamodel.TranslationSnapShot;
import org.ensembl.util.Pair;

/**
 * Provides access to StableIDEvents in the datasource.
 */
public interface StableIDEventAdaptor extends Adaptor {


  /**
   * Return all StableIDEvents that relate _accession_ to 0 or more stable
   * ids in the current data release.
   * 
   * NOTE: This method is currently disabled due to a major change in the
   * way the data is stored. It throws a NotImplementedYet exception.
   * 
   */
  List fetchCurrent(String stableID) throws AdaptorException;



  /**
   * @return all StableIDEvents that have happened to the stable order in the
   * order they happened, returns an empty list if no such events exist.
   */
  List fetch(String stableID) throws AdaptorException;


  /**
   * @return StableIDEvent describing what happened to the specified stableID
   * during the specified session. Returns null if no such event exists.
   */
  StableIDEvent fetch(String stableID, MappingSession session) throws AdaptorException;



  void store(StableIDEvent event) throws AdaptorException;



  /**
   * Store unpacked StableIDPairs with session in database. This is the 'raw'
   * representation of the events.
   */
  void store(Pair[] pairs, MappingSession session) throws AdaptorException ;



	/**
	 * Fetches snapshot from datasource.
	 * @param stableID stable ID of snapshot
	 * @param version version of snapshot
	 * @return snapshot if it exists, otherwise null
	 * @throws AdaptorException
	 */
	GeneSnapShot fetchGeneSnapShot(String stableID, int version)  throws AdaptorException;


	/**
	 * Fetches transcript snapshot from datasource.
	 * @param stableID stable ID of snapshot
	 * @param version version of snapshot
	 * @return snapshot if it exists, otherwise null
	 * @throws AdaptorException
	 */
	TranscriptSnapShot fetchTranscriptSnapShot(String stableID, int version)  throws AdaptorException;



	/**
		 * Fetches translation snapshot from datasource.
		 * @param stableID stable ID of snapshot
		 * @param version version of snapshot
		 * @return snapshot if it exists, otherwise null
		 * @throws AdaptorException
		 */
	TranslationSnapShot fetchTranslationSnapShot(String stableID, int version)  throws AdaptorException;


	/**
	 * Store the translation snapshot.
	 * @param translation SnapShot item to be stored
	 * @param session the mapping session when this translation snapshot was created
	 * @throws AdaptorException if a problem occurs with the store
	 */
	void store(TranslationSnapShot translation,MappingSession session) throws AdaptorException;

	/**
	 * Store the translation snapshots.
	 * @param translations SnapShots item to be stored
	 * @param session the mapping session when this translation snapshot was created
	 * @throws AdaptorException if a problem occurs with the store
	 */
	void store( TranslationSnapShot[] translations,MappingSession session) throws AdaptorException;
			
			
			
	/**
	 * Deletes TranslationSnapshot.
	 * @param translation translation to be deleted.
	 * @return whether the translation was deleted
	 * @throws AdaptorException if a problem occurs with the delete
	 */
	boolean delete(TranslationSnapShot translation)  throws AdaptorException;
	
	
	/**
	 * Deletes all TranslationSnapShots associated with this session.
	 * @param session the session to delete TranslationSnapShots for
	 * @return whether any SnapShots were deleted
	 * @throws AdaptorException if a problem occurs with the delete
	 */
	boolean deleteTranslationSnapShots(MappingSession session) throws AdaptorException;
			
	/**
	 * Store the gene snapshot.
	 * @param geneSnapShot item to be stored
	 * @param session the mapping session when this genesnapshot was created
	 * @throws AdaptorException if a problem occurs with the store
	 */
	void store(GeneSnapShot geneSnapShot, MappingSession session) throws AdaptorException;
	
	
	
	/**
	 * Stores the genes. It is more efficient to call this method once rather
	 * than store(GeneSnapShot) many times. All snapshots should have the same archiveStableID.oldDatabaseName.
	 * @param geneSnapShots snapshots to be stored
	 * @param session the mapping session when this genesnapshot was created
	 * @throws AdaptorException if a problem occurs with the store
	 */
	void store(GeneSnapShot[] geneSnapShots, MappingSession session) throws AdaptorException;
	
	
	
	/**
	 * Deletes the gene snapshot.
	 * @param geneSnapShot item to be deleted
	 * @return whether the gene was deleted
	 * @throws AdaptorException if a problem occurs with the delete
	 */
	boolean delete(GeneSnapShot geneSnapShot) throws AdaptorException;
	


	/**
	 * Deletes all GeneSnapShots associated with this session.
	 * @param session the session to delete GeneSnapShots for
	 * @return whether any GeneDnapShots were deleted
	 * @throws AdaptorException if a problem occurs with the delete
	 */
	boolean deleteGeneSnapShots(MappingSession session) throws AdaptorException;

  /**
   * @return List of zero or more mapping sessions.
   */
  List fetchMappingSessions() throws AdaptorException;


  /**
   * @return All stable id events generated during the specified session.
   */
  List fetch(MappingSession session) throws AdaptorException;


  /**
   * Deletes session and corresponding stable id events.
   * @return number of sessions deleted.
   */
  int  deleteSession(MappingSession session) throws AdaptorException;

  /**
   * Deletes all stable id events associated with the session. Leaves the
   * session in the database.
   * @return number of events deleted.
   */
  int deleteEvents( MappingSession session )  throws AdaptorException;

  /**
   * Stores stable id events, and there session, in database. 
   */
  void store(StableIDEvent[] events) throws AdaptorException;

  /** 
   * If the session is new then it is added to the database. If a session
   * with the same session.oldDatabase and session.newDatabase is already in
   * database it's creation time is updated to the current time.
   * @return internalID of session.
   */
  long store( MappingSession session) throws AdaptorException;

  /** 
   * Name of the default StableIDEventAdaptor available from a driver. 
   */
  final static String TYPE = "stable_id_event";




}
