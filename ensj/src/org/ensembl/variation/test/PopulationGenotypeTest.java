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

import java.util.List;

import org.ensembl.driver.AdaptorException;
import org.ensembl.variation.datamodel.Population;
import org.ensembl.variation.datamodel.PopulationGenotype;

/**
 * Tests PopulationGenotype support.
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public class PopulationGenotypeTest extends VariationBase {

	public PopulationGenotypeTest(String name) throws Exception {
		super(name);
	}

	public void testFetchByID() throws AdaptorException {
	    final long ID = 1;
	    PopulationGenotype p = vdriver.getPopulationGenotypeAdaptor().fetch(ID);
	    check(p);
	    assertEquals(ID, p.getInternalID());
	  }

	public void testFetchByPopulation() throws AdaptorException {
	  // choose a population with relatively few rows in the population_genotype
	  // table otherwise it will be very slow and we might run out memory
    final long ID = 831;
    Population population = vdriver.getPopulationAdaptor().fetch(ID); 
    List pgs = vdriver.getPopulationGenotypeAdaptor().fetch(population);
    check(pgs);
    assertEquals(ID, ((PopulationGenotype)pgs.get(0)).getPopulation().getInternalID());
  }
	
	private void check(List pgs) {
		for (int i = 0, n = pgs.size(); i < n; i++) 
			check((PopulationGenotype) pgs.get(i));
	}

	/**
	 * Checks that all the fields are set with valid values.
	 */
	private void check(PopulationGenotype p) {
		
		assertTrue(p.getInternalID()>0);
		assertNotNull(p.getAllele1());
		assertTrue(p.getAllele1().length()>0);
		assertNotNull(p.getAllele2());
		assertTrue(p.getAllele2().length()>0);
		
		assertTrue(p.getFrequency()>0);
		assertNotNull(p.getPopulation());
		
	}

}
