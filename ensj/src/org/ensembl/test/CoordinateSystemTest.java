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

import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ensembl.datamodel.CoordinateSystem;

/**
 * Test class for CoordinateSystems.
 */
public class CoordinateSystemTest extends TestCase {

	private static Logger logger = Logger.getLogger(CoordinateSystemTest.class.getName());

	public static final void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	public CoordinateSystemTest(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(CoordinateSystemTest.class);
		return suite;
	}

	public void setUp() {

	}


	public void testEquals() {

		CoordinateSystem chromosomeVersion1 = new CoordinateSystem("chromosome", "NCBI33");
		CoordinateSystem chromosomeVersion2 = new CoordinateSystem("chromosome", "NCBI34");
		CoordinateSystem chromosomeVersion3 = new CoordinateSystem("chromosome", "NCBI33");
		CoordinateSystem chromosomeNoVersion1 = new CoordinateSystem("chromosome");
		CoordinateSystem chromosomeNoVersion2 = new CoordinateSystem("chromosome");
		CoordinateSystem chromosomeNoVersion3 = new CoordinateSystem("contig");

		assertTrue(chromosomeVersion1.equals(chromosomeVersion3));
		assertTrue(!chromosomeVersion1.equals(chromosomeVersion2));
		assertTrue(chromosomeNoVersion1.equals(chromosomeNoVersion2));
		assertTrue(!chromosomeNoVersion1.equals(chromosomeNoVersion3));

	}

}