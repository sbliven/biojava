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

package org.ensembl.variation.datamodel;

import java.util.List;

import org.ensembl.datamodel.Persistent;

/**
 * Ensembl representation of a grouping of alleles (aka haplotype).
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public interface AlleleGroup extends Persistent {

  /**
   * Name of the allele group.
   * @return name of the allele group.
   */
  String getName();

  /**
   * VariationGroup associated with this allele group.
   */ 
  VariationGroup getVariationGroup();

  /**
   * Get the internalID of the VariationGroup associated with this allele group.
   * @see #getVariationGroup()
   */ 
  long getVariationGroupID(long variationGroupID);
  
  /**
   * Population associated with this allele group. 
   * @return population or null if there is
   * no population associated with this allele group
   */
  Population getPopulation();

  /**
   * Name of the source database this allele group comes from.
   * @return Name of the source database this allele group comes from.
   */
  String getSource();

  /**
   * Frequency of the allele group.
   * @return frequency of the allele group.
   */
  double getFrequency();
  
  /**
   *  Adds an allele and associated variation to this allele group
   */
  void addVariation(Variation variation, String allele);

  /**
   * Retrieves all variations that are associated with the alleles that
   * comprise this allele group.  The ordering of the Variations
   * returned by this method are consistant with the ordering of
   * the alleles returned by the get_all_alleles method.  The third
   * allele corresponds to the third variation, etc.  List
   * getVariations();
   * @return variations associated with this group.
   */
  List getVariations();

  /**
   * Retrieves all alleles that are part of this allele group.
   * The alleles are returned as simple strings.  The ordering of
   * the alleles returned by this method are consistant with the
   * ordering of the variations returned by the get_all_Variations
   * method.  The third allele corresponds to the third variation,
   * etc.
   * @return alleles associated with this group.
   */
  List getAlleles();

}
