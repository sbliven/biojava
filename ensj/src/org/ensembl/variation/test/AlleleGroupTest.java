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
package org.ensembl.variation.test;

import java.util.Iterator;
import java.util.List;

import org.ensembl.driver.AdaptorException;
import org.ensembl.variation.datamodel.Allele;
import org.ensembl.variation.datamodel.AlleleGroup;
import org.ensembl.variation.datamodel.Variation;
import org.ensembl.variation.datamodel.VariationGroup;

/**
 * Tests the Allele group adaptor.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 *  
 */
public class AlleleGroupTest extends VariationBase {

	public AlleleGroupTest(String name) throws Exception {
		super(name);
	}

	public void testFetchByID() throws AdaptorException {
		final long ID = 434;
		AlleleGroup ag = vdriver.getAlleleGroupAdaptor().fetch(ID);
		check(ag, true);
	}

	public void testFetchByName() throws AdaptorException {
		final String NAME = "ABDR-20";
		AlleleGroup ag = vdriver.getAlleleGroupAdaptor().fetch(NAME);
		check(ag,false); // might not have alleles in test db
	}

	public void testFetchByVariationGroup() throws AdaptorException {
		final long ID = 1574;
		VariationGroup vg = vdriver.getVariationGroupAdaptor().fetch(ID);
		List ags = vdriver.getAlleleGroupAdaptor().fetch(vg);
		assertTrue(ags.size() > 0);
		for (int i = 0, n = ags.size(); i < n; i++)
			check((AlleleGroup) ags.get(i), false);
	}

		/**
		 * Check the allele group has all it's fields set.
		 * @param ag allele group to check
		 * @param expectAlleles whether or not the allele group should have
		 * alleles. Prints warning if they do. This overcomes a possible
		 * data bug in the test db. 
		 */
	private void check(AlleleGroup ag, boolean expectAlleles) {
		assertNotNull(ag);
		assertTrue(ag.getInternalID() > 0);
		assertTrue(ag.getName().length() > 0);
		assertTrue(ag.getSource().length() > 0);
		List as = ag.getAlleles();
		List vs = ag.getVariations();
		assertEquals(as.size(), vs.size());
		// allele group not always have alleles?
		if (as.size() > 0 ) {
		  
			for (int i = 0; i < as.size(); i++) {
				String a = ((String) as.get(i)).toLowerCase();
				Variation v = (Variation) vs.get(i);
				List vAlleles = v.getAlleles();
				boolean found = false;
				for (Iterator iter = vAlleles.iterator(); iter.hasNext();) {
					Allele vallele = (Allele) iter.next();
					if (vallele.getAlleleString().toLowerCase().equals(a)) found=true;
//					System.out.println(a + " (" + vallele.getAlleleString()
//					    + ")\t ag = " + ag.getInternalID() + "\tv = " + v.getInternalID() + "\t"+found);
				} 
				if (!found) 
				  System.err.println("WARNING: data inconsistency: allele '"+a
				      +"' from AlleleGroup("+ag.getInternalID()+") does not appear in Variation("
				      +v.getInternalID()+")");
			}
			
		} else if (expectAlleles) {
			System.err.println("WARNING allelegroup does not contain any alleles!" + ag);
		}

	}

}
