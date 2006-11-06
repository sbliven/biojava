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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.DnaDnaAlignment;
import org.ensembl.datamodel.DnaProteinAlignment;
import org.ensembl.datamodel.Exon;
import org.ensembl.datamodel.Gene;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Transcript;
import org.ensembl.datamodel.Translation;
import org.ensembl.driver.CoreDriver;

/**
 * JUnit tests for TranscriptAdaptor / TranscriptAdaptorImpl
 */
public class TranscriptAdaptorTest extends CoreBase {

  CoordinateSystem chromosomeCS = new CoordinateSystem("chromosome");
  Location chrLoc1 = new Location(chromosomeCS, "20", 1, 100000, 1);

  // -----------------------------------------------------------------

  public TranscriptAdaptorTest(String arg0) {
    super(arg0);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();

    //suite.addTest(new TranscriptAdaptorTest("testFetchByExonAccession"));

    suite.addTestSuite(TranscriptAdaptorTest.class);

    return suite;
  }

  //	-----------------------------------------------------------------

  public static void main(String[] args) {
    junit.textui.TestRunner.run(TranscriptAdaptorTest.class);
  }

  //	-----------------------------------------------------------------

  public void testFetchByInternalID() throws Exception {

    Transcript t = driver.getTranscriptAdaptor().fetch(1);
    assertNotNull(t);
  }

  //	-----------------------------------------------------------------

  public void testFetchByAccessionID() throws Exception {

    Transcript t = driver.getTranscriptAdaptor().fetch("ENST00000302092");
    assertNotNull(t);

  }
  //---------------------------------------------------------------------

  public void testFetchBySynonym() throws Exception {

    String synonym = "Q00005";
    List l = driver.getTranscriptAdaptor().fetchBySynonym(synonym);
    assertNotNull(l);
    Transcript t = (Transcript) l.get(0);
    assertNotNull(t);

  }

  //	-----------------------------------------------------------------

  public void testFetchAllByLocation() throws Exception {

    List l = driver.getTranscriptAdaptor().fetch(chrLoc1);

    assertNotNull(l);
    assertTrue(l.size() > 0);
    //for (Iterator it = l.iterator(); it.hasNext();) {
    //	Transcript t = (Transcript)it.next();
    //	System.out.println(t.toString());
    //}

  }

  //---------------------------------------------------------------------

  //---------------------------------------------------------------------

  public void testInterproSupport() throws Exception {

    String id = "IPR000405";
    List ts = driver.getTranscriptAdaptor().fetchByInterproID(id);
    assertTrue(ts.size() > 0);
    Transcript t = (Transcript) ts.get(0);

    // check id occurs on constituent translation
    boolean found = false;

    Translation tn = t.getTranslation();
    if (tn != null) {
      String[] interproIDs = tn.getInterproIDs();
      Arrays.sort(interproIDs);
      if (Arrays.binarySearch(interproIDs, id) > -1)
        found = true;

    }
    assertTrue(
      "No translation with specified interproID found on the transcript. InterproID= "
        + id
        + " transcript="
        + t,
      found);

    // check that id occurs on transcript
    assertTrue(t.getInterproIDs().length > 0);
    found = false;
    String[] interproIDs = t.getInterproIDs();
    Arrays.sort(interproIDs);
    if (Arrays.binarySearch(interproIDs, id) > -1)
      found = true;
    assertTrue(
      "interproID:" + id + " not associated with transcript: " + t,
      found);
  }

  public void testCanLazyLoadTranscriptsFromInsideAnIntron() throws Exception {

    String geneID = "ENSG00000139618";
    String transcriptID = "ENST00000267071";

    // ****** Pre check to ensure the IDs we are using are in db with appropriate relationship

    // Check geneID contains transcriptID when load gene -> exon. 
    Gene gene = driver.getGeneAdaptor().fetch(geneID);
    boolean transcriptInGene = false;
    for (Iterator iter = gene.getTranscripts().iterator();
      !transcriptInGene && iter.hasNext();
      ) {
      Transcript t = (Transcript) iter.next();
      if (t.getAccessionID().equals(transcriptID))
        transcriptInGene = true;
    }
    assertTrue(transcriptInGene);

    // ****** Real 'test' follows

    // Try to load exon -> gene and check that the loading gives the same results.
    Transcript transcript = driver.getTranscriptAdaptor().fetch(transcriptID);
    gene = transcript.getGene();
    assertNotNull(transcript.getGene());
    assertEquals(geneID, transcript.getGene().getAccessionID());

    // check that gene.transcripts contains transcript
    assertTrue(gene.getTranscripts().contains(transcript));

    // check that transcript.exon.transcripts contains transcript
    List exons = transcript.getExons();
    assertNotNull(exons);
    assertTrue(exons.size() > 0);
    Exon exon = (Exon) exons.get(0);
    List exonTranscripts = exon.getTranscripts();
    assertTrue(exonTranscripts.contains(transcript));
  }

  
  public void testSupportingFeatures() throws Exception {
    
    // only cow has transcript supporting features at time
    // group defined in ~/.ensembl/unit_test.ini
    CoreDriver driver = registry.getGroup("cow").getCoreDriver();
    
    Transcript t = driver.getTranscriptAdaptor().fetch(1);
    
    List supportingFeatures = t.getSupportingFeatures();
    assertNotNull(supportingFeatures);
    assertTrue(supportingFeatures.size()>0);
    for (int i = 0; i < supportingFeatures.size(); i++) {
      Object sf = (Object) supportingFeatures.get(i);
      assertTrue(sf instanceof DnaDnaAlignment || sf instanceof DnaProteinAlignment);
    }
  }
}
