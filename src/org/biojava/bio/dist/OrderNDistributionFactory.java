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

import org.biojava.bio.symbol.*;
import java.util.*;

/**
 * Default factory for Order-N distributions.
 *
 * @author Thomas Down
 */

public class OrderNDistributionFactory implements DistributionFactory {
    /**
     * Factory which used DistributionFactory.DEFAULT to create conditioned
     * distributions.
     */
    
    public static final DistributionFactory DEFAULT;

    static {
	DEFAULT = new OrderNDistributionFactory(DistributionFactory.DEFAULT);
    }

    private final DistributionFactory df;

    /**
     * Construct a new OrderNDistributionFactory with a specified factory
     * for conditioned distributions.
     *
     * @param df The DistributionFactory used for construction new conditioned
     *           distributions.
     */

    public OrderNDistributionFactory(DistributionFactory df) {
	this.df = df;
    }

    public Distribution createDistribution(Alphabet alpha)
        throws IllegalAlphabetException
    {
	List aList = alpha.getAlphabets();
	if (
          aList.size() == 2 &&
          aList.get(0) == org.biojava.bio.seq.DNATools.getDNA()
        ) {
	    return new IndexedNthOrderDistribution(alpha, df);
	} else {
	    return new GeneralNthOrderDistribution(alpha, df);
        }
    }
}
