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

package org.biojava.bio.program.das;

import java.util.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;

/**
 * Experimental interface, required to do some of our scariest
 * optimizations in a vaguely clean way.  Will either go
 * away, or move into core packages.
 *
 * @author Thomas Down
 */

public interface DASOptimizableFeatureHolder extends FeatureHolder {
    public Set getOptimizableFilters() throws BioException;
    public FeatureHolder getOptimizedSubset(FeatureFilter ff) throws BioException;
}
