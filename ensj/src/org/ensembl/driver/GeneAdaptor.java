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

import org.ensembl.datamodel.Gene;

/**
 * Provides access to genes in the datasource.
 */
public interface GeneAdaptor extends FeatureAdaptor {

	/** 
	 * Name of the default GeneAdaptor available from a driver. 
	 */
	final static String TYPE = "gene";
	
  /**
   * Store the gene in the approriate data source.
   * @return internalID assigned to gene.
   */
  long store(Gene gene) throws  AdaptorException;

  /**
   * @return A gene matching the internalID, or null if non found.
   */
  Gene fetch(long internalID) throws AdaptorException;

  /**
   * @return A gene matching the accessionID, or null if non found.
   */
  Gene fetch(String accessionID) throws AdaptorException;

  /**
   * @return zero or more Genes with the specified synonym.
   */
  List fetchBySynonym(String synonym) throws AdaptorException;
  
  
  /**
   * @return Genes matching the interpro id, empty list if non found.
   */
  List fetchByInterproID(String interproID) throws AdaptorException;

  /**
   * Retrieves gene's accession from persistent store and sets gene.accession.
   */
  void fetchAccessionID(Gene gene) throws AdaptorException;

  /**
   * @return List containing zero or more items matching the query.
   * @deprecated since version 27.0. Use other fetch(...) methods instead.
   */
  List fetch(org.ensembl.datamodel.Query query) throws AdaptorException;

  /**
   * Deletes gene with specified internalID plus it's transcripts,
   * translations and exons.
   */
  void delete(Gene gene)  throws  AdaptorException;

  /**
   * Deletes gene with specified internalID plus it's transcripts,
   * translations and exons.
   */
  void delete(long geneInternalID)  throws  AdaptorException;
  
}
