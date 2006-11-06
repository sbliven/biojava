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

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ensembl.util.mapper.Range;
import org.ensembl.util.mapper.RangeRegistry;
/**
 * @author Arne Stabenau
 * Testing the RangeRegistry object
 */
public class RangeRegistryTest extends TestCase {
	private static Logger logger =
		Logger.getLogger(RangeRegistryTest.class.getName());

	public RangeRegistryTest(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(RangeRegistryTest.class);
		//suite.addTest(new MapperTest("testRetrievePredictionTranscriptsByCloneFragmentLocation"));
		return suite;
	} //end Test 

	protected void setUp() throws Exception {
		// empty
	}

	public void testSmall() throws Exception {
		RangeRegistry rr = new RangeRegistry();
		List result;

		result = rr.checkAndRegister("1", 20, 30, 1, 1000);
		assertEquals("new range", result.size(), 1);
		result = rr.checkAndRegister("1", 900, 950, 1, 1000);
		assertNull("range found", result);
		rr.checkAndRegister("1", 2001, 3000);
		rr.checkAndRegister("1", 2900, 3100);
		result = rr.checkAndRegister("1", 950, 1050, 1, 4000);
		assertEquals("Right number of elems", result.size(), 2);
		Range range = (Range) result.get(0);
		assertEquals("Wrong range returned", range.start, 1001);
		assertEquals("Wrong range returned", range.end, 2000);
		result = rr.checkAndRegister("1", 4101, 4200);
		assertEquals("new range", result.size(), 1);
    assertEquals("overlap size", rr.overlapSize("1", 4100, 4101), 1 );
    assertEquals("overlap size", rr.overlapSize( "1", 3000, 4101), 1002 );
	}

	public void testTiming() throws Exception {
		Date current;
		long startTime, endTime;
		int start, end;

    startTime = (new Date()).getTime();
    Random rand = new Random(startTime);
    
    for (int j = 0; j < 10; j++) {
		  startTime = (new Date()).getTime();
			RangeRegistry rr = new RangeRegistry();
			for (int i = 0; i < 1000; i++) {
				start = rand.nextInt(10000000);
				end = rand.nextInt(10000) + 50 + start;
				rr.checkAndRegister("SeqName", start, end);
			}
      System.out.print( rr.overlapSize("SeqName", 1, Integer.MAX_VALUE-1));
      endTime = (new Date()).getTime();
      System.out.println(
        " - 1000 regs lasted " + (endTime - startTime) + "msecs");
		}
	}
}
