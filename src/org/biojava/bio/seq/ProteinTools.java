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

package org.biojava.bio.seq;

import java.util.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;

/**
 * The central port-of-call for all information and functionality specific to
 * SymbolLists over the protein alphabet.
 *
 * @author Matthew Pocock
 */
public class ProteinTools {
  private static final FiniteAlphabet proteinAlpha;
  private static final FiniteAlphabet proteinXAlpha;
  private static final FiniteAlphabet proteinTAlpha;
  private static final Symbol x;
  private static final Symbol termination;
  
  static {
    try {
      AlphabetManager am = AlphabetManager.instance();
      proteinAlpha = (FiniteAlphabet) am.alphabetForName("PROTEIN");
      SimpleAlphabet xAlpha = new SimpleAlphabet();
      SimpleAlphabet tAlpha = new SimpleAlphabet();
      xAlpha.setName(proteinAlpha.getName() + "+X");
      tAlpha.setName(proteinAlpha.getName() + "+T");
      for(Iterator i = proteinAlpha.iterator(); i.hasNext(); ) {
        Symbol r = (Symbol) i.next();
        xAlpha.addSymbol(r);
        tAlpha.addSymbol(r);
      }
      x = am.symbolForName("X");
      termination = am.symbolForName("termination");
      xAlpha.addSymbol(x);
      tAlpha.addSymbol(termination);
      proteinXAlpha = xAlpha;
      proteinTAlpha = tAlpha;
    } catch (IllegalSymbolException ire) {
      throw new BioError(ire, " Could not initialize ProteinTools");
    }
  }
  
  public static final FiniteAlphabet getAlphabet() {
    return proteinAlpha;
  }
  
  public static final FiniteAlphabet getXAlphabet() {
    return proteinXAlpha;
  }
  
  public static final Symbol getXSymbol() {
    return x;
  }
  
  public static final FiniteAlphabet getTAlphabet() {
    return proteinTAlpha;
  }
  
  public static final Symbol getTSymbol() {
    return termination;
  }
}
