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


/**
 * Provides access to some datatype in some datasource.
 */
public interface Adaptor extends java.rmi.Remote {
  
  
    String getType() throws AdaptorException;

	/**
	 * Closes all connections opened by the adaptor.
	 * @throws AdaptorException
	 */
	void closeAllConnections() throws AdaptorException;
	
  
  /**
   * Clears any caches being used by the adaptor. 
   * 
   * This should be called if the data in the 
   * database changes during the adaptors lifetime to prevent
   * the adaptor becoming out of sync with the database.
   * 
   * Not all adaptors will necessarily use caches, in those
   * cases this method will do nothing.
   * @throws AdaptorException
   */
  void clearCache() throws AdaptorException;
    // EJB Mode won't allow exposure to the underlying driver class.
    //CoreDriver getDriver();
}
