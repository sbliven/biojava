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


package org.biojava.bio.structure;

import java.util.*;


import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.utils.*;

public final class AlphaCTools {

  public static final double MAX_ANGLE = 180.0;
  public static final double MIN_ANGLE = -180.0;

  private static String ALPHA = "ALPHA CARBON ANGLES";
  private static DoubleAlphabet daInstance = DoubleAlphabet.getInstance();


  /**
   * Returns a reference to the Alphabet that contains Symbols that represent PHI,
   * PSI angles.
   *
   * @return a reference to the ALPHA CARBON ANGLES alphabet
   */
  public static Alphabet getAlphaCarbonAngleAlphabet(){
    if (AlphabetManager.registered(ALPHA)) {
      return AlphabetManager.alphabetForName(ALPHA);
    }
    else {
      List l = Collections.nCopies(2, DoubleAlphabet.getInstance());
      try {
        Alphabet a = AlphabetManager.getCrossProductAlphabet(l, ALPHA);
        AlphabetManager.registerAlphabet(ALPHA, a);

        return a;
      }
      catch (IllegalAlphabetException ex) {
        throw new BioError(ex, "Cannot construct "+ALPHA+" alphabet");
      }
    }
  }

  /**
   * Makes a Phi - Psi Symbol from the ALPHA CARBON ANGLES alphabet
   * @param phiAngle the phi angle between -180.0 and +180.0
   * @param psiAngle the psi angle between -180.0 and +180.0
   * @return a reference to the 'fly weight' Symbol.
   * @throws IllegalSymbolException if the bond angles are outside the specified range
   */
  public static Symbol getPhiPsiSymbol(double phiAngle, double psiAngle)
    throws IllegalSymbolException{

    if(phiAngle > MAX_ANGLE || phiAngle < MIN_ANGLE){
      throw new IllegalSymbolException("Phi angle must be between -180.0 and +180.0");
    }

    if(psiAngle > MAX_ANGLE || psiAngle < MIN_ANGLE){
      throw new IllegalSymbolException("Psi angle must be between -180.0 and +180.0");
    }

    Symbol phi = daInstance.getSymbol(phiAngle);
    Symbol psi = daInstance.getSymbol(psiAngle);

    return getAlphaCarbonAngleAlphabet().getSymbol(new ListTools.Doublet(phi, psi));
  }
}