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

import org.ensembl.datamodel.RepeatConsensus;

/**
 * Provides access to RepeatConsensus' in the datasource.
 */
public interface RepeatConsensusAdaptor extends Adaptor {
    /**
     * Fetches consensus with the specified internalID.
     * @return consensus, or null if object not found.
     * @throws AdaptorException if an adaptor error occurs
     * @param internalID internalID of the consensus
     */
    RepeatConsensus fetch(long internalID) throws AdaptorException;

    /**
     * Stores consensus.
     * @param repeatConsensus consensus to be stored
     * @return internalID assigned to consensus during store.
     * @throws AdaptorException if an adaptor error occurs
     */
    long store(RepeatConsensus repeatConsensus) throws AdaptorException;

    /**
     * @throws AdaptorException if an adaptor error occurs
     * @param internalID internalID of consensus to be deleted from database.
     */
    void delete( long internalID ) throws AdaptorException;

    /**
     * @throws AdaptorException if an adaptor error occurs
     * @param consensus consensus to delete.
     */
    void delete(RepeatConsensus consensus) throws AdaptorException;

  /** 
   * Name of the default RepeatConsensusAdaptor available from a driver. 
   */
  String TYPE="repeat_consensus";
}
