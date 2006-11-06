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

import java.text.ParseException;
import java.util.Arrays;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.Location;

/**
 * Test class for Locations.
 */
public class LocationTest extends CoreBase {

  private static Logger logger = Logger.getLogger(LocationTest.class.getName());

  public static final void main(String[] args) throws Exception {
    junit.textui.TestRunner.run(suite());
  }

  public LocationTest(String name) {
    super(name);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(LocationTest.class);
    // suite.addTest( new LocationTest("testResize") );
    return suite;
  }

  public void testComplement() throws Exception {

    Location al = new Location("chromosome:1:10-20:1");
    Location al2 = al.complement();

    assertEquals(al.getSeqRegionName(), al2.getSeqRegionName());
    assertEquals(al.getCoordinateSystem(), al2.getCoordinateSystem());
    assertEquals(al.getStart(), al2.getStart());
    assertEquals(al.getEnd(), al2.getEnd());
    assertTrue(al.getStrand() != al2.getStrand());

    al.append(new Location("chromosome:1:20-30"));
    al.append(new Location("chromosome:1:40-50"));
    al.append(new Location("chromosome:1:60-70"));

    logger.fine("Before reverse:" + al);

    int sizeB4 = al.size();
    int lenB4 = al.getLength();

    Location prev = al.complement();

    logger.fine("After reverse: " + prev);

    assertEquals("Num elements wrong", sizeB4, prev.size());
    assertEquals("length wrong", lenB4, prev.getLength());

    Location nxt = prev.next();
    while (nxt != null) {
      assertTrue("order wrong after sort: " + al, prev.getStart() > nxt
          .getStart());
      prev = nxt;
      nxt = nxt.next();
    }
  }

  public void testTransform() throws Exception {

    Location loc, resized;
    loc = new Location("chromosome:1:10-20");
    resized = loc.transform(1, 2);
    assertEquals(11, resized.getStart());
    assertEquals(22, resized.getEnd());

    loc = new Location("chromosome:1:10-20:-1");
    resized = loc.transform(1, 2);
    assertEquals(8, resized.getStart());
    assertEquals(19, resized.getEnd());

    loc = new Location("chromosome:1:10-20:1").append(
        new Location("chromosome:1:50-60:1")).append(
        new Location("chromosome:1:70-80:1"));
    resized = loc.transform(15, 0);
    assertEquals(2, resized.size());
    assertEquals(54, resized.getStart());
    assertEquals(60, resized.getEnd());
    assertEquals(70, resized.last().getStart());
    assertEquals(80, resized.last().getEnd());

    // shrink list by 24 bases, leave some in the
    // middle node and crop the first and last completely.
    loc = new Location("chromosome:5:80-90:-1").append(
        new Location("chromosome:5:50-70:-1")).append(
        new Location("chromosome:5:10-20:-1"));
    resized = loc.transform(14, -13);
    assertEquals(1, resized.size());
    assertEquals(52, resized.getStart());
    assertEquals(67, resized.getEnd());

    // Stretch node
    loc = new Location("chromosome:1:10-20");
    resized = loc.transform(-1, 3);
    assertEquals(9, resized.getStart());
    assertEquals(23, resized.getEnd());

    // Stretch list
    loc = new Location("chromosome:5:80-90:-1").append(
        new Location("chromosome:5:50-70:-1")).append(
        new Location("chromosome:5:10-20:-1"));
    resized = loc.transform(-5, 5);
    assertEquals(3, resized.size());
    assertEquals(95, resized.getEnd());
    assertEquals(5, resized.last().getStart());

  }

  public void testTransformForFlanking() throws Exception {

    // Get right/downstream flanking location
    Location loc = new Location("chromosome:22:100-200:1");
    int startDiff = loc.getLength();
    int endDiff = 1000 + 1;
    Location resized = loc.transform(startDiff, endDiff);
    assertNotNull("Failed to resize loc by " + startDiff + "," + endDiff,
        resized);
    assertEquals("start wrong", 201, resized.getStart());
    assertEquals("end wrong", 1201, resized.getEnd());

    // Get left/upstream flanking region
    loc = new Location("chromosome:22:100-200:1");
    startDiff = -10;
    endDiff = -loc.getLength();
    resized = loc.transform(startDiff, endDiff);
    assertNotNull("Failed to resize loc by " + startDiff + "," + endDiff,
        resized);
    assertEquals("start wrong", 90, resized.getStart());
    assertEquals("end wrong", 99, resized.getEnd());

    // Get left/upstream flanking region on negative strand
    loc = new Location("chromosome:22:100-200:-1");
    startDiff = -10;
    endDiff = -loc.getLength();
    resized = loc.transform(startDiff, endDiff);
    assertNotNull("Failed to resize loc by " + startDiff + "," + endDiff,
        resized);
    assertEquals("start wrong", 201, resized.getStart());
    assertEquals("end wrong", 210, resized.getEnd());

    // Get a location way off upstream beyond the location
    loc = new Location("chromosome:22:100-200:1");
    startDiff = loc.getStart() + loc.getLength() + 1;
    endDiff = loc.getEnd() + 1001;
    resized = loc.transform(startDiff, endDiff);
    assertNotNull("Failed to resize loc by " + startDiff + "," + endDiff,
        resized);
    assertEquals("start wrong", 302, resized.getStart());
    assertEquals("end wrong", 1401, resized.getEnd());

    // Get a location downstream of location on the negave strandbeyond the
    // location
    loc = new Location("chromosome:22:100-110:-1");
    startDiff = 30;
    endDiff = 40;
    resized = loc.transform(startDiff, endDiff);
    assertNotNull("Failed to resize loc by " + startDiff + "," + endDiff,
        resized);
    assertEquals("start wrong", 60, resized.getStart());
    assertEquals("end wrong", 80, resized.getEnd());
  }

  public void testRelativeLocation() throws Exception {
    Location al1 = new Location(new CoordinateSystem("chromosome"), "3", 2100,
        2200, 0);
    Location al2 = al1.relative(34);
    assertTrue("New location is wrong", al2.getStart() == 2134
        && al2.getEnd() == 2134);

    // check each "base" is available, including boundary conditions
    Location l2 = new Location("chromosome:1:10-20").append(new Location(
        "chromosome:1:30-40"));
    int len = l2.getLength();
    // System.out.println(len);
    assertEquals(22, len);
    for (int i = 0; i < len; i++) {
      Location r = l2.relative(i);
      assertEquals(1, r.getLength());
      // System.out.println(""+i+"\t"+r);
      assertTrue(l2.overlaps(r));
    }

    // check all the bases are as expected after a "resize"
    Location l3 = l2.transform(7, -3);
    len = l3.getLength();
    assertEquals(12, len);

    Location[] expectedLocs = { new Location("chromosome:1:17-17"),
        new Location("chromosome:1:18-18"), new Location("chromosome:1:19-19"),
        new Location("chromosome:1:20-20"), new Location("chromosome:1:30-30"),
        new Location("chromosome:1:31-31"), new Location("chromosome:1:32-32"),
        new Location("chromosome:1:33-33"), new Location("chromosome:1:34-34"),
        new Location("chromosome:1:35-35"), new Location("chromosome:1:36-36"),
        new Location("chromosome:1:37-37") };

    for (int i = 0; i < len; i++) {
      Location r = l3.relative(i);
      assertEquals(1, r.getLength());
      // System.out.println(""+i+"\t"+r);
      assertTrue(l3.overlaps(r));
      // Location.equals() is (by default) instance equality
      // so we use compareTo==0 to test for semantic equality
      assertEquals(expectedLocs[i] + "\t"+ r, 0, expectedLocs[i].compareTo(r));
    }
  }

  public void testDiff() {
    CoordinateSystem cs = new CoordinateSystem("chromosome");
    Location al1 = new Location(cs, "3", 2100, 2200, 0);
    Location al2 = new Location(cs, "3", 3100, 3200, 0);
    assertTrue("Diff is wrong", al1.diff(al2) == 1000);
  }

  public void testAssembllyLocationOverlaps() throws Exception {

    Location x1 = new Location("chromosome:3:4-10");
    Location x2 = new Location("chromosome:3:5-15");

    // test simple overlaps
    assertTrue(x1.overlaps(x2));
    assertTrue(x2.overlaps(x1));

    Location x3 = new Location("chromosome:3:13-14");
    Location x4 = new Location("chromosome:3:2-2");

    // test overlapping location in list
    x1.append(x2);
    assertTrue(x1.overlaps(x3));
    assertTrue(x3.overlaps(x1));

    x4.append(x3);
    assertTrue(x4.overlaps(x1));
    assertTrue(x1.overlaps(x4));

    // test "all assembly" overlaps with each individual assembly
    CoordinateSystem cs = new CoordinateSystem("chromosome");
    Location x5 = new Location(cs);
    assertTrue(x5.overlaps(x1));
    assertTrue(x1.overlaps(x5));
    assertTrue(x5.overlaps(x2));
    assertTrue(x2.overlaps(x5));
    assertTrue(x5.overlaps(x3));
    assertTrue(x3.overlaps(x5));
    assertTrue(x5.overlaps(x4));
    assertTrue(x4.overlaps(x5));

  }

  public void testLocationComparable() throws Exception {

    Location same1 = new Location("chromosome:5:34-56");
    Location same2 = new Location("chromosome:5:34-56");
    Location last = new Location("chromosome:x:34-56");
    Location before = new Location("chromosome:4:34-56").append(new Location(
        "chromosome:4:59-60"));
    Location after = new Location("chromosome:4:34-56").append(new Location(
        "chromosome:4:60-61"));

    Location[] locs = new Location[] { new Location("chromosome:1:100-110"),
        same1, new Location("chromosome:2:34-56"), after,
        new Location("chromosome:1:10-11"), same2, last,
        new Location("chromosome:1:10-11:-1"), before,
        new Location("chromosome:1:10-11:1") };

    checkComparable(locs, same1, same2, last, before, after);
  }

  private void checkComparable(Location[] locs, Location same1, Location same2,
      Location last, Location before, Location after) {
    Arrays.sort(locs);

    boolean wrongOrder = false;
    boolean foundBefore = false;
    boolean foundAfter = false;
    for (int i = 0; i < locs.length; ++i) {
      logger.fine("(" + i + ")\t" + locs[i]);
      if (locs[i] == before)
        foundBefore = true;
      if (locs[i] == after) {
        foundAfter = true;
        if (!foundAfter)
          wrongOrder = true;
      }
    }

    assertTrue("Wrong order", !wrongOrder);

    assertSame("last location is wrong.", locs[locs.length - 1], last);

    assertTrue("Distinct locations with same values should be the same.", same1
        .compareTo(same2) == 0);

  }

  public void testOverlapSize() throws Exception {

    Location l1 = new Location("chromosome:1:100-200");

    compare(l1, l1.copy(), l1.getLength());

    // extra flank at one end
    compare(l1, new Location("chromosome:1:100-300"), l1.getLength());

    // partial overlap
    compare(l1, new Location("chromosome:1:150-250"), 51);

    // diff seq region
    compare(l1, new Location("chromosome:2:100-200"), 0);

    // diff cs
    compare(l1, new Location("contig:1:100-200"), 0);

    // diff cs version
    compare(l1, new Location("chromosome_v2:1:100-200"), 0);

    // non overlapping on same sequence
    compare(l1, new Location("chromosome:1:10-99"), 0);

    // ignores strand
    compare(l1, new Location("chromosome:1:150-250:1"), 51);
    compare(l1, new Location("chromosome:1:150-250:-1"), 51);

    // handle location lists
    compare(l1, new Location("chromosome:1:150-250:1").append(new Location(
        "chromosome:1:350-450:1")), 51);
    compare(l1, new Location("chromosome:1:10-20:1").append(new Location(
        "chromosome:1:190-210:1")), 11);
    compare(l1, new Location("chromosome:1:10-20:1").append(
        new Location("chromosome:1:190-210:1")).append(
        new Location("chromosome:1:590-610:1")), 11);

    // check strand support

    Location a = new Location("chromosome:1:1-10");
    Location b = new Location("chromosome:1:1-10:1");
    Location c = new Location("chromosome:1:1-10:-1");

    assertTrue(a.overlaps(b));
    assertEquals(10, a.overlapSize(b));

    // a is unstranded so ALLWAYS overlaps
    assertTrue(a.overlaps(b));
    assertEquals(10, a.overlapSize(b));
    assertTrue(b.overlaps(a));
    assertEquals(10, b.overlapSize(a));

    assertTrue(a.overlaps(b, true));
    assertEquals(10, a.overlapSize(b, true));
    assertTrue(b.overlaps(a, true));
    assertEquals(10, b.overlapSize(a, true));

    assertTrue(a.overlaps(c));
    assertEquals(10, a.overlapSize(c));
    assertTrue(c.overlaps(a));
    assertEquals(10, c.overlapSize(a));

    assertTrue(a.overlaps(c, true));
    assertEquals(10, a.overlapSize(c, true));
    assertTrue(c.overlaps(a, true));
    assertEquals(10, c.overlapSize(a, true));

    // overlap because default is to IGNORE strand
    assertTrue(b.overlaps(c));
    assertEquals(10, b.overlapSize(c));
    assertTrue(c.overlaps(b));
    assertEquals(10, c.overlapSize(b));

    assertTrue(!b.overlaps(c, true));
    assertEquals(0, b.overlapSize(c, true));
    assertTrue(!c.overlaps(b, true));
    assertEquals(0, c.overlapSize(b, true));

  }

  private void compare(Location a, Location b, int expectedOverlap) {
    assertEquals(a.overlapSize(b), b.overlapSize(a));
    assertEquals(expectedOverlap, a.overlapSize(b));

  }

  public void testCopy() throws Exception {
    Location l1 = new Location("chromosome:1:1m-3m:1");
    assertTrue(l1.compareTo(l1.copy()) == 0);

    Location l2 = new Location("chromosome:1:100-200:1").append(l1);
    assertTrue(l2.compareTo(l2.copy()) == 0);

    Location l3 = new Location("chromosome:1:10-20:1").append(l2);
    assertTrue(l3.compareTo(l3.copy()) == 0);

  }

  public void testLocationListConstructor() throws ParseException {

    Location l1 = new Location("chromosome:2:1-10");
    Location l2 = new Location("chromosome:2:100-110");
    Location l3 = new Location("chromosome:2:200-210");
    Location l4 = new Location("chromosome:2:300-310");
    Location l5 = new Location("chromosome:2:400-410");
    Location l6 = new Location("chromosome:2:500-510");

    Location l = null;

    try {
      l = new Location(new Location[] {});
      fail("should have thrown illegal argument exception because the array is empty");
    } catch (IllegalArgumentException e) {
    }

    // 1 element array
    l = new Location(new Location[] { l1 });
    compareLocationNode(l, l1);

    // 3 element "flat" array
    l = new Location(new Location[] { l1, l2, l3 });
    compareLocationNode(l, l1);
    compareLocationNode(l.next(), l2);
    compareLocationNode(l.next().next(), l3);

    // array containing single location list
    l = new Location(new Location[] { l1.append(l2).append(l3) });
    compareLocationNode(l, l1);
    compareLocationNode(l.next(), l2);
    compareLocationNode(l.next().next(), l3);
    // set next pointers to null because we use them again below and not doing
    // so will lead to
    // circular refs.
    l1.setNext(null);
    l2.setNext(null);

    // array containing a mixture of songle node and list locations
    l = new Location(new Location[] { l1.append(l2).append(l3), l4.append(l5),
        l6 });
    compareLocationNode(l, l1);
    compareLocationNode(l.next(), l2);
    compareLocationNode(l.next().next(), l3);
    compareLocationNode(l.next().next().next(), l4);
    compareLocationNode(l.next().next().next().next(), l5);
    compareLocationNode(l.next().next().next().next().next(), l6);

  }

  /**
   * @param l
   * @param l1
   */
  private void compareLocationNode(Location a, Location b) {
    assertEquals(a.getCoordinateSystem(), b.getCoordinateSystem());
    assertEquals(a.getStart(), b.getStart());
    assertEquals(a.getEnd(), b.getEnd());
    assertEquals(a.getStrand(), b.getStrand());
    assertEquals(a.getNodeLength(), b.getNodeLength());
    assertEquals(a.getSeqRegionName(), b.getSeqRegionName());
    assertEquals(a.getSequenceRegion(), b.getSequenceRegion());

  }

  public void testLocationStringRepresentationSupport() throws Exception {
    checkString2Location2StringRoundtrip("c:1:10-20:-1");
    checkString2Location2StringRoundtrip("c:1:10-20:-1->c:1:30-40");
    checkString2Location2StringRoundtrip("c:1:10-20:-1->c:1:30-40->c:2:5k-10m");

  }

  private void checkString2Location2StringRoundtrip(String s) throws Exception {
    // Test support for creating location from a string and test the conversion
    // to
    // a string and then back into a Location again. If there is a bug in the
    // code then
    // the conversion will fail OR the assertions will fail.
    Location loc1 = new Location(s);
    Location loc2 = new Location(loc1.toString());
    assertTrue(loc1.compareTo(loc2) == 0);
    assertEquals(loc1.toString(), loc2.toString());
  }

  public void testMerge() throws Exception {
    
    // leave single node unchanged
    checkMerge("chr:1", "chr:1");
    
    // simple merge
    checkMerge("chr:1:20-40", "chr:1:20-30->chr:1:31-40");
    
    // distinct beginning and merged middle 
    checkMerge("chr:1:1-10->chr:1:20-40",
        "chr:1:1-10->chr:1:20-30->chr:1:31-40");

    // merge in middle
    checkMerge("chr:1:1-10->chr:1:20-40->chr:1:50-60",
        "chr:1:1-10->chr:1:20-30->chr:1:31-40->chr:1:50-60");
    
    // merge in middle and at end
    checkMerge("chr:1:1-10->chr:1:20-40->chr:1:50-70",
    "chr:1:1-10->chr:1:20-30->chr:1:31-40->chr:1:50-60->chr:1:61-70");
    
    // handle diff seq region
    checkMerge("chr:1:1-10->chr:1:20-40->chr:2:50-60->chr:1:61-70",
    "chr:1:1-10->chr:1:20-30->chr:1:31-40->chr:2:50-60->chr:1:61-70");
    
    // merge several nodes
    checkMerge("chr:1:1-70",
    "chr:1:1-10->chr:1:11-30->chr:1:31-40->chr:1:41-60->chr:1:61-70");
    
  }

  public void testGapNodeOperations() throws Exception {
    Location loc = new Location("c:s:1-2");
    assertTrue(!loc.isGap());
    assertTrue(!loc.containsGapNodes());
    assertTrue(loc.compareTo(loc.removeGapNodes())==0);
    
    loc = new Location("c:s:1-2->c:unset:3-4");
    Location expected = new Location("c:s:1-2");
    Location result = loc.removeGapNodes();
    assertTrue(loc.containsGapNodes());
    assertTrue(loc.compareTo(result)!=0);
    assertTrue(result.compareTo(expected)==0);
    assertEquals(result.toString(), expected.toString());
    
    
    loc = new Location("c:unset:1-2->c:s:3-4->c:unset:5-6->c:s:7-8->c:unset:9-10");
    expected = new Location("c:s:3-4->c:s:7-8");
    result = loc.removeGapNodes();
    assertTrue(loc.containsGapNodes());
    assertTrue(loc.compareTo(result)!=0);
    assertTrue(result.compareTo(expected)==0);
    assertEquals(expected.toString(), result.toString());
    
  }
  
  public void testToStringAndConstructorConversion() throws Exception {
    checkToStringConstructorConversion("c:s:1-2");
    checkToStringConstructorConversion("c:unset:1-2");
    checkToStringConstructorConversion("c:unset:1-2->c:s:3-4->c:s:5-6->c:unset:7-8");
  }
  
  private void checkToStringConstructorConversion(String locStr) throws Exception {
    Location loc = new Location(locStr);
    assertEquals(locStr, loc.toString());
    assertEquals(loc.toString(), new Location(loc.toString()).toString());
  }
  
  private void checkMerge(String expected, String src) throws Exception {
    assertEquals(new Location(expected).toString(), new Location(src)
        .mergeAdjacentNodes().toString());
  }
}
