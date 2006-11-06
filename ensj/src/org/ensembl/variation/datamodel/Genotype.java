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

import org.ensembl.datamodel.Persistent;


/**
 * Genotype. Genotypes are assumed to be for diploid organisms and are
 * represented by two alleles.
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public interface Genotype extends Persistent{

  /**
   * One of the alleles that defines this genotype.
   * @return One of the alleles that defines this genotype.
   */
	String getAllele1();
	
  /**
   * One of the alleles that defines this genotype.
   * @return One of the alleles that defines this genotype.
   */
	String getAllele2();
	
  /**
   * Variation.
   * @return variation.
   */
	Variation getVariation();
	
}
