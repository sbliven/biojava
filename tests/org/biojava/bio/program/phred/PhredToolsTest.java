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

package org.biojava.bio.program.phred;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.utils.*;
import junit.framework.TestCase;

/**
 * @author Matthew Pocock
 */
public class PhredToolsTest
extends TestCase {
  public PhredToolsTest(String name) {
    super(name);
  }
  
  public void testGetPhredSymbol() {
    try {
      PhredTools.getPhredSymbol(DNATools.a(), IntegerAlphabet.getInstance().getSymbol(1));
      PhredTools.getPhredSymbol(DNATools.g(), IntegerAlphabet.getInstance().getSymbol(2));
      PhredTools.getPhredSymbol(DNATools.c(), IntegerAlphabet.getInstance().getSymbol(3));
      PhredTools.getPhredSymbol(DNATools.t(), IntegerAlphabet.getInstance().getSymbol(4));
    } catch (IllegalSymbolException ise) {
      throw new AssertionFailure(ise);
    }
  }
  
  public void testGetPhredSymbolAmbiguous() {
    try {
      PhredTools.getPhredSymbol(DNATools.n(), IntegerAlphabet.getInstance().getSymbol(5));
    } catch (IllegalSymbolException ise) {
      throw new AssertionFailure(ise);
    }
  }
}
