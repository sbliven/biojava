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
import junit.framework.TestSuite;

import org.ensembl.datamodel.CloneFragmentLocation;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Marker;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.MarkerAdaptor;


/**
 * Test class for Markers. 
 */
public class MarkerTest extends CoreBase {

	private static Logger logger = Logger.getLogger( MarkerTest.class.getName() );

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  public MarkerTest (String name) throws AdaptorException{
    super(name);
  }


  protected void setUp() throws Exception {
    super.setUp();
    markerAdaptor = driver.getMarkerAdaptor();
  }

  public static Test suite() { 
     TestSuite suite = new TestSuite();
     
     suite.addTestSuite(MarkerTest.class);
     
     //suite.addTest( new MarkerTest("testFetchBySynonym") );
     //suite.addTest( new MarkerTest("testLazyLoadLocations") );
		 //suite.addTest( new
		 //MarkerTest("testFetchMarkerFeaturesByAssemblyLocation"));
		 //suite.addTest( new MarkerTest("testFetchByCloneFragmentLocation"));
     
     return suite;
  }


  
	public void testFetchByID()  throws Exception {
		Marker m = markerAdaptor.fetch( 100 );
		assertNotNull( m );
    assertNotNull(m.getSeqLeft());
    assertNotNull(m.getSeqRight());
    
    assertTrue(m.getInternalID()>0);
    assertTrue(m.getMaxPrimerDistance()>0);
    assertTrue(m.getMaxPrimerDistance()>0);
    assertTrue(m.getSynonyms().size()>0);
    
    // optional
    //  assertNotNull(m.getType());
  }


  public void testFetchBySynonym() throws Exception {
    Marker marker = markerAdaptor.fetchBySynonym(synonym);
    assertNotNull(marker);
    logger.fine("marker (synonym = " + synonym + ") : " + marker);
    
  }

  public void testLazyLoadLocations() throws Exception {
    Marker m = markerAdaptor.fetchBySynonym(synonym);
    assertTrue("Too few locations to make a useful test, "
               +"choose a marker other than this one that appears on genome several times: "
               +m,
               m.getMarkerFeatures().size()>0 );
  }

  private MarkerAdaptor markerAdaptor;
  
  // Choose a marker that appears>1 times in genome
  private final String synonym = "RH143742";

  // choose a location with a marker that doesn't appear too many
  // times or makes test very slow.
  private final Location contigLoc = new CloneFragmentLocation("BX248409.5.1.127227");
}

