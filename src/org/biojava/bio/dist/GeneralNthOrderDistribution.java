package org.biojava.bio.dist;

import java.util.*;
import java.io.*;

import org.biojava.utils.*;
import org.biojava.bio.symbol.*;

class GeneralNthOrderDistribution extends NthOrderDistribution {
    private Map dists;

    GeneralNthOrderDistribution(CrossProductAlphabet alpha, DistributionFactory df) 
        throws IllegalAlphabetException
    {
	super(alpha);
	dists = new HashMap();

	for(Iterator i = ((FiniteAlphabet) getConditioningAlphabet()).iterator(); i.hasNext(); ) {
	    Symbol si = (Symbol) i.next();
	    dists.put(si, df.createDistribution(getConditionedAlphabet()));
	}
    }

    public Distribution getDistribution(Symbol sym)
	throws IllegalSymbolException 
    {
	Distribution d = (Distribution) dists.get(sym);
	if(d == null) {
	    getConditioningAlphabet().validate(sym);
	}
	return d;
    }

    public void setDistribution(Symbol sym, Distribution dist)
	throws IllegalSymbolException, IllegalAlphabetException 
    {
	getConditioningAlphabet().validate(sym);
	if(dist.getAlphabet() != getConditionedAlphabet()) {
	    throw new IllegalAlphabetException(
		    "The distribution must be over " + getConditionedAlphabet() +
		    ", not " + dist.getAlphabet());
	}
	
	Distribution old = (Distribution) dists.get(sym);
	if( (old != null) && (weightForwarder != null) ) {
	    old.removeChangeListener(weightForwarder);
	}
	
	if(weightForwarder != null) {
	    dist.addChangeListener(weightForwarder);
	}
	
	dists.put(sym, dist);
    }

    public Collection distributions() {
	return dists.values();
    }
}

