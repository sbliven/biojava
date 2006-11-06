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

import org.ensembl.datamodel.Feature;

/**
 * A linkage disequilibrium between two VariationFeatures.
 * 
 * The location is a single location node that covers both
 * VariationFeatures and any intervenening gap.
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public interface LDFeature extends Feature {

	/**
	 * First VariationFeature.
	 * @return first VariationFeature.
	 */
	VariationFeature getVariationFeature1();
	
	/**
	 * Second VariationFeature.
	 * @return second VariationFeature.
	 */
	VariationFeature getVariationFeature2();
	
	/**
	 * Get Population this linkage disequilibrium appears in.
	 * @return Population this linkage disequilibrium appears in.
	 */
	Population getPopulation();
	
	/**
	 * The R Square value.
	 * @return The R Square value.
	 */
	double getRSquare();
	
	/**
	 * The D Prime value.
	 * @return The D Prime value.
	 */
	double getDPrime();
	
	/**
	 * The sample count.
	 * @return The sample count.
	 */
	int getSampleCount();
	
}
