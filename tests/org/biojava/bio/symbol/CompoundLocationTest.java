/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */

package org.biojava.bio.symbol;

import java.util.List;
import java.util.ArrayList;

import junit.framework.TestCase;

/**
 * <code>CompoundLocationTest</code> tests the behaviour of
 * <code>CompoundLocation</code> by itself and combined with
 * <code>LocationTools</code>.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 */
public class CompoundLocationTest extends TestCase
{
    protected Location r1;
    protected Location r2;
    protected Location r3;

    private   List     locs1;
    private   List     locs2;
    private   List     locs3;

    public CompoundLocationTest(String name)
    {
	super(name);
    }

    protected void setUp()
    {
	locs1 = new ArrayList();
	locs1.add(new RangeLocation(1, 100));
	locs1.add(new RangeLocation(150, 200));

	locs2 = new ArrayList();
	locs2.add(new RangeLocation(150, 160));
	locs2.add(new RangeLocation(170, 190));

	locs3 = new ArrayList();
	locs3.add(new RangeLocation(250, 300));
	locs3.add(new RangeLocation(350, 400));

	r1 = LocationTools.buildLoc(locs1);
	r2 = LocationTools.buildLoc(locs2);
	r3 = LocationTools.buildLoc(locs3);
    }

    /**
     * <code>testEquals</code> tests equality directly.
     *
     */
    public void testEquals()
    {
  	assertEquals(r1, r1);
  	assertEquals(r1, LocationTools.buildLoc(locs1));
    }

    /**
     * <code>testAreEqual</code> tests equality via
     * <code>LocationTools</code>.
     *
     */
    public void testAreEqual()
    {
	assertTrue(LocationTools.areEqual(r1, r1));
	assertEquals(r1, LocationTools.buildLoc(locs1));
    }

    /**
     * <code>testOverlaps</code> tests overlaps via
     * <code>LocationTools</code>.
     *
     */
    public void testOverlaps()
    {
	assertTrue(LocationTools.overlaps(r1, r2));
	assertTrue(! LocationTools.overlaps(r1, r3));
    }

    /**
     * <code>testContains</code> tests contains via
     * <code>LocationTools</code>.
     *
     */
    public void testContains()
    {
	assertTrue(LocationTools.contains(r1, r2));
	assertTrue(! LocationTools.contains(r1, r3));
	assertTrue(r1.contains(1));
	assertTrue(r1.contains(100));
	assertTrue(r1.contains(150));
	assertTrue(r1.contains(200));

	// Between contained ranges
	assertTrue(! r1.contains(101));
	// Outside contained ranges
	assertTrue(! r1.contains(201));
    }

    /**
     * <code>testIntersection</code> tests intersection via
     * <code>LocationTools</code>.
     *
     */
    public void testIntersection()
    {
	assertEquals(LocationTools.intersection(r1, r2),
		     LocationTools.intersection(r2, r1));
	assertEquals(LocationTools.intersection(r1, r3),
		     Location.empty);
	assertEquals(LocationTools.intersection(r3, r1),
		     Location.empty);
    }

    /**
     * <code>testUnion</code> tests union via
     * <code>LocationTools</code>.
     *
     */
    public void testUnion()
    {
	assertEquals(r1, LocationTools.union(r1, r1));
	assertEquals(r2, LocationTools.union(r2, r2));
	assertEquals(LocationTools.union(r1, r2),
		     LocationTools.union(r2, r1));
	assertEquals(LocationTools.union(r1, r3),
		     LocationTools.union(r3, r1));
    }

    /**
     * <code>testIsContiguous</code> tests contiguous.
     *
     */
    public void testIsContiguous()
    {
	assertTrue(! r1.isContiguous());

	List single = new ArrayList();
	single.add(new RangeLocation(1, 100));
	Location contig = LocationTools.buildLoc(single);

	assertTrue(contig.isContiguous());
    }
}
