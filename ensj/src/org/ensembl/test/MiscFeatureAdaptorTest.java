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

import java.util.List;

import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.MiscFeature;
import org.ensembl.datamodel.MiscSet;
import org.ensembl.driver.MiscFeatureAdaptor;


/**
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public class MiscFeatureAdaptorTest extends CoreBase {

// Leave this code in because useful during development.
//  public static TestSuite suite() {
//    TestSuite s = new TestSuite();
//    s.addTest(new MiscFeatureAdaptorTest("testFetchByMiscSet"));
//    s.addTest(new MiscFeatureAdaptorTest("testFetchByLocationAndMiscSet"));
//    return s;
//  }



  private MiscFeatureAdaptor mfa;

  /**
   * @param name
   */
  public MiscFeatureAdaptorTest(String name) {
    super(name);
  }

  public void setUp() throws Exception{
    super.setUp();
    mfa = driver.getMiscFeatureAdaptor();
  }

  
  public void testFetchByID() throws Exception {
    MiscFeature one = mfa.fetch(1);
    assertNotNull(one);

    assertNotNull(one.getLocation());
    assertTrue(one.getAttributes().size()>0);
    assertTrue(one.getMiscSets().size()>0);
    
  }
  
  public void testFechByLocation() throws Exception {
    List some = mfa.fetch(new Location("chromosome:1:149m-150m"));
    assertTrue(some.size()>0);    
  }

  public void testFetchByAttributeCriteria() throws Exception{

    List all;

    // query disabled because too slow for unit test because too much data retrieved
//    all = mfa.fetchByAttributeType("clone_name");
//    assertTrue(all.size()>0);

    all = mfa.fetchByAttributeTypeAndValue("name","ENr231");
    assertTrue(all.size()>0);

   }

  public void testFetchByMiscSet() throws Exception{

      MiscSet ms = driver.getMiscSetAdaptor().fetch("encode");
      List all = mfa.fetch(ms);
      assertTrue(all.size()>0);
  }  

  public void testFetchByLocationAndMiscSet() throws Exception{

      MiscSet ms = driver.getMiscSetAdaptor().fetch("encode");
      assertNotNull(ms);
      List all = mfa.fetch(new Location("chromosome:1:149m-150m"), ms);
      assertTrue(all.size()>0);
  }  


}
