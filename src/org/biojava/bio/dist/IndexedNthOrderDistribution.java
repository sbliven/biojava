package org.biojava.bio.dist;

import java.util.*;
import java.io.*;

import org.biojava.utils.*;
import org.biojava.bio.symbol.*;

class IndexedNthOrderDistribution extends NthOrderDistribution {
    private Distribution[] dists;
    private AlphabetIndex index;

    IndexedNthOrderDistribution(CrossProductAlphabet alpha, DistributionFactory df) 
        throws IllegalAlphabetException
    {
	super(alpha);

	index = AlphabetManager.getAlphabetIndex(getConditioningAlphabet(), false); // Throws if alpha isn't indexable
	dists = new Distribution[index.size()];

	for(int i = 0; i < index.size(); ++i) {
	    dists[i] = df.createDistribution(getConditionedAlphabet());
	}
    }

    public Distribution getDistribution(Symbol sym)
	throws IllegalSymbolException 
    {
	return dists[index.indexForSymbol(sym)];
    }

    public void setDistribution(Symbol sym, Distribution dist)
	throws IllegalSymbolException, IllegalAlphabetException 
    {
	int indx = index.indexForSymbol(sym);
	if(dist.getAlphabet() != getConditionedAlphabet()) {
	    throw new IllegalAlphabetException(
		    "The distribution must be over " + getConditionedAlphabet() +
		    ", not " + dist.getAlphabet());
	}
	
	Distribution old = dists[indx];
	if( (old != null) && (weightForwarder != null) ) {
	    old.removeChangeListener(weightForwarder);
	}
	
	if(weightForwarder != null) {
	    dist.addChangeListener(weightForwarder);
	}
	
	dists[indx] = dist;
    }

    public Collection distributions() {
	return Arrays.asList(dists);
    }
}

