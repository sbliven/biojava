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
 *
 */

package org.biojava.bio.symbol;

import java.util.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import junit.framework.TestCase;

/**
 * Test for gapped symbol lists.
 *
 * @author Matthew Pocock
 * @since 1.3
 */

public class GappedSymbolListTest extends TestCase {
  private SymbolList symList;
  private SymbolList symList1;
  private SymbolList symList2;
  private SymbolList symList3;
  private SymbolList symList4;
  
  public GappedSymbolListTest(String name) {
    super(name);
  }
  
  protected void setUp()
  throws Exception {
    FiniteAlphabet dna = (FiniteAlphabet) AlphabetManager.alphabetForName("DNA");
    SymbolTokenization tok = dna.getTokenization("token");
    symList  = new SimpleSymbolList(tok,"gtgtggga");
    symList1 = new SimpleSymbolList(tok,"gtg-tggga");
    symList2 = new SimpleSymbolList(tok,"gtg--tggga");
    symList3 = new SimpleSymbolList(tok,"gtg---tggga");
    symList4 = new SimpleSymbolList(tok,"gtg----tggga");
  }
  
  public void testBlockedInsertIndividualRemove()
  throws Exception {
    GappedSymbolList gll = new GappedSymbolList(symList);
    assertTrue("Gapped same as ungapped:\n" + gll.seqString() + "\n" + symList.seqString(), SymbolUtils.compareSymbolLists(gll, symList));

    gll.addGapsInSource(4,4);
    assertTrue("Four gaps:\n" + gll.seqString() + "\n" + symList4.seqString(), SymbolUtils.compareSymbolLists(gll, symList4));
    
    gll.removeGap(4);
    assertTrue("Three gaps:\n" + gll.seqString() + "\n" + symList3.seqString(), SymbolUtils.compareSymbolLists(gll, symList3));
    
    gll.removeGap(4);
    assertTrue("Two gaps:\n" + gll.seqString() + "\n" + symList2.seqString(), SymbolUtils.compareSymbolLists(gll, symList2));
    
    gll.removeGap(4);
    assertTrue("One gap:\n" + gll.seqString() + "\n" + symList1.seqString(), SymbolUtils.compareSymbolLists(gll, symList1));
    
    gll.removeGap(4);
    assertTrue("All gaps removed:\n" + gll.seqString() + "\n" + symList.seqString(), SymbolUtils.compareSymbolLists(gll, symList));
  }
  
  public void testBlockedInsertBlockRemove()
  throws Exception {
    GappedSymbolList gll = new GappedSymbolList(symList);
    assertTrue(SymbolUtils.compareSymbolLists(gll, symList));

    gll.addGapsInSource(4,4);
    assertTrue(SymbolUtils.compareSymbolLists(gll, symList4));
    
    gll.removeGaps(4,4);
    assertTrue(SymbolUtils.compareSymbolLists(gll, symList));
  }
}
