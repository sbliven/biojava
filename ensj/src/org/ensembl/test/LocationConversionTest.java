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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.Location;
import org.ensembl.driver.CoreDriver;
import org.ensembl.driver.CoreDriverFactory;
import org.ensembl.driver.LocationConverter;

/**
 * Test class for LocationConversion. Currently only test LocationConverterImpl
 * 
 * <b>Note: </b> These tests are specific to database content.
 */
public class LocationConversionTest extends CoreBase {

  private static Logger logger = Logger.getLogger(LocationConversionTest.class
      .getName());

  private LocationConverter locationConverter;

  public static void main(String[] args) throws Exception {
    LocationConversionTest t = new LocationConversionTest("");
    t.setUp();
  }

  public LocationConversionTest(String name) {
    super(name);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();
    //    suite.addTest(new LocationConversionTest("testResolveLocation"));
    //suite.addTest(new LocationConversionTest("testRoundTrip2"));
    //suite.addTest(new LocationConversionTest("testContigLocationOnDR51"));
    //suite.addTest(new LocationConversionTest("testGap"));
    //suite.addTest( new
    // LocationConversionTest("testCloneFragment_2_AssemblyLocation"));
    //suite.addTest( new
    // LocationConversionTest("testCloneFragmentSubLocation_2_AssemblyLocation"));
    //suite.addTest( new
    // LocationConversionTest("testRoundTripAssemblyLoc_2_CompositieCloneFragLoc_2_AssemblyLoc"));
    //suite.addTest( new LocationConversionTest("");

    suite.addTestSuite(LocationConversionTest.class);

    return suite;
  }

  protected void setUp() throws Exception {
    super.setUp();
    locationConverter = driver.getLocationConverter();

  }

  public void testRoundTrip() throws Exception {
    //  must be on assembly
    roundTrip(new Location(chromosomeCS, "22", 21000000, 21000100),
        chromosomeCS);
  }

  public void testRoundTrip2() throws Exception {
    //  must be on assembly
    roundTrip(new Location(contigCS, "D87018.1.1.38756", 17000, 17100),
        chromosomeCS);

  }

  public void testRoundTrip3() throws Exception {

    // must choose a contig that mapped 100% to contig. Otherwise if there are
    // gaps
    // we loose info when we convert conig-> chr which prevents correct
    // chr->contig
    // conversion afterwards.
    Location loc = new Location(contigCS, "AC008066.4.1.174347");
    //loc = driver.getLocationConverter().fetchComplete(loc);
    //assertNotNull(loc);
    Location loc2 = driver.getLocationConverter().convert(loc, chromosomeCS);
    Location loc3 = driver.getLocationConverter().convert(loc2, contigCS);

    // don't compare the length because unset in loc but IS set in loc3
    // because set in loc2.

    assertEquals(loc.getCoordinateSystem().getName(), loc3
        .getCoordinateSystem().getName());
    assertEquals(loc2.getLength(), loc3.getLength());
    assertEquals(loc.getSeqRegionName(), loc3.getSeqRegionName());

  }
  
  public void testRoundTripChainedMapping() throws Exception {

    // Location conversion between these CSs uses intermediate CS
    // e.g. "chromosome->clone" = "chromosome->contig->clone"
    
    roundTrip(new Location(chromosomeCS, "22", 21000000, 21000100),
        cloneCS);
    
    roundTrip(new Location(chromosomeCS, "22", 21000000, 21000100),
        superContigCS);
  }

  public void testGap() throws Exception {
    // this part of the contig is not in the assembly
    Location loc = new Location("contig:AL139403.7.1.111344:1-100");
    Location loc2 = driver.getLocationConverter().convert(loc, chromosomeCS);

    logger.fine("loc    : " + loc);
    logger.fine("loc2 : " + loc2);

    assertTrue(loc2.isGap());
    assertEquals(loc2.getCoordinateSystem().getName(), chromosomeCS.getName());
  }

  public void testResolveLocation() throws Exception {
    Location loc = new Location("contig:AL139403.7.1.111344:1-100");
    Location loc2 = driver.getLocationConverter().fetchComplete(loc);
    assertTrue(loc.compareTo(loc2) == 0);

    loc = new Location("contig:AL139403.7.1.111344:10");
    loc2 = driver.getLocationConverter().fetchComplete(loc);
    assertTrue(loc2.getStart() == 10);
    assertTrue(loc2.isEndSet());
    assertTrue(loc2.getEnd() > 0);

    loc = new Location("contig:AL139403.7.1.111344:-1000");
    loc2 = driver.getLocationConverter().fetchComplete(loc);
    assertTrue(loc2.isStartSet() && loc2.getStart() > 0);
    assertTrue(loc2.getEnd() == 1000);

    loc = new Location("chromosome");
    loc2 = driver.getLocationConverter().fetchComplete(loc);
    for (Location head = loc2; head != null; head = head.next()) {
      assertNotNull(head.getSeqRegionName());
    }

  }

  private void roundTrip(Location loc, CoordinateSystem intermediateCS)
  throws Exception {
    roundTrip(loc, intermediateCS, driver);
  }
  
    private void roundTrip(Location loc, CoordinateSystem intermediateCS, CoreDriver d) throws Exception {

    Location loc2 = d.getLocationConverter().convert(loc, intermediateCS);
    Location loc3 = d.getLocationConverter().convert(loc2,
        loc.getCoordinateSystem());
    loc3.mergeAdjacentNodes(); // incase intermediate location consists of >1 nodes
    
    logger.fine("loc    : " + loc);
    logger.fine("loc2 : " + loc2);
    logger.fine("loc3   : " + loc3);

    assertTrue(loc.compareTo(loc3) == 0);
  }

  public void testChromosomeToAllCoordinateSystems() throws Exception {

    Location l1 = new Location(chromosomeCS, "6", 1000000, 2000000, 1);
    Location l2 = locationConverter.convert(l1, contigCS, true, false, true);
    //  this is a chained conversion
    Location l3 = locationConverter.convert(l1, cloneCS, true, false, true);
    // this is a chained conversion
    Location l4 = locationConverter.convert(l1, superContigCS, true, false,
        true);

    // roundtrip conversion checks
    // only works because 6 is finished.
    Location l5 = locationConverter.convert(l2, chromosomeCS);
    assertEquals(l5.getStart(), 1000000);
    assertEquals(l5.getLength(), 1000001);
    assertEquals(l5.getStrand(), 1);
    assertEquals(l5.getSeqRegionName(), "6");

    l5 = locationConverter.convert(l3, chromosomeCS);
    assertEquals(l5.getStart(), 1000000);
    assertEquals(l5.getLength(), 1000001);
    assertEquals(l5.getStrand(), 1);
    assertEquals(l5.getSeqRegionName(), "6");

    l5 = locationConverter.convert(l4, chromosomeCS);
    assertEquals(l5.getStart(), 1000000);
    assertEquals(l5.getLength(), 1000001);
    assertEquals(l5.getStrand(), 1);
    assertEquals(l5.getSeqRegionName(), "6");

    l1 = new Location(chromosomeCS, "1", 1000000, 2000000, -1);
    l2 = locationConverter.convert(l1, contigCS, true, false, false);
    l3 = locationConverter.convert(l1, cloneCS, true, false, false);
    l4 = locationConverter.convert(l1, superContigCS, true, false, false);
  }

  public void testBuildIdList() throws Exception {
    String chrName = "1";
    Location l1 = new Location(chromosomeCS, chrName, 10000000, 10100000, 1);
    Location l2 = locationConverter.convert(l1, contigCS, true, false, false);

    long[] ids = locationConverter.locationToIds(l2);
    assertNotNull(ids);
    assertTrue(ids.length > 0);

    // build a location from an Id (start and end need to be inside part of contig
    // mapped to chr)
    Location l3 = locationConverter.idToLocation(ids[0], 90100, 90110, 1);
    assertNotNull(l3);
    assertTrue(l3.getLength() == 11);
    // back to chromosome
    Location l4 = locationConverter.convert(l3, chromosomeCS);
    assertEquals(chrName, l4.getSeqRegionName());
  }

  public void testDereference() throws Exception {
    Location l1 = new Location(chromosomeCS, "DR51", 20000000, 40000000, 1);
    Location l2 = locationConverter.dereference(l1);
    assertNotNull(l2.toString());
  }

  public void testConvertToTopLevel() throws Exception {
    Location l1 = new Location("contig:AL713966.7.1.124047");
    Location l2 = locationConverter.convertToTopLevel(l1);
    assertEquals(l2.getCoordinateSystem().getName(), "chromosome");
  }

  public void testSupportForReusedComponentSequenceRegionsInMapper()
      throws Exception {

    // At the time of writing mapping 1 component_seq_region to N
    // assembly_seq_regions
    // is only used in the rat database.

    //    find contigs that map to >1 regions on chr
    //    select cmp_seq_region_id, count(cmp_seq_region_id) as n from
    // rattus_norvegicus_core_30_34.assembly group by cmp_seq_region_id having
    // n>1;
    //
    //    list chr regions that map to contigs which appear >1 times
    //    select * from rattus_norvegicus_core_30_34.assembly,
    //    rattus_norvegicus_core_30_34.seq_region where cmp_seq_region_id in
    //    (139865,145760,147892,149184,149541,151688,154916,155198,155489,163030,166204,169441,177779,178919,182322,182336,182364,192001,194146,200349,203709,208567,216037,217421,219799,220482,221013,226618,230678,236048,240022,246885)
    //    and asm_seq_region_id=seq_region_id;

    CoreDriver rd = CoreDriverFactory
        .createCoreDriver("resources/data/unit_test_rat.properties");
    Location l = new Location("chromosome:x:24573175-24595381");

    Location l2 = rd.getLocationConverter().convert(l,
        new CoordinateSystem("contig"));
    
    assertEquals(l.getLength(), l2.getLength());
    
    // ensure we are using a contig which maps to multiple parts of the
    // chromosome otherwise this test is checking an inappropriate region.
    boolean duplicateFound = false;
    Set srs = new HashSet();
    for (Location node = l2; !duplicateFound && node != null; node = node.next()) {
      String sr = node.getSeqRegionName();
      assertNotNull(
          "Converted location has a null sequence region: Invalid test location ("
              + l + ") or mapping system broken", sr);
      duplicateFound = !srs.add(sr);
    }
    assertTrue("Location " + l + " only maps to each component once",
        duplicateFound);

    
    String s = rd.getSequenceAdaptor().fetch(l).getString();
    assertEquals("Returned sequence is the wrong length", l.getLength(), s
        .length());

    assertTrue("Returned sequence has big gaps", s.indexOf("NNNNNNNNNNNN") == -1);

    roundTrip(rd.getLocationConverter().fetchComplete(l), new CoordinateSystem("contig"), rd);
    
  }

  public void testConversionWithManyPartsOfContigToChromosomeMapping() throws Exception {
    // In some species, such as zebrafish since version 32, some contigs are mapped to to 
    // discontinuous parts of chromosomes.
    // This simple test ensures all the intervening stages of the mapping process work correctly.
    CoreDriver d = registry.getGroup("zebrafish").getCoreDriver();
    List genes = d.getGeneAdaptor().fetch(new Location("chromosome:1:1m-2m"));
    assertTrue(genes.size()>0);
    
  }
  
} // LocationConversionTest
