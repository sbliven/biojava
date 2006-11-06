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
import org.ensembl.variation.datamodel.Individual;
import org.ensembl.variation.datamodel.IndividualGenotype;

/**
 * Tests IndividualGenotype support.
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public class IndividualGenotypeTest extends VariationBase {

	public IndividualGenotypeTest(String name) throws Exception {
		super(name);
	}

	public void testFetchByPopulation() throws AdaptorException {
    final long ID = 1744;
    Individual individual = vdriver.getIndividualAdaptor().fetch(ID); 
    List l = vdriver.getIndividualGenotypeAdaptor().fetch(individual);
    check(l);
    assertEquals(ID, ((IndividualGenotype)l.get(0)).getIndividual().getInternalID());
  }
	
	private void check(List l) {
		for (int i = 0, n = l.size(); i < n; i++) 
			check((IndividualGenotype) l.get(i));
	}

	/**
	 * Checks that all the fields are set with valid values.
	 */
	private void check(IndividualGenotype ig) {
		
		assertNotNull(ig.getAllele1());
		assertTrue(ig.getAllele1().length()>0);
		assertNotNull(ig.getAllele2());
		assertTrue(ig.getAllele2().length()>0);
		
		assertNotNull(ig.getIndividual());
		
	}

}
