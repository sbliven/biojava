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
 
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ensembl.datamodel.Location;
import org.ensembl.util.SequenceUtil;

/**
 * Test class for org.ensembl.util.SequenceUtil.
**/
public class SequenceUtilTest extends TestCase {
  
  private static Logger logger = Logger.getLogger(SequenceUtilTest.class.getName());
  
  public SequenceUtilTest(String name){
    super(name);
  }
  
  
  public static Test suite() { 
    return new TestSuite(SequenceUtilTest.class);
  }
  
  protected void setUp() throws Exception {
  }

  public void testDna2ProteinConverstions() throws Exception {

    for( int i=0; i<invalidCodons.length; ++i) {
      try {
        SequenceUtil.codon2aminoAcid( invalidCodons[i] );
        fail("Should have failed to convert this dna:" + invalidCodons[i]);
      } catch( IllegalArgumentException e ) {}
    }    
      
    for( int i=0; i<validDNA.length; ++i) {
      String dna = validDNA[i];
      String protein = SequenceUtil.dna2protein( dna,false );
      assertNotNull("Failed to convert dna:"+dna, 
                    protein);

      assertEquals("Protein is wrong length for dna:"+dna
                   + " --> protein:"+protein+"<"
                   ,dna.length()/3
                   ,protein.length() );

    }
  }

  
  public void testApplyForwardStrandAlleleToForwardStrandDNA() throws Exception {
    
    assertEquals("aaa", SequenceUtil.replaceSubSequence("aaa", new Location("chromosome:1:1-3:1"),
        "a", new Location("chromosome:1:1-1:1")));
    assertEquals("taa", SequenceUtil.replaceSubSequence("aaa", new Location("chromosome:1:1-3:1"),
        "t", new Location("chromosome:1:1-1:1")));
    assertEquals("aata", SequenceUtil.replaceSubSequence("aaaa", new Location("chromosome:1:1-4:1"),
        "t", new Location("chromosome:1:3-3:1")));
    assertEquals("aatttaaaaa", SequenceUtil.replaceSubSequence("aaaaaaaaaa", new Location("chromosome:1:1-10:1"),
        "ttt", new Location("chromosome:1:3-5:1")));
    assertEquals("aatttaaaaa", SequenceUtil.replaceSubSequence("aaaaaaaaaa", new Location("chromosome:1:13-22:1"),
        "ttt", new Location("chromosome:1:15-17:1")));
  }
  
  public void testApplyReverseStrandAlleleToForwardStrandDNA() throws Exception {
    assertEquals("taa", SequenceUtil.replaceSubSequence("aaa", new Location("chromosome:1:1-3:1"),
        "a", new Location("chromosome:1:1-1:-1")));
    assertEquals("aca", SequenceUtil.replaceSubSequence("aaa", new Location("chromosome:1:1-3:1"),
        "g", new Location("chromosome:1:2-2:-1")));
    assertEquals("aag", SequenceUtil.replaceSubSequence("aaa", new Location("chromosome:1:1-3:1"),
        "c", new Location("chromosome:1:3-3:-1")));
    assertEquals("aacga", SequenceUtil.replaceSubSequence("aaaaa", new Location("chromosome:1:1-5:1"),
        "cgt", new Location("chromosome:1:2-4:-1")));
    
  }
  
  
  public void testApplyForwardStrandAlleleToReverseStrandDNA() throws Exception {
    
    assertEquals("aac", SequenceUtil.replaceSubSequence("aaa", new Location("chromosome:1:1-3:-1"),
        "g", new Location("chromosome:1:1-1:1")));
    assertEquals("aca", SequenceUtil.replaceSubSequence("aaa", new Location("chromosome:1:1-3:-1"),
        "g", new Location("chromosome:1:2-2:1")));
    assertEquals("gaa", SequenceUtil.replaceSubSequence("aaa", new Location("chromosome:1:1-3:-1"),
        "c", new Location("chromosome:1:3-3:1")));
    assertEquals("agcta", SequenceUtil.replaceSubSequence("aaaaa", new Location("chromosome:1:1-5:-1"),
        "agc", new Location("chromosome:1:2-4:1")));
    
  }
  
  public void testApplyReverseStrandAlleleToReverseStrandDNA() throws Exception {
    
    assertEquals("aag", SequenceUtil.replaceSubSequence("aaa", new Location("chromosome:1:1-3:-1"),
        "g", new Location("chromosome:1:1-1:-1")));
    assertEquals("aga", SequenceUtil.replaceSubSequence("aaa", new Location("chromosome:1:1-3:-1"),
        "g", new Location("chromosome:1:2-2:-1")));
    assertEquals("caa", SequenceUtil.replaceSubSequence("aaa", new Location("chromosome:1:1-3:-1"),
        "c", new Location("chromosome:1:3-3:-1")));
    assertEquals("aagca", SequenceUtil.replaceSubSequence("aaaaa", new Location("chromosome:1:1-5:-1"),
        "agc", new Location("chromosome:1:2-4:-1")));
  }

  
  private String[] invalidCodons = new String[] 
    {
      "A"
      ,"AC"
      ,"AAAA"
      ,"ACTGA"
      ,"NA"
      ,"NNNN"
    };
  
  private String[] validDNA = new String[] 
    { 
      ""
      ,"ACT"
      ,"CCTTAA"
      ,"CTGATGGTC"
      ,"NNN"
      ,"NNNNNNNNN"
      ,"ATGGATGGAGAGAATCACTCAGTGGTATCTGAGTTTTTGTTTCTGGGACTCACTCATTCATGGGAGATCCAGCTCCTCCTCCTAGTGTTTTCCTCTGTGCTCTATGTGGCAAGCATTACTGGAAACATCCTCATTGTGTTTTCTGTGACCACTGACCCTCACTTACACTCCCCCATGTACTTTCTACTGGCCAGTCTCTCCTTCATTGACTTAGGAGCCTGCTCTGTCACTTCTCCCAAGATGATTTATGACCTGTTCAGAAAGCGCAAAGTCATCTCCTTTGGAGGCTGCATCGCTCAAATCTTCTTCATCCACGTCGTTGGTGGTGTGGAGATGGTGCTGCTCATAGCCATGGCCTTTGACAGATATGTGGCCCTATGTAAGCCCCTCCACTATCTGACCATTATGAGCCCAAGAATGTGCCTTTCATTTCTGGCTGTTGCCTGGACCCTTGGTGTCAGTCACTCCCTGTTCCAACTGGCATTTCTTGTTAATTTAGCCTTCTGTGGCCCTAATGTGTTGGACAGCTTCTACTGTGACCTTCCTCGGCTTCTCAGACTAGCCTGTACCGACACCTACAGATTGCAGTTCATGGTCACTGTTAACAGTGGGTTTATCTGTGTGGGTACTTTCTTCATACTTCTAATCTCCTACGTCTTCATCCTGTTTACTGTTTGGAAACATTCCTCAGGTGGTTCATCCAAGGCCCTTTCCACTCTTTCAGCTCACAGCACAGTGGTCCTTTTGTTCTTTGGTCCACCCATGTTTGTGTATACACGGCCACACCCTAATTCACAGATGGACAAGTTTCTGGCTATTTTTGATGCAGTTCTCACTCCTTTTCTGAATCCAGTTGTCTATACATTCAGGAATAAGGAGATGAAGGCAGCAATAAAGAGAGTATGCAAACAGCTAGTGATTTACAAGAGGATCTCA"
      
      ,"ATGGCGCAACCTTGCTGGTCACTGCAACCTCTGCCTCCTGGGTTCAAGAAATTCTCCTGCCTTAGCCTCCCAAGTCACTGGGATTACAGGTGCCCACCACCACACCAGGCTAATTTTTGTATTTTTAGTGGAGATGCGGTTTCACCATGTTGGCCGGGCCAGTCTCGAACTCCTGACGTCAAG"


      ,"GAGAGGGAGGGAGGGATGCAGAGAGGGAGAGAGAGAGAGGGAGGGATGCAGAGAGAGGGAGATGCACAGAGAGAGGGAGGGATGCAGAGAGAGAGGGAGGGGAGGGATGTAGAGAGAGAGAGAGAGAGAGGGAGGGATGCAGAGAGAGAGAGAGGGAGGGAGGGATGCAGAGAGAGAGGGAGGGAGGGATGCAGAGAGAGAGGGAGGGAGGGATGCAGAGAGAGAGGGAGGGAAGAATGCAGAGAGAGAGAGAGGGAGGGAGGGATGCAGAGAGAGAGAGATGCACAGAGAGAGGGAGGGATGCAGAGAGAGGGAGGGATGCAGAGAGAGAGAGAGGGAGGGAGGGATGCAGAGAGAGAGAGAGAGAGGGAGGGATGCAGAGAGAGAGAGAGGGAGGGAGGGATGCAGAGAGAGAGAGATGCACAGAGAGAGGGAGGGATGCAGAGAGAGGGAGGGATGCAGAGAGAGAGGGAGGGGAGGGATGCAGAGAGAGAGATGCACAGAGAGAGGGAGGGATGCAGAGAGAGAGGTGGGGTTGAAGAGAGAGAGAGGCAGTCAGAGAGGTGGGGGAGGAGAGAGAAGGAGGCAGGCAGAGAGAGAGAGTGGGAGAGAGAGAGAGAGGCAGAGAGAGAGAGGGAGGAGAGGTGGGAGGAGAGAGGCAGGCAGAGAGAGAAGGGGAGAGAGAGAGTTGGGAGGAGAGAGAGAGGCAAGGAGAGAGGGAGAGGGAAGGGGAGAGAGAGAGAGGCAGGCAGAGAGAGAGGGAGAGGGAGGGGGAGAGAGAGAGGCAGAGGGAGAGGGAGGGAGAGAGAGAGACAGAGAGAGAGGGAGAGGGAGGGAGAGAGAGAGGCAGAGAGAGAGGGAGAGGGAGGGAGAGAGAGAGGCAGAGAGGGAGGGAGAGAGAGAGGCAGAGAGAGAGGGAGGGGGAGAGAGGCAGAGAGAGAGAGGGAGGGGGAGAGAGAGAGGCAGAGAAGGGGGGAGAGAGAGAGGGAGGGGGAGAGAGAGAGGCCGTCAGAGAGGGAGGGCAGAGAGAGATGGGCAGAGAAAGAGAGAGGGAAA"

      ,"TTTAAAAAGAGAAAGGGAAGCCTCCGTATACTTCAGTCCTCAGCTCTGGCAGCTGCCCATCGGACAAAGGAACAGGACGATGATGCTGGCGTCGGTGCTGGGGAGCGGCCCCCGGGCGGGCCTCTGCTCTGGCCCCTCCTGGGGCCCGCACTCTCGCTCTGGGCCCGCTCCTCTTCCGCCACTGCCATGGACCACGAGAAGATGTCCCCCCTCGATCCTTGCCACCATCCAAGGAATTTTGCAATTGATATTGGGGATATTGGCAGATACAGCCCCGGAGCCAGAGGGTGGCTTGACTTTGTTCCTGGTGAAGCGGTGAGTGCTGGAGATGCATTATGTGAAATTGAGACTGACAAAGCTGTGGTTACCTTAGATGCAAGTGATGATGGAATCTTGGCCAAAATCGTGGTTGAAGAAGGAAGTAAAAATATACGGCTAGGTTCACTAATTGGTTTGATAGTAGAAGAAGGAGAAGATTGGAAACATGTTGAAATTCCCAAAGACGTAGGTCCTCCACCACCAGTTTCAAAACCTTCAGAGCCTCGCCCCTCACCAGAACCACAGATTTCCATCCCTGTCAAGAAGGAACACATACCCGGGACACTACGGTTCCGTTTAAGTCCAGCTGCCCGCAATATTCTGGAAAAACACTCACTGGATGCTAGCCAGGGCACAGCCACTGGCCCTCGGGGGATATTCACTAAAGAGGATGCTCTCAAACTTGTCCAGTTGAAACAAACGGGCAAGATTACCGAGTCCAGACCAACTCCAGCCCCCACAGCCACTCCCACAGCACCTTCGCCCCTACAGGCCACAGCTGGACCATCTTATCCCCGGCCTGTGATCCCACCAGTATCAACTCCTGGACAACCCAATGCAGTGGGCACATTCACTGAAATCCCCGCCAGCAATATTCGAAGAGTCATTGCCAAGAGATTAACTGAATCTAAAAGTACTGTACCTCATGCATATGCTACTGCTGACTGTGACCTTGGAGCTGTTTTAAAAGTTAGGCAAGATCTGGTCAAAG"
    };

}
