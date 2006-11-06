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

import org.ensembl.datamodel.ExternalDatabase;


/**
 * Provides access to ExternalDatabases in the datasource.
 */
public interface ExternalDatabaseAdaptor extends Adaptor {

	/**
	 * Fetches all external databases.
	 * @return all (zero or more) ExternalDatabases from database.
	 * @throws AdaptorException
	 */
	List fetch() throws AdaptorException;
	
  /**
   * Fetches ExternalDb for the the given externalDbId
   * @param externalDbId internalID id of the external database
   * @return the ExternalDatabase object
   * @throws AdaptorException if an adaptor error occurs
   */
  ExternalDatabase fetch(long externalDbId) throws AdaptorException;


  /**
   * Fetches ExternalDb with the specied databaseName.
   * @param databaseName name of the database.
   * @return ExternalDatabase if found, otherwise null.
   */
  ExternalDatabase fetch(String databaseName)  throws AdaptorException;
  
  
   /**
   * @return internalID assigned to externalDatabase in database.
   * @throws AdaptorException if an adaptor error occurs
   */
  long store(ExternalDatabase externalDatabase) throws AdaptorException;
  

  /**
   * @param internalID internalID of externalDatabase to be deleted from database.
   * @throws AdaptorException if an adaptor error occurs
   */
  void delete( long internalID ) throws AdaptorException; 

  /**
   * @param externalDatabase to be delete.
   * @throws AdaptorException if an adaptor error occurs
   */
  void delete( ExternalDatabase externalDatabase ) throws AdaptorException; 

   /** 
   * Name of the default ExternalDatabaseAdaptor available from a driver. 
   */
  final static String TYPE = "external_database";


}
