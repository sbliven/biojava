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

import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Sequence;

/**
 * Provides access to Sequence in the datasource.
 */
public interface SequenceAdaptor extends Adaptor {
  
  /**
   * Fetches sequence corresponding to location.
   * @param location location to retrieve sequence for, defaults to positive strand if
   * sequence is unset. 
   * @return sequence corresponding to location, or null if location specified 
   * an invalid genomic region.
   * @throws AdaptorException
   */
  Sequence fetch(Location location) throws AdaptorException;

  Sequence fetch(long internalID) throws AdaptorException;

  void delete(Sequence sequence) throws AdaptorException;

  void delete(long internalID) throws AdaptorException;

  /**
  * Store the Sequence in data source.
  */
  long store(Sequence sequence) throws  AdaptorException;

  /** 
   * Name of the default SequenceAdaptor available from a driver. 
   */
  final static String TYPE = "sequence";
}

