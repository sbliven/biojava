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

import org.ensembl.datamodel.Exon;
import org.ensembl.datamodel.Feature;
import org.ensembl.datamodel.Location;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.FeatureAdaptor;
import org.ensembl.driver.FeatureIterator;

public class FeatureIteratorTest extends CoreBase {

  public FeatureIteratorTest(String arg0) {
    super(arg0);
  }
  
  public static void main(String[] args) {
    junit.textui.TestRunner.run(FeatureIteratorTest.class);
  }
  
  
  public void testLocationFeatureTest() throws Exception {
    
    // choose a haplotype to ensure reference-re-reference is working.
    check(driver.getExonAdaptor(), new Location("chromosome:c22_H2:14m-15m:-1"), 10000);
    
    // choose a small chunk size relative to location size so that
    // we find some features which cross boundary between chunks. This ensures
    // the feature iterator handles these cases.
    check(driver.getExonAdaptor(), new Location("chromosome:22:21m-21.1m"), 1000);
    
  }
  
  
  private void check(FeatureAdaptor adaptor, Location location, int chunkSize) throws AdaptorException {
    
    // compare the results of fetchIterator() and fetch().iterator() to
    // check they are the same.
    
    Iterator iter1 = driver.getExonAdaptor().fetch(location.copy()).iterator();
    Iterator iter2 = new FeatureIterator(driver.getExonAdaptor(),
        chunkSize, false, location.copy() ,driver.getLocationConverter());
    
    int c = 0;
    while(iter1.hasNext()==iter2.hasNext() && iter1.hasNext()) {
      c++;
      Feature e1 = (Feature) iter1.next();
      Feature e2 = (Feature) iter2.next();
      assertEquals("Features out of sync", e1.getInternalID(), e2.getInternalID());
      Location l1 = e1.getLocation();
      Location l2 = e2.getLocation();
      assertTrue("Locations different "+l1+"\t"+l2, l1.compareTo(l2)==0);
      
    }
    assertEquals("Iterators out of sync one has finished and the other has more items", iter1.hasNext(), iter2.hasNext());
    assertTrue("Invalid test, no data retrieved by either fetch mechanism for location "+ location,c>0);
  }
}
