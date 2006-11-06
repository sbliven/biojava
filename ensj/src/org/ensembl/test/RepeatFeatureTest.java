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

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.RepeatFeature;
import org.ensembl.driver.RepeatFeatureAdaptor;

/**
 * Test class for RepeatFeatures. 
 */
public class RepeatFeatureTest extends CoreBase {

  private static Logger logger =
    Logger.getLogger(RepeatFeatureTest.class.getName());

  private RepeatFeatureAdaptor repeatAdaptor;

  public RepeatFeatureTest(String name) {
    super(name);
  } //end RepeatFeatureTest

  public static Test suite() {
    TestSuite suite = new TestSuite();
    //suite.addTest( new RepeatFeatureTest("testRetrieveSpecificRepeatFeature") );
    suite.addTestSuite(RepeatFeatureTest.class);
    return suite;
  } //end Test 

  protected void setUp() throws Exception {
    super.setUp();
    repeatAdaptor = driver.getRepeatFeatureAdaptor();

  }

  public void testRetrieveSpecificRepeatFeature() throws Exception {
    long id = 74570;
    RepeatFeature feature = repeatAdaptor.fetch(id);

    assertNotNull("RepeatFeature (id = " + id + ") not found ", feature);
    assertTrue(feature.getInternalID() == id);
    assertNotNull(feature.getDisplayName());
    //assertNotNull(feature.getHitDescription());
    assertNotNull(feature.getAnalysis());
    assertNotNull(feature.getHitDisplayName());
    assertNotNull(feature.getLocation());
    assertNotNull(feature.getHitLocation());
    assertNotNull("RepeatConsensus not set", feature.getRepeatConsensus());

  } //end testRetrieveSpecificRepeatFeature

  public void testRetrieveRepeatsByAssemblyLocation() {
    try {
      logger.fine("Retrieving features by assembly location");
      Location location =
        new Location(
          new CoordinateSystem("chromosome"),
          "1",
          1000000,
          1100000,
          1);

      List features = repeatAdaptor.fetch(location);
      Iterator featureIterator = features.iterator();

      while (featureIterator.hasNext()) {
        logger.fine("Feature: " + featureIterator.next());
      } //end while

      logger.fine("Number of RepeatFeatures found:" + features.size());

      assertEquals(true, features.size() > 0);
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  } //end testRetrieveRepeatsByCloneFragmentLocation

  public static void main(String[] args) throws Exception {
    new RepeatFeatureTest("testRetrieveSpecificRepeatFeature").run();
    new RepeatFeatureTest("testRetrieveRepeatsByCloneFragmentLocation").run();
    new RepeatFeatureTest("testRetrieveRepeatsByAssemblyLocation").run();
  } //end main
} // RepeatFeatureTest
