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

import org.ensembl.util.mapper.Coordinate;
import org.ensembl.util.mapper.Mapper;

/**
 * Test class for PredictionTranscripts
 */
public class MapperTest extends TestCase {

	private static Logger logger = Logger.getLogger(MapperTest.class.getName());

	public MapperTest(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(MapperTest.class);
		//suite.addTest(new MapperTest("testRetrievePredictionTranscriptsByCloneFragmentLocation"));
		return suite;
	} //end Test 

	protected void setUp() throws Exception {
		// empty
	}

	private static boolean testMap(
		Mapper mapper,
		String id,
		int start,
		int end,
		int strand,
		String tag,
		String[] expectedResults)
		throws Exception {

		boolean testResult = true;

		Coordinate results[] = mapper.mapCoordinate(id, start, end, strand, tag);

		if (results.length != expectedResults.length) {
			testResult = false;
		} else {
			for (int i = 0; i < results.length; i++) {
				if (!results[i].toString().equals(expectedResults[i])) {
					testResult = false;
				}
			}
		}

		if (!testResult) {
			System.out.println("Failed mapping");
			System.out.println("--- Expected ---");
			for (int i = 0; i < expectedResults.length; i++) {
				System.out.println(expectedResults[i]);
			}
			System.out.println("\n--- Current ---");
			for (int i = 0; i < results.length; i++) {
				System.out.println(results[i]);
			}
		}
		assertTrue("Results differ", testResult);
		return testResult;
	}

	public void testEverything() throws Exception {
		Mapper mapper;

		mapper = new Mapper("virtualContig", "rawContig");
		mapper.addMapCoordinates(
			"chr1",
			359545,
			383720,
			-1,
			"314696",
			31917,
			56092);
		mapper.addMapCoordinates("chr1", 383721, 443368, -1, "341", 126, 59773);
		mapper.addMapCoordinates("chr1", 443369, 444727, 1, "315843", 5332, 6690);

		String[] result0 =
			{ "314696:31917-31937,-1", "341:126-59773,-1", "315843:5332-5963,1" };
		testMap(mapper, "chr1", 383700, 444000, 1, "virtualContig", result0);

		mapper = new Mapper("asm1", "asm2");

		mapper.addMapCoordinates("1", 1, 10, 1, "1", 101, 110);
		mapper.addMapCoordinates("1", 21, 30, 1, "1", 121, 130);
		mapper.addMapCoordinates("1", 11, 20, 1, "1", 111, 120);

		String[] result1 = { "1:105-125,1" };
		testMap(mapper, "1", 5, 25, 1, "asm1", result1);

		//
		// dont merge on wrong orientation
		//

		mapper = new Mapper("asm1", "asm2");

		mapper.addMapCoordinates("1", 1, 10, 1, "1", 101, 110);
		mapper.addMapCoordinates("1", 21, 30, 1, "1", 121, 130);
		mapper.addMapCoordinates("1", 11, 20, -1, "1", 111, 120);

		String[] result2 = { "1:105-110,1", "1:111-120,-1", "1:121-125,1" };

		testMap(mapper, "1", 5, 25, 1, "asm1", result2);

		//
		// can reverse strands merge?
		//
		mapper = new Mapper("asm1", "asm2");

		mapper.addMapCoordinates("1", 1, 10, -1, "1", 121, 130);
		mapper.addMapCoordinates("1", 21, 30, -1, "1", 101, 110);
		mapper.addMapCoordinates("1", 11, 20, -1, "1", 111, 120);

		String[] result3 = { "1:106-126,-1" };
		testMap(mapper, "1", 5, 25, 1, "asm1", result3);

		//
		// normal merge, not three
		//

		mapper = new Mapper("asm1", "asm2");

		mapper.addMapCoordinates("1", 1, 10, 1, "1", 101, 110);
		mapper.addMapCoordinates("1", 11, 20, 1, "1", 111, 120);
		mapper.addMapCoordinates("1", 22, 30, 1, "1", 132, 140);
		mapper.addMapCoordinates("1", 51, 70, 1, "1", 161, 180);
		mapper.addMapCoordinates("1", 31, 35, 1, "1", 141, 145);

		String[] result4 =
			{ "1:105-120,1", "null:21-21,0", "1:132-145,1", "null:36-45,0" };

		testMap(mapper, "1", 5, 45, 1, "asm1", result4);
	}

	public static void main(String[] args) throws Exception {
		new MapperTest("testEverything").run();
	} //end main
} // PredictionTranscriptTest
