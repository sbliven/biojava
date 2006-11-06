/*
 * Copyright (C) 2003 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.ensembl.test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Sequence;
import org.ensembl.datamodel.Translation;
import org.ensembl.datamodel.impl.SequenceImpl;
import org.ensembl.driver.TranslationAdaptor;
import org.ensembl.util.IDSet;
import org.ensembl.util.JDBCUtil;

/**
 * Test class for Translations.
 */
public class TranslationTest extends CoreBase {

    private static final Logger logger = Logger.getLogger(TranslationTest.class.getName());

    private TranslationAdaptor translationAdaptor;

    public static final void main(String[] args) {

        junit.textui.TestRunner.run(suite());
    }

    public TranslationTest(String name) {

        super(name);
    }

    public static Test suite() {

        TestSuite suite = new TestSuite();
        suite.addTestSuite(TranslationTest.class);
        //suite.addTest( new TranslationTest("testFetchBySynonym") );
        //suite.addTest( new TranslationTest("testBasic") );
        return suite;
    }

    protected void setUp() throws Exception {
      super.setUp();
      translationAdaptor = (TranslationAdaptor) driver.getAdaptor("translation");
    }


    public void testFetchBySynonym() throws Exception {

        //String[] synonyms = new String[] { "NP_001059", "TOP2B"};
      String[] synonyms = new String[] { "TOP2B"};
        for (int i = 0; i < synonyms.length; ++i) {
            logger.fine("Fetching translation for synonym : " + synonyms[i]);
            List translations = translationAdaptor.fetchBySynonym(synonyms[i]);
            assertTrue("Failed to retrieve translation with synonym=" + synonyms[i], translations.size() > 0);

        }
    }

    public void testBasic() throws Exception {

        int[] internalIDs = new int[] {1,2,3};
        for (int i = 0; i < internalIDs.length; ++i) {
            Translation translation = translationAdaptor.fetch(internalIDs[i]);
            assertNotNull(translation);
            logger.fine(translation.toString());
        }
    }

    public void testPeptideTranslationAndGetAminoAcidStart() throws Exception {

        Translation transl = new org.ensembl.datamodel.impl.TranslationImpl();
        String testDNA = "ATGGCCTTCAGCGGTTCCCAGGCTCCCTACCTGAGTCCAGCTGTCCCCTTTTCTGGG";
        String expected = "MAFSGSQAPYLSPAVPFSG";
        Sequence seq = new SequenceImpl();
        seq.setString(testDNA);
        transl.setSequence(seq);
        String pep = transl.getPeptide();
        assertEquals(expected, pep);
        
        // make sure we test transscripts on +ve and -ve strands
        checkPeptideAndGetAminoAcid(1,1);
        checkPeptideAndGetAminoAcid(6, -1);
        
    }

    private void checkPeptideAndGetAminoAcid(long transcriptID, int expectedStrand) throws Exception{
        Translation transl = driver.getTranscriptAdaptor().fetch(transcriptID).getTranslation();
        Location codingLoc = transl.getCodingLocation();
        assertEquals(expectedStrand, codingLoc.getStrand());
        // make sure we test a multi-exon tranlation because they are
        // more complicated and therefore more likely to be wrong
        assertTrue(codingLoc.getNodeLength()>1);
        String pep = transl.getPeptide();
        // 3 bases (1 codon) = 1 amino acid, sometimes coding location includes stop codon
        assertTrue(3*pep.length()==codingLoc.getLength() || 3*pep.length()==codingLoc.getLength()-3);
        for (int i = 1; i < pep.length()+1; i++) {
            Location r = transl.getAminoAcidStart(i);
            assertEquals(1, r.getLength());
            assertTrue(r.overlaps(codingLoc));
        }
    }

    public void testPeptideTranslationNotTriplet() throws Exception {

        //System.out.println("running testPeptideTranslation()");
        Translation transl = new org.ensembl.datamodel.impl.TranslationImpl();
        String testDNA = "ATGGCCTTCAGCGGTTCCCAGGCTCCCTACCTGAGTCCAGCTGTCCCCTTTTCTG";
        String expected = "MAFSGSQAPYLSPAVPFS";
        Sequence seq = new SequenceImpl();
        seq.setString(testDNA);
        transl.setSequence(seq);
        String pep = transl.getPeptide();
        assertEquals(expected, pep);
    }

    public void testInterproSupport() throws Exception {

        List tns = driver.getTranslationAdaptor().fetchByInterproID("IPR000405");
        assertTrue(tns.size() > 0);
        Translation tn = (Translation) tns.get(0);
        assertTrue(tn.getInterproIDs().length > 0);

    }

//    public void testFetchAll() throws Exception {
//
//        List tns = driver.getTranslationAdaptor().fetchAll();
//        assertTrue(tns.size() > 0);
//        System.out.println("fetchAll() got " + tns.size());
//    }

    
    public void testSelenocysteinSupport() throws Exception {
      // At the time the test was 
      // written (ensembl 36) all translation attribs were selenocystein edits that should
      // be applied to the peptide.
      IDSet ids = new IDSet();
      Connection conn = null;
      try {
        conn = driver.getConnection();
        ResultSet rs = conn.createStatement().executeQuery("select translation_id from translation_attrib");
        while(rs.next()) 
          ids.add(rs.getLong(1));
      } finally {
        JDBCUtil.close(conn);
      }
      
      long[] idsa = ids.to_longArray();
      for (int i = 0; i < idsa.length; i++) {
        Translation tn = driver.getTranslationAdaptor().fetch(idsa[i]);
        // getting the peptide should be enough to ensure everyting is work
        String pep = tn.getPeptide();
        assertTrue(pep.length()>0);
        //System.out.println(tn.getAccessionID() + "\t" + pep);
      }
    }
    
}