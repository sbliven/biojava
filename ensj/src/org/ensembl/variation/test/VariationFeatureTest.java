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

import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Persistent;
import org.ensembl.util.LongSet;
import org.ensembl.variation.datamodel.Variation;
import org.ensembl.variation.datamodel.VariationFeature;

/**
 * VariationFeature test cases.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public class VariationFeatureTest extends VariationBase {

  public VariationFeatureTest(String name) throws Exception {
    super(name);
  }


  public void testFetchByID() throws Exception{
    
    final long ID = 2311906;
    VariationFeature vf = vdriver.getVariationFeatureAdaptor().fetch(ID);
    
    assertNotNull(vf);
    assertEquals(ID,vf.getInternalID());
    assertNotNull(vf.getLocation());
    assertTrue(vf.getMapWeight()>0);
    assertNotNull(vf.getAlleleString());
    assertTrue(vf.getAlleleString().length()>0);
    assertTrue(vf.getMapWeight()>0);
    assertNotNull(vf.getDriver());
    assertNotNull(vf.getVariationName());
    assertNotNull(vf.getSequence());
    assertTrue(vf.getSequence().toString().length()>0);
    assertTrue(vf.getVariationInternalID()>0);
    
    
    assertNotNull(vf.getVariation());
    assertEquals(vf.getVariation().getName(), vf.getVariationName());    
    assertEquals(vf.getVariation().getInternalID(), vf.getVariationInternalID());    

  }
  
  
  public void testFetchByIDs() throws Exception{
  
    
    long[] ids = new long[]{9, 2573771, 2309150, 2309698}; 
    List vfs = vdriver.getVariationFeatureAdaptor().fetch(ids);
  
    LongSet idsSet = new LongSet(ids);
    LongSet vfsSet = new LongSet(vfs, Persistent.class);
    assertEquals(idsSet, vfsSet);
  }
  
  public void testFetchByLocation() throws Exception {
    Location loc = new Location("chromosome:20:20m-20.001m");
    List vfs = vdriver.getVariationFeatureAdaptor().fetch( loc );
    assertTrue(vfs.size()>0);
    for (int i = 0; i < vfs.size(); i++) {
      VariationFeature vf = (VariationFeature) vfs.get(i);
      assertTrue(vf.getLocation().overlaps(loc,true));
    }

  }
  
  public void testFetchByVariation() throws Exception {
    
    Variation v = vdriver.getVariationAdaptor().fetch(1889);
    assertNotNull(v);
    List vfs = vdriver.getVariationFeatureAdaptor().fetch(v);
    
    assertTrue(vfs.size()>0);
    assertTrue(vfs.get(0) instanceof VariationFeature);
    
  }
  
  public void testFetchByLocationIterator() throws Exception {
    assertNotNull("No variation features", vdriver.getVariationFeatureAdaptor().fetchIterator(new Location("chromosome:X:1-1m")).next());
  }
}
