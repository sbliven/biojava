/*
 * GenomicAlignAdaptorTest.java
 * JUnit based test
 *
 * Created on December 13, 2004, 4:34 PM
 */

package org.ensembl.compara.test;

import java.util.logging.Logger;

import org.ensembl.compara.datamodel.GenomicAlign;
import org.ensembl.compara.driver.GenomicAlignAdaptor;

/**
 * 
 * @author vvi
 */
public class GenomicAlignAdaptorTest extends ComparaBase {

  private static Logger logger = Logger.getLogger(GenomeDBAdaptorTest.class
      .getName());

  public GenomicAlignAdaptorTest(String testName) throws Exception {
    super(testName);
  }


  public void testFetch() throws Exception {
    
    GenomicAlignAdaptor theAdaptor = comparaDriver.getGenomicAlignAdaptor();
    
    for (int i = 1; i < 100; i++) {
      GenomicAlign align = theAdaptor.fetch(i);
    }
    
  }
}