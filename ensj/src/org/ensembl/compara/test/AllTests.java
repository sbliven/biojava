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

package org.ensembl.compara.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Run all compara tests.
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public class AllTests {

  /**
   * Runs all tests in this package.
   * 
   * Prints results to stdout.
   * 
   * @param args ignored.
   */
  public static void main(String[] args) {
    junit.textui.TestRunner.run(AllTests.suite());
  }

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for org.ensembl.compara.test");
    //$JUnit-BEGIN$
    // skip next test because v. slow to run.
    //suite.addTest(new TestSuite(MemberAdaptorTest.class));
    suite.addTest(new TestSuite(GenomeDBAdaptorTest.class));
    suite.addTest(new TestSuite(DnaFragmentAdaptorTest.class));
    suite.addTest(new TestSuite(DnaDnaAlignFeatureAdaptorTest.class));
    suite.addTest(new TestSuite(GenomicAlignBlockAdaptorTest.class));
    suite.addTest(new TestSuite(MethodLinkSpeciesSetAdaptorTest.class));
    suite.addTest(new TestSuite(GenomicAlignAdaptorTest.class));
    //$JUnit-END$
    return suite;
  }
}
