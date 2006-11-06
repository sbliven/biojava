/*
    Copyright (C) 2002 EBI, GRL

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

import java.util.List;

/**
 * Marker. Markers can appear zero, one or more times in a genome and each
 * appearance is represented by a different location.
 *
 * <p>Note: Some markers do not have a genomic location because they can not
 * be mapped to a clone fragment. In this case getLocation() returns null and
 * getAllLocations() returns an emmpty array.
 */
public interface Marker extends Persistent {
  int getMaxPrimerDistance();

  void setMaxPrimerDistance(int maxPrimerDistance);

  int getMinPrimerDistance();

  void setMinPrimerDistance(int minPrimerDistance);

  /**
   * MarkerFeatures representing where this marker appears on the genome.
   * @return zero or more MarkerFeatures.
   */
  List getMarkerFeatures();

  /**
   * MarkerFeatures representing where this marker appears on the genome.
   * @param markerFeatures zero or more MarkerFeatures.
   */
  void setMarkerFeatures(List markerFeatures);

  int getPriority();

  void setPriority(int priority);

  String getType();

  void setType(String type);

  String getSeqRight();

  void setSeqRight(String seqRight);

  String getSeqLeft();

  void setSeqLeft(String seqLeft);


  /**
  * @return Ordered list of synonyms, primary synonym first.
  * */
  List getSynonyms();

  void setSynonyms(List synonyms);

  String getDisplayName();

  void setDisplayName(String displayName);

}
