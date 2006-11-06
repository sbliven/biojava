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

package org.ensembl.datamodel;

import java.util.List;

/**
 * Represents a quantitative trait locus (Qtl) in the EnsEMBL database. A Qtl
 * is defined by two or three markers, two flanking and one optional peak
 * marker.  Its a region (or more often a group of regions) which is likely
 * to affect the phenotype (trait) described in this Qtl. Each region is
 * specified by a QtlFeature. If the Qtl can not be mapped to any regions in
 * a the assembly then it has no QtlFeatures.
 * @see org.ensembl.datamodel.QtlFeature
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public interface Qtl extends Persistent {


  /**
   * One flanking marker of the interest region, the two flanking markers
   * define the region.
   * @return first flanking marker or null if undefined.
   */
   Marker getFlankMarker1();


  /**
   * One flanking marker of the interest region, the two flanking markers
   * define the region.
   * @return second flanking marker or null if undefined.
   */
   Marker getFlankMarker2();


  /**
   * A score for the Qtl.
   * @return a score for the Qtl.
   */
   float getLodScore();


  /**
   * An optional Marker which has the peak probabitity for this traits
   * occurence.
   * @return peak marker, or null if undefined.
   */
   Marker getPeakMarker();


  /**
   * Returns QtlSynonyms.
   * @return zero or more synonyms.
   * @see org.ensembl.datamodel.QtlSynonym
   */
   List getSynoyms();


  /**
   * Phenotype of this Qtl.
   * @return Phenotype of this Qtl.
   */
   String getTrait();


  /**
   * One flanking marker of the interest region, the two flanking markers
   * define the region.
   * @param marker first flanking marker.
   */
   void setFlankMarker1(Marker marker);


  /**
   * One flanking marker of the interest region, the two flanking markers
   * define the region.
    * @param marker second flanking marker.
   */
   void setFlankMarker2(Marker marker);


  /**
   * A score for the Qtl.
   * @param score a score for the Qtl.
   */
   void setLodScore(float score);


  /**
   * An optional Marker which has the peak probablitity for this traits
   * occurence.
   * @param marker peak marker, null if none exists.
   */
   void setPeakMarker(Marker marker);


  /**
   * Set QtlSynonyms. 
   * @param synonyms zero or more synonyms.
   * @see org.ensembl.datamodel.QtlSynonym
   */
   void setSynoyms(List synonyms);


  /**
   * Phenotype of this Qtl.
   * @param trait Phenotype of this Qtl.
   */
   void setTrait(String trait);
  

  /**
   * The qtl features associated with this Qtl. A QtlFeature specifies where
   * the Qtl appears on the genome.
   * 
   * @return zero or more QtlFeatures.
   * @see org.ensembl.datamodel.QtlFeature
   */
  List getQtlFeatures();
  
  /**
   * The qtl features associated with this Qtl. A QtlFeature specifies where
   * the Qtl appears on the genome.
   * 
   * @return zero or more QtlFeatures.
   * @see org.ensembl.datamodel.QtlFeature
   */
  void setQtlFeatures(List qtlFeatures);
}
