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

import org.ensembl.datamodel.GeneSnapShot;
import org.ensembl.datamodel.MappingSession;
import org.ensembl.datamodel.StableIDEvent;
import org.ensembl.driver.StableIDEventAdaptor;
import org.ensembl.util.StringUtil;


/**
 * Test class for StableIDEvents. 
 */
public class StableIDEventTest extends CoreBase {

  private static Logger logger = Logger.getLogger( StableIDEventTest.class.getName() );

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  public StableIDEventTest (String name){
    super(name);
  }


  public static Test suite() { 
    return new TestSuite(StableIDEventTest.class);
  }


  protected void setUp() throws Exception {
    super.setUp();
    stableIDEventAdaptor = driver.getStableIDEventAdaptor();
  }

  
  public void testFetchCurrent() throws Exception {
    assertNotNull( stableIDEventAdaptor );
    List stableIDs = stableIDEventAdaptor.fetchCurrent("xxxxxsdfsdf");
    assertTrue( stableIDs.size()==0 );

    // returns 1
    stableIDs = stableIDEventAdaptor.fetchCurrent("ENSG00000000003");
    assertTrue( stableIDs.size()>0 );

    // return >1 sql to get some samples: select old_stable_id,
    // count(new_stable_id) as cnt from stable_id_event s where
    // mapping_session_id=348 group by old_stable_id having cnt>1 limit 10;
    stableIDs = stableIDEventAdaptor.fetchCurrent("ENSG00000004469");
    assertTrue( stableIDs.size()>1 );

  }


	public void testVersionSupport() throws Exception {
		
	  List events = stableIDEventAdaptor.fetch( "ENSG00000172983" );
		assertTrue( events.size()>0 );
		for (Iterator iter = events.iterator(); iter.hasNext();) {
			StableIDEvent element = (StableIDEvent) iter.next();
			assertNotNull( "type not set", element.getType() );
			for (Iterator iterator = element.getRelatedStableIDs().iterator(); iterator.hasNext();) {
				String relatedStableID = (String) iterator.next();
				int[] versions = element.getRelatedVersions( relatedStableID);
				assertTrue( versions.length>0 );
				logger.fine( relatedStableID + ": versions = " 
																		+   StringUtil.toString( versions ));
			}
			logger.fine( element.toString() );
		}	
		
	}

	public void testFetchGeneSnapshot() throws Exception {
	  String id = "ENSG00000172983";
	  GeneSnapShot gss = stableIDEventAdaptor.fetchGeneSnapShot( id, 1 );
	  assertNotNull(gss);
	  assertEquals(gss.getArchiveStableID().getStableID(), id);
  }
	
  private StableIDEventAdaptor stableIDEventAdaptor;
}// StableIDEventTest
