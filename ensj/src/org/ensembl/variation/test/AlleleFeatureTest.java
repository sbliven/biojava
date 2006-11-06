/*
    Copyright (C) 2001 EBI, GRL

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
package org.ensembl.variation.test;

import java.util.List;

import org.ensembl.datamodel.InvalidLocationException;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Transcript;
import org.ensembl.driver.AdaptorException;
import org.ensembl.variation.datamodel.AlleleFeature;

/**
 * AlleleFeature test cases.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public class AlleleFeatureTest extends VariationBase {

  public AlleleFeatureTest(String name) throws Exception {
    super(name);
  }


  public void testFetchByLocation() throws Exception {
    
    Location loc = new Location("chromosome:20:20m-20.001m");
    List afs = vdriver.getAlleleFeatureAdaptor().fetch( loc );
    
    assertTrue(afs.size()>0);
    for (int i = 0; i < afs.size(); i++) {
      AlleleFeature af = (AlleleFeature) afs.get(i);
      check(af, loc);
    }

  }
  
  public void testFetchByTranscript() throws Exception {
    
    final long transcriptID = 1;
    final int flank = 2000;
    
    Transcript t = vdriver.getCoreDriver().getTranscriptAdaptor().fetch(transcriptID);
    assertNotNull(t);
    assertNotNull(t.getLocation());
    List afs = vdriver.getAlleleFeatureAdaptor().fetch(t, flank);
    
    assertTrue(afs.size()>0);
    AlleleFeature af = (AlleleFeature) afs.get(0); 
    check(af, t.getLocation().transform(-flank, flank));
    
  }


  private void check(AlleleFeature af, Location loc) throws InvalidLocationException, AdaptorException {
    assertTrue(af.getLocation().overlaps(loc));
    assertNotNull(af.getAlleleString());
    assertNotNull(af.getVariation());
    assertEquals(af.getVariation().getInternalID(), af.getVariationInternalID());
    //System.out.println(af);
  }
}
