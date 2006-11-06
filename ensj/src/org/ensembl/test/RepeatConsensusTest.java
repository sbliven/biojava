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

import org.ensembl.datamodel.RepeatConsensus;
import org.ensembl.driver.RepeatConsensusAdaptor;

/**
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public class RepeatConsensusTest extends CoreBase {

  private RepeatConsensusAdaptor rca;

  /**
   * @param name    
   */
  public RepeatConsensusTest(String name) {
    super(name);
  }

  

  public static Test suite() {
    TestSuite suite = new TestSuite();
    //suite.addTest( new RepeatConsensusTest("testRetrieveSpecificRepeatFeature") );
    suite.addTestSuite(RepeatConsensusTest.class);
    return suite;
  }

  protected void setUp() throws Exception {
    super.setUp();
    rca = driver.getRepeatConsensusAdaptor();

  }


  public void testFetchByInternalID() throws Exception {
    long id = 11272;
    RepeatConsensus rc = rca.fetch(id);
    assertNotNull(rc);
    assertEquals(rc.getInternalID(),id);
    assertNotNull(rc.getName());
    assertNotNull(rc.getType());
    assertNotNull(rc.getSequence().getString());
      
  }

}
