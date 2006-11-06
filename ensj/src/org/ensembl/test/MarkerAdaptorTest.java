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

import org.ensembl.datamodel.Marker;
import org.ensembl.datamodel.MarkerFeature;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.MarkerAdaptor;

public class MarkerAdaptorTest extends CoreBase {
  public MarkerAdaptorTest(String name) throws AdaptorException {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(MarkerAdaptorTest.class);
  }

  
  protected void setUp() throws Exception {
    super.setUp();
    adaptor = driver.getMarkerAdaptor();
  }

  public void testRetreiveByID() throws Exception {
    Marker m = adaptor.fetch(1);
    assertNotNull(m);
    assertNotNull(m.getDisplayName());
    MarkerFeature f = (MarkerFeature)(m.getMarkerFeatures().get(0));
    assertNotNull(f);
    assertNotNull(f.getAnalysis());
    assertNotNull(m.getSeqLeft());
    assertNotNull(m.getSeqRight());
    // type is NULL in our (human) test database so we can't check it here
    //assertNotNull(m.getType());
    List synonyms = m.getSynonyms();
    assertNotNull((String) synonyms.get(0));
    
  }



  private MarkerAdaptor adaptor;
}
