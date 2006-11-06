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
package org.ensembl.driver;

import java.util.List;

import org.ensembl.datamodel.Transcript;



/**
 * Provides access to Transcripts in the datasource.
 */
public interface TranscriptAdaptor extends FeatureAdaptor {

  /**
   * Stores transcript plus it's exons and translation in persistent storage.
   */
  void store(Transcript transcript)  throws  AdaptorException;
  
  /**
   * Deletes transcript plus it's exons and translation from persistent storage.
   */
  void delete(Transcript transcript)  throws  AdaptorException;
  

  /**
   * Deletes transcript with specified internalID plus it's exons and
   * translation from persistent storage.
   */
  void delete(long transcriptInternalID)  throws  AdaptorException;

  /**
    * @return A transcript matching the internalID, or null if non found.
    */
  Transcript fetch(long internalID) throws AdaptorException;




  /**
    * @return A transcript matching the accessionID, or null if non found.
    */
  Transcript fetch(String accessionID) throws AdaptorException;


  /**
   * Loads missing gene and exons into the transcript where these are
   * available in the database.
    * @return transcript with exons and gene set where possible.
    */
  Transcript fetchComplete(Transcript transcript) throws AdaptorException;


  /**
   * Get all the transcripts associated with the specified genes.
   * @param geneIDs internal IDs of genes.
   * @return A list of all the transcripts for that genes in no partticular order.
   * @see #fetchByGeneIDs(long[], boolean)
   */
  List fetchByGeneIDs(long[] geneIDs)
    throws AdaptorException;
    
  /**
   * Get all the transcripts associated with the specified genes
   * and optionally preload the child transcripts, translations
   * and exons.
   * @param geneIDs internal IDs of genes.
   * @return A list of all the transcripts for that genes in no partticular order.
   */
  List fetchByGeneIDs(long[] geneIDs, boolean loadChildren)
    throws AdaptorException;

  /**
    * @return list of zero or more Transcripts.
    * @deprecated use more specific fetch methods
    */
  List fetch(org.ensembl.datamodel.Query query) throws AdaptorException;

  /**
   * @return zero or more Transcripts matching the synonym.
   */
  List fetchBySynonym(String synonym) throws AdaptorException;
  
  
  /**
   * @return Transcripts matching the interpro id, empty list if non found.
   */
  List fetchByInterproID(String interproID) throws AdaptorException;

  
  /**
   * Retrieves transcript's accession from persistent store and sets transcript.accession.
   */
  void fetchAccessionID(Transcript transcript) throws AdaptorException;
  
  /**
   * Retrieves transcript's version from persistent store and sets transcript.version. Version
   * is set to 0 if it is unavailable in persistent store.
   * @param transcript transcript.internalID should be >0
   */
  void fetchVersion(Transcript transcript) throws AdaptorException;


  /** 
   * Name of the default TranscriptAdaptor available from a driver. 
   */
  final static String TYPE = "transcript";


  /**
   * Returns supporting features for this transcript.
   * @param transcriptID internal ID of the transcript of interest.
   * @return zero or more supporting features where a supporting feature is
   * either a DnaDnaAlignment or a DnaProteinAlignment.
   * @see org.ensembl.datamodel.DnaDnaAlignment
   * @see org.ensembl.datamodel.DnaProteinAlignment
   */
  List fetchSupportingFeatures(long transcriptID) throws AdaptorException;


}
