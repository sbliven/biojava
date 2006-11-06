/*
 * DnaFragmentAdaptorTest.java
 * JUnit based test
 *
 * Created on December 13, 2004, 4:34 PM
 */

package org.ensembl.compara.test;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.ensembl.compara.datamodel.GenomeDB;
import org.ensembl.test.Base;

/**
 * 
 * @author vvi
 */
public class DnaFragmentAdaptorTest extends ComparaBase {

  private static Logger logger = Logger.getLogger(DnaFragmentAdaptorTest.class
      .getName());

  public DnaFragmentAdaptorTest(String testName) throws Exception {
    super(testName);
  }


  public void testFetch() throws Exception {

    GenomeDB genome2 = comparaDriver.getGenomeDBAdaptor().fetch("Homo sapiens",
        Base.LATEST_HUMAN_CHROMOSOME_VERSION);

    logger.fine("Retrieving dna fragments for by chromosome, start/end");
    List frags = comparaDriver.getDnaFragmentAdaptor().fetch(genome2,
        "Chromosome", null);

    for (Iterator fragIterator = frags.iterator(); fragIterator.hasNext();)
      logger.info(fragIterator.next().toString());

    assertEquals(true, frags.size() > 0);

  }

}