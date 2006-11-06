/*
    Copyright (C) 2001 EBI, GRL

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
package org.ensembl.variation.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Runs all the variation tests.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for org.ensembl.variation.test");
    suite.addTestSuite(IndividualTest.class);
    suite.addTestSuite(VariationFeatureTest.class);
    suite.addTestSuite(VariationGroupTest.class);
    suite.addTestSuite(IndividualGenotypeTest.class);
    suite.addTestSuite(LDFeatureTest.class);
    suite.addTestSuite(PopulationGenotypeTest.class);
    suite.addTestSuite(VariationGroupFeatureTest.class);
    suite.addTestSuite(TranscriptVariationTest.class);
    suite.addTestSuite(VariationTest.class);
    suite.addTestSuite(PopulationTest.class);
    suite.addTestSuite(AlleleGroupTest.class);
    return suite;
  }

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
}
