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

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.utils.*;
import java.util.*;

/**
 * @author Thomas Down
 */

class DASMergeFeatureHolder extends MergeFeatureHolder implements DASOptimizableFeatureHolder {
    public Set getOptimizableFilters() {
	Map mm = getMergeMap();
	Set osf = new HashSet();
	for (Iterator i = mm.values().iterator(); i.hasNext(); ) {
	    osf.add(i.next());
	}

	return osf;
    }

    public FeatureHolder getOptimizedSubset(FeatureFilter ff) 
        throws BioException
    {
	List ss = new ArrayList();
	Map mm = getMergeMap();
	for (Iterator i = mm.entrySet().iterator(); i.hasNext(); ) {
	    Map.Entry me = (Map.Entry) i.next();
	    FeatureHolder fh = (FeatureHolder) me.getKey();
	    FeatureFilter tff = (FeatureFilter) me.getValue();
	    if (tff.equals(ff)) {
		ss.add(fh);
	    }
	}

	if (ss.size() == 0) {
	    throw new BioException("No optimized subset matching: " + ff);
	} else if (ss.size() == 1) {
	    return (FeatureHolder) ss.get(0);
	} else {
	    MergeFeatureHolder mfh = new MergeFeatureHolder();
	    for (Iterator i = ss.iterator(); i.hasNext(); ) {
		try {
		    mfh.addFeatureHolder((FeatureHolder) i.next());
		} catch (ChangeVetoException cve) {
		    throw new BioError(cve);
		}
	    }
	    return mfh;
	}
    }
}
