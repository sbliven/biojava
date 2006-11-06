/*
    Copyright (C) 2001 EBI, GRL

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
package org.ensembl.variation.datamodel;

import org.ensembl.datamodel.Feature;

/**
 * This is a an occurrence of a nucleotide variation in the genome. The
 * actual variation information is represented by an associated Variation
 * object. Some of the information has been denormalized and is available on
 * the feature for speed purposes.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 * @see Variation
 */
public interface VariationFeature extends Feature {

  /**
   * The allele_string is a '/' demimited string representing the
   * alleles associated with this feature's variation.
   * @return alleles associated with with this feature's variation.
  */
  String getAlleleString();
  
  /**
   * Set the allele string.
   * @see #getAlleleString()
   */
  void setAlleleString(String alleleString);

  /**
   * Name of the variation associated with the feature.
   * @return Name of the variation associated with the feature.
   */
  String getVariationName();

  /**
   * @see #getVariationName()
   */
  void setVariationName(String name);

  /**
   * The map_weight is the number of times this features variation was mapped
   * to the genome.
   */
  int  getMapWeight();
  
  /**
   * @see #getMapWeight()
   */
  void setMapWeight(int mapWeight);

  /**
   * The variation associated with this feature.  If not set, and this
   * VariationFeature has an associated adaptor an attempt will be made to
   * lazy-load the variation from the database.
   * @return variation if available, otherwise null.
   */
  Variation getVariation();

  /**
   * @see #getVariation()
   */void setVariation(Variation variation);
  

  /**
   * The internal id of the variation associated with this feature.
   * @return internal id of the variation associated with this feature.
   */
  long getVariationInternalID();

  /**
   * @see #getVariationInternalID()
   */
  void setVariationInternalID(long internalID);
}
