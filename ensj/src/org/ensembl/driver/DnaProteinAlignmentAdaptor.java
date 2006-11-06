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


import java.util.Iterator;
import java.util.List;

import org.ensembl.datamodel.Analysis;
import org.ensembl.datamodel.DnaProteinAlignment;
import org.ensembl.datamodel.Location;


/**
 * Provides access to DnaProteinAlignments in a datasource.
 * */
public interface DnaProteinAlignmentAdaptor extends FeatureAdaptor {



  /**
   * Stores feature.
   */
  long store(DnaProteinAlignment feature) throws  AdaptorException;



  /**
   * @param internalID internalID of feature to be deleted.
   * @throws AdaptorException if an adaptor error occurs
   */
  void delete( long internalID ) throws AdaptorException;

  /**
   * @param feature feature to be deleted.
   * @throws AdaptorException if an adaptor error occurs
   */
  void delete( DnaProteinAlignment feature ) throws AdaptorException;


  /**
   * @return A feature matching the internalID, or null if non found.
   */
  DnaProteinAlignment fetch(long internalID) throws AdaptorException;



  /**
   * @return A list of features inside the Location. An empty List is returned if non found.
   */
  List fetch( Location location) throws AdaptorException;



  /**
   * @return A list of features inside the Location, and filtered by the
   * Analysis. An empty List is returned if non found.
   */
  List fetch( Location location, Analysis analysis ) throws AdaptorException;



  /**
   * @return A list of features inside the Location, and filtered by the
   * Analyses. An empty List is returned if non found.
   */
  List fetch( Location location, Analysis[] analyses ) throws AdaptorException;


  /**
   * @return A list of features inside the Location of type "logicName". An
   * empty List is returned if non found.
   */
  List fetch( Location location, String logicalName ) throws AdaptorException;



  /**
   * Fetch all DNAProteinAlignments with the specified logicalName.
   * 
   * Often requires alot of memory because there are many DNAProteinAlignments
   * in the database.
   * A functionally equivalent method is fetchIterator(String) which uses less memory.
   * 
   * @param logicalName logical name of the DNAProteinAlignments of interest.
   * @return zero or more DNAProteinAlignments matching the logicalName.
   * @see #fetchIterator(String) alternative means of retrieving the same data]
   * that requires less memory.
   */
  List fetch(String logicalName) throws AdaptorException;


  /**
   * Fetch an iterator over all DNAProteinAlignments with the specified logicalName.
   * 
   * Like fetch(String) but uses less memory.
   * 
   * @param logicalName logical name of the DNAProteinAlignments of interest.
   * @return an iterator over zero or more DNAProteinAlignments.
   * @see #fetch(String)
   */
  Iterator fetchIterator(String logicalName) throws AdaptorException;
  
  /**
   * @return A list of features inside the Location with a type specified in
   * "locicNames". An empty List is returned if non found.
   */
  List fetch( Location location, String[] logicalNames ) throws AdaptorException;

  /** 
   * Name of the default DnaProteinAlignmentAdaptor available from a driver. 
   */  
  final static String TYPE = "dna_protein_alignment";
}
