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

package org.ensembl.variation.test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ensembl.datamodel.Location;
import org.ensembl.util.FrequencyCounter;
import org.ensembl.util.IDSet;
import org.ensembl.variation.datamodel.LDFeature;
import org.ensembl.variation.datamodel.LDFeatureContainer;
import org.ensembl.variation.datamodel.Population;
import org.ensembl.variation.datamodel.Variation;
import org.ensembl.variation.datamodel.VariationFeature;
import org.ensembl.variation.datamodel.impl.PopulationImpl;
import org.ensembl.variation.datamodel.impl.VariationFeatureImpl;

/**
 * Tests the LDFeature support.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 */
public class LDFeatureTest extends VariationBase {

	public LDFeatureTest(String name) throws Exception {
		super(name);
	}

	public void testBothFetchByLocation() throws Exception {

		//Location location = new Location("chromosome:7:27686081-27686081");
	  Location location = new Location("chromosome:7:1m-1m");

		List ldfs = vdriver.getLDFeatureAdaptor().fetch(location);

		LDFeatureContainer ldfc = vdriver.getLDFeatureAdaptor()
				.fetchLDFeatureContainer(location);

		assertSame(ldfs, ldfc);

		assertTrue(ldfc.getPopulations().size() > 0);
		assertNotNull(ldfc.getDefaultPopulation());
		assertTrue(ldfc.getLDFeaturesFromDefaultPopulation().size() > 0);

		// filter check: no ldfeatures should have
		// variation features with these ids.
		assertEquals(0, ldfc.getPopulations(new VariationFeatureImpl(10000),
				new VariationFeatureImpl(10001)).size());

		// filter check: no variations relate to population 10000
		// in db.
		assertEquals(0, ldfc.getVariations(new PopulationImpl(10000)).size());
		
		LDFeature f = (LDFeature) ldfc.getLDFeaturesFromDefaultPopulation().get(0);
		assertEquals(ldfc.getDefaultPopulation().getInternalID(), 
				f.getPopulation().getInternalID());
	}

	public void testBothFetchByVariationFeature() throws Exception {

		VariationFeature variationFeature = vdriver
				.getVariationFeatureAdaptor().fetch(10012468);
		assertNotNull(variationFeature);
		
		List ldfs = vdriver.getLDFeatureAdaptor().fetch(variationFeature);
		assertTrue(ldfs.size()>0);
		
		LDFeatureContainer ldfc = vdriver.getLDFeatureAdaptor()
				.fetchLDFeatureContainer(variationFeature);
		assertTrue(ldfc.getLDFeatures().size()>0);
		
		assertSame(ldfs, ldfc);
	}

	/**
	 * Compares the contents of the list against that in the container, should
	 * be the same.
	 * 
	 * @param ldfs
	 *            list of LDFeatures.
	 * @param ldfc
	 *            container of LDFeatures.
	 */
	private void assertSame(List ldfs, LDFeatureContainer ldfc) {
		assertTrue(ldfs.size() > 0);
		assertEquals(ldfs.size(), ldfc.getLDFeatures().size());

		// Check the values are set on the LDFeatures
		LDFeature lf = (LDFeature) ldfs.get(0);
		LDFeature cf = (LDFeature) ldfc.getLDFeatures().get(0);
		assertTrue(lf.getLocation().compareTo(cf.getLocation()) == 0);
		assertTrue(lf.getDPrime() != 0);
		assertEquals(lf.getDPrime(), cf.getDPrime(), 0);
		assertTrue(lf.getRSquare() != 0);
		assertEquals(lf.getRSquare(), cf.getRSquare(), 0);

//		// Compare populations
		Set listPops = new HashSet();
		for (int i = 0, n = ldfs.size(); i < n; i++)
			listPops.add(((LDFeature) ldfs.get(i)).getPopulation());
		Set containerPops = new HashSet(ldfc.getPopulations());
		assertEquals(listPops, containerPops);

		// compare variations.
		Set listVariations = new HashSet();
		for (int i = 0, n = ldfs.size(); i < n; i++) {

		  LDFeature ldf = (LDFeature) ldfs.get(i);
			
		  VariationFeature vf1 = ldf.getVariationFeature1();
			Variation v1 = vf1.getVariation();
			listVariations.add(v1);
			
			VariationFeature vf2 = ldf.getVariationFeature2();
			Variation v2 = vf2.getVariation();
			listVariations.add(v2);
		}
		Set containerVariations = new HashSet(ldfc.getVariations());
		// convert variations into sets containing their internal
		// ids for easy comparison.
		assertEquals(new IDSet(listVariations), new IDSet(containerVariations));

		// compare default populations
		FrequencyCounter pop2count = new FrequencyCounter();
		for (int i = 0, n = ldfs.size(); i < n; i++) {
			LDFeature ldf = (LDFeature) ldfs.get(i);
			pop2count.addOrIncrement(ldf.getPopulation());
		}
		Population p = (Population) pop2count.getMostFrequent();
		assertTrue(ldfc.getDefaultPopulation().sameInternalID(p));
	}

	
	public static void main(String[] args) {
    junit.textui.TestRunner.run(LDFeatureTest.class);
  }
}
