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

/**
 * FeatureHolder which exposes all the features in a set
 * of sub-FeatureHolders.  This is provided primarily as
 * a support class for ViewSequence.  It may also be useful
 * for other applications, such as simple distributed
 * annotation systems.
 *
 * @author Thomas Down
 */

public class MergeFeatureHolder extends AbstractFeatureHolder {
    private Set featureHolders;

    {
	featureHolders = new HashSet();
    }

    /**
     * Add an extra FeatureHolder to the set of FeatureHolders which
     * are merged.
     */

    public void addFeatureHolder(FeatureHolder fh) {
	featureHolders.add(fh);
    }

    /**
     * Remove a FeatureHolder from the set of FeatureHolders which
     * are merged.
     */

    public void removeFeatureHolder(FeatureHolder fh) {
	featureHolders.remove(fh);
    }

    public int countFeatures() {
	int fc = 0;
	for (Iterator i = featureHolders.iterator(); i.hasNext(); ) {
	    fc += ((FeatureHolder) i.next()).countFeatures();
	}
	return fc;
    }

    /**
     * Iterate over all the features in all child FeatureHolders.
     * The Iterator may throw ConcurrantModificationException if
     * there is a change in the underlying collections during
     * iteration.
     */

    public Iterator features() {
	return new MFHIterator();
    }

    /**
     * When applied to a MergeFeatureHolder, this filters each child
     * FeatureHolder independently.
     */

    public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
	MergeFeatureHolder mfh = new MergeFeatureHolder();
	for (Iterator fhi = featureHolders.iterator(); fhi.hasNext(); ) {
	    FeatureHolder fh = (FeatureHolder) fhi.next();
	    mfh.addFeatureHolder(fh.filter(ff, recurse));
	}
	return mfh;
    }

    private class MFHIterator implements Iterator {
	private Iterator fhIterator;
	private Iterator fIterator;

	public MFHIterator() {
	    fhIterator = featureHolders.iterator();
	    if (fhIterator.hasNext())
		fIterator = ((FeatureHolder) fhIterator.next()).features();
	    else
		fIterator = Collections.EMPTY_SET.iterator();
	}

	public boolean hasNext() {
	    if (fIterator.hasNext())
		return true;
	    if (fhIterator.hasNext()) {
		fIterator = ((FeatureHolder) fhIterator.next()).features();
		return hasNext();
	    }
	    return false;
	}

	public Object next() {
	    if (fIterator.hasNext())
		return fIterator.next();
	    if (fhIterator.hasNext()) {
		fIterator = ((FeatureHolder) fhIterator.next()).features();
		return next();
	    }
	    throw new NoSuchElementException();
	}

	public void remove() {
	    throw new UnsupportedOperationException();
	}
    }
}
