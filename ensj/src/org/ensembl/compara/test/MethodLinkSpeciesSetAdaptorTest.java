/*
 * MethodLinkSpeciesSetAdaptorTest.java JUnit based test
 * 
 * Created on December 20, 2004, 9:45 AM
 */

package org.ensembl.compara.test;

import java.util.Iterator;
import java.util.logging.Logger;

public class MethodLinkSpeciesSetAdaptorTest extends ComparaBase {

  private static Logger logger = Logger.getLogger(GenomeDBAdaptorTest.class
      .getName());

  public MethodLinkSpeciesSetAdaptorTest(String testName) throws Exception {
    super(testName);
  }

  public void testFetch() throws Exception {

    int count = 0;
    
    for(Iterator list = comparaDriver.getMethodLinkSpeciesSetAdaptor().fetch()
        .iterator(); list.hasNext(); ) {
      count++;
      logger.info(list.next().toString());
    }
    
    if (count == 0) 
      fail("No MSLL records found");
    
  }

}