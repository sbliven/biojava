package org.biojava.bio.search;

import junit.framework.TestCase;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.seq.DNATools;

/**
 * Test the MaxMissmatchPattern and MaxMissmatchMatcher classes.
 *
 * @author Matthew Pocock
 * @since 1.4
 */
public class MaxMissmatchPatternTest
extends TestCase {
  public void testTooShort()
  throws IllegalSymbolException, IllegalAlphabetException{
    SymbolList zero = DNATools.createDNA("");
    SymbolList one = DNATools.createDNA("a");
    SymbolList five = DNATools.createDNA("aaaaa");
    SymbolList agct3 = DNATools.createDNA("agctagctagct");
    SymbolList agct = DNATools.createDNA("agct");
    SymbolList aggt = DNATools.createDNA("aggt");
    SymbolList gcta = DNATools.createDNA("gcta");
    SymbolList gctt = DNATools.createDNA("gctt");

    MaxMissmatchPattern mmp = new MaxMissmatchPattern(agct, 0);
    assertFalse("Zero length string has no matches", mmp.matcher(zero).find());
    assertFalse("One length string has no matches", mmp.matcher(one).find());
    assertTrue("Four length identical string has a match", mmp.matcher(agct).find());
    assertFalse("Four length non-identical string has no match", mmp.matcher(aggt).find());
  }
}
