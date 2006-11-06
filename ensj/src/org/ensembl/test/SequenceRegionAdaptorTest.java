/*
 * Copyright (C) 2003 EBI, GRL
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

import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ensembl.datamodel.Attribute;
import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.SequenceRegion;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.SequenceRegionAdaptor;

/**
 * JUnit tests for CoordinateSystemAdaptor.
 */
public class SequenceRegionAdaptorTest extends CoreBase {

	private static final Logger logger = Logger.getLogger(CoordinateSystemAdaptorTest.class.getName());

	SequenceRegionAdaptor sequenceRegionAdaptor = null;

	public SequenceRegionAdaptorTest(String testName) {
		super(testName);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(SequenceRegionAdaptorTest.class);
		return suite;
	}

	  
	// --------------------------------------------------------------------------

	public void testFetchByID() throws AdaptorException {

		SequenceRegion sr = driver.getSequenceRegionAdaptor().fetch(143909);
		assertNotNull(sr);
		System.out.println(sr.toString());

	}

	// --------------------------------------------------------------------------

	public void testFetchByNameAndCS() throws AdaptorException {

		CoordinateSystem cs = new CoordinateSystem("chromosome", Base.LATEST_HUMAN_CHROMOSOME_VERSION);
		SequenceRegion sr = driver.getSequenceRegionAdaptor().fetch("X", cs);
		assertNotNull(sr);
		System.out.println(sr.toString());

	}

	//	--------------------------------------------------------------------------

	public void testFetchByLocation() throws AdaptorException {

		CoordinateSystem cs = new CoordinateSystem("chromosome", Base.LATEST_HUMAN_CHROMOSOME_VERSION);
		Location loc = new Location(cs, "X");
		SequenceRegion sr = driver.getSequenceRegionAdaptor().fetch(loc);
		assertNotNull(sr);

	}

	// -----------------------------------------------------------------

	public void testFetchAllByCoordinateSystem() throws AdaptorException {

		CoordinateSystem cs = new CoordinateSystem("chromosome", Base.LATEST_HUMAN_CHROMOSOME_VERSION);
		SequenceRegion[] srs = driver.getSequenceRegionAdaptor().fetchAllByCoordinateSystem(cs);
		assertNotNull(srs);
		assertTrue(srs.length > 0);
		System.out.println("Got " + srs.length + " SequenceRegions with coordinate system " + cs.getName());

	}

	// -----------------------------------------------------------------

	public void testFetchByAttributes() throws AdaptorException {

		SequenceRegion[] srs = driver.getSequenceRegionAdaptor().fetchAllByAttributeCode("toplevel");
    assertNotNull(srs);
    assertTrue(srs.length > 0);
    
		srs = driver.getSequenceRegionAdaptor().fetchAllByAttributeValue("toplevel", "1");
		assertNotNull(srs);
		assertTrue(srs.length > 0);
		logger.fine("Got " + srs.length + " sequence regions with htg_phase attribute = 1");

    Attribute a = srs[0].getAttributes()[0];
    assertNotNull(a.getName());
    assertNotNull(a.getCode());
    assertNotNull(a.getDescription());
    assertNotNull(a.getValue());

	}

	// -----------------------------------------------------------------

	public static final void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	// --------------------------------------------------------------------------

}
