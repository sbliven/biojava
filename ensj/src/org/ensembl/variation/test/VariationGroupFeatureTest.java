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

import org.ensembl.datamodel.Location;
import org.ensembl.variation.datamodel.VariationGroupFeature;

/**
 * Tests the variation group feature support.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 */
public class VariationGroupFeatureTest extends VariationBase {

	public VariationGroupFeatureTest(String name) throws Exception {
		super(name);
	}

	public void testFetchByID() throws Exception {
		final long ID = 1;
		VariationGroupFeature vgf = vdriver.getVariationGroupFeatureAdaptor().fetch(ID);
		assertEquals(ID, vgf.getInternalID());
		check(vgf);
	}

	public void testFetchByName() throws Exception {
		Location loc = new Location("chromosome:21:28622831-28622831");
		List is = vdriver.getVariationGroupFeatureAdaptor().fetch(loc);
	}

	/**
	 * Checks that all the attributes in all the Individual are set.
	 * 
	 * @param individual
	 *            individual to check
	 */
	private void check(List individuals) {
		assertTrue(individuals.size() > 0);
		for (int i = 0, n = individuals.size(); i < n; i++)
			check((VariationGroupFeature) individuals.get(i));

	}

	/**
	 * Checks that all the attributes are available and that
	 * associated items make sense.
	 * 
	 * @param vgf variation group feature
	 */
	private void check(VariationGroupFeature vgf) {

		assertTrue(vgf.getInternalID() > 0);
		assertNotNull(vgf.getLocation());
		assertNotNull(vgf.getVariationGroup());
		assertEquals(vgf.getVariationGroupName(), vgf.getVariationGroup().getName());
		
	}
}
