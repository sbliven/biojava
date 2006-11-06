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

import org.ensembl.datamodel.ProteinFeature;
import org.ensembl.datamodel.Translation;
import org.ensembl.driver.CoreDriver;
import org.ensembl.driver.ProteinFeatureAdaptor;
import org.ensembl.driver.TranslationAdaptor;

public class ProteinFeatureTest extends CoreBase {

  private static Logger logger = Logger.getLogger( ProteinFeatureTest.class.getName() );


  public ProteinFeatureTest( String name ) {
    super( name );
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();
    //suite.addTest( new SimplePeptideFeatureTest("testRetreiveByTranslationObject") );
    suite.addTestSuite(ProteinFeatureTest.class);
    return suite;
  }


  private ProteinFeatureAdaptor getAdaptor(CoreDriver driver, String type) throws Exception {
    Object tmp = driver.getAdaptor( type );
    if ( tmp!=null ) return ( ProteinFeatureAdaptor )tmp;
    else return null;
  }

  protected void setUp() throws Exception {
    super.setUp();
    simplePeptideAdaptor = getAdaptor( driver, "simple_peptide_feature" );
    translationAdaptor = (TranslationAdaptor)driver.getAdaptor("translation");
  }


  public void testRetreiveByTranslationObject()  throws Exception {
    String tStableID = "ENSP00000230449";
    Translation t = translationAdaptor.fetch( tStableID );
    List spfs = simplePeptideAdaptor.fetch( t );
    assertTrue( "Failed to retrieve simple peptides for "+ tStableID, spfs.size()>0 );
    logger.fine( spfs.size() + ", " + spfs );
  }


  public void testRetreiveByID()  throws Exception {
    long id = 405764;
    ProteinFeature spf = simplePeptideAdaptor.fetch( id );
    assertNotNull( "Failed to retrieve simple peptides with id= "+ id, spf );
    assertEquals(id, spf.getInternalID());
    
    assertNotNull( "Failed to retrieve simple peptide's translation", spf.getTranslation() );
  }


  private ProteinFeatureAdaptor simplePeptideAdaptor;
  private TranslationAdaptor translationAdaptor;
}
