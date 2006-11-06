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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.MarkerFeature;
import org.ensembl.driver.AdaptorException;

public class MarkerFeatureAdaptorTest extends CoreBase {
  public MarkerFeatureAdaptorTest(String name) throws AdaptorException {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(MarkerFeatureAdaptorTest.class);
  }

  public void testRetreiveByID() throws Exception {
    MarkerFeature f = driver.getMarkerFeatureAdaptor().fetch(2);
    assertNotNull(f);
    assertNotNull(f.getAnalysis());
    assertTrue(f.getMapWeight() > 0);
    assertNotNull(f.getMarker());
    assertNotNull(f.getDisplayName());
  }

  public void testRetrieveByLocation() throws Exception {
    List r = driver.getMarkerFeatureAdaptor().fetch(new Location("chromosome:22:21m-22m"));
    assertTrue(r.size() > 0);
  }

  public void testRetrieveByLocationAndMapWeight()
    throws Exception {
      
    List r = driver.getMarkerFeatureAdaptor().fetch(new Location("chromosome:22:21m-22m"),2);
    assertTrue(r.size() > 0);
 
  }

}
