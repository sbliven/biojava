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

import org.ensembl.datamodel.MiscSet;
import org.ensembl.driver.MiscSetAdaptor;

/**
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public class MiscSetAdaptorTest extends CoreBase {

  private MiscSetAdaptor msa;

  /**
   * @param name
   */
  public MiscSetAdaptorTest(String name) {
    super(name);
  }


  public void setUp() throws Exception{
    super.setUp();
    msa = driver.getMiscSetAdaptor();
  }

  public void testFetch() throws Exception{
    assertNotNull(msa.getType());
    List all = msa.fetch();
    assertTrue(all.size()>0);
   }

  
  public void testFetchByID() throws Exception {
    MiscSet one = msa.fetch(1);
    assertNotNull(one);

    assertNotNull(one.getCode());
    assertNotNull(one.getDescription());
    assertNotNull(one.getName());
    assertTrue(one.getMaxFeatureLength()>0);
  }
  
  public void testFechByCode() throws Exception {
    MiscSet ms = msa.fetch("encode");
    assertNotNull(ms);    
  }


}
