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
package org.biojava.bio.dist;

import java.util.*;
import java.io.*;

import org.biojava.utils.*;
import org.biojava.bio.symbol.*;

/**
 * Provides an N'th order distribution.  This is a distribution over one
 * alphabet which is conditioned on having previously observed one or
 * more other symbols (potentially from different alphabets).
 *
 * <p>
 * Order-N distributions are always over a CrossProductAlphabet.
 * </p>
 *
 * <p>
 * <strong>Note:</strong> Unlike normal distributions, the total weights for
 * all symbols in the overall alphabet do <em>not</em> sum to 1.0.  Instead,
 * the weights of each sub-distribution should sum to 1.0.
 * </p>
 *
 * <p>
 * This would typically be used in conjunction with an OrderNSymbolList.
 * </p>
 *
 * @author Thomas Down
 * @author Samiul Hasan
 * @author Matthew Pocock
 */

public interface OrderNDistribution extends Distribution {
    /**
     * Get the conditioning alphabet of this distribution.  If the `overall'
     * alphabet is a cross-product of two alphabets, this will be the first 
     * of those alphabets.  If it is a cross-product of more than two alphabets,
     * the conditioning alphabet is the cross-product of all but the last
     * alphabet.
     */

    public Alphabet getConditioningAlphabet();

    /**
     * Get the conditioned alphabet.  This is the last alphabet in the
     * distribution's overall cross-product.  It will be the alphabet of
     * all the sub-distributions contained within this OrderNDistribution.
     */

    public Alphabet getConditionedAlphabet();

    public Collection conditionedDistributions();

      public abstract void setDistribution(Symbol sym, Distribution dist)
      throws IllegalSymbolException, IllegalAlphabetException;
  
  public abstract Distribution getDistribution(Symbol sym)
      throws IllegalSymbolException;
}
