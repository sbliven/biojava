package org.ensembl.compara.test;

import java.util.Iterator;
import java.util.logging.Logger;

import org.ensembl.compara.datamodel.GenomeDB;
import org.ensembl.compara.driver.GenomeDBAdaptor;
import org.ensembl.test.Base;

public class GenomeDBAdaptorTest extends ComparaBase {

  private static Logger logger = Logger.getLogger(GenomeDBAdaptorTest.class
      .getName());

  public GenomeDBAdaptorTest(String testName) throws Exception {
    super(testName);
  }

  public void testFetch() throws Exception {

    logger.fine("Retrieving genome dbs");

    GenomeDBAdaptor theAdaptor = comparaDriver.getGenomeDBAdaptor();
    Iterator objects = theAdaptor.fetch().iterator();

    GenomeDB object;
    while (objects.hasNext()) {
      object = (GenomeDB) objects.next();
      logger.info("genomeDB: " + object.toString());
    }

    object = theAdaptor.fetch(3);
    logger.info(object.toString());

    object = theAdaptor.fetch("Homo sapiens",
        Base.LATEST_HUMAN_CHROMOSOME_VERSION);
    logger.info(object.toString());

    object = theAdaptor.fetch("Mus musculus");
    logger.info(object.toString());


  }
}