package org.biojava.bio.dist;

import java.util.*;
import java.io.*;

import org.biojava.utils.*;
import org.biojava.bio.symbol.*;

class IndexedNthOrderDistribution extends AbstractOrderNDistribution {
    public static final long serialVersionUID = 5959499231593592515L;
    private Distribution[] dists;
    private transient AlphabetIndex index;

    IndexedNthOrderDistribution(CrossProductAlphabet alpha, DistributionFactory df) 
        throws IllegalAlphabetException
    {
	super(alpha);

  FiniteAlphabet conditioning = (FiniteAlphabet) getConditioningAlphabet();
	index = AlphabetManager.getAlphabetIndex( conditioning, false); // Throws if alpha isn't indexable
	dists = new Distribution[conditioning.size()];

	for(int i = 0; i < conditioning.size(); ++i) {
	    dists[i] = df.createDistribution(getConditionedAlphabet());
	}
    }

    private Object readResolve() throws ObjectStreamException {
      // FIXME: new index may be in different order to distribution array
	try {
	    index = AlphabetManager.getAlphabetIndex((FiniteAlphabet) getConditioningAlphabet(), false);
		return this;
	} catch (IllegalAlphabetException ex) {
	    throw new InvalidObjectException("Couldn't regenerate index");
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

    public Collection conditionedDistributions() {
	return Arrays.asList(dists);
    }
}

