package org.biojava.bio.dist;

import java.util.*;
import java.io.*;

import org.biojava.utils.*;
import org.biojava.bio.symbol.*;

/**
 * Simple base class for OrderNDistributions
 *
 * @author Samiul Hasan
 * @author Matthew Pocock
 * @author Thomas Down
 */
public abstract class AbstractOrderNDistribution
extends AbstractDistribution
implements OrderNDistribution, Serializable {
  private CrossProductAlphabet alphabet;
  private Alphabet firstA;
  private Alphabet lastA;
  private Distribution nullModel;
  
  protected transient WeigthForwarder weightForwarder = null;
  
  protected void generateChangeSupport(ChangeType ct) {
    super.generateChangeSupport(ct);
    if(
      ( (ct == null) || (ct == Distribution.WEIGHTS) ) &&
      weightForwarder == null
    ) {
      weightForwarder = new WeigthForwarder(this, changeSupport);
      for(Iterator i = conditionedDistributions().iterator(); i.hasNext(); ) {
        Distribution dist = (Distribution) i.next();
        dist.addChangeListener(weightForwarder, Distribution.WEIGHTS);
      }
    }
  }
  
    /**
     * Construct a new NthOrderDistribution.
     */

  protected AbstractOrderNDistribution(CrossProductAlphabet alpha)
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
    this.nullModel = new UniformNullModel();
  }
  
    /**
     * Get the conditioning alphabet of this distribution.  If the `overall'
     * alphabet is a cross-product of two alphabets, this will be the first 
     * of those alphabets.  If it is a cross-product of more than two alphabets,
     * the conditioning alphabet is the cross-product of all but the last
     * alphabet.
     */

    public Alphabet getConditioningAlphabet() {
	return firstA;
    }

    /**
     * Get the conditioned alphabet.  This is the last alphabet in the
     * distribution's overall cross-product.  It will be the alphabet of
     * all the sub-distributions contained within this OrderNDistribution.
     */

    public Alphabet getConditionedAlphabet() {
	return lastA;
    }
   
  public Alphabet getAlphabet() {
    return alphabet;
  }
  
    /**
     * Get a weight from one of the sub-distributions, conditioned
     * on the first part of the symbol.
     */

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

    /**
     * Set a weight in one of the conditioned distributions.  It is the callers
     * responsibility to ensure that all the conditioned distributions have total
     * weights which sum to 1.0.
     */

    public void setWeightImpl(Symbol sym, double w) 
        throws IllegalSymbolException, ChangeVetoException
    {
	if (sym instanceof AtomicSymbol) {
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
	    dist.setWeight((Symbol) symL.get(lb1), w);
	} else {
	    throw new IllegalSymbolException("Can't set a weight for an ambiguity symbol");
	}
    }
  
  public void setNullModelImpl(Distribution nullModel) {
  	this.nullModel = nullModel;   
  }
  
  public Distribution getNullModel()  {
  	return this.nullModel;
  }
  
  public void registerWithTrainer(DistributionTrainerContext dtc) {
    for(Iterator i = conditionedDistributions().iterator(); i.hasNext(); ) {
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
  
  private class UniformNullModel
  extends AbstractDistribution implements Serializable {
    private Distribution nullModel = new UniformDistribution(
      (FiniteAlphabet) lastA
    );
    
    public Alphabet getAlphabet() {
      return AbstractOrderNDistribution.this.getAlphabet();
    }
    
    public double getWeight(Symbol sym)
    throws IllegalSymbolException {
      CrossProductSymbol cps = (CrossProductSymbol) sym;
      List symL = cps.getSymbols();
      int lb1 = symL.size() - 1;
      return nullModel.getWeight((Symbol) symL.get(lb1));
    }
    
    protected void setWeightImpl(Symbol sym, double weight)
    throws ChangeVetoException {
      throw new ChangeVetoException(
        "Can't change the weight of this null model"
      );
    }
    
    public Distribution getNullModel() {
      return this;
    }
    
    protected void setNullModelImpl(Distribution nullModel)
    throws IllegalAlphabetException, ChangeVetoException {
      throw new ChangeVetoException(
        "Can't set the null model for NthOrderDistribution.UniformNullModel"
      );
    }
  }
  
  private class WeigthForwarder extends ChangeForwarder {
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
