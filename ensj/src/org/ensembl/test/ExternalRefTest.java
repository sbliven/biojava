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
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ensembl.datamodel.ExternalRef;
import org.ensembl.datamodel.Translation;
import org.ensembl.driver.ExternalRefAdaptor;

/**
 * Test class for Genes.
 */
public class ExternalRefTest extends CoreBase {

  public static final void main(String[] args) throws Exception {
    junit.textui.TestRunner.run(suite());
  }

  public ExternalRefTest(String name){
    super(name);
  }

  public static Test suite() {
    return new TestSuite(ExternalRefTest.class);
  }


  protected void setUp() throws Exception {
    super.setUp();
    externalRefAdaptor = (ExternalRefAdaptor)driver.getAdaptor("external_ref");

  }


  public void testFetchExternalRefsBySynonym()  throws Exception {

    final String NAME = "SIS";
    List externalRefs = externalRefAdaptor.fetch(NAME);
    assertNotNull(externalRefs);
    assertTrue(externalRefs.size() > 0);
    // there should be on xref returned
    for(Iterator xIt = externalRefs.iterator();xIt.hasNext();) {
      ExternalRef xRef = (ExternalRef)xIt.next();
      assertNotNull(xRef);
      
      // check that the externalDB can be loaded
      assertNotNull(xRef.getExternalDatabase());
      
      // The synonym might appear as the displayID or a
      // synonym
      boolean found = xRef.getDisplayID().equals(NAME);
      for(Iterator it=xRef.getSynonyms().iterator();!found && it.hasNext();) {
        String synonym = (String)it.next();
        if(synonym.equals(NAME)) {
          found = true;
        }
      }
      // make sure we found the synonym we were looking for
      if(found == false) {
        fail("Could not find synonym " + NAME + " in xRef " + xRef.getInternalID() + " that we searched for");
      }
    }
  }
 
  
  public void testFetchByDBPrimaryID() throws Exception {

    final String DB_PRIMARY_ID = "P01112";
    List externalRefs = externalRefAdaptor.fetch(DB_PRIMARY_ID);
    assertNotNull(externalRefs);
    assertTrue(externalRefs.size() > 0);
    for(Iterator xIt = externalRefs.iterator();xIt.hasNext();) {
      ExternalRef xRef = (ExternalRef)xIt.next();
      assertNotNull(xRef);
      assertEquals(xRef.getPrimaryID(),DB_PRIMARY_ID);

      // check identity is available - NOTE not all entries have identity info
      // so need to choose xrefs that do
      assertTrue(xRef.getQueryIdentity()>0);
      assertTrue(xRef.getTargetIdentity()>0);
      

    }
  }
    
  public void testFetchByDisplayID() throws Exception {
    
    final String DISPLAY_ID = "206548_at";
    List externalRefs = externalRefAdaptor.fetch(DISPLAY_ID);
    assertNotNull(externalRefs);
    assertTrue(externalRefs.size() > 0);
    // there should be on xref returned
    for(Iterator xIt = externalRefs.iterator();xIt.hasNext();) {
       ExternalRef xRef = (ExternalRef)xIt.next();
       assertNotNull(xRef);
       assertTrue(DISPLAY_ID.equals(xRef.getDisplayID()));
    }

  }


  public void testFetchByExternalIdAndType() throws Exception {
    List externalRefs;

    // must pick an ID that is translationID with an xref but not a geneID with an xref.
    long tnID = 21735;
    
    externalRefs = externalRefAdaptor.fetch(tnID,ExternalRef.TRANSLATION);
    assertTrue(externalRefs.size() > 0);

    externalRefs = externalRefAdaptor.fetch(tnID,ExternalRef.GENE);
    assertEquals(externalRefs.size(),0);
  }



  public void testExonTranscriptLazyLoading() throws Exception {

    Translation translation = driver.getTranslationAdaptor().fetch( 1 );
    List refs = translation.getExternalRefs();
    assertNotNull(refs);

  }

  public void testInfoFields() throws Exception {
    // use rat because it contains projected xrefs
    List xrefs = registry.getGroup("rat").getCoreDriver().getExternalRefAdaptor().fetch(4347759);
    assertTrue(xrefs.size()>0);
    for (int i = 0; i < xrefs.size(); i++) {
      ExternalRef xref = (ExternalRef) xrefs.get(i);
      assertNotNull(xref.getInfoType());
      assertNotNull(xref.getInfoText());
    }
  }
  
  // private -------------------------------------------------------------------


  private ExternalRefAdaptor externalRefAdaptor;

}// ExternalRefTest
