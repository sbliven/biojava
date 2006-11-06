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
import junit.framework.TestSuite;

/**
 * Runs all tests in this package except the write back test(s).
 * 
 * The write back test(s) require write access and should
 * be run against a suitable database.
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
    TestSuite suite = new TestSuite("Test for org.ensembl.test");
    //$JUnit-BEGIN$
    suite.addTest(KaryotypeBandAdaptorTest.suite());
    suite.addTestSuite(ProbeMappingTest.class);
    suite.addTest(SequenceUtilTest.suite());
    suite.addTest(GeneAdaptorTest.suite());
    suite.addTest(AssemblyMapperTest.suite());
    suite.addTestSuite(MiscFeatureAdaptorTest.class);
    suite.addTest(TranscriptTest.suite());
    suite.addTestSuite(SimpleFeatureAdaptorTest.class);
    suite.addTest(SupportingFeatureAdaptorTest.suite());
    suite.addTest(StableIDEventTest.suite());
    suite.addTest(MarkerTest.suite());
    suite.addTest(PredictionTranscriptTest.suite());
    suite.addTest(CoordinateSystemAdaptorTest.suite());
    suite.addTestSuite(MiscSetAdaptorTest.class);
    suite.addTest(OligoTest.suite());
    suite.addTestSuite(ScoredMappingMatrixTest.class);
    suite.addTest(SequenceRegionAdaptorTest.suite());
    suite.addTestSuite(UtilTest.class);
    suite.addTest(DnaDnaAlignmentTest.suite());
    suite.addTest(RangeRegistryTest.suite());
    suite.addTest(SequenceTest.suite());
    suite.addTestSuite(SequenceEditTest.class);
    suite.addTest(LocationTest.suite());
    suite.addTest(MarkerAdaptorTest.suite());
    suite.addTest(LruTest.suite());
    suite.addTest(MarkerFeatureAdaptorTest.suite());
    suite.addTest(TranscriptAdaptorTest.suite());
    suite.addTestSuite(NamedTimerTest.class);
    suite.addTest(SequenceRegionTest.suite());
    suite.addTestSuite(MailerTest.class);
    suite.addTestSuite(ConnectionPoolTest.class);
    suite.addTestSuite(ExonAdaptorTest.class);
    suite.addTest(ExternalRefTest.suite());
    suite.addTestSuite(QtlFeatureAdaptorTest.class);
    suite.addTest(DnaProteinAlignmentTest.suite());
    suite.addTest(RepeatFeatureTest.suite());
    suite.addTest(CoordinateSystemTest.suite());
    suite.addTest(ProteinFeatureTest.suite());
    suite.addTest(RepeatConsensusTest.suite());
    suite.addTestSuite(CoreBase.class);
    suite.addTest(SimpleFeatureTest.suite());
    suite.addTest(MapperTest.suite());
    suite.addTest(TranslationTest.suite());
    suite.addTest(CoordinateSystemMappingTest.suite());
    suite.addTest(LocationConversionTest.suite());
    suite.addTestSuite(AssemblyExceptionTest.class);
    suite.addTestSuite(QtlAdaptorTest.class);
    suite.addTest(GeneTest.suite());
    suite.addTest(AnalysisTest.suite());

    // Skip write back test because this can't be run against a read only db
    // this test must be run against a writeable db.
    //  suite.addTest(ExternalRefWriteBackTest.suite());

    //$JUnit-END$
    return suite;
  }
}
