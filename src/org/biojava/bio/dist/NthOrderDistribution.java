package org.biojava.bio.dist;

import org.biojava.bio.symbol.*;
import java.util.*;

/**
 * Provides an N'th order distribution.
 *
 * @author Samiul Hasan
 * @author Matthew Pocock
 */
public class NthOrderDistribution extends AbstractDistribution {
  private CrossProductAlphabet alphabet;
  private Alphabet firstA;
  private Alphabet lastA;
  private Map dists;
  private Distribution nullModel;
  
  public NthOrderDistribution(CrossProductAlphabet alpha, DistributionFactory df)
  throws IllegalAlphabetException  {
    this.alphabet = alpha;
    List aList = alpha.getAlphabets();
    int lb1 = aList.size() - 1;
    this.firstA = AlphabetManager.getCrossProductAlphabet(aList.subList(0, lb1));
    this.lastA = (Alphabet) aList.get(lb1);
    this.dists = new HashMap(); 
    this.nullModel = new UniformNullModel();
    
    for(Iterator i = ((FiniteAlphabet) firstA).iterator(); i.hasNext(); ) {
      Symbol si = (Symbol) i.next();
      dists.put(si, df.createDistribution(lastA));
    }
  }
  
  public void setDistribution(Symbol sym, Distribution dist)
  throws IllegalSymbolException, IllegalAlphabetException {
    firstA.validate(sym);
    if(dist.getAlphabet() != lastA) {
      throw new IllegalAlphabetException(
        "The distribution must be over " + lastA +
        ", not " + dist.getAlphabet()
      );
    }
    
    dists.put(sym, dist);
  }
  
  public Distribution getDistribution(Symbol sym)
  throws IllegalSymbolException {
    Distribution d = (Distribution) dists.get(sym);
    if(d == null) {
      firstA.validate(sym);
    }
    return d;
  }
  
  public Alphabet getAlphabet() {
    return alphabet;
  }
  
  public double getWeight(Symbol sym) throws IllegalSymbolException {
    CrossProductSymbol cps = (CrossProductSymbol) sym;
    List symL = cps.getSymbols();
    int lb1 = symL.size() - 1;
    Symbol firstS;
    if(lb1 == 1) {
      firstS = (Symbol) symL.get(0);
    } else {
      firstS = ((CrossProductAlphabet) firstA).getSymbol(symL.subList(0, lb1));
    }
  	Distribution dist = getDistribution(firstS);
  	return dist.getWeight((Symbol) symL.get(lb1));
  }
  
  public void setNullModel(Distribution nullModel) throws IllegalAlphabetException {
  	if(nullModel == null) {
    	    throw new NullPointerException(
          	"The null model must not be null." +
          	" The apropreate null-model is a UniformDistribution instance."
    	    );
  	}
  	if(nullModel.getAlphabet() != getAlphabet()) {
    	    throw new IllegalAlphabetException(
          	"Could not use distribution " + nullModel +
          	" as its alphabet is " + nullModel.getAlphabet().getName() +
          	" and this distribution's alphabet is " + getAlphabet().getName()
    	    );
  	}
  	this.nullModel = nullModel;   
  }
  
  public Distribution getNullModel()  {
  	return this.nullModel;
  }
  
  public void registerWithTrainer(DistributionTrainerContext dtc) {
    for(Iterator i = dists.values().iterator(); i.hasNext(); ) {
      dtc.registerDistribution((Distribution) i.next());
    }
    dtc.registerTrainer(this, new IgnoreCountsTrainer() {
      public void addCount(
        DistributionTrainerContext dtc,
        Symbol sym,
        double count
      ) throws IllegalSymbolException {
        CrossProductSymbol cps = (CrossProductSymbol) sym;
        List symL = cps.getSymbols();
        int lb1 = symL.size() - 1;
        Symbol firstS;
        if(lb1 == 1) {
          firstS = (Symbol) symL.get(0);
        } else {
          firstS = ((CrossProductAlphabet) firstA).getSymbol(symL.subList(0, lb1));
        }
        Distribution dist = getDistribution(firstS);
        dtc.addCount(dist, (Symbol) symL.get(lb1), count);
      }
    });
  }
  
  private class UniformNullModel extends AbstractDistribution {
    private Distribution nullModel = new UniformDistribution((FiniteAlphabet) lastA);
    
    public Alphabet getAlphabet() {
      return NthOrderDistribution.this.getAlphabet();
    }
    
    public double getWeight(Symbol sym)
    throws IllegalSymbolException {
      CrossProductSymbol cps = (CrossProductSymbol) sym;
      List symL = cps.getSymbols();
      int lb1 = symL.size() - 1;
      return nullModel.getWeight((Symbol) symL.get(lb1));
    }
    
    public Distribution getNullModel() {
      return this;
    }
  }
}
