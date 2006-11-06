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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ensembl.datamodel.Attribute;
import org.ensembl.datamodel.Exon;
import org.ensembl.datamodel.Locatable;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Transcript;
import org.ensembl.datamodel.Translation;
import org.ensembl.datamodel.impl.AttributeImpl;
import org.ensembl.datamodel.impl.ExonImpl;
import org.ensembl.datamodel.impl.TranscriptImpl;
import org.ensembl.datamodel.impl.TranslationImpl;
import org.ensembl.driver.TranscriptAdaptor;
import org.ensembl.util.Timer;

/**
 * Test class for Transcripts.
 */
public class TranscriptTest extends CoreBase {

  private static final Logger logger =
    Logger.getLogger(TranscriptTest.class.getName());

  private TranscriptAdaptor transcriptAdaptor;

  public static final void main(String[] args) throws Exception {
    if (args.length == 0)
      junit.textui.TestRunner.run(suite());

    else {

      TestSuite suite = new TestSuite();
      for (int i = 0; i < args.length; ++i)
        suite.addTest(new TranscriptTest(args[i]));
      junit.textui.TestRunner.run(suite);
    }
  }

  public TranscriptTest(String name) {
    super(name);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();
    //suite.addTest(new TranscriptTest("testFetchByIterator"));
    //suite.addTest(new TranscriptTest("testSeqEditSupport"));
    suite.addTestSuite(TranscriptTest.class);
    return suite;
  }

  protected void setUp() throws Exception {
    super.setUp();
    transcriptAdaptor = (TranscriptAdaptor) driver.getAdaptor("transcript");
  }

  //	public void testLazyLoadTranscriptAccessionAndVersion() throws Exception {

  //		ExonTest.checkAccessionAndVersion(transcriptAdaptor.fetch(100));
  //	}

  public void testBasicAttributes() throws Exception {

    long transcriptID = 1;
    Transcript t = transcriptAdaptor.fetch(transcriptID);
    assertNotNull(t.getDisplayName());

    String accession = "ENST00000221494";
    t = transcriptAdaptor.fetch(accession);
    assertNotNull(t.getDisplayName());
    assertNotNull(t.getStatus());
    assertTrue(t.getAnalysisID()>0);
    assertEquals(t.getAnalysisID(), t.getAnalysis().getInternalID());
  }

  public void testFetchByInternalID() throws Exception {

    int[] internalIDs = new int[] { 1, 2, 3 };
    for (int i = 0; i < internalIDs.length; ++i) {
      Transcript transcript = transcriptAdaptor.fetch(internalIDs[i]);
      assertNotNull(
        "Trascript with internalID=" + internalIDs[i] + "Not found.",
        transcript);
      assertEquals(
        "Returned transcript has wrong internalID. ",
        internalIDs[i],
        transcript.getInternalID());
    }
  }

  public void testFetchByAccessionID() throws Exception {

    String[] accessionIDs =
      new String[] { "ENST00000302092", "ENST00000285609" };
    for (int i = 0; i < accessionIDs.length; ++i) {
      Transcript transcript = transcriptAdaptor.fetch(accessionIDs[i]);
      assertNotNull(
        "Trascript with accession = " + accessionIDs[i] + "Not found.",
        transcript);
      assertEquals(
        "Returned transcript has wrong accession. ",
        accessionIDs[i],
        transcript.getAccessionID());
      logger.fine(transcript.toString());
    }
  }

  /**
   * Check that the order of exons in a transcript is correct.
   * Not sure if this is correct for negative strand
   * @throws Exception
   */
  public void testExonOrder() throws Exception {

    Transcript t = transcriptAdaptor.fetch("ENST00000221494");
    assertNotNull(t);
    List exons = t.getExons();

    Iterator it = exons.iterator();
    long previousStart = -1;
    long previousEnd = -1;
    while (it.hasNext()) {

      Exon e = (Exon) it.next();
      Location loc = e.getLocation();
      assertTrue("Exon end is less than start", loc.getStart() < loc.getEnd());

      assertTrue("Exons not in correct order", loc.getStart() >= previousStart);
      assertTrue("Exons not in correct order", loc.getEnd() >= previousEnd);
      previousStart = loc.getStart();
      previousEnd = loc.getEnd();

    }
  }

  public void testIsKnown() throws Exception {

    // just get a significant number of transcripts, then check that there is a mixture of known/unknown
    Location chrLoc = new Location("chromosome:22:22m-22.1m");

    List transcripts = transcriptAdaptor.fetch(chrLoc);

    int nKnown = 0;
    int nUnknown = 0;
    Iterator it = transcripts.iterator();
    while (it.hasNext()) {
      Transcript t = (Transcript) it.next();
      if (t.isKnown()) {
        nKnown++;
      } else {
        nUnknown++;
      }
    }

    assertTrue(
      "No known transcripts found out of "
        + transcripts.size()
        + " - some are expected",
      nKnown > 0);
    assertTrue(
      "No unknown transcripts found out of "
        + transcripts.size()
        + " - some are expected",
      nUnknown > 0);
  }

  public void testFetchByLocation() throws Exception {

    final boolean showRuntimes = false;

    Location[] locations =
      new Location[] {
        //new Location("chromosome:22"),
        new Location("chromosome:22:21m-22m")
//        ,new Location(chromosomeCS, "17", 1, 1000000)
        };

    Timer timer = new Timer();

    for (int i = 0; i < locations.length; ++i) {

      // fetch + preload
      if (showRuntimes) {
        System.out.println(
          "START fetch + preload child data for location "
            + locations[i]
            + "...");
        timer.start();
      }
      List ttcs = transcriptAdaptor.fetch(locations[i], true);
      checkTranscripts(locations[i], ttcs);
      if (showRuntimes)
              System.out.println(
                " ... took " + timer.stop().getDurationInSecs() + "s");

      // fetch + lazy load
      if (showRuntimes) {
        System.out.println(
          "START basic fetch + lazy load for location " + locations[i] + "...");
        timer.start();
      }
//      List tts = transcriptAdaptor.fetch(locations[i]);
//      checkTranscripts(locations[i], tts);
      if (showRuntimes)
        System.out.println(
          " ... took " + timer.stop().getDurationInSecs() + "s");

      // check same results from 2 types of query   
//      assertEquals(tts.size(), ttcs.size());
//      for (int j = 0; j < tts.size(); j++)
//        compareTranscripts((Transcript) tts.get(j), (Transcript) ttcs.get(j));
    }

  }


  public void testFetchByIterator() throws Exception {

      final boolean showRuntimes = false;

      Location[] locations =
        new Location[] {
          //new Location("chromosome:22"),
          new Location(chromosomeCS, "22", 21000000, 21100000)
          //,new Location(chromosomeCS, "17", 1, 1000000)
          };

      Timer timer = new Timer();

      for (int i = 0; i < locations.length; ++i) {

        // fetch + preload
        if (showRuntimes) {
          System.out.println(
            "START PRELOAD iterator for location "
              + locations[i]
              + "...");
          timer.start();
        }
        Iterator iterttcs = transcriptAdaptor.fetchIterator(locations[i], true);
        List ttcs = new ArrayList();
        while(iterttcs.hasNext())
          ttcs.add(iterttcs.next());
        checkTranscripts(locations[i], ttcs);
        if (showRuntimes)
          System.out.println(
            " ... "+ttcs.size()+" transcripts took " + timer.stop().getDurationInSecs() + "s");



        // fetch + lazy load
        if (showRuntimes) {
          System.out.println(
            "START BASIC iterator for location " + locations[i] + "...");
          timer.start();
        }
        Iterator iter = transcriptAdaptor.fetchIterator(locations[i]);
        List tts = new ArrayList();
        while(iter.hasNext())
          tts.add(iter.next());
        checkTranscripts(locations[i], tts);
        if (showRuntimes)
          System.out.println(
            " ...  "+tts.size()+" transcripts took " + timer.stop().getDurationInSecs() + "s");



        System.out.println();

        // check same results from 2 types of query   
        assertEquals(tts.size(), ttcs.size());
        for (int j = 0; j < tts.size(); j++)
          compareTranscripts((Transcript) tts.get(j), (Transcript) ttcs.get(j));
      }

    }


  private void checkTranscripts(Location location, List transcripts) {

    assertTrue(
      "No transcripts found in location " + location,
      transcripts.size() > 0);

    for (int t = 0; t < transcripts.size(); ++t) {
      Transcript transcript = (Transcript) transcripts.get(t);
      assertNotNull(transcript);

      // Transcripts should overlap the requested regions by at least one
      // exon.
      boolean overlap = false;
      for (int e = 0; e < transcript.getExons().size(); ++e) {
        Exon exon = (Exon) transcript.getExons().get(e);
        if (location.overlaps(exon.getLocation()))
          overlap = true;
      }
      assertTrue(
        "Transcript "
          + transcript.getInternalID()
          + "does not overlap "
          + "\nlocation = "
          + location
          + ".\n This might happen if the parent"
          + " gene has an exon inside the location but this particular transcript"
          + " doesn't. Try another location.",
        overlap);

    }

  }

  public void testGetPeptideLocation1() throws Exception {

    Location cfl1 = new Location("contig:222:100-200");

    Exon e1 = new ExonImpl();
    e1.setInternalID(1);
    e1.setLocation(cfl1);
    List exons = new ArrayList();
    exons.add(e1);

    Translation tn = new TranslationImpl();
    tn.setStartExonInternalID(1);
    tn.setPositionInStartExon(1);
    tn.setEndExonInternalID(1);
    tn.setPositionInEndExon(101);

    Transcript t = new TranscriptImpl();
    t.setExons(exons);
    t.setTranslation(tn);
    tn.setTranscript(t);

    Translation start = t.getTranslation();
    Location result = start.getAminoAcidStart(3);

    logger.fine("Result = " + result);

    assertEquals("Start position is wrong", 106, result.getStart());
    assertEquals("End position is wrong", 106, result.getEnd());
    //assertEquals(
    //"CloneFragmentInternalID is wrong", cfl1.getCloneFragmentInternalID(), result.getCloneFragmentInternalID());
    assertEquals("Strand is wrong", cfl1.getStrand(), result.getStrand());

  }

  public void testGetPeptideLocation2() throws Exception {

    Location cfl1 = new Location("contig:222:100-200:-1");

    Exon e1 = new ExonImpl();
    e1.setInternalID(1);
    e1.setLocation(cfl1);
    List exons = new ArrayList();
    exons.add(e1);

    Translation tn = new TranslationImpl();
    tn.setStartExonInternalID(1);
    tn.setPositionInStartExon(1);
    tn.setEndExonInternalID(1);
    tn.setPositionInEndExon(101);

    Transcript t = new TranscriptImpl();
    t.setExons(exons);
    t.setTranslation(tn);
    tn.setTranscript(t);

    Location result = (Location) t.getTranslation().getAminoAcidStart(3);
    assertEquals("Start position is wrong", 194, result.getStart());
    assertEquals("End position is wrong", 194, result.getEnd());
    //assertEquals("CloneFragmentInternalID is wrong", cfl1.getCloneFragmentInternalID(), result.getCloneFragmentInternalID());
    assertEquals("Strand is wrong", cfl1.getStrand(), result.getStrand());

  }

  // Test getPeptiteLocation on from composite / clone fragments on the reverse strand.

  public void testGetPeptideLocation3() throws Exception {

    // Total exon length = 11 + 101 + 101 + 101 + 41 + 401 = 756
    // Total transcribed exon length = 

    Location cfl1 = new Location("contig:222:10-20:-1"); //11

    Location cfl2 = new Location("contig:225:100-200:-1"); // 101

    Location ccfl3 = new Location("contig:227:100-200:-1");
    ccfl3.append(new Location("contig:223:200-400:-1"));

    Location cfl4 = new Location("contig:229:30-70:-1"); // 41

    Location cfl5 = new Location("contig:1000:300-700:-1"); // 401

    List exons = new ArrayList();
    Exon e1 = new ExonImpl();
    e1.setInternalID(1);
    e1.setLocation(cfl1);

    Exon e2 = new ExonImpl();
    e2.setInternalID(2);
    e2.setLocation(cfl2);

    Exon e3 = new ExonImpl();
    e3.setInternalID(3);
    e3.setLocation(ccfl3);

    Exon e4 = new ExonImpl();
    e4.setInternalID(4);
    e4.setLocation(cfl4);

    Exon e5 = new ExonImpl();
    e5.setInternalID(5);
    e5.setLocation(cfl5);

    // Exons added in reverse order because -1 strand
    exons.add(e5);
    exons.add(e4);
    exons.add(e3);
    exons.add(e2);
    exons.add(e1);

    Translation tn = new TranslationImpl();
    tn.setStartExonInternalID(4);
    tn.setPositionInStartExon(20);
    tn.setEndExonInternalID(2);
    tn.setPositionInEndExon(10);

    Transcript t = new TranscriptImpl();
    t.setExons(exons);
    t.setTranslation(tn);
    tn.setTranscript(t);

    // expected result is inside exon4
    Translation translation = t.getTranslation();
    logger.fine(translation.getCodingLocations().toString());
    Location result = (Location) translation.getAminoAcidStart(3);
    assertEquals("Start position is wrong", 45, result.getStart());
    assertEquals("End position is wrong", 45, result.getEnd());
    //assertEquals("CloneFragmentInternalID is wrong", cfl4.getCloneFragmentInternalID(), result.getCloneFragmentInternalID());
    assertEquals("Strand is wrong", cfl4.getStrand(), result.getStrand());

    // amino acid start is inside CompositeCloneFragmentLocation
    Location result2 = (Location) (tn.getAminoAcidStart(19));
    // (19-1)*3 = 54
    assertEquals("Start position is wrong", 168, result2.getStart());
    assertEquals("End position is wrong", 168, result2.getEnd());
    assertEquals("Strand is wrong", cfl4.getStrand(), result2.getStrand());

    // check 5' and 3' UTRs
    List fivePrimeUTR = translation.getFivePrimeUTR();
    List threePrimeUTR = translation.getThreePrimeUTR();
    //System.out.println( "5primeUTR = " +  fivePrimeUTR );
    //System.out.println( "3primeUTR = " +  threePrimeUTR );

    assertEquals("Wrong number of fivePrimeUTR locs", 2, fivePrimeUTR.size());
    assertEquals("Wrong number of threePrimeUTR locs", 2, threePrimeUTR.size());

    int transcriptLen = sumLocatableLocations(t.getExons());
    int fivePrimeUTRLen = sumLocations(translation.getFivePrimeUTR());
    int threePrimeUTRLen = sumLocations(translation.getThreePrimeUTR());
    int codingLength = sumLocations(translation.getCodingLocations());

    assertEquals(
      "len(CodingLocs) + len(5PrimeUTR) + len(3PrimeUTR) should be the same as len(transcript)",
      fivePrimeUTRLen + threePrimeUTRLen + codingLength,
      transcriptLen);

    //System.out.println(Integer.toString(fivePrimeUTRLen + threePrimeUTRLen + codingLength));
    //System.out.println(Integer.toString(transcriptLen));

  }

  private int sumLocations(List locs) {
    int total = 0;
    for (int i = 0; i < locs.size(); ++i)
      total += ((Location) locs.get(i)).getLength();
    return total;
  }

  private int sumLocatableLocations(List locatables) {
    int total = 0;
    for (int i = 0; i < locatables.size(); ++i) {
      Locatable l = (Locatable) locatables.get(i);
      total += l.getLocation().getLength();
    }
    return total;
  }

  public void testGetPeptideLocation4() throws Exception {

    Location cfl1 = new Location("chromosome:22:100-200");

    Exon e1 = new ExonImpl();
    e1.setInternalID(1);
    e1.setLocation(cfl1);
    List exons = new ArrayList();
    exons.add(e1);

    Translation tn = new TranslationImpl();
    tn.setStartExonInternalID(1);
    tn.setPositionInStartExon(1);
    tn.setEndExonInternalID(1);
    tn.setPositionInEndExon(101);

    Transcript t = new TranscriptImpl();
    t.setExons(exons);
    t.setTranslation(tn);
    tn.setTranscript(t);

    Location result = (Location) t.getTranslation().getAminoAcidStart(3);
    assertEquals("Start position is wrong", 106, result.getStart());
    assertEquals("End position is wrong", 106, result.getEnd());
    //assertEquals("AssemblyInternalID is wrong", cfl1.getChromosome(), result.getChromosome());
    assertEquals("Strand is wrong", cfl1.getStrand(), result.getStrand());

  }

  public void testFetchByInternalIDArray() throws Exception {

    // set true if you want to see how long each fetch method takes to run 
    final boolean showRuntimes = false;
    // set the number of transcripts to fetch
    final int n = 3;
    final int startID = 1;
    
    long[] ids = new long[n];
    for (int i = 0; i < n; i++) {
      ids[i] = i + startID; // first id is 1
    }

    Timer timer = new Timer();

    // check that we can handle empty arrays!
    assertEquals(0, transcriptAdaptor.fetch(new long[] {
    }).size());
    assertEquals(0, transcriptAdaptor.fetch(new long[] {
    }, true).size());

    if (showRuntimes) {
      System.out.println("START id array plus children fetch ...");
      timer.start();
    }
    List transcriptsByIDsIncludeChildren = transcriptAdaptor.fetch(ids, true);
    forceChildrenToLoad(transcriptsByIDsIncludeChildren);
    if (showRuntimes) {

      timer.stop();
      System.out.println(
        "id array plus children took " + timer.getDurationInSecs() + "s");
    }

    if (showRuntimes) {
      System.out.println("START sequential fetch ...");
      timer.start();
    }
    List transcriptsSequential = new ArrayList();
    for (int i = 0; i < n; i++) {
      transcriptsSequential.add(transcriptAdaptor.fetch(ids[i]));
    }
    forceChildrenToLoad(transcriptsSequential);
    if (showRuntimes) {
      timer.stop();
      System.out.println("sequential took " + timer.getDurationInSecs() + "s");
    }

    if (showRuntimes) {
      System.out.println("START id array fetch ...");
      timer.start();
    }
    List transcriptsByIDs = transcriptAdaptor.fetch(ids);
    forceChildrenToLoad(transcriptsByIDs);
    if (showRuntimes) {
      timer.stop();
      System.out.println(
        "id array only took " + timer.getDurationInSecs() + "s");
    }

    // id list fetches against the sequential fetch
    for (int i = 0; i < n; i++) {

      Transcript seqt = (Transcript) transcriptsSequential.get(i);

      Transcript idt = (Transcript) transcriptsByIDs.get(i);
      compareTranscripts(seqt, idt);

      Transcript idct = (Transcript) transcriptsByIDsIncludeChildren.get(i);
      compareTranscripts(seqt, idct);
    }
  }

  /**
   * compares the two transcripts to see if they contain the same data.
   */
  private void compareTranscripts(Transcript t1, Transcript t2) {

    assertEquals(t1.getInternalID(), t2.getInternalID());
    assertEquals(t1.getGeneInternalID(), t2.getGeneInternalID());

    assertTrue(t1.getLocation().compareTo(t2.getLocation()) == 0);

    Exon e1 = (Exon) t1.getExons().get(0);
    Exon e2 = (Exon) t2.getExons().get(0);
    assertEquals(e1.getInternalID(), e2.getInternalID());
    assertEquals(t1.getExons().size(), t1.getExons().size());

    Translation tn1 = t1.getTranslation();
    Translation tn2 = t2.getTranslation();

    if (tn1 == null) {
      
      assertEquals(tn1,tn2);
      
    } else {

      assertNotNull(tn1);
      assertNotNull(tn2);
      assertEquals(tn1.getInternalID(), tn2.getInternalID());

      Exon tn1startExon = tn1.getStartExon();
      assertNotNull(tn1startExon);
      Exon tn2startExon = tn2.getStartExon();
      assertNotNull(tn2startExon);
      assertEquals(tn1startExon.getInternalID(), tn2startExon.getInternalID());
    }
  }

  /**
   * Examines the child data in the transcripts.
   * @param transcripts list of transcripts
   */
  private void forceChildrenToLoad(List transcripts) {
    for (int i = 0, n = transcripts.size(); i < n; i++) {

      Transcript t = (Transcript) transcripts.get(i);

      List exons = t.getExons();
      //System.out.println("Checking transcript " + t.getInternalID());
      assertTrue(exons.size() > 0);
      for (int j = 0, ne = exons.size(); j < ne; j++) {

        Exon e = (Exon) exons.get(j);
        Location l = e.getLocation();
        //System.out.println("got transcript, exon, location " + t.getInternalID() + '\t'+e.getInternalID() + '\t'+l);
        assertNotNull(l);
      }

      // TODO ? can't check for null because they are not allways present
      //t.getTranslation();
    }

  }


	public void testSeqEditSupport() throws Exception {
		
		Transcript t = transcriptAdaptor.fetch(1);
		
		String originalSeq = t.getSequence().getString();
		
		Attribute edit;
		String tmp;
	
		// test preprending seq edit and removing it
		edit = new AttributeImpl("_rna_edit","name","description","1 0 CC");
		t.addAttribute(edit);
		tmp = t.getSequence().getString();
		assertEquals(originalSeq.length()+2, tmp.length());
		t.removeAttribute(edit);
		tmp = t.getSequence().getString();
		assertEquals(originalSeq, tmp);
		
	
	}
	
} // TranscriptTest
