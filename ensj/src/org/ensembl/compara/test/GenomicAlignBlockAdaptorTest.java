/*
 * GenomicAlignBlockAdaptorTest.java
 * JUnit based test
 *
 * Created on December 23, 2004, 4:57 PM
 */

package org.ensembl.compara.test;

import java.util.List;
import java.util.logging.Logger;

import org.ensembl.compara.datamodel.DnaFragment;
import org.ensembl.compara.datamodel.GenomeDB;
import org.ensembl.compara.datamodel.MethodLink;
import org.ensembl.compara.datamodel.MethodLinkSpeciesSet;
import org.ensembl.compara.driver.DnaFragmentAdaptor;
import org.ensembl.compara.driver.GenomeDBAdaptor;
import org.ensembl.compara.driver.GenomicAlignBlockAdaptor;
import org.ensembl.compara.driver.MethodLinkAdaptor;
import org.ensembl.compara.driver.MethodLinkSpeciesSetAdaptor;
import org.ensembl.test.Base;
import org.ensembl.util.IDSet;

/**
 * 
 * @author vvi
 */
public class GenomicAlignBlockAdaptorTest extends ComparaBase {
  private static Logger logger = Logger.getLogger(GenomeDBAdaptorTest.class
      .getName());

  public GenomicAlignBlockAdaptorTest(String name) throws Exception {
    super(name);
  }

  public void testFetch() throws Exception {

    GenomicAlignBlockAdaptor theAdaptor = comparaDriver
        .getGenomicAlignBlockAdaptor();
    
    DnaFragmentAdaptor dnaFragAdaptor = comparaDriver.getDnaFragmentAdaptor();

    GenomeDBAdaptor genomeDBAdaptor = comparaDriver.getGenomeDBAdaptor();

    GenomeDB genomeDB = genomeDBAdaptor.fetch("Homo sapiens",
        Base.LATEST_HUMAN_CHROMOSOME_VERSION);

    GenomeDB targetGenomeDB = genomeDBAdaptor.fetch("Mus musculus",
        Base.LATEST_MOUSE_CHROMOSOME_VERSION);

    MethodLinkAdaptor mlAdaptor = comparaDriver.getMethodLinkAdaptor();

    MethodLinkSpeciesSetAdaptor mlSSAdaptor = comparaDriver.getMethodLinkSpeciesSetAdaptor();

    GenomeDB[] genomes = new GenomeDB[2];
    genomes[0] = genomeDB;
    genomes[1] = targetGenomeDB;

    MethodLink link = mlAdaptor.fetch("BLASTZ_NET");

    MethodLinkSpeciesSet set = (MethodLinkSpeciesSet) mlSSAdaptor.fetch(link,
        genomes).get(0);
    logger.info(set.toString());

    DnaFragment fragment = (DnaFragment) dnaFragAdaptor.fetch(genomeDB,
        "Chromosome", "10").get(0);

    List genomicAlignBlocks = theAdaptor.fetch(set, fragment, 500000, 600000);

    for (int i = 0; i < genomicAlignBlocks.size() && i < 100; i++) 
      logger.info(genomicAlignBlocks.get(i).toString());

  }
  
  public void testFetchByArray() throws Exception {
    long[] ids = new long[]{1490002520584l, 1490002520572l};
    List blocks = comparaDriver.getGenomicAlignBlockAdaptor()
    .fetch(ids);
    assertEquals(ids.length, blocks.size());
    IDSet idsSet = new IDSet(blocks);
    for (int i = 0; i < ids.length; i++) 
      idsSet.contains(ids[i]);
    
  }
}