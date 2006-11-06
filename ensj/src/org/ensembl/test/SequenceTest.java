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
package org.ensembl.test;

import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ensembl.datamodel.CloneFragmentLocation;
import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.Exon;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Sequence;
import org.ensembl.datamodel.impl.SequenceImpl;
import org.ensembl.driver.ExonAdaptor;
import org.ensembl.driver.LocationConverter;
import org.ensembl.driver.SequenceAdaptor;
import org.ensembl.util.SequenceUtil;

/**
 * Test class for Sequence related classes.
 *
 * <b>Note:</b> These tests are specific to database content.
 */
public class SequenceTest extends CoreBase {

	private static Logger logger = Logger.getLogger(SequenceTest.class.getName());

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	public SequenceTest(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		//suite.addTest( new SequenceTest("testShortAssemblyLocSequences"));
		suite.addTestSuite(SequenceTest.class);
		return suite;
	}

	protected void setUp() throws Exception {
	  
	  super.setUp();

		//shortCloneFragLocation = new CloneFragmentLocation(10057, 10, 20, 1);
		//longCloneFragLocation = new CloneFragmentLocation(10057, 1, 15476, 1);

		shortAssemblyLocation = new Location(new CoordinateSystem("chromosome"), "22", 22524231, 22524272, 1);
		longAssemblyLocation = new Location(new CoordinateSystem("chromosome"), "12", 14000000, 15000000, 1);
		sequenceAdaptor = (SequenceAdaptor)driver.getAdaptor("sequence");

	}

	public void testReverseComplement() throws Exception {
		String original = "ACTTAGCAAGT";
		String reverse = SequenceUtil.reverseComplement(original);
		assertTrue("Reversed should be different", !original.equals(reverse));
		String reverse2 = SequenceUtil.reverseComplement(reverse);
		assertEquals("Reverse complement is broken", original, reverse2);
	}

	public void testSequence() throws Exception {
		String shortSeq = "ctga";
		Sequence seq = new SequenceImpl();
		seq.setString(shortSeq);
		assertEquals("Sequence not set", shortSeq, seq.getString());

		/*
				Location[] locs =
					{
						new Location("chromosome:3:10-13:1"),
						new Location("chromosoms:3:10-13:-1"),
						CloneFragmentLocation.valueOf("3:110-113:1"),
						CloneFragmentLocation.valueOf("3:110-113:-1")};
		*/
		Location[] locs = { new Location("chromosome:3:10-13:1"), new Location("chromosome:3:10-13:-1")};

		for (int i = 0; i < locs.length; ++i) {
			seq.setLocation(locs[i]);
			Sequence result = seq.subSequence(2, 3);
			assertEquals("subSequence() failed.", "tg", result.getString());
			assertNotNull(seq.getLocation());
			assertNotNull(result.getLocation());
			assertEquals(
				"subSequence() failed; before" + seq.getLocation() + " after " + result.getLocation(),
				2,
				result.getLocation().getLength());

			result = seq.subSequence(1, 4);
			assertEquals("subSequence() failed.", "ctga", result.getString());
			assertEquals("subSequence() failed.", 4, result.getLocation().getLength());

			result = seq.subSequence(1, 1);
			assertEquals("subSequence() failed.", "c", result.getString());
			assertEquals("subSequence() failed.", 1, result.getLocation().getLength());

			result = seq.subSequence(4, 4);
			assertEquals("subSequence() failed.", "a", result.getString());
			assertEquals("subSequence() failed.", 1, result.getLocation().getLength());
		}
	}

	/**
	 * Check that sequence is the same after being through the location converter
	 * @throws Exception
	 */
	public void testLocationConverterSequence() throws Exception {

		String correctSeq = "ATGGACTGGAATTGGAGGATCCTGTTTTTGGTGGTCATAGCTGCGG";
		LocationConverter locationConverter = driver.getLocationConverter();

		ExonAdaptor exonAdaptor = driver.getExonAdaptor();

		Exon exon = exonAdaptor.fetch("ENSE00001410410");
		assertEquals("Exon sequence is not what was expected", exon.getSequence().getString(), correctSeq);

		Location originalExonLoc = exon.getLocation();
		String rawLocSeq = sequenceAdaptor.fetch(originalExonLoc).getString();
		assertEquals("Sequence from ExonAdaptor not the same as from SequenceAdapor", rawLocSeq, correctSeq);

		Location contigLoc = locationConverter.convert(originalExonLoc, new CoordinateSystem("contig"));
		String contigLocSeq = sequenceAdaptor.fetch(contigLoc).getString();
		assertEquals("Sequence changed after being through LocationConverter", contigLocSeq, correctSeq);

		Location cloneLoc = locationConverter.convert(originalExonLoc, new CoordinateSystem("clone"));
		String cloneLocSeq = sequenceAdaptor.fetch(contigLoc).getString();
		assertEquals("Sequence changed after being through LocationConverter", cloneLocSeq, correctSeq);

		Location chrLoc = locationConverter.convert(originalExonLoc, new CoordinateSystem("chromosome"));
		String chrLocSeq = sequenceAdaptor.fetch(contigLoc).getString();
		assertEquals("Sequence changed after being through LocationConverter", chrLocSeq, correctSeq);

	}

	/**
	 * Check that a particular exon's sequence is correct.
	 * The exon accession and sequence used here are OK for NCBI34 but may not always be
	 * 
	 * @throws Exception
	 */
	public void testKnownSequence() throws Exception {

		ExonAdaptor exonAdaptor = driver.getExonAdaptor();

		Exon exon = exonAdaptor.fetch("ENSE00001371535");
		assertEquals(
			"Exon sequence is not what was expected",
			exon.getSequence().getString(),
			"GTGCCCAGTCCCAGGTACAGCTGGTGCAGTCTGGGGCTGAGGTGAAGAAGCCTGGGGCCTCAGTGAAGGTCTCCTGCAAGGCTTCTGGATACACCATCACCAGCTACTGTATGCACTGGGTGCACCAGGTCCATGCACAAGGGCTTGAGTGGATGGGATTGGTGTGCCCTAGTGATGGCAGCACAAGCTATGCACAGAAGTTCCAGGCCAGAGTCACCATAACCAGGGACACATCCATGAGCACAGCCTACATGGAGCTAAGCAGTCTGAGATCTGAGGACACGGCCATGTATTACTGTGTGAGAGACACAATGTGA");

	}

	//public void testShortCloneFragLocSequences() throws Exception {
	//	checkSequenceFromLocationAndComplement(shortCloneFragLocation);
	//}

	//public void testLongCloneFragLocSequences() throws Exception {
	//	checkSequenceFromLocationAndComplement(longCloneFragLocation);
	//}

	public void testShortAssemblyLocSequences() throws Exception {
		checkSequenceFromLocationAndComplement(shortAssemblyLocation);
	}

	public void testLongAssemblyLocSequences() throws Exception {
		checkSequenceFromLocationAndComplement(longAssemblyLocation);
	}

	public void testAssemblyLocWhereSomeSequenceMissing() throws Exception {

		Location location = new Location(new CoordinateSystem("chromosome"), "20", 25000, 750000, 1);
		Sequence seq = sequenceAdaptor.fetch(location);
		assertTrue(
			"Returned sequence should be same length as location",
			location.getLength() == seq.getString().length());
	}


  public void testCompositeLocationsToSequence() throws Exception {
    Location l1 = new Location("chromosome:1:5-10");
    Location l2 = new Location("chromosome:1:25-30");
    l1.append(l2);
    assertNotNull("Failed to get sequence for location: " + l1, sequenceAdaptor.fetch(l1));
    
  }

	private void checkSequencesComplementary(Sequence seq, Sequence seq2) {
		assertTrue("Complementing sequence has changed its length!", seq.getString().length() == seq2.getString().length());

		assertTrue(
			"Complementing sequence has not changed it " + "(ignore if all bases are N) : " + seq,
			!seq.getString().equals(seq2.getString()));
	}

	private void checkSequenceInstance(Location location, Sequence seq) {
		assertNotNull("Failed to retrieve sequence for " + location, seq);
		String retrievedSequenceStr = seq.getString();
		int expectedLength = location.getEnd() - location.getStart() + 1;
		int actualLength = retrievedSequenceStr.length();
		assertTrue(
			"Retrieved Sequence is wrong length; "
				+ "expected "
				+ expectedLength
				+ ", found "
				+ actualLength
				+ ". This might reflect gap(s) in the assembly.",
			actualLength == expectedLength);
		if (logger.isLoggable(Level.FINE))
			logger.fine("dna " + seq);

	}

	private void checkSequenceFromLocationAndComplement(Location location) throws Exception {

		Sequence seq = sequenceAdaptor.fetch(location);
		System.out.println("Got sequence of length " + seq.getString().length() + " from location of length " + location.getLength());
		assertEquals("Returned sequence is wrong length", location.getLength(), seq.getString().length());

		checkSequenceInstance(location, seq);

		Location compLoc = location.complement();

		Sequence seq2 = sequenceAdaptor.fetch(compLoc);
		checkSequenceInstance(compLoc, seq2);

		checkSequencesComplementary(seq, seq2);

	}

	
	
	public void testUnstrandedBehaviour() throws Exception {
	  
	  String unstranded = sequenceAdaptor.fetch(new Location("chromosome:1:1-10")).getString();
	  String positive =  sequenceAdaptor.fetch(new Location("chromosome:1:1-10:1")).getString();
	  String negative =  sequenceAdaptor.fetch(new Location("chromosome:1:1-10:-1")).getString();
	  
	  assertEquals(unstranded,positive);
	  // just check this as well for completeness!
	  assertEquals(unstranded,SequenceUtil.reverseComplement(negative));
	  
	}
	
	
	private CloneFragmentLocation shortCloneFragLocation;
	private CloneFragmentLocation longCloneFragLocation;

	private Location shortAssemblyLocation;
	private Location longAssemblyLocation;

	private SequenceAdaptor sequenceAdaptor;

} // SequenceTest
