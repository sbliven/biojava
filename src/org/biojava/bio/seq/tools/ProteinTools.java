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

package org.biojava.bio.seq.tools;

import java.util.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;

public class ProteinTools {
  private static final FiniteAlphabet proteinAlpha;
  private static final FiniteAlphabet proteinXAlpha;
  private static final Residue x;
  
  static {
    try {
      AlphabetManager am = AlphabetManager.instance();
      proteinAlpha = (FiniteAlphabet) am.alphabetForName("PROTEIN");
      SimpleAlphabet xAlpha = new SimpleAlphabet();
      xAlpha.setName(proteinAlpha.getName() + "+X");
      for(Iterator i = proteinAlpha.residues().iterator(); i.hasNext(); ) {
        xAlpha.addResidue((Residue) i.next());
      }
      x = am.residueForName("X");
      xAlpha.addResidue(x);
      proteinXAlpha = xAlpha;
    } catch (IllegalResidueException ire) {
      throw new BioError(ire, " Could not initialize ProteinTools");
    }
  }
  
  public static final FiniteAlphabet getAlphabet() {
    return proteinAlpha;
  }
  
  public static final FiniteAlphabet getXAlphabet() {
    return proteinXAlpha;
  }
  
  public static final Residue getXResidue() {
    return x;
  }
}
