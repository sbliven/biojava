/*
 * Copyright (C) 2002 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.test;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.Exon;
import org.ensembl.datamodel.Gene;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Transcript;
import org.ensembl.driver.impl.ExonAdaptorImpl;
import org.ensembl.util.StringUtil;

/**
 * JUnit tests for ExonAdaptor / ExonAdaptorImpl
 */
public class ExonAdaptorTest extends CoreBase {

	private static final Logger logger = Logger.getLogger(ExonAdaptorTest.class
			.getName());

	private ExonAdaptorImpl exonAdaptor = null;

	private long EXON_ID = 267281;

	private Location chrLoc1;

	// -----------------------------------------------------------------

	public ExonAdaptorTest(String arg0) {
		super(arg0);

	}

	// -----------------------------------------------------------------

	public static void main(String[] args) {
		junit.textui.TestRunner.run(ExonAdaptorTest.class);
	}

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
	  super.setUp();
		exonAdaptor = (ExonAdaptorImpl) driver.getExonAdaptor();
		chrLoc1 = new Location(chromosomeCS, "20", 30249935,
				312546401, 1);
	}

	// -----------------------------------------------------------------

	public void testFetchByInternalID() throws Exception {

		Exon exon = exonAdaptor.fetch(EXON_ID);
		assertNotNull(exon);
	}

	// -----------------------------------------------------------------

	public void testFetchByAccessionID() throws Exception {

		Exon exon = exonAdaptor.fetch("ENSE00000972417");
		assertNotNull(exon);
	}

	// -----------------------------------------------------------------

	public void testFetchAllByLocation() throws Exception {

		List l = exonAdaptor.fetch(chrLoc1);
		assertNotNull(l);
		assertTrue(l.size() > 0);

	}

	// -----------------------------------------------------------------

	public void testFetchByTranscript() throws Exception {

		// by ID first
		long tID = 1;
		List l = exonAdaptor.fetchAllByTranscript(tID);
		assertNotNull(l);
		assertTrue(l.size() > 0);

		Exon e = (Exon) l.get(0);
		assertNotNull(e);

		boolean found = false;
		List ts = e.getTranscripts();
		assertTrue(ts.size() > 0);
		for (int i = 0, n = ts.size(); i < n; i++) {
			Transcript t = (Transcript) ts.get(i);
			if (t.getInternalID() == tID)
				found = true;

		}
		assertTrue("Transcript parent not set on exon.", found);

	}

	// ---------------------------------------------------------------------

	public void testCanLazyLoadGeneAndTranscriptsFromInsideAnIntron()
			throws Exception {

		String geneID = "ENSG00000077809";
		String exonID = "ENSE00001147855";

		// Check geneID contains exonID when load gene -> exon.
		Gene gene = driver.getGeneAdaptor().fetch(geneID);
		boolean exonInGene = false;
		for (Iterator iter = gene.getExons().iterator(); !exonInGene
				&& iter.hasNext();) {
			Exon e = (Exon) iter.next();
			if (e.getAccessionID().equals(exonID))
				exonInGene = true;
		}
		assertTrue(exonInGene);

		// Try to load exon -> gene and check that the loading gives the same
		// results.
		Exon exon = exonAdaptor.fetch(exonID);
		assertNotNull(exon.getGene());
		assertEquals(geneID, exon.getGene().getAccessionID());
		assertNotNull(exon.getTranscripts());
		assertEquals(geneID, ((Transcript) exon.getTranscripts().get(0))
				.getGene().getAccessionID());
	}

	/**
	 * 
	 * @deprecated since version 27.0. Remanins in code to test deprecated 
	 * adaptor methods.
	 */
	public void testExonTranscriptLazyLoading() throws Exception {

		org.ensembl.datamodel.Query q = new org.ensembl.datamodel.Query();
		q.setInternalID(EXON_ID);

		List allExons = exonAdaptor.fetch(q);
		Exon exon = (Exon) allExons.get(0);
		logger.fine("Loaded exon = " + StringUtil.formatForPrinting(exon));
		logger.fine("Loaded exon " + exon.getAccessionID());
		assertNotNull(exon);

		Gene gene = exon.getGene();
		assertNotNull(gene);
		logger.fine("Lazy loaded gene = " + StringUtil.formatForPrinting(gene));
		logger.fine("##Lazy loaded gene " + gene.getAccessionID()
				+ " for exon " + exon.getAccessionID());
		List exons = gene.getExons();
		// System.out.println("Exons = " + StringUtil.formatForPrinting(exons));
		assertTrue("Failed to set exon reference in gene", exons.contains(exon));

		Transcript transcript = (Transcript) exon.getTranscripts().get(0);
		assertTrue("Failed to set exon reference in transcript", transcript
				.getExons().contains(exon));

		// logger.fine("exons:"+ format(gene.getExons()));
		// logger.fine("transcripts:"+ format(gene.getTranscripts()));
	}

	public void testFetchFromUnlikelyAssembly() throws Exception {

		Location loc = new Location(new CoordinateSystem(
				UNLIKELY_ASSEMBLY_MAP_NAME), "12", 1, 999660, 0);
		List exons = exonAdaptor.fetch(loc);
		assertTrue("Exons loaded but none should have been", exons.size() == 0);

	}
}
