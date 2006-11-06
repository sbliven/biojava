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
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ensembl.datamodel.KaryotypeBand;
import org.ensembl.driver.KaryotypeBandAdaptor;

/**
 * Test class for Karyotypes.
**/
public class KaryotypeBandAdaptorTest extends CoreBase {

  private static Logger logger =
    Logger.getLogger(KaryotypeBandAdaptorTest.class.getName());

  public KaryotypeBandAdaptorTest(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(KaryotypeBandAdaptorTest.class);
  }

  protected void setUp() throws Exception {
    super.setUp();
    adaptor = driver.getKaryotypeBandAdaptor();
  }

  public void testRetrieveKaryotypeBands() throws Exception {

    List l = adaptor.fetch(chromosomeCS, "1");
    assertTrue(l.size()>0);
    for (int i = 0, n = l.size(); i < n; i++) 
      logger.fine(l.get(i).toString());

    KaryotypeBand kb = (KaryotypeBand) l.get(0);
    assertNotNull(kb.getLocation());
    assertEquals("1", kb.getLocation().getSeqRegionName());

    l = adaptor.fetch(chromosomeCS, "2","P11.1");
    assertTrue(l.size()>0);
    for (int i = 0, n = l.size(); i < n; i++) 
      logger.fine(l.get(i).toString());

  }

  private KaryotypeBandAdaptor adaptor;

}
