package org.biojava.bio.dist;

import java.util.*;
import java.io.*;

import org.biojava.utils.*;
import org.biojava.bio.symbol.*;

/**
 * Provides an N'th order distribution.
 *
 * @author Samiul Hasan
 * @author Matthew Pocock
 */
public class NthOrderDistribution extends AbstractDistribution implements Serializable {
  private CrossProductAlphabet alphabet;
  private Alphabet firstA;
  private Alphabet lastA;
  private Map dists;
  private Distribution nullModel;
  
  protected WeigthForwarder weightForwarder = null;
  
  protected void generateChangeSupport(ChangeType ct) {
    super.generateChangeSupport(ct);
    if(
      ( (ct == null) || (ct == Distribution.WEIGHTS) ) &&
      weightForwarder == null
    ) {
      weightForwarder = new WeigthForwarder(this, changeSupport);
      for(Iterator i = dists.values().iterator(); i.hasNext(); ) {
        Distribution dist = (Distribution) i.next();
        dist.addChangeListener(weightForwarder, Distribution.WEIGHTS);
      }
    }
  }
  
  public NthOrderDistribution(CrossProductAlphabet alpha, DistributionFactory df)
  throws IllegalAlphabetException  {
    this.alphabet = alpha;
    List aList = alpha.getAlphabets();
    int lb1 = aList.size() - 1;
    if(aList.size() == 2) {
      this.firstA = (Alphabet) aList.get(0);
    } else {
      this.firstA = AlphabetManager.getCrossProductAlphabet(aList.subList(0, lb1));
    }
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
    
    Distribution old = (Distribution) dists.get(sym);
    if( (old != null) && (weightForwarder != null) ) {
      old.removeChangeListener(weightForwarder);
    }
    
    if(weightForwarder != null) {
      dist.addChangeListener(weightForwarder);
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
    if(sym instanceof AtomicSymbol) {
      CrossProductSymbol cps = (CrossProductSymbol) sym;
      List symL = cps.getSymbols();
      int lb1 = symL.size() - 1;
      Symbol firstS;
      if(symL.size() == 2) {
        firstS = (Symbol) symL.get(0);
      } else {
        firstS = ((CrossProductAlphabet) firstA).getSymbol(symL.subList(0, lb1));
      }
      Distribution dist = getDistribution(firstS);
      return dist.getWeight((Symbol) symL.get(lb1));
    } else {
      return getAmbiguityWeight(sym);
    }
  }
  
  public void setNullModel(Distribution nullModel)
  throws IllegalAlphabetException, ChangeVetoException {
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
    
    public void setNullModel(Distribution nullModel)
    throws IllegalAlphabetException, ChangeVetoException {
      throw new ChangeVetoException(
        "Can't set the null model for NthOrderDistribution.UniformNullModel"
      );
    }
  }
  
  private class WeigthForwarder extends ChangeAdapter {
    public WeigthForwarder(Object source, ChangeSupport cs) {
      super(source, cs);
    }
    
    protected ChangeEvent generateEvent(ChangeEvent ce) {
      if(ce.getType() == Distribution.WEIGHTS) {
        return new ChangeEvent(
          getSource(),
          Distribution.WEIGHTS,
          ce
        );
      }
      return null;
    }
  }
}
