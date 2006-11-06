/*
 * Copyright (C) 2002 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.test;

import java.sql.Connection;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import junit.framework.TestSuite;

import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.Gene;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.SequenceRegion;
import org.ensembl.datamodel.Transcript;
import org.ensembl.datamodel.Translation;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.impl.GeneAdaptorImpl;
import org.ensembl.util.IDSet;
import org.ensembl.util.JDBCUtil;

/**
 * JUnit tests for GeneAdaptor / GeneAdaptorImpl
 */
public class GeneAdaptorTest extends CoreBase {

  private static final Logger logger =
    Logger.getLogger(GeneAdaptorTest.class.getName());

  GeneAdaptorImpl geneAdaptor = null;

  CoordinateSystem chromosomeCS =
    new CoordinateSystem("chromosome", Base.LATEST_HUMAN_CHROMOSOME_VERSION);
  Location chr1Loc1 = new Location(chromosomeCS, "1", 1, 100000, 1);
  Location chr1Loc2 = new Location(chromosomeCS, "1", 1, 1000000, 1);

  // -----------------------------------------------------------------

  public GeneAdaptorTest(String arg0) {
    super(arg0);
  }
  


  public static TestSuite suite() {
    TestSuite s = new TestSuite();
    //s.addTest(new GeneAdaptorTest("testInterproSupport"));
    s.addTestSuite(GeneAdaptorTest.class);
    return s;
  }

  //	-----------------------------------------------------------------

  public static void main(String[] args) {
    junit.textui.TestRunner.run(GeneAdaptorTest.class);
  }

  //	-----------------------------------------------------------------

  /*
   * @see TestCase#setUp()
   */
  protected void setUp() throws Exception {

    super.setUp();
    geneAdaptor = (GeneAdaptorImpl) driver.getGeneAdaptor();
  }

  
  public void testFetchByLocation() throws Exception {
    List genes = driver.getGeneAdaptor().fetch(new Location("chromosome:1:1-1m"));
    assertTrue(genes.size()>0);
  }
  
  //	-----------------------------------------------------------------

  public void testFetchByPARLocation() throws Exception {

    // check fetch loc on a PAR (psedo autosomal region) because that's even harder than a simple 
    // seq region. Genes on the Y chromosome are stored on both the Y chromosome
    // directly and indirectly on 'overlapping' parts of X.
    CoordinateSystem cs = driver.getCoordinateSystemAdaptor().fetch("chromosome",null);
    SequenceRegion sr = driver.getSequenceRegionAdaptor().fetch("Y", cs);
    List features = geneAdaptor.fetch(new Location(cs, sr));

    assertNotNull(features);
    assertTrue(features.size() > 0);

    boolean xStartHit = false;
    boolean yMiddleHit = false;
    boolean xEndHit = false;
    Set regionsHit = new HashSet();
    for (Iterator it = features.iterator(); it.hasNext();) {
      Gene g = (Gene) it.next();
      Location loc = g.getLocation();
      assertEquals("Gene on wrong seq region", sr.getName(), loc.getSeqRegionName());
      assertTrue(loc.getEnd()<sr.getLength());
      assertTrue(loc.getStart()>0);
      assertTrue(loc.getStrand()!=0);
      if (loc.getStart()<2000000) // approximate end of "first" x region in y
        xStartHit = true;
      else if (loc.getStart()>57372175) // approximate start of "final" x region in y
        xEndHit = true;
      else 
        yMiddleHit = true;
    }
    
    // ensure genes found across the chromosome. This will ensure
    // all PAR parts have been loaded from X chromosome 
    assertTrue("No genes in region from x at beggining of y chromosome", xStartHit);
    assertTrue("No genes in region from x at end of y chromosome", xEndHit);
    assertTrue("No genes in region at the middle of y chromosome", yMiddleHit);
  }

  
  public void testFetchByHAPLocation() throws Exception {
    
    String chr6 = "chromosome:6";
    String chr6other = "chromosome:c6_QBL";
    
    // DR52 is stored in the db as a HAPlotype
    IDSet six = new IDSet(driver.getGeneAdaptor().fetchIterator(new Location(chr6), false));
    IDSet sixOther = new IDSet(driver.getGeneAdaptor().fetchIterator(new Location(chr6other), false));
    logger.info("chr6: " + six.size() + "\t" + six);
    logger.info("dr52: " + sixOther.size() + "\t" + sixOther);
    
    
    // ensure fetchIterator and fetchInternalIDs produce the same result
    IDSet sixArray = new IDSet(driver.getGeneAdaptor().fetchInternalIDs(new Location(chr6)));
    IDSet sixOtherArray = new IDSet(driver.getGeneAdaptor().fetchInternalIDs(new Location(chr6other)));
    assertEquals(six, sixArray);
    assertEquals(sixOther, sixOtherArray);
    
    // Check that chr6 and chr6other have overlapping genes and some that are unique to each one.
    // WARNING: This ASSUMES that chr6other is stored as a haplotype of chr6 in the db.
    // If this is not the case then the tests will probably fail even if the code
    // and data are correct.
    IDSet justChr6other = new IDSet(sixOther);
    boolean chr6oherHadGenesOnChr6 = justChr6other.removeAll(six);
    IDSet justSix = new IDSet(six);
    boolean chr6HadGenesOnChr = justSix.removeAll(sixOther);
    logger.info("just "+chr6+" : " + justSix.size() + "\t" + justSix);
    logger.info("just "+chr6other+" : " + justChr6other.size() + "\t" + justChr6other);
    assertTrue("No overlap between gene sets on "+chr6+" and "+chr6other+" (acceptable if that is how data is stored in db)", 
        chr6HadGenesOnChr && chr6oherHadGenesOnChr6);
    assertTrue(chr6other+" should have some genes that don't lie on "+chr6, justChr6other.size()>0);
    assertTrue(chr6+" should have some genes that don't lie on "+chr6other, justSix.size()>0);
  }
  
  //	-----------------------------------------------------------------

  public void testFetchByInternalID() throws Exception {

    long geneID = 1;
    Gene g  = geneAdaptor.fetch(geneID);
    assertNotNull(g);
    assertEquals(geneID, g.getInternalID());

  }

  //	-----------------------------------------------------------------

  public void testFetchByAccessionID() {

    Gene g = null;
    try {
      g = geneAdaptor.fetch("ENSG00000171456");
    } catch (AdaptorException e) {
      e.printStackTrace();
    }
    assertNotNull(g);

  }
  //---------------------------------------------------------------------

  public void testFetchBySynonym() throws Exception {

    String synonym = "NM_020974";
    List genes = geneAdaptor.fetchBySynonym(synonym);
    assertNotNull(genes);
    assertEquals(
      "Found wrong number of genes with synonym = " + synonym,
      1,
      genes.size());

  }

  //---------------------------------------------------------------------

  public void testInterproSupport() throws Exception {

    String id = "IPR000405";
    List gs = geneAdaptor.fetchByInterproID(id);
    assertTrue(gs.size() > 0);
    Gene g = (Gene) gs.get(0);
    List ts = g.getTranscripts();

    // check id occurs on at least one constituent translation
    boolean found = false;
    for (int i = 0, n = ts.size(); found == false && i < n; i++) {
      Transcript t = (Transcript) ts.get(i);
      Translation tn = t.getTranslation();
      if (tn != null) {
        String[] interproIDs = tn.getInterproIDs();
        Arrays.sort(interproIDs);
        if (Arrays.binarySearch(interproIDs, id) > -1)
          found = true;
      }
    }
    assertTrue(
      "No translation with specified interproID found on the gene. InterproID= "
        + id
        + " gene="
        + g,
      found);

    // check that id occurs on gene
    found = false;
    String[] interproIDs = g.getInterproIDs();
    Arrays.sort(interproIDs);
    if (Arrays.binarySearch(interproIDs, id) > -1)
      found = true;
    assertTrue("interproID:" + id + " not associated with gene: " + g, found);

  }
  
  
  public void testUnpopulatedGeneAndMetaCoord() throws Exception {
    
    assertNotNull(UNINITIALISED_TEST_DB_CORE_DRIVER_ERROR,testCoreDriver);
	  
    // Test the adaptor handles the case where there
    // are no genes in the gene table and no "gene" reference in the 
    // and meta_cood table. 
    
    Connection conn = null;
    try {
      
      // test 1 - completely empty the meta_coord table
      testCoreDriver.backupAndClearTable("gene");
      testCoreDriver.backupAndClearTable("meta_coord");
      testCoreDriver.clearAllCaches();
      assertTrue(testCoreDriver.getGeneAdaptor().fetch(new Location("chromosome:20")).size()==0);
      
      // test 2 - just remove the "gene" entry from the meta_coord table
      testCoreDriver.restoreTable("meta_coord");
      conn = testCoreDriver.getConnection();
      conn.createStatement().execute("delete from meta_coord where table_name=\"gene\"");
      conn.close();
      testCoreDriver.clearAllCaches();
      assertTrue(testCoreDriver.getGeneAdaptor().fetch(new Location("chromosome:20")).size()==0);
      
    } finally {
      testCoreDriver.restoreTable("gene");
      testCoreDriver.restoreTable("meta_coord");
      JDBCUtil.close(conn);
    }
    assertTrue(testCoreDriver.getGeneAdaptor().fetchCount()>0);
    
    
  }
}
