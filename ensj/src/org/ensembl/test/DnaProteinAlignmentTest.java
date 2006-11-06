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
import org.ensembl.datamodel.DnaProteinAlignment;
import org.ensembl.datamodel.Location;
public class DnaProteinAlignmentTest extends CoreBase {


  private static Logger logger = Logger.getLogger( DnaProteinAlignmentTest.class.getName() );

  public DnaProteinAlignmentTest( String name ) {
    super( name );
  }

  public static Test suite() {
    return new TestSuite( DnaProteinAlignmentTest.class );
  }



  public void testRetrieveByLocation(){
    try{
      CoordinateSystem chromosomeCoordSystem = new CoordinateSystem("chromosome", Base.LATEST_HUMAN_CHROMOSOME_VERSION);
      Location location = new Location(chromosomeCoordSystem, "1", 1, 1000000, 0);
      List features = driver.getDnaProteinAlignmentAdaptor().fetch(location, "Swall");
      logger.fine( "fetch with location: " + location);
      logger.fine( "Number of features: "+features.size());
      for (int i=0; i<features.size(); i++){
        logger.fine( "fetched feature: \n" + (DnaProteinAlignment)features.get(i) );
      }
      
    }catch(Throwable exception){
      exception.printStackTrace();
      logger.severe(exception.getMessage());
      fail("Failed to fetch DnaProteinAlignFeatures");
    }
  }
 
 
  public void testFetchByInternalID() throws Exception {
    
   
    DnaProteinAlignment f = driver.getDnaProteinAlignmentAdaptor().fetch(350);
    assertTrue(f.getInternalID()>0);
    assertNotNull(f);
    assertNotNull(f.getAnalysis());
    assertTrue(f.getScore()>0);
    assertTrue(f.getEvalue()>0);
    assertNotNull(f.getCigarString());
    assertNotNull(f.getHitDescription());
    assertNotNull(f.getHitAccession());
    assertNotNull(f.getHitDescription());
    assertNotNull(f.getHitDisplayName());
    assertNotNull(f.getHitLocation());
    
    // optional attributes
    //assertTrue(f.getPercentageIdentity()>0);
    //assertNotNull(f.getDescription());
    //assertNotNull(f.getDisplayName());
     
  }
  
  
  /**
   * Slow.
   * @throws Exception
   */
  public void testFetchByLogicalName() throws Exception{
    // this is a slow query and memory intensive query because there
  	// are many instances of this logical name in the db. It typically
  	// takes about 2mins and requires a lot of memory e.g. -Xmx300m. 
    //List l = driver.getDnaProteinAlignmentAdaptor().fetch("other_protein");
  	Iterator iter = driver.getDnaProteinAlignmentAdaptor().fetchIterator("other_protein");
    assertTrue(iter.hasNext());
    DnaProteinAlignment f = (DnaProteinAlignment) iter.next();
    assertNotNull(f);
  }
  
  public static void main(String[] args){

    DnaProteinAlignmentTest test = new DnaProteinAlignmentTest("testRetrieveByLocation");
    test.run();
    
  }
  
  
}
