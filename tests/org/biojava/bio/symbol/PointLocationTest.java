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
 * <code>PointLocationTest</code> tests the behaviour of
 * <code>PointLocation</code> by itself and combined with
 * <code>LocationTools</code>.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
public class PointLocationTest extends TestCase
{
    protected Location r1;
    protected Location r2;

    public PointLocationTest(String name)
    {
	super(name);
    }

    protected void setUp()
    {
	r1 = new PointLocation(1);
	r2 = new PointLocation(10);
    }

    /**
     * <code>testEquals</code> tests equality directly.
     *
     */
    public void testEquals()
    {
  	assertEquals(r1, r1);
  	assertEquals(r1, new PointLocation(1));
    }

    /**
     * <code>testAreEqual</code> tests equality via
     * <code>LocationTools</code>.
     *
     */
    public void testAreEqual()
    {
	assert(LocationTools.areEqual(r1, r1));
	assert(LocationTools.areEqual(r1, new PointLocation(1)));
    }

    /**
     * <code>testOverlaps</code> tests overlaps via
     * <code>LocationTools</code>.
     *
     */
    public void testOverlaps()
    {
	assert(LocationTools.overlaps(r1, r1));
	assert(! LocationTools.overlaps(r1, r2));
    }

    /**
     * <code>testContains</code> tests contains via
     * <code>LocationTools</code>.
     *
     */
    public void testContains()
    {
	assert(LocationTools.contains(r1, new PointLocation(1)));
	assert(! LocationTools.contains(r1, r2));

	assert(r1.contains(1));
	assert(! r1.contains(2));
    }

    /**
     * <code>testIntersection</code> tests intersection via
     * <code>LocationTools</code>.
     *
     */
    public void testIntersection()
    {
	assertEquals(LocationTools.intersection(r1, r1),
		     new PointLocation(1));
	assertEquals(LocationTools.intersection(r1, r2),
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
	assert(r1.isContiguous());
    } 
}
