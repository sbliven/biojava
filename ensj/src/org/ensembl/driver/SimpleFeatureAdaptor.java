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
import org.ensembl.datamodel.Feature;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.SimpleFeature;


/**
 * Provides access to SimpleFeatures in the datasource.
 */
public interface SimpleFeatureAdaptor extends FeatureAdaptor {

  /** Attempts to store the feature in the approriate data source. */
  long store( Feature feature ) throws  AdaptorException;

  /** Attempts to store the feature in the approriate data source. */
  long store( Feature feature, java.sql.Connection connection ) throws  AdaptorException;
  
  /** @return A feature matching the internalID, or null if non found. */
  SimpleFeature fetch( long internalID ) throws AdaptorException;

  
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
   * @return A feature matching the logicalName, or null if non found.
   */
  List fetch(String logicalName) throws AdaptorException;


  /**
   * @return A list of features inside the Location with a type specified in
   * "locicNames". An empty List is returned if non found.
   */
  List fetch( Location location, String[] logicalNames ) throws AdaptorException;

  /** 
   * Name of the default SimpleFeatureAdaptor available from a driver. 
   */
  final static String TYPE = "simple_feature"; 
}
