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
import org.biojava.utils.*;
import org.biojava.bio.symbol.*;

/**
 * FeatureHolder which exposes all the features in a set
 * of sub-FeatureHolders.  This is provided primarily as
 * a support class for ViewSequence.  It may also be useful
 * for other applications, such as simple distributed
 * annotation systems.
 *
 * @author Thomas Down
 * @author Matthew Pocock
 */

public class MergeFeatureHolder extends AbstractFeatureHolder {
    private Map featureHolders;

    /**
     * Create a new, empty, MergeFeatureHolder.
     */

    public MergeFeatureHolder() {
	featureHolders = new SmallMap();
    }

    /**
     * Create a populated MFH
     */

    private MergeFeatureHolder(Map m) {
	featureHolders = m;
    }

    /**
     * Add an extra FeatureHolder to the set of FeatureHolders which
     * are merged.  This method is provided for backward compatibility,
     * and is equivalent to:
     *
     * <pre>
     *     mfh.addFeatureHolder(fh, FeatureFilter.all);
     * </pre>
     *
     * <p>
     * You should always use the two-arg version in preference if you
     * are able to define the membership of a FeatureHolder.
     * </p>
     */

    public void addFeatureHolder(FeatureHolder fh) 
        throws ChangeVetoException
    {
	featureHolders.put(fh, FeatureFilter.all);
    }

    /**
     * Add an extra FeatureHolder to the set of FeatureHolders which
     * are merged, with a filter defining the membership of the new
     * child.
     *
     * @param fh A featureholder
     * @param membershipFilter A featureFilter defining the membership of fh
     * @since 1.2
     */

    public void addFeatureHolder(FeatureHolder fh,
				 FeatureFilter membershipFilter) 
	throws ChangeVetoException
    {
       if(!hasListeners()) {
         featureHolders.put(fh, membershipFilter);
       } else {
         ChangeSupport changeSupport = getChangeSupport(FeatureHolder.FEATURES);
         synchronized(changeSupport) {
           ChangeEvent ce = new ChangeEvent(this, FeatureHolder.FEATURES);
           changeSupport.firePreChangeEvent(ce);
           featureHolders.put(fh, membershipFilter);
           changeSupport.firePostChangeEvent(ce);
         }
       }
    }

    /**
     * Remove a FeatureHolder from the set of FeatureHolders which
     * are merged.
     */

     public void removeFeatureHolder(FeatureHolder fh) 
     throws ChangeVetoException
     {
       if(!hasListeners()) {
         featureHolders.remove(fh);
       } else {
         ChangeSupport changeSupport = getChangeSupport(FeatureHolder.FEATURES);
         synchronized(changeSupport) {
           ChangeEvent ce = new ChangeEvent(this, FeatureHolder.FEATURES);
           changeSupport.firePreChangeEvent(ce);
           featureHolders.remove(fh);
           changeSupport.firePostChangeEvent(ce);
         }
       }
     }

    public int countFeatures() {
	int fc = 0;
	for (Iterator i = featureHolders.keySet().iterator(); i.hasNext(); ) {
	    fc += ((FeatureHolder) i.next()).countFeatures();
	}
	return fc;
    }
    
    public boolean containsFeature(Feature f) {
	for (Iterator i = featureHolders.entrySet().iterator(); i.hasNext(); ) {
	    Map.Entry me = (Map.Entry) i.next();
	    FeatureHolder subFH = (FeatureHolder) me.getKey();
	    FeatureFilter membership = (FeatureFilter) me.getValue();

	    if (membership.accept(f)) {
		if(subFH.containsFeature(f)) {
		    return true;
		}
	    }
	}
      
	return false;
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

    private Location extractInterestingLocation(FeatureFilter ff) {
	if (ff instanceof FeatureFilter.OverlapsLocation) {
	    return ((FeatureFilter.OverlapsLocation) ff).getLocation();
	} else if (ff instanceof FeatureFilter.ContainedByLocation) {
	    return ((FeatureFilter.ContainedByLocation) ff).getLocation();
	} else if (ff instanceof FeatureFilter.And) {
	    FeatureFilter.And ffa = (FeatureFilter.And) ff;
	    Location l1 = extractInterestingLocation(ffa.getChild1());
	    Location l2 = extractInterestingLocation(ffa.getChild2());

	    if (l1 != null) {
		if (l2 != null) {
		    return l1.intersection(l2);
		} else {
		    return l1;
		}
	    } else {
		if (l2 != null) {
		    return l2;
		} else {
		    return null;
		}
	    }
	}

	// Don't know how this filter relates to location.

	return null;
    }

    /**
     * When applied to a MergeFeatureHolder, this filters each child
     * FeatureHolder independently.
     */

    public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
	Map results = new SmallMap();
	for (Iterator fhi = featureHolders.entrySet().iterator(); fhi.hasNext(); ) {
	    Map.Entry me = (Map.Entry) fhi.next();
	    FeatureHolder fh = (FeatureHolder) me.getKey();
	    FeatureFilter mf = (FeatureFilter) me.getValue();
	    if (recurse) {
		Location ffl = extractInterestingLocation(ff);
		if (ffl != null && FilterUtils.areDisjoint(mf, new FeatureFilter.OverlapsLocation(ffl))) {
		    continue;
		}
	    } else {
		if (FilterUtils.areDisjoint(mf, ff)) {
		    // Nothing interesting here...
		    continue;
		}
	    }

	    if (recurse) {
		FeatureHolder filterResult = fh.filter(ff, true);
		if (filterResult.countFeatures() > 0) {
		    results.put(filterResult, FeatureFilter.all);
		}
	    } else {
		if (FilterUtils.areProperSubset(mf, ff)) {
		    results.put(fh, mf);
		} else {
		    FeatureHolder filterResult = fh.filter(ff, false);
		    if (filterResult.countFeatures() != 0) {
			results.put(filterResult, new FeatureFilter.And(mf, ff));
		    }
		}
	    }
	}

	if (results.size() == 0) {
	    return FeatureHolder.EMPTY_FEATURE_HOLDER;
	} else if (results.size() == 1) {
	    return (FeatureHolder) results.keySet().iterator().next();
	} else {
	    return new MergeFeatureHolder(results);
	}
    }

    /**
     * Get a map of featureHolders to filters.  This might well go away
     * once we have more sophisticated optimizable filters.
     *
     * @since 1.2 (temporary)
     */

    public Map getMergeMap() {
	return Collections.unmodifiableMap(featureHolders);
    }

    private class MFHIterator implements Iterator {
	private Iterator fhIterator;
	private Iterator fIterator;

	public MFHIterator() {
	    fhIterator = featureHolders.keySet().iterator();
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
