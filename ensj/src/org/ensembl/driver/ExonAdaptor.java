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

import org.ensembl.datamodel.Exon;


/**
 * Provides access to Exons in the datasource.
 * @see org.ensembl.datamodel.Exon
 */
public interface ExonAdaptor extends FeatureAdaptor {

  


  /**
   * @return A Exon matching the internalID, or null if non found.
   */
  public Exon fetch(long internalID) throws AdaptorException;

  /**
   * @return A Exon matching the accessionID, or null if non found.
   */
  public Exon fetch(String accessionID) throws AdaptorException;

  /**
   * Retrieves accession from persistent store and sets that value on the
   * exon.  */
  void fetchAccessionID(Exon exon) throws AdaptorException;

  /**
	 * Retrieves version from persistent store and sets that value on the
 	 * exon.  */
  void fetchVersion(Exon exon)  throws AdaptorException;

  /**
   * @return List containing zero or more items matching the query.
   * @deprecated since version 27.0 use other fetch(...) methods.
   */
  List fetch(org.ensembl.datamodel.Query query) throws AdaptorException;


  /**
   * This method loads the gene and transcripts into the exon if 
   * it corresponds to entries in the database. 
   *
   * @param exon potentially incomplete exon. 
   * @return exon with gene and transcripts loaded, or null if no such entries where 
   * found.
   */
  Exon fetchComplete(Exon exon) throws AdaptorException;

  /**
	 * Attempts to remove exon from the data source.
	 */
	void delete(Exon exon) throws  AdaptorException;

	/**
	 * Attempts to remove exon with the specified internalID from the data
	 * source.
	 */
	void delete(long internalID) throws  AdaptorException;

	/**
	 * Stores new exon in the data source.
	 */
	long store(Exon exon) throws  AdaptorException;

  /** 
   * Name of the default ExonAdaptor available from a driver. 
   */
  final static String TYPE = "exon";

  /**
   * Fetch all exons associated with the transcript.
   * @param transcriptID internal ID of a transcript.
   * @return zero or more Exons.
   */
  List fetchAllByTranscript(long transcriptID) throws  AdaptorException;


}
