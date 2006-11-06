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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.CoordinateSystemMapping;

public class CoordinateSystemMappingTest extends TestCase {

	CoordinateSystemMapping path2, path3;
	CoordinateSystem chr, cln, ctg;

	public CoordinateSystemMappingTest(String testName) {
		super(testName);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(CoordinateSystemMappingTest.class);
		return suite;
	}

	public void setUp() {

		chr = new CoordinateSystem("chromosome", Base.LATEST_HUMAN_CHROMOSOME_VERSION);
		cln = new CoordinateSystem("clone");
		ctg = new CoordinateSystem("contig");

		CoordinateSystem[] array2 = new CoordinateSystem[2];
		array2[0] = chr;
		array2[1] = ctg;
		path2 = new CoordinateSystemMapping(array2);

		CoordinateSystem[] array3 = new CoordinateSystem[3];
		array3[0] = chr;
		array3[1] = ctg;
		array3[2] = cln;
		path3 = new CoordinateSystemMapping(array3);

	}

	public void testGetPath() {

		CoordinateSystem[] result = path2.getPath();
		assertNotNull(result);
		assertEquals(result.length, 2);
		result = path3.getPath();
		assertNotNull(result);
		assertEquals(result.length, 3);

	}

	public void testGetFirst() {

		CoordinateSystem first = path2.getFirst();
		assertNotNull(first);
	assertTrue(first.equals(chr));
	
	first = path3.getFirst();
	assertTrue(first.equals(chr));
	
	}

	public void testGetLast() {

		CoordinateSystem last = path2.getLast();
		assertNotNull(last);
		assertTrue(last.equals(ctg));
		last = path3.getLast();
		assertTrue(last.equals(cln));
		

	}

}
