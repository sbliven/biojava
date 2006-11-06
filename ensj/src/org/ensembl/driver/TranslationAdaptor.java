/*
 * Copyright (C) 2003 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.ensembl.driver;

import java.util.List;

import org.ensembl.datamodel.Translation;

/**
 * Provides access to Translations in the datasource.
 */
public interface TranslationAdaptor extends Adaptor {

  /**
   * Fetches a translation with the specified id.
   * @param internalID internalID of a translation in the datagabase.
   * @return A translation matching the internalID, or null if non found.
   */
  Translation fetch(long internalID) throws AdaptorException;

  /**
   * Fetches a translation with the specified id with or without
   * start and end exons preloaded.
   * @param internalID internalID of a translation in the datagabase.
   * @param loadChildren whether to preload the child exons.
   * @return A translation matching the internalID, or null if non found.
   */
  Translation fetch(long internalID, boolean loadChildren)
    throws AdaptorException;

  /**
   * Fetch a translation by the internal ID of its associated translation.
   * 
   * @param transcriptInternalID The internal ID of the transcript this translation belongs to.
   * @return The translation associated with the transcript, or null if there is none (e.g. pseudogene).
   */
  Translation fetchByTranscript(long transcriptInternalID)
    throws AdaptorException;

  /**
   * Fetch a translation by the internal ID of its associated translation
   * with or without including start and end exons.
   * 
   * @param transcriptInternalID The internal ID of the transcript this translation belongs to.
   * @param loadChildren whether to preload the child exons.
   * @return The translation associated with the transcript, or null if there is none (e.g. pseudogene).
   */
  Translation fetchByTranscript(
    long transcriptInternalID,
    boolean loadChildren)
    throws AdaptorException;

  /**
   * Fetch a translations by the internal IDs of their associated translation.
   * 
   * @param transcriptInternalIDs array of transcript internal IDs.
   * @return The translations associated with the transcripts, or null if there is none (e.g. pseudogene.)
   * return order is not specified.
   */
  List fetchByTranscripts(
    long[] transcriptInternalIDs,
    boolean loadChildren)
    throws AdaptorException;

  /**
   * @return A translation matching the accessionID, or null if non found.
   */
  Translation fetch(String accessionID) throws AdaptorException;

  /**
   * @return Translations matching the synonym, empty list if non found.
   */
  List fetchBySynonym(String synonym) throws AdaptorException;

  /**
   * @return Translations matching the interpro id, empty list if non found.
   */
  List fetchByInterproID(String interproID) throws AdaptorException;

  /**
   * Retrieves translation's accession from persistent store and sets translation.accession.
   */
  void fetchAccessionID(Translation translation) throws AdaptorException;

  /**
   * Retrieves translation's version from persistent store and sets translation.version. Version is set to 0 if it is unavailable
   * in persistent store.
   * 
   * @param translation translation.internalID should be >0
   */
  void fetchVersion(Translation translation) throws AdaptorException;

  /**
   * Retrieves from persistent store whether translation is known and sets translation.known.
   */
  void fetchKnown(Translation translation) throws AdaptorException;

  /**
   * Retrieves from persistent store whether translations are known and for each one sets translation.known.
   */
  void fetchKnown(Translation[] translations) throws AdaptorException;

  /**
   * Name of the default TranslationAdaptor available from a driver.
   */
  final static String TYPE = "translation";

  /**
   * Retrieves the interproIDs for the translation and sets them on the translation. Sets the interproIDs to be an empty array if
   * non are found.
   * 
   * @param translation translation that interproIDs should be retrieved for.
   */
  public void completeInterproIDs(Translation translation)
    throws AdaptorException;

  /**
   * Fetch all the translations from the database.
   */
  public List fetchAll() throws AdaptorException;

}