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

import org.ensembl.datamodel.Qtl;

/**
 * Retrieves Qtls from the database. Works in conjunction with the
 * QtlFeatureAdaptor.
 * @see org.ensembl.datamodel.Qtl
 * @see org.ensembl.driver.QtlFeatureAdaptor
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public interface QtlAdaptor extends Adaptor {
  
  /**
   * Adaptor type. TYPE = "qtl".
   */
  final static String TYPE = "qtl";
  
  /**
   * Fetch Qtl by it's internalID.
   * @return Qtl with specied internalID, or null if no such Qtl exists in database.
   */
  Qtl fetch(long internalID) throws AdaptorException ;
  
  /**
   * Fetch all Qtls from database.
   * @return list of zero or more Qtls.
   */
  List fetchAll() throws AdaptorException ;
  
  /**
   * Fetch all Qtls from database with the specified trait.
   * @param trait trait affected by Qtl.
   * @return list of zero or more Qtls.
   */
  List fetchByTrait(String trait) throws AdaptorException ;
  

  /**
   * Fetch all Qtls from database that originally come from the specified
   * database.
   * @param sourceDatabaseName name of the source database.
   * @return list of zero or more Qtls.
   */
  List fetchBySourceDatabase(String sourceDatabaseName) throws AdaptorException ;
  
  /**
   * Fetch all Qtls from database that originally come from the specified
   * database and have the specified id.
   * @param sourceDatabaseName name of the source database.
   * @param sourceID id in the source database.
   * @return list of zero or more Qtls matching the criteria.
   */
  List fetchBySourceDatabase(String sourceDatabaseName, String sourceID) throws AdaptorException ;
  
}
