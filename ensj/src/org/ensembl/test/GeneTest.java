/*
 * Copyright (C) 2003 EBI, GRL
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ensembl.datamodel.Analysis;
import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.Exon;
import org.ensembl.datamodel.ExternalRef;
import org.ensembl.datamodel.Gene;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Persistent;
import org.ensembl.datamodel.Transcript;
import org.ensembl.datamodel.Translation;
import org.ensembl.driver.CoordinateSystemAdaptor;
import org.ensembl.driver.GeneAdaptor;
import org.ensembl.driver.LocationConverter;
import org.ensembl.util.StringUtil;
import org.ensembl.util.Timer;

/**
 * Test class for Genes.
 */
public class GeneTest extends CoreBase {

  private class InternalIDComparator implements Comparator {
    public int compare(Object o1, Object o2) {
      return (int)
        (((Persistent) o1).getInternalID() - ((Persistent) o2).getInternalID());
    }
  };

  private static Logger logger = Logger.getLogger(GeneTest.class.getName());

  private static boolean useDefaultInitialisation = true;

  // choose a gen that has a display name (some don't)
  private final int geneID = 1;

  private final String accession = "ENSG00000186239";

  private CoordinateSystemAdaptor coordinateSystemAdaptor;

  public static final void main(String[] args) throws Exception {
    if (args.length == 0)
      junit.textui.TestRunner.run(suite());
    else {
      TestSuite suite = new TestSuite();
      for (int i = 0; i < args.length; ++i)
        suite.addTest(new GeneTest(args[i]));
      junit.textui.TestRunner.run(suite);
    }
  }

  public GeneTest(String name) {
    super(name);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();
//    suite.addTest(new GeneTest("testEquivalentRegionFetches"));
//    suite.addTest(new GeneTest("testRecursiveCoordinateSystemSetting"));
////    suite.addTest(new GeneTest("testDisplayNameSet"));
//    //suite.addTest(new GeneTest("testGeneSerialisation"));
    suite.addTestSuite(GeneTest.class);
    return suite;
  }

  protected void setUp() throws Exception {
    super.setUp();
    geneAdaptor = (GeneAdaptor) driver.getAdaptor("gene");
    locationConverter =
      (LocationConverter) driver.getAdaptor("location_converter");
    coordinateSystemAdaptor = driver.getCoordinateSystemAdaptor();
  }

  public void testTimeGeneRetrieval() throws Exception {

    int runs = 5;

    String accession = "ENSG00000139618";

    long then = System.currentTimeMillis();

    for (int i = 0; i < runs; i++) {
      Gene g = geneAdaptor.fetch(accession);
    }

    long now = System.currentTimeMillis();

    float time = (now - then) / (runs * 1000);
    logger.fine(
      "Retrieval of "
        + accession
        + " averaged over "
        + runs
        + " runs: "
        + time
        + "s");

  }

  public void testAnalysisSet() throws Exception {
    Gene g = geneAdaptor.fetch(geneID);
    Analysis a = g.getAnalysis();
    assertNotNull("analysis not set", a);
    assertEquals(
      "analysis internal id different to the one in gene.",
      a.getInternalID(),
      g.getAnalysisID());
  }

  public void testBasicAttributes() throws Exception {
    // note that some genes DON'T have a display name so we must choose ones that do.
    Gene g = geneAdaptor.fetch(geneID);
    assertNotNull("Gene not found with internalID = " + geneID, g);
    assertNotNull("No DisplayID for gene " + geneID, g.getDisplayName());

    String acc = "ENSG00000176679";
    g = geneAdaptor.fetch(acc);
    assertNotNull("Gene not found with accession = " + acc, g);
    assertNotNull(g.getDisplayName());
    assertNotNull(g.getStatus());
  }

  public void testExternalRefs() throws Exception {
    // gene should ideally have multiple translations which share xrefs. Such
    // a gene can be be found by this approach:
    // (1) find xrefs shared by >1 translation
    //     select xref_id, count(distinct(ensembl_id)) as nTranslations, gene_id 
  	//     from object_xref, translation tn, transcript tt 
  	//     where ensembl_id=tn.translation_id and ensembl_object_type="Translation" 
  	//        and tn.transcript_id=tt.transcript_id group by xref_id 
  	//     having nTranslations>1 limit 10;
    // (2) look for gene with > one of these translations (have to look
    // manually)
    Gene g = geneAdaptor.fetch(1);
    Set gRefs = xRefsToDisplayIDs(g.getExternalRefs(true));
    Set tRefs = new HashSet();
    Set tnRefs = new HashSet();
    assertTrue("Gene has no external refs", gRefs.size() > 0);
    assertEquals(
      "Gene contains duplicate xrefs",
      gRefs.size(),
      g.getExternalRefs().size());

    for (int i = 0; i < g.getTranscripts().size(); ++i) {
      Transcript t = (Transcript) g.getTranscripts().get(i);
      tRefs.addAll(xRefsToDisplayIDs(t.getExternalRefs(true)));
      Translation tn = t.getTranslation();
      tnRefs.addAll(xRefsToDisplayIDs(tn.getExternalRefs()));
    }
    System.out.println(
      "#gRefs = " + gRefs.size() + "\t" + StringUtil.toString(gRefs));
    System.out.println(
      "#tRefs = " + tRefs.size() + "\t" + StringUtil.toString(tRefs));
    System.out.println(
      "#tnRefs = " + tnRefs.size() + "\t" + StringUtil.toString(tnRefs));
    assertTrue(
      "Transcripts should have same or more xrefs",
      tRefs.size() >= tnRefs.size());
    assertTrue(
      "Gene should have same or more xrefs than all it's translations",
      gRefs.size() >= tnRefs.size());
  }

  private Set xRefsToDisplayIDs(List xrefs) {
    Set ids = new HashSet();
    for (int r = 0; r < xrefs.size(); ++r) {
      ids.add(((ExternalRef) (xrefs.get(r))).getDisplayID());
    }
    return ids;
  }

  public void testEquivalentRegionFetches() throws Exception {

    //Location chrLoc = new Location("chromosome:22:21m-21.05m");
    // Must choose a location that location conversion can can be done between
    // in both directions.
    Location chrLoc = new Location("chromosome:22:23m-23.05m");
    Location contigLoc =
      driver.getLocationConverter().convert(
        chrLoc,
        new CoordinateSystem("contig"));
    List chrLocGenes = geneAdaptor.fetch(chrLoc);
    List contigLocGenes = geneAdaptor.fetch(contigLoc);

    logger.fine("chrLoc: " + chrLoc.toString(true, true));
    logger.fine("contigLoc: " + contigLoc.toString(true, true));
    logger.fine(
      "chrLocGenes: " + chrLocGenes.size() + "::" + listToIds(chrLocGenes));
    logger.fine(
      "contigLocGenes: "
        + contigLocGenes.size()
        + "::"
        + listToIds(contigLocGenes));

    assertEquals(
      "Loaded different number of genes but should the same because same region: "
        + chrLoc.toString(true,true) + " == " + contigLoc.toString(true,true),
      chrLocGenes.size(),
      contigLocGenes.size());
  }

  public String listToIds(List l) {
    List l2 = new ArrayList(l);
    Collections.sort(l2, new InternalIDComparator());
    StringBuffer buf = new StringBuffer();
    for (int i = 0, n = l2.size(); i < n; i++) {
      if (i > 0)
        buf.append(", ");
      Persistent e = (Persistent) l2.get(i);
      buf.append(e.getInternalID());
    }

    return buf.toString();
  }

  public void testFetchByInternalIDs() throws Exception {
    long[] ids = { 5, 7, 10 };
    List genes = geneAdaptor.fetch(ids);
    for (int i = 0; i < ids.length; i++) {
      Gene g = (Gene) genes.get(i);
      assertEquals(
        "Gene order is different to array",
        ids[i],
        g.getInternalID());
      assertNotNull("Gene type not set", g.getBioType());
    }

  }

  public void testGeneSerialisation() throws Exception {

    // find a suitable gene
    // mysql> select gene_id from translation t, translation_attrib ta, transcript tt where ta.translation_id=t.translation_id and tt.transcript_id=t.transcript_id limit 10;

    Gene g = geneAdaptor.fetch(geneID);
    g.getTranscripts(); // force lazy load
    g.getExons(); // force lazy load
    // NO ATTRIBUTES FOR TRANSLATIONS IN human 26_35 DB
    // force lazy load before serialize
//    List as =
//      ((Transcript) (g.getTranscripts().get(0)))
//        .getTranslation()
//        .getAttributes();
    //assertTrue(as.size() > 0);

    File tmp = File.createTempFile("genetest", "ser");
    OutputStream os = new FileOutputStream(tmp);
    ObjectOutputStream oos = new ObjectOutputStream(os);
    oos.writeObject(g);
    os.close();

    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(tmp));
    Gene g2 = (Gene) ois.readObject();
    ois.close();
//    List as2 =
//      ((Transcript) (g2.getTranscripts().get(0)))
//        .getTranslation()
//        .getAttributes();
//    
    //assertEquals(as.size(), as2.size());
    assertEquals(g.getAccessionID(), g2.getAccessionID());
    assertEquals(g.getExons().size(), g2.getExons().size());
    assertEquals(g.getTranscripts().size(), g2.getTranscripts().size());

  }

  public void testFetchGenesByAssemblyLocationAndTranslate() throws Exception {
    
    Location loc = new Location("chromosome:20:20m-21m");
    List genes = geneAdaptor.fetch(loc);
    assertTrue("No genes loaded", genes.size() > 0);
    int nTranscriptsTotal = 0;
    for (int g = 0; g < genes.size(); ++g) {
      Gene gene = (Gene) genes.get(g);
      List transcripts = gene.getTranscripts();
      nTranscriptsTotal += transcripts.size();
      for (int t = 0; t < transcripts.size(); ++t) {
        Transcript transcript = (Transcript) transcripts.get(t);
        Translation translation = transcript.getTranslation();
        if (translation == null) {
          logger.warning("No peptide for " + transcript.getAccessionID());
          continue;
        }
        String peptide = translation.getPeptide();
        assertTrue(
          "Failed to translate gene/transcript/translation "
            + gene.getInternalID()
            + "/"
            + transcript.getInternalID()
            + "/"
            + translation.getInternalID(),
          peptide != null && peptide.length() > 0);
        logger.fine(
          "translation " + translation.getInternalID() + " = " + peptide);
      }
    }
    logger.fine("nGenes = " + genes.size());
    logger.fine("nTranscripts = " + nTranscriptsTotal);
  }

  public void testFetchGenesByAssemblyLocationAndType() throws Exception {
    Location loc = new Location("chromosome:22:21m-21.1m:0");
    List genes = geneAdaptor.fetch(loc);
    assertTrue("No genes loaded", genes.size() > 0);
  }

  public void testNoGenesFetchedFromUnlikelyAssembly() throws Exception {
    try {
      Location loc =
        new Location(
          new CoordinateSystem(UNLIKELY_ASSEMBLY_MAP_NAME),
          "22",
          21000000,
          21100000,
          0);
      //fail("Should have thrown exception when trying to convert between
      // non-existent co-ordinate systems.");
    } catch (Exception ae) {
      // nothing done here as AdaptorException being thrown is the correct
      // result
    }
  }

  public void testFetchByLocation() throws Exception {

    final boolean showRuntimes = false;
    Timer timer = new Timer();

    Location loc = new Location("chromosome:22:21m-21.5m:0");

    if (showRuntimes) {
      System.out.println(
        "START fetch genes + load for location " + loc + "...");
      timer.start();
    }
    List genesLoaded = geneAdaptor.fetch(loc, true);
    checkOrder(genesLoaded);
    checkGenes(genesLoaded);
    if (showRuntimes)
      System.out.println(
        " ... found "
          + genesLoaded.size()
          + " genes and took "
          + timer.stop().getDurationInSecs()
          + "s");

    if (showRuntimes) {
      System.out.println(
        "START fetch genes + lazy load for location " + loc + "...");
      timer.start();
    }
    List genesLazy = geneAdaptor.fetch(loc);
    checkOrder(genesLazy);
    checkGenes(genesLazy);
    if (showRuntimes)
      System.out.println(
        " ... "
          + genesLoaded.size()
          + " genes and took "
          + timer.stop().getDurationInSecs()
          + "s");

    assertEquals(genesLazy.size(), genesLoaded.size());
  }

  public void testFetchGenesByCloneFragmentLocation() throws Exception {
    Location cfl = new Location("contig:AC010089.4.1.103356");
    assertTrue(fetch(cfl).size() > 0);
  }

  public void testRetrieveGenesByInternalID() {
    try {
      long[] id = new long[] { geneID,
        geneID+1 
      };
      for (int i = 0; i < id.length; ++i) {
        Gene gene = geneAdaptor.fetch(id[i]);
        logger.fine("#Fetched gene ID " + id[i] + ":" + gene.getDisplayName());
        assertNotNull(gene);
        logger.fine("Gene (id = " + id + ") : " + format(gene));
        List exons = gene.getExons();
        Iterator exonIter = exons.iterator();
        while (exonIter.hasNext()) {
          Exon exon = (Exon) exonIter.next();
          assertNotNull(exon);
          Location rawLoc = exon.getLocation();
          Location loc = locationConverter.convert(rawLoc, chromosomeCS);
          logger.fine(
            gene.getInternalID() + "\t" + exon.getInternalID() + "\t" + loc);
        }
        // test all transcript.exons in exons and vice versa
        // build transcriptExons, a map of all the exons that come from
        // transcripts, keyed on accession
        Map transcriptExons = new HashMap();
        Iterator transcriptsIter = gene.getTranscripts().iterator();
        while (transcriptsIter.hasNext()) {
          Transcript transcript = (Transcript) transcriptsIter.next();
          List exonList = transcript.getExons();
          if (exonList == null) {
            logger.fine(
              "transcript.getExons is null for transcript ID "
                + transcript.getInternalID());
          } else {
            logger.fine(
              "##transcript.getExons returns "
                + exonList.size()
                + " for transcript with ID "
                + transcript.getInternalID());
            exonIter = exonList.iterator();
            while (exonIter.hasNext()) {
              Exon e = (Exon) exonIter.next();
              assertNotNull(
                "ERROR: null exon found amongst"
                  + " exons retrieved from transcript : "
                  + transcript.getExons(),
                e);
              if (e == null) {
                logger.fine("#error - null exon found");
              }
              transcriptExons.put(e.getAccessionID(), e);
            }
          }
        }
        // now check that all the exons that come from genes (exons list) are
        // in transcriptExons
        Iterator it = exons.iterator();
        while (it.hasNext()) {
          Exon e = (Exon) it.next();
          String acc = e.getAccessionID();
          assertTrue(
            "Exon embedded in transcript not found in gene " + acc,
            transcriptExons.containsKey(acc));
          //logger.fine(acc + " " + transcriptExons.containsKey(acc));
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  /**
   * Fails if it finds a duplicate gene, exon, transcript or translation with
   * the same internal IDs. If duplicates are found they are written to the
   * error message. Note that the cause could be problems in the adaptor or in
   * the data in then db. The method tests
   * _geneAdaptor.fetch(CloneFragmentLocation)_.
   */
  public void testForDuplicates() throws Exception {
    Location location = new Location(chromosomeCS, "22", 22000000, 23000000, 1);
    List allGenes = geneAdaptor.fetch(location);
    assertNotNull(allGenes);
    Set geneIDs = new HashSet();
    ArrayList geneDuplicates = new ArrayList();
    Set exonIDs = new HashSet();
    ArrayList exonDuplicates = new ArrayList();
    Set transcriptIDs = new HashSet();
    ArrayList transcriptDuplicates = new ArrayList();
    Set translationIDs = new HashSet();
    ArrayList translationDuplicates = new ArrayList();
    boolean isNovelID;
    Iterator geneIter = allGenes.iterator();
    while (geneIter.hasNext()) {
      Gene gene = (Gene) geneIter.next();
      Long geneID = new Long(gene.getInternalID());
      isNovelID = geneIDs.add(geneID);
      if (!isNovelID)
        geneDuplicates.add(geneID);
      Iterator transcriptIter = gene.getTranscripts().iterator();
      while (transcriptIter.hasNext()) {
        Transcript transcript = (Transcript) transcriptIter.next();
        Long transcriptID = new Long(transcript.getInternalID());
        isNovelID = transcriptIDs.add(transcriptID);
        if (!isNovelID)
          transcriptDuplicates.add(transcriptID);
        Translation translation = transcript.getTranslation();
        if (translation != null) {
          Long translationID = new Long(translation.getInternalID());
          isNovelID = translationIDs.add(translationID);
          if (!isNovelID)
            translationDuplicates.add(translationID);
        }
      }
      Iterator exonIter = gene.getExons().iterator();
      exonIDs.clear();
      while (exonIter.hasNext()) {
        Exon exon = (Exon) exonIter.next();
        Long exonID = new Long(exon.getInternalID());
        isNovelID = exonIDs.add(exonID);
        if (!isNovelID) {
          logger.fine(
            "Found duplicate exons for gene with internal ID "
              + gene.getInternalID()
              + " accession "
              + gene.getAccessionID()
              + " exon ID "
              + exonID
              + " total exons in gene "
              + gene.getExons().size());
          exonDuplicates.add(exonID);
        }
      }
    }
    if (geneDuplicates.size() > 0
      || transcriptDuplicates.size() > 0
      || translationDuplicates.size() > 0
      || exonDuplicates.size() > 0) {
      StringBuffer message = new StringBuffer();
      message.append("Duplicates found (check no duplicates in db):\n");
      Iterator i;
      i = geneDuplicates.iterator();
      while (i.hasNext()) {
        message.append("gene\t");
        message.append(i.next());
        message.append("\n");
      }
      i = exonDuplicates.iterator();
      while (i.hasNext()) {
        message.append("exon\t");
        message.append(i.next());
        message.append("\n");
      }
      i = transcriptDuplicates.iterator();
      while (i.hasNext()) {
        message.append("transcript\t");
        message.append(i.next());
        message.append("\n");
      }
      i = translationDuplicates.iterator();
      while (i.hasNext()) {
        message.append("translation\t");
        message.append(i.next());
        message.append("\n");
      }
      fail(message.toString());
    }
  }

  public void testExonTranscriptLazyLoading() throws Exception {
    Gene gene = geneAdaptor.fetch(geneID);
    logger.fine("gene.getExons: " + gene.getExons().size());
    logger.fine("gene.getTranscripts: " + gene.getTranscripts().size());
    assertTrue("Failed to lazy load exons.", gene.getExons().size() != 0);
    assertTrue(
      "Failed to lazy load transcripts.",
      gene.getTranscripts().size() != 0);
    Exon exon = (Exon) gene.getExons().get(0);
    assertEquals(
      "Gene reference in exon is wrong",
      gene.getInternalID(),
      exon.getGeneInternalID());
    Transcript transcript = (Transcript) gene.getTranscripts().get(0);
    assertEquals(
      "Gene reference int transcript is wrong",
      gene.getInternalID(),
      transcript.getGeneInternalID());
    logger.fine("exons:" + format(gene.getExons()));
    logger.fine("transcripts:" + format(gene.getTranscripts()));
  }

  private List fetch(Location loc) throws Exception {
    List genes = geneAdaptor.fetch(loc);
    checkGenes(genes);
    return genes;
  }

  private void checkGenes(List genes) throws Exception {
    assertNotNull(genes);
    Iterator geneIter = genes.iterator();
    while (geneIter.hasNext()) {
      Gene gene = (Gene) geneIter.next();
      logger.fine(
        "Found gene : "
          + gene.getInternalID()
          + ", exons = (#"
          + gene.getExons().size());
      List exons = gene.getExons();
      for (int i = 0; i < exons.size(); ++i) {
        Exon exon = (Exon) exons.get(i);
        logger.fine(exon.getInternalID() + ", " + exon.getLocation());
      }
    }

  }

  private void checkOrder(List locatables) {
    assertTrue("No genes loaded", locatables.size() > 0);
    List genes = locatables;
    int prevStart = -1;
    Iterator geneIter = genes.iterator();
    while (geneIter.hasNext()) {
      Gene gene = (Gene) geneIter.next();
      int nextStart = ((Location) (gene.getLocation())).getStart();
      Location l = gene.getLocation();
      logger.fine(
        "GENE "
          + gene.getInternalID()
          + " : "
          + l.getStart()
          + ", "
          + l.getEnd()
          + ", "
          + l.getStrand());
      assertTrue("Genes in wrong order", nextStart > prevStart);
      prevStart = nextStart;
    }
  }

  private String format(Object o) {
    if (true)
      return StringUtil.formatForPrinting(o);
    else
      return o.toString();
  }

  public void testFetchByAccession() throws Exception {
    Gene gene = geneAdaptor.fetch(accession);
    assertNotNull("Fetch gene by accession failed for: " + accession, gene);
  }

  public void testFetchBySynonym() throws Exception {
    String synonym = "NM_020974";
    List genes = geneAdaptor.fetchBySynonym(synonym);
    assertEquals(
      "Found wrong number of genes with synonym = " + synonym,
      1,
      genes.size());
  }

  public void testFetchInternalIDsByLocation() throws Exception {

    Location loc = new Location("chromosome:22:21m-21.1m");
    List genesList = geneAdaptor.fetch(loc);
    long[] genesArr = geneAdaptor.fetchInternalIDs(loc);

    assertTrue(genesArr.length > 0);
    assertEquals(genesList.size(), genesArr.length);

    // check the order is the same
    for (int i = 0; i < genesArr.length; i++) {
      assertEquals(genesArr[i], ((Gene) genesList.get(i)).getInternalID());
    }
  }

  public void testFetchIteratorByLocation() throws Exception {

    Location loc = new Location("chromosome:22:21m-21.1m");

    List genes = geneAdaptor.fetch(loc);
    long[] ids = new long[genes.size()];
    for (int i = 0; i < ids.length; i++) {
      ids[i] = ((Gene) genes.get(i)).getInternalID();
    }
    Arrays.sort(ids);

    Iterator iter = geneAdaptor.fetchIterator(loc);
    int c = 0;
    while (iter.hasNext()) {
      Object o = iter.next();
      Gene g = (Gene) o;
      assertTrue(
        "Gene isn't in specified location. ",
        g.getLocation().overlaps(loc));
      assertEquals(
        "Gene does not appear in fetch(location) list:",
        ids[c],
        g.getInternalID());
      c++;
    }
    assertEquals("Different number of genes retrieved", genes.size(), c);

    // TODO check fetchIterator(loc,true)

  }

  public void testFetchIterator() throws Exception {

    Location loc = new Location("chromosome:22:21m-21.5m");

    Timer t = new Timer();

    List genesLazy = new ArrayList();
    List genesLoad = new ArrayList();
    List genesSeq = new ArrayList();

    t.start();
    for (Iterator iter = geneAdaptor.fetchIterator(loc, true); iter.hasNext();)
      genesLoad.add(iter.next());
    checkGenes(genesLoad);
    checkOrder(genesLoad);
    double genesLoadTime = t.stop().getDurationInSecs();

    t.start();
    for (Iterator iter = geneAdaptor.fetchIterator(loc, false);
      iter.hasNext();
      )
      genesLazy.add(iter.next());
    checkGenes(genesLazy);
    checkOrder(genesLazy);
    double genesLazyTime = t.stop().getDurationInSecs();

    t.start();
    long[] ids = geneAdaptor.fetchInternalIDs(loc);
    for (int i = 0; i < ids.length; i++)
      genesSeq.add(geneAdaptor.fetch(ids[i]));
    checkGenes(genesSeq);
    checkOrder(genesSeq);
    double genesSeqTime = t.stop().getDurationInSecs();

    assertEquals(genesSeq.size(), genesLazy.size());
    assertEquals(genesSeq.size(), genesLoad.size());

//    assertTrue(
//      "Iterator that does preloading should be faster than sequential fetch",
//      genesLoadTime < genesSeqTime);
//    assertTrue(
//      "Iterator that does preloading should be faster than iterator with lazy loading",
//      genesLoadTime < genesLazyTime);

    // UNCOMMENT if comparing performance
    //      System.out.println(
    //        "Seq fetch = "
    //          + genesSeqTime
    //          + "\nIter lazy load fetch =  "
    //          + genesLazyTime
    //          + "\nIter preload fetch =  "
    //          + genesLoadTime);

  }

  
  public void testLoadingChildDataFromGenesInLocationWithouGenes() throws Exception {
  	// should return empty list without crashing
  	List genes = driver.getGeneAdaptor().fetch(new Location("clone:AB000878.1"),true);
  	assertTrue(genes.size()==0);
    
  }
  
  public void testRecursiveCoordinateSystemSetting() throws Exception {
    final long ID = 1;
    Gene g = geneAdaptor.fetch(ID);
    assertNotNull("Can't find gene in db with internal_id= " + ID, g);
    CoordinateSystem[] css = new CoordinateSystem[] {new CoordinateSystem("chromosome"), new CoordinateSystem("contig")};
    for (int i = 0; i < css.length; i++) {
      CoordinateSystem cs = driver.getCoordinateSystemAdaptor().fetchComplete(css[i]);
      g.setCoordinateSystem(cs, driver.getLocationConverter());
      assertSame(cs, g.getLocation().getCoordinateSystem());
      assertSame(cs, ((Transcript)g.getTranscripts().get(0)).getLocation().getCoordinateSystem());      
      assertSame(cs, ((Exon)g.getExons().get(0)).getLocation().getCoordinateSystem());      
    }
  }
  
  private GeneAdaptor geneAdaptor;

  private LocationConverter locationConverter;
} // GeneTest