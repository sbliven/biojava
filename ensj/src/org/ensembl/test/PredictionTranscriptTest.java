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

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.PredictionTranscript;

/**
 * Test class for PredictionTranscripts
 */
public class PredictionTranscriptTest extends CoreBase {

  private static Logger logger = Logger
      .getLogger(PredictionTranscriptTest.class.getName());

  public PredictionTranscriptTest(String name) {
    super(name);
  } //end PredictionTranscriptTest

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(PredictionTranscriptTest.class);
    return suite;
  } //end Test

  public void testRetrievePredictionTranscriptsByLocation() throws Exception {

    Location location = new Location("chromosome:19:1-200000");
    List transcripts = driver.getPredictionTranscriptAdaptor().fetch(location);
    check(transcripts);

    transcripts = driver.getPredictionTranscriptAdaptor().fetch(location,
        "Genscan");
    check(transcripts);

  }

  /**
   * @param transcripts
   */
  private void check(List transcripts) {
    Iterator featureIterator = transcripts.iterator();

    assertTrue(featureIterator.hasNext());

    while (featureIterator.hasNext()) {
      PredictionTranscript transcript = (PredictionTranscript) featureIterator
          .next();
      logger.fine("PredictionTranscript: " + transcript);
      Iterator exonIterator = transcript.getExons().iterator();
      assertTrue(exonIterator.hasNext());
      while (exonIterator.hasNext()) {
        Object e = exonIterator.next();
        logger.fine("\t\tPredictionExon: " + e);
      }
      assertNotNull(transcript.getAnalysis());
    }

    logger.fine("Number of PredictionTranscripts found:" + transcripts.size());
    assertTrue("No PredictionTranscripts found", transcripts.size() > 0);
  }

  public static void main(String[] args) {
    try {
      new PredictionTranscriptTest(
          "testRetrievePredictionTranscriptsByLocation").run();
    } catch (Throwable exception) {
      exception.printStackTrace();
    }
  } //end main
} // PredictionTranscriptTest
