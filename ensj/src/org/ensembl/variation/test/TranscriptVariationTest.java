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

import org.ensembl.datamodel.Transcript;
import org.ensembl.driver.AdaptorException;
import org.ensembl.variation.datamodel.TranscriptVariation;
import org.ensembl.variation.datamodel.VariationFeature;

/**
 * Tests transcript variation support.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 */
public class TranscriptVariationTest extends VariationBase {

	public TranscriptVariationTest(String name) throws Exception {
		super(name);
	}

	public void testFetchByID() throws AdaptorException {
		final long ID = 90047; // must choose one with a transcript in core db!
		TranscriptVariation tv = vdriver.getTranscriptVariationAdaptor().fetch(
				ID);
		check(tv);
		assertEquals(ID, tv.getInternalID());
	}

	public void testFetchByVariationFeature() throws AdaptorException {
		// choose one that hits a transcript
    // Simple check: consequence_type=='NON_SYNONYMOUS_CODING','SYNONYMOUS_CODING'
		final long ID = 4300559; 
		VariationFeature vf = vdriver.getVariationFeatureAdaptor().fetch(ID);
    assertNotNull(vf);
		List l = vdriver.getTranscriptVariationAdaptor().fetch(vf);
		check(l);
		assertTrue("Test requires a VariationFeature with VariationTranscripts", l.size()>0); // can only do next test if VariationFeature has TranscriptVariations 
		assertEquals(ID, ((TranscriptVariation) l.get(0)).getVariationFeature()
				.getInternalID());
	}

	public void testFetchByTranscript() throws AdaptorException {
		final long ID = 3675; // choose one that has all the relevant entries in the core and variation dbs 
		Transcript t = vdriver.getCoreDriver().getTranscriptAdaptor().fetch(ID);
		List l = vdriver.getTranscriptVariationAdaptor().fetch(t);
		check(l);
		assertEquals(ID, ((TranscriptVariation) l.get(0)).getTranscript()
				.getInternalID());
	}

	private void check(List l) {
		for (int i = 0, n = l.size(); i < n; i++)
			check((TranscriptVariation) l.get(i));
	}

	/**
	 * Checks that all the fields are set with valid values.
	 */
	private void check(TranscriptVariation tv) {

		assertTrue(tv.getInternalID() > 0);
		assertNotNull(tv.getConsequenceType());
		assertNotNull("TranscriptVariation ("+tv.getInternalID()+") has no transcript", tv.getTranscript());
		assertNotNull(tv.getVariationFeature());
		
		// can't check overlap because core and variation test dbs out of sync
		//		assertTrue(tv.getTranscript().getLocation().transform(-10000,10000).overlaps(
		//				tv.getVariationFeature().getLocation()));
		
		//	 these can be 0 if the relationship is upstream or downstram of the
		// transcript
		//		assertTrue(tv.getCDNAstart()>0);
		//		assertTrue(tv.getCDNAend()>0);
		//		assertTrue(tv.getTranslationStart()>0);
		//		assertTrue(tv.getTranslationEnd()>0);

	}

}
