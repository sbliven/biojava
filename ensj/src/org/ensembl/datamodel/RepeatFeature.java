/*
    Copyright (C) 2002  Ensembl

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

package org.ensembl.datamodel;


/**
 * Repetitive sequence on genome. I am a type of FeaturePair.
**/
public interface RepeatFeature extends FeaturePair {

  /**
   * Returns repeat consensus internal id, this is the same
   * as repeatConsensus.internalID if repeatConsensus is available.
   *
   * @return internalID of the cloneFragment.
   */
  long getRepeatConsensusInternalID();

  /**
   * Sets repeatConsensus internal id, also sets the
   * repeatConsensus.internalID if repeatConsensus is available.
   */

  void setRepeatConsensusInternalID(long internalID);
  
  /**
   * @return RepeatConsensus this repeat is a partial instance of.`
   */
  RepeatConsensus getRepeatConsensus();
  
  void setRepeatConsensus(RepeatConsensus repeatConsensus);
}//end RepeatFeature
