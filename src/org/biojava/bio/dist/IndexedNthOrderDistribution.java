package org.biojava.bio.dist;

import java.util.*;
import java.io.*;

import org.biojava.utils.*;
import org.biojava.bio.symbol.*;

/**
 *  Description of the Class 
 *
 * @author     Thomas Down
 * @author     Matthew Pocock 
 * @since      1.1 
 */
class IndexedNthOrderDistribution extends AbstractOrderNDistribution {
  private transient Distribution[] dists;
  private transient AlphabetIndex index;

  IndexedNthOrderDistribution(CrossProductAlphabet alpha, DistributionFactory df)
       throws IllegalAlphabetException {
    super(alpha);

    FiniteAlphabet conditioning = (FiniteAlphabet) getConditioningAlphabet();
    index = AlphabetManager.getAlphabetIndex(conditioning);
    // Throws if alpha isn't indexable
    dists = new Distribution[conditioning.size()];

    for(int i = 0; i < conditioning.size(); ++i) {
      dists[i] = df.createDistribution(getConditionedAlphabet());
    }
  }

  /**
   *  Sets the Distribution attribute of the IndexedNthOrderDistribution 
   *  object 
   *
   * @param  sym                           The new Distribution value 
   * @param  dist                          The new Distribution value 
   * @exception  IllegalSymbolException    Description of Exception 
   * @exception  IllegalAlphabetException  Description of Exception 
   */
  public void setDistribution(Symbol sym, Distribution dist)
       throws IllegalSymbolException, IllegalAlphabetException {
    int indx = index.indexForSymbol(sym);
    if(dist.getAlphabet() != getConditionedAlphabet()) {
      throw new IllegalAlphabetException(
          "The distribution must be over " + getConditionedAlphabet() + 
          ", not " + dist.getAlphabet());
    }

    Distribution old = dists[indx];
    if((old != null) && (weightForwarder != null)) {
      old.removeChangeListener(weightForwarder);
    }

    if(weightForwarder != null) {
      dist.addChangeListener(weightForwarder);
    }

    dists[indx] = dist;
  }

  /**
   *  Gets the Distribution attribute of the IndexedNthOrderDistribution 
   *  object 
   *
   * @param  sym                         Description of Parameter 
   * @return                             The Distribution value 
   * @exception  IllegalSymbolException  Description of Exception 
   */
  public Distribution getDistribution(Symbol sym)
       throws IllegalSymbolException {
    return dists[index.indexForSymbol(sym)];
  }

  /**
   *  Description of the Method 
   *
   * @return    Description of the Returned Value 
   */
  public Collection conditionedDistributions() {
    return Arrays.asList(dists);
  }

  private void writeObject(ObjectOutput out)
  throws IOException {
    for(int i = 0; i < dists.length; i++) {
      out.writeObject(index.symbolForIndex(i));
      out.writeObject(dists[i]);
    }
  }
  
  private void readObject(ObjectInputStream in)
  throws IOException, ClassNotFoundException {
    index = AlphabetManager.getAlphabetIndex(
      (FiniteAlphabet) getConditioningAlphabet()
    );
    int len = index.getAlphabet().size();
    dists = new Distribution[len];
    for(int i  = 0; i < len; i++) {
      Symbol s = (Symbol) in.readObject();
      try {
        dists[index.indexForSymbol(s)] = (Distribution) in.readObject();
      } catch (IllegalSymbolException ise) {
        throw new IOException("Found unexpected symbol: " + ise.getMessage());
      }
    }
  }
}

