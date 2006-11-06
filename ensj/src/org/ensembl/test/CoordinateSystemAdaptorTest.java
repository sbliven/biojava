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

import java.util.List;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.CoordinateSystemAdaptor;

/**
 * JUnit tests for CoordinateSystemAdaptor.
 */
public class CoordinateSystemAdaptorTest extends CoreBase {

  private static final Logger logger =
    Logger.getLogger(CoordinateSystemAdaptorTest.class.getName());

  CoordinateSystemAdaptor coordSystemAdaptor = null;

  public CoordinateSystemAdaptorTest(String testName) {
    super(testName);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(CoordinateSystemAdaptorTest.class);
    return suite;
  }

  public void setUp() throws Exception {

    super.setUp();
    coordSystemAdaptor = driver.getCoordinateSystemAdaptor();
  }

  // --------------------------------------------------------------------------

  public void testFetchAll() throws AdaptorException {

    CoordinateSystem[] cs = coordSystemAdaptor.fetchAll();
    assertNotNull(cs);
    for (int i = 0; i < cs.length; i++) {
      assertNotNull(cs[i]);
    }

  }

  // --------------------------------------------------------------------------

  public void testFetchByID() throws AdaptorException {

    CoordinateSystem cs = coordSystemAdaptor.fetch(4);
    System.out.println(cs.toString());
    assertNotNull(cs);

    // check internal ID has been set (defaults to 0 in CoordinateSystem.java)
    assertTrue(cs.getInternalID() > 0);

  }

  // --------------------------------------------------------------------------

  public void testFetchByNameAndVersion() throws AdaptorException {

    CoordinateSystem cs = coordSystemAdaptor.fetch("chromosome", Base.LATEST_HUMAN_CHROMOSOME_VERSION);
    assertNotNull(cs);
    cs = coordSystemAdaptor.fetch("contig", "");
    assertNotNull(cs);
  }

  // --------------------------------------------------------------------------

  public void testFetchSequenceLevel() throws AdaptorException {

    CoordinateSystem cs = coordSystemAdaptor.fetchSequenceLevel();
    assertNotNull(cs);
    assertTrue(cs.isSequenceLevel());

  }

  // --------------------------------------------------------------------------

  public void testDefault() throws AdaptorException {

    int numDefaults = 0;

    CoordinateSystem[] cs = coordSystemAdaptor.fetchAll();
    assertNotNull(cs);
    for (int i = 0; i < cs.length; i++) {
      if (cs[i].isDefault()) {
        numDefaults++;
      }
    }

  }

  // --------------------------------------------------------------------------

  public void testMappingPath() throws AdaptorException {

    // "simple" case
    CoordinateSystem[] mappingPath =
      coordSystemAdaptor.getMappingPath(chromosomeCS, contigCS);
    assertNotNull(mappingPath);
    assertEquals(chromosomeCS, mappingPath[0]);
    assertEquals(contigCS, mappingPath[1]);

    // "reverse" case
    mappingPath = coordSystemAdaptor.getMappingPath(contigCS, chromosomeCS);
    assertNotNull(mappingPath);
    // should still return in order
    // assembled, component
    assertEquals(chromosomeCS, mappingPath[0]);
    assertEquals(contigCS, mappingPath[1]);    
    
  }

  // -----------------------------------------------------------------

  public void testFetchAllByFeatureTable() throws AdaptorException {

    CoordinateSystem[] cs = coordSystemAdaptor.fetchAllByFeatureTable("gene");
    assertNotNull(cs);
    assertTrue(cs.length>0);

    CoordinateSystem[] cs2 = coordSystemAdaptor.fetchAllByFeatureTable("protein_align_feature");
    assertNotNull(cs2);
    assertTrue(cs2.length>0);
    
    // check case insensitivity
    CoordinateSystem[] cs3 = coordSystemAdaptor.fetchAllByFeatureTable("PROTEIN_ALIGN_FEATURE");
    assertNotNull(cs3);
    assertTrue(cs3.length>0);
    assertEquals(cs2.length, cs3.length);
    
    cs =  coordSystemAdaptor.fetchAllByFeatureTable("XXX");
    assertTrue("Should have found no CSs for feature XXX", cs.length==0);
    
  }

  // -----------------------------------------------------------------

  public void testFetchComplete() {

    CoordinateSystem skeletonCS = new CoordinateSystem("chromosome");
    CoordinateSystem completeCS = null;
    try {
      completeCS = coordSystemAdaptor.fetchComplete(skeletonCS);
    } catch (AdaptorException e) {
      e.printStackTrace();
    }

    assertNotNull(completeCS);
    assertTrue(completeCS.getInternalID() > 0);
    assertNotNull(completeCS.getVersion());
    assertTrue(completeCS.getRank() > 0);
  }

  //--------------------------------------------------------------------------

  public void testFetchingTopLevelLocations() throws Exception {
    List locs = coordSystemAdaptor.fetchTopLevelLocations();
    assertTrue("No top level locations found.", locs.size()>0);
    System.out.println("N top level:  "+locs.size());
  }

  //--------------------------------------------------------------------------

  public static final void main(String[] args) throws Exception {
    junit.textui.TestRunner.run(suite());
  }

  // --------------------------------------------------------------------------

}
