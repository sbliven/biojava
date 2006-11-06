/*
 * DnaDnaAlignFeatureAdaptorTest.java
 * JUnit based test
 *
 * Created on December 13, 2004, 4:34 PM
 */

package org.ensembl.compara.test;

import java.util.List;
import java.util.logging.Logger;

import org.ensembl.datamodel.Location;

/**
 * 
 * @author vvi
 */
public class DnaDnaAlignFeatureAdaptorTest extends ComparaBase {
  private static Logger logger = Logger.getLogger(GenomeDBAdaptorTest.class
      .getName());

  public DnaDnaAlignFeatureAdaptorTest(String testName) throws Exception {
    super(testName);
  }


  public void testFetch() {
    try {
      Location tmp = new Location("chromosome:1:1000000-1200000:0");
      List theDnaAligns = comparaDriver.getDnaDnaAlignFeatureAdaptor().fetch("Homo sapiens", tmp, "Mus musculus",
          "BLASTZ_NET");

      for (int i = 0; i < theDnaAligns.size() && i < 100; i++) {
        logger.info(theDnaAligns.get(i).toString());
      }// end for

    } catch (Throwable exception) {
      exception.printStackTrace();
      fail(exception.getMessage());
    }// end try
  }

}
