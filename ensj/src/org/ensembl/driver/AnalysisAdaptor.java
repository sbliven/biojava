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

import org.ensembl.datamodel.Analysis;


/**
 * Provides access to Analyses in the datasource.
 */
public interface AnalysisAdaptor extends Adaptor {

  /**
   * Stores analysis.
   */
  long store(Analysis analysis) throws  AdaptorException;

  /**
   * @param analysis analysis to delete.
   * @throws AdaptorException if an adaptor error occurs
   */
  void delete( Analysis analysis ) throws AdaptorException;


 /**
  * @param internalID internalID of analysis to delete.
  * @throws AdaptorException if an adaptor error occurs
  */
  void delete( long internalID ) throws AdaptorException;
  

  /**
   * @return An analysis object matching the internalID, or null if non found.
   */
  Analysis fetch(long internalID) throws AdaptorException;

  /**
   * @return A list of all Analysis objects in the database, or null if
   * none found
   */
  List fetch() throws AdaptorException;

  /**
   * @return An Analysis object matching the logicalName, or null if non found.
   */
  Analysis fetchByLogicalName(String logicalName) throws AdaptorException;

  /**
   * @return An Analysis object matching the GFFFeature field, or null if non found.
   */
  List fetchByGffFeature(String gffFeature) throws AdaptorException;

  /** 
   * Name of the default AnalysisAdaptor available from a driver. 
   */
  final static String TYPE = "analysis";
}
