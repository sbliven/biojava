/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 */


package org.biojava.bio.symbol;

import junit.framework.*;
import java.util.*;
import org.biojava.bio.seq.*;


public class AlphabetManagerTest extends TestCase {

  public AlphabetManagerTest(String s) {
    super(s);
  }

  protected void setUp() {
  }

  protected void tearDown() {
  }

  public void testAlphabetForName() {
    String name1=  "DNA";
    String name2 = "PROTEIN";
    String name3 = "(DNA x DNA x DNA)";
    String name4 = "((DNA x DNA x DNA) x PROTEIN)";
    String name5 = "(PROTEIN x (DNA x DNA x DNA))";
    String name6 = "((DNA x DNA x DNA) x DNA x (PROTEIN x DNA))";

    try {
      Alphabet alphabetRet = AlphabetManager.alphabetForName(name1);
      assertEquals(DNATools.getDNA(),alphabetRet);

      alphabetRet = AlphabetManager.alphabetForName(name2);
      assertEquals(ProteinTools.getAlphabet(),alphabetRet);

      alphabetRet = AlphabetManager.alphabetForName(name3);
      assertEquals(alphabetRet.getName(), name3);

      alphabetRet = AlphabetManager.alphabetForName(name4);
      assertEquals(alphabetRet.getName(), name4);

      alphabetRet = AlphabetManager.alphabetForName(name5);
      assertEquals(alphabetRet.getName(), name5);

      alphabetRet = AlphabetManager.alphabetForName(name6);
      assertEquals(alphabetRet.getName(), name6);
    }
    catch(Exception e) {
      System.err.println("Exception thrown:  "+e);
    }
  }
  
  public void testGetAllSymbols() 
      throws Exception
  {
      FiniteAlphabet dna = DNATools.getDNA();
      Set allDna = AlphabetManager.getAllSymbols(dna);
      Symbol n = DNATools.n();
      Set allN = AlphabetManager.getAllSymbols((FiniteAlphabet) n.getMatches());
      assertEquals(allDna.size(), 16);
      assertEquals(allN.size(), 16);
      for (Iterator i = allN.iterator(); i.hasNext(); ) {
          Symbol is = (Symbol) i.next();
          boolean found = false;
          for (Iterator j = allDna.iterator(); j.hasNext(); ) {
              Symbol js = (Symbol) j.next();
              if (is == js) {
                  found = true;
              }
          }
          assertTrue(found);
      }
  }
}
