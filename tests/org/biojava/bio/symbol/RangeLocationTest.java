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

import junit.framework.TestCase;

/**
 * <code>RangeLocationTest</code> tests the behaviour of
 * <code>RangeLocation</code> by itself and combined with
 * <code>LocationTools</code>.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
public class RangeLocationTest extends TestCase
{
    protected Location r1;
    protected Location r2;
    protected Location r3;
    protected Location r4;

    public RangeLocationTest(String name)
    {
	super(name);
    }

    protected void setUp()
    {
	r1 = new RangeLocation(1, 100);
	r2 = new RangeLocation(90, 190);
	r3 = new RangeLocation(200, 300);
	r4 = new RangeLocation(210, 290);
    }

    /**
     * <code>testEquals</code> tests equality directly.
     *
     */
    public void testEquals()
    {
  	assertEquals(r1, r1);
  	assertEquals(r1, new RangeLocation(1, 100));
    }

    /**
     * <code>testAreEqual</code> tests equality via
     * <code>LocationTools</code>.
     *
     */
    public void testAreEqual()
    {
	assertTrue(LocationTools.areEqual(r1, r1));
	assertTrue(LocationTools.areEqual(r1, new RangeLocation(1, 100)));
    }

    /**
     * <code>testOverlaps</code> tests overlaps via
     * <code>LocationTools</code>.
     *
     */
    public void testOverlaps()
    {
	assertTrue(LocationTools.overlaps(r1, r1));
	assertTrue(LocationTools.overlaps(r2, r2));
	assertTrue(LocationTools.overlaps(r1, r2));
	assertTrue(LocationTools.overlaps(r2, r1));

	assertTrue(! LocationTools.overlaps(r1, r3));
    }

    /**
     * <code>testContains</code> tests contains via
     * <code>LocationTools</code>.
     *
     */
    public void testContains()
    {
	assertTrue(LocationTools.contains(r3, r4));
	assertTrue(! LocationTools.contains(r4, r3));

	assertTrue(r1.contains(1));
	assertTrue(r1.contains(100));
	assertTrue(! r1.contains(101));
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
	assertEquals(LocationTools.union(r1, r2),
		     LocationTools.union(r2, r1));
    }

    /**
     * <code>testIsContiguous</code> tests contiguous.
     *
     */
    public void testIsContiguous()
    {
	assertTrue(r1.isContiguous());
    } 
}
