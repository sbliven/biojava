/*
Copyright (C) 2005 EBI, GRL

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

import org.ensembl.datamodel.AssemblyException;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.SequenceRegion;

/**
 * Tests the assembly exception support.
 * 
 * @see org.ensembl.datamodel.AssemblyException
 * @see org.ensembl.driver.AssemblyExceptionAdaptor
 */
public class AssemblyExceptionTest extends CoreBase {

  private static final Logger logger = Logger
      .getLogger(AssemblyExceptionTest.class.getName());
  
  public AssemblyExceptionTest(String name) {
    super(name);
  }

  public void testFetchByInternalID() throws Exception {
    final long id = 8;
    AssemblyException ae = driver.getAssemblyExceptionAdaptor().fetch(id);
    assertEquals(id, ae.getInternalID());
    check(ae, null);
  }
  
  

  public void testAll() throws Exception {
    check(driver.getAssemblyExceptionAdaptor().fetch(), null);
  }

  public void testFetchByLocation() throws Exception {
    
    Location loc = new Location("chromosome:c5_H2");
    check(driver.getAssemblyExceptionAdaptor().fetch(loc), loc);
    assertEquals(0, driver.getAssemblyExceptionAdaptor().fetch(new Location("chromosome:X")).size());    
  }

  
  public void testDereferenceAndRereference() throws Exception {
    
    //logger.setLevel(Level.FINE);
    
    // DR52 chromosome is stored as a haplotype
    String srName = "c5_H2";
    Location loc = new Location("chromosome:"+srName);
    loc = driver.getLocationConverter().fetchComplete(loc);
    logger.fine("LOC: " + loc);
    logger.fine("LOC: " + loc);
    assertTrue(loc.getLength()>0);
    SequenceRegion sr = driver.getSequenceRegionAdaptor().fetch(loc.getSeqRegionName(), loc.getCoordinateSystem()); // need this for later reref
    
    assertNotNull(sr);
    Location derefLoc = driver.getAssemblyExceptionAdaptor().dereference(loc);
    logger.fine("DEREFLOC: "+derefLoc);
    assertNotNull(derefLoc);
    assertEquals("Dereferenced location should be same size as "+srName, loc.getLength(), derefLoc.getLength());
    assertTrue("Dereferenced location doesn't contain any 'raw' "+srName+" components", loc.overlapSize(derefLoc)>0);
    
    Location rerefLoc = driver.getAssemblyExceptionAdaptor().rereference(derefLoc, sr);
    logger.fine("REREFLOC: "+rerefLoc);
    assertEquals("Rereferenced location should be same size as "+srName, loc.getLength(), rerefLoc.getLength());
    assertEquals("Rereferenced location should overlap "+srName+" exactly", rerefLoc.getLength(), loc.overlapSize(loc));
    for(Location head=rerefLoc;head!=null;head=head.next())
      assertEquals("Referenced location should only have "+srName+" components in it:", srName, head.getSeqRegionName());
    
    
    
    // Y chromosome is stored as is a PAR
    loc = new Location("chromosome:Y");
    loc = driver.getLocationConverter().fetchComplete(loc);
    logger.fine("LOC: " + loc);
    assertTrue(loc.getLength()>0);
    sr = driver.getSequenceRegionAdaptor().fetch(loc.getSeqRegionName(), loc.getCoordinateSystem()); // need this for later reref
    
    assertNotNull(sr);
    derefLoc = driver.getAssemblyExceptionAdaptor().dereference(loc);
    logger.fine("DEREFLOC: "+derefLoc);
    assertNotNull(derefLoc);
    assertEquals("Dereferenced location should be same size as Y", loc.getLength(), derefLoc.getLength());
    assertTrue("Dereferenced location doesn't contain any 'raw' Y components", loc.overlapSize(derefLoc)>0);
    
    rerefLoc = driver.getAssemblyExceptionAdaptor().rereference(derefLoc, sr);
    logger.fine("REREFLOC: "+rerefLoc);
    assertEquals("Rereferenced location should be same size as Y", loc.getLength(), rerefLoc.getLength());
    assertEquals("Rereferenced location should overlap Y exactly", rerefLoc.getLength(), loc.overlapSize(loc));
    for(Location head=rerefLoc;head!=null;head=head.next())
      assertEquals("Referenced location should only have Y components in it:", "Y", head.getSeqRegionName());

    
  }

  private void check(AssemblyException ae, Location srcLocation) {
    
    assertNotNull(ae);
    assertTrue(ae.getInternalID()>0);
    assertNotNull(ae.getLocation());
    assertNotNull(ae.getTarget());
    assertNotNull(ae.getType());
    if (srcLocation!=null)
      ae.getLocation().overlaps(srcLocation);
    
  }

  private void check(List list, Location srcLocation) {
    
    assertTrue("No assembly exceptions corresponding to location:"+srcLocation + driver, list.size()>0);
    
    for (Iterator iter = list.iterator(); iter.hasNext();) 
      check((AssemblyException) iter.next(), srcLocation);
      
  }

}
