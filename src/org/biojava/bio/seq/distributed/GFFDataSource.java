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

package org.biojava.bio.seq.distributed;

import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.bio.program.gff.*;

public class GFFDataSource implements DistDataSource {
    private GFFEntrySet gffe;
    private Set ids;

    public GFFDataSource(GFFEntrySet gffe) {
	this.gffe = gffe;
    }

    public boolean hasSequence(String id) throws BioException {
	return false;
    }

    public boolean hasFeatures(String id) throws BioException {
	return ids(false).contains(id);
    }

    public FeatureHolder getFeatures(FeatureFilter ff) throws BioException {
	throw new BioException();
    }

    public FeatureHolder getFeatures(String id, FeatureFilter ff, boolean recurse) throws BioException {
	if (! hasFeatures(id)) {
	    return FeatureHolder.EMPTY_FEATURE_HOLDER;
	}
	
	SymbolList dummy = new DummySymbolList(DNATools.getDNA(), 1000000000);
	Sequence seq = new SimpleSequence(dummy, id, id, Annotation.EMPTY_ANNOTATION);
	try {
	    seq = gffe.getAnnotator().annotate(seq);
	} catch (ChangeVetoException cve) {
	    throw new BioError(cve);
	}
	
	if (recurse == false && FilterUtils.areProperSubset(FeatureFilter.all, ff)) {
	    return seq;
	} else {
	    return seq.filter(ff, recurse);
	}
    }

    public Sequence getSequence(String id) throws BioException {
	throw new BioException();
    }

    public Set ids(boolean topLevel) throws BioException {
	if (ids == null) {
	    Set _ids = new HashSet();

	    for (Iterator i = gffe.lineIterator(); i.hasNext(); ) {
		Object o = i.next();
		if (o instanceof GFFRecord) {
		    GFFRecord rec = (GFFRecord) o;
		    _ids.add(rec.getSeqName());
		}
	    }

	    ids = Collections.unmodifiableSet(_ids);
	}

	return ids;
    }
}
