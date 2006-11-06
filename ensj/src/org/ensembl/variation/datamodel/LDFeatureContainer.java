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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ensembl.util.FrequencyCounter;
import org.ensembl.util.IDMap;
import org.ensembl.util.PersistentSet;

/**
 * A container for LDFeatures (linkage disequilibrium features)
 * that provides various methods for filtering and accessing the 
 * the contained data.
 * 
 * Instances of this 
 * class can be created manually from a List of LDFeatures and 
 * are created by LDFeatureAdaptors.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 * @see org.ensembl.variation.datamodel.LDFeature
 * @see org.ensembl.variation.driver.LDFeatureAdaptor adaptor which can create
 *      instances of this class.
 */
public class LDFeatureContainer {

	private LDFeature[] ldFeatures;

	private List populations;

	private List variations;

	/**
	 * Create a container for the specified LD features.
	 * 
	 * @param ldFeatures
	 *            LD Features this container manages.
	 */
	public LDFeatureContainer(List ldFeatures) {
		this.ldFeatures = (LDFeature[]) ldFeatures
				.toArray(new LDFeature[ldFeatures.size()]);
	}

	/**
	 * The default population is the population with the maximum number of
	 * LDFeatures.
	 * 
	 * @return the population with the maximum number of LDFeatures.
	 */
	public Population getDefaultPopulation() {

		FrequencyCounter popCounter = new FrequencyCounter();
		for (int i = 0; i < ldFeatures.length; i++)
			popCounter.addOrIncrement(ldFeatures[i].getPopulation());
		return (Population) popCounter.getMostFrequent();
	}

	/**
	 * All LDFeatures held in this container.
	 * 
	 * @return zero or more LDFeatures.
	 */
	public List getLDFeatures() {
		return Arrays.asList(ldFeatures);
	}

	/**
	 * Subset of contained LDFeatures that come from the specified
	 * population.
	 * 
	 * @return zero or more LDFeatures.
	 */
	public List getLDFeatures(Population population) {
		List r = new ArrayList();
		for (int i = 0; i < ldFeatures.length; i++) {
			Population p = ldFeatures[i].getPopulation();
			if (p.sameInternalID(population))
				r.add(ldFeatures[i]);
		}
		return r;
	}

	/**
	 * Subset of contained LDFeatures that come from the
	 * default population.
	 * 
	 * @return zero or more LDFeatures.
	 * @see #getDefaultPopulation()
	 */
	public List getLDFeaturesFromDefaultPopulation() {
		return getLDFeatures(getDefaultPopulation());
	}

	/**
	 * Get all the populations present in this instance.
	 * 
	 * @return all the populations present in this instance.
	 */
	public List getPopulations() {
		if (populations == null) {
			IDMap id2Pops = new IDMap();
			for (int i = 0, n = ldFeatures.length; i < n; i++)
				id2Pops.put(ldFeatures[i].getPopulation());
			populations = new ArrayList(id2Pops.values());
		}
		return populations;
	}

	/**
	 * All the populations referenced by LDFeatures in this instance.
	 * 
	 * @return All the populations referenced by LDFeatures in this instance. 
	 */
	public List getPopulations(VariationFeature variationFeature1,
			VariationFeature variationFeature2) {
		PersistentSet p = new PersistentSet();
		for (int i = 0, n = ldFeatures.length; i < n; i++) {
			long vfaID = ldFeatures[i].getVariationFeature1().getInternalID();
			long vfbID = ldFeatures[i].getVariationFeature2().getInternalID();
			if ((vfaID == variationFeature1.getInternalID() && vfbID == variationFeature2
					.getInternalID())
					|| (vfaID == variationFeature1.getInternalID() && vfbID == variationFeature2
							.getInternalID()))
				p.add(ldFeatures[i].getPopulation());
		}
		return p.toList();
	}

	/**
	 * All the variations associated to LDFeatures
	 * in this instance.
	 * 
	 * Assocaition: LDFeature->VariationFeature[1,2]->Variation.
	 * 
	 * @return all Variations associated with the LDFeatures.
	 */
	public List getVariations() {
		if (variations == null)
			variations = getVariations(null, true);
		return variations;
	}

	/**
	 * Retrieves and filters all associated Variations or those
	 * appearing in specified population.
	 * 
	 * LDFeature->VariationFeature->Variation.
	 * 
	 * @param population population filter
	 * @param includeAllVariations whether to include all variations
	 * @return zero or more variations derived from variation features.
	 */
	private List getVariations(Population population,
			boolean includeAllVariations) {

		PersistentSet vs = new PersistentSet();
		for (int i = 0, n = ldFeatures.length; i < n; i++) {
			if (includeAllVariations
					|| ldFeatures[i].getPopulation().sameInternalID(population)) {
				vs.add(ldFeatures[i].getVariationFeature1().getVariation());
				vs.add(ldFeatures[i].getVariationFeature2().getVariation());
			}
		}

		return vs.toList();
	}

	/**
	 * Variations associated with the population.
	 * 
	 * @return zero or more Variations associated with the population.
	 */
	public List getVariations(Population population) {
		return getVariations(population, false);
	}

	/**
	 * All Variations associated with the default population.
	 * 
	 * @return zero or more Variations associated with the default population.
	 * @see #getDefaultPopulation()
	 */
	public List getVariationsFromDefaultPopulation() {
		return getVariations(getDefaultPopulation(), false);
	}

}
