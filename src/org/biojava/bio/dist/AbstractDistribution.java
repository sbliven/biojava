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


package org.biojava.bio.dist;

import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;

/**
 * An abstract implementation of Distribution.
 * <p>
 * You will need to over-ride <code>getWeight()</code> for a simple
 * implementation. You may also wish to over-ride the other methods if the
 * default implementation is not suitable.
 * </p>
 *
 * <p>
 * Note that, in this implementation, the <code>setWeight</code> implementation
 * throws an exception.  The <code>registerWithTrainer</code> method registers
 * an <code>IgnoreCountsTrainer</code>.  To make an <code>AbstractDistribution</code>
 * subclass trainable, both these methods must be overridden.
 * </p>
 *
 * @author Matthew Pocock
 * @author Thomas Down
 */

public abstract class AbstractDistribution implements Distribution {
  protected transient ChangeSupport changeSupport = null;
  protected transient Distribution.NullModelForwarder nullModelForwarder = null;
  
  protected void generateChangeSupport(ChangeType ct) {
    if(changeSupport == null) {
      changeSupport = new ChangeSupport();
    }
    
    if(
      ((ct == null) || (ct == Distribution.NULL_MODEL)) &&
      nullModelForwarder == null
    ) {
      nullModelForwarder = new Distribution.NullModelForwarder(this, changeSupport);
      getNullModel().addChangeListener(nullModelForwarder, Distribution.WEIGHTS);
    }
  }
  
  public void addChangeListener(ChangeListener cl) {
    generateChangeSupport(null);
    synchronized(changeSupport) {
      changeSupport.addChangeListener(cl);
    }
  }
  
  public void addChangeListener(ChangeListener cl, ChangeType ct) {
    generateChangeSupport(ct);
    synchronized(changeSupport) {
      changeSupport.addChangeListener(cl, ct);
    }
  }
  
  public void removeChangeListener(ChangeListener cl) {
    if(changeSupport != null) {
      synchronized(changeSupport) {
        changeSupport.removeChangeListener(cl);
      }
    }
  }
  
  public void removeChangeListener(ChangeListener cl, ChangeType ct) {
    if(changeSupport != null) {
      synchronized(changeSupport) {
        changeSupport.removeChangeListener(cl, ct);
      }
    }
  }

  abstract protected void setWeightImpl(AtomicSymbol sym, double weight)
  throws IllegalSymbolException, ChangeVetoException;
  
  /**
   * Set the weight of a given symbol in this distribution.
   * <P>
   * This implementation informs all listeners of the change, and then calls
   * setWeightImpl to make the actual change. Sub-classes should over-ride
   * setWeightImpl to implement the actual storage of the weights.
   */
  final public void setWeight(Symbol sym, double weight)
  throws IllegalSymbolException, ChangeVetoException {
    if(changeSupport == null) {
      doSetWeight(sym, weight);
    } else {
      ChangeEvent ce = new ChangeEvent(
        this,
        Distribution.WEIGHTS,
        new Object[] {sym, new Double(weight)},
        new Object[] {sym, new Double(getWeight(sym))}
      );
      synchronized(changeSupport) {
        changeSupport.firePreChangeEvent(ce);
        doSetWeight(sym, weight);
        changeSupport.firePostChangeEvent(ce);
      }
    }
  }
  
  private void doSetWeight(Symbol sym, double weight)
  throws IllegalSymbolException, ChangeVetoException {
    if(sym instanceof AtomicSymbol) {
      setWeightImpl((AtomicSymbol) sym, weight);
    } else {
      FiniteAlphabet fa = (FiniteAlphabet) sym.getMatches();
      for(Iterator si = fa.iterator(); si.hasNext(); ) {
        AtomicSymbol as = (AtomicSymbol) si.next();
        setWeightImpl(as, weight);
      }
    }
  }
  
  abstract protected void setNullModelImpl(Distribution nullModel)
  throws IllegalAlphabetException, ChangeVetoException;
  
  final public void setNullModel(Distribution nullModel)
  throws IllegalAlphabetException, ChangeVetoException {
    if(nullModel.getAlphabet() != getAlphabet()) {
      throw new IllegalAlphabetException(
        "Could not use distribution " + nullModel +
        " as its alphabet is " + nullModel.getAlphabet().getName() +
        " and this distribution's alphabet is " + getAlphabet().getName()
      );
    }
    Distribution oldModel = getNullModel();
    if(nullModelForwarder != null) {
      if(oldModel != null) {
        oldModel.removeChangeListener(nullModelForwarder);
      }
      nullModel.addChangeListener(nullModelForwarder);
    }
    if(changeSupport == null) {
      // if there are no listners yet, don't g through the overhead of
      // synchronized regions or of trying to inform them.
      setNullModelImpl(nullModel);
    } else {
      // OK - so somebody is intereted in me. Do it properly this time.
      ChangeEvent ce = new ChangeEvent(
        this,
        Distribution.NULL_MODEL,
        nullModel,
        oldModel
      );
      synchronized(changeSupport) {
        changeSupport.firePreChangeEvent(ce);
        setNullModelImpl(nullModel);
        changeSupport.firePostChangeEvent(ce);
      }
    }    
  }
  
  /**
   * Retrieve the weight for this distribution.
   * <P>
   * Performs the standard munge to handle ambiguity symbols. The actual weights
   * for each attomic symbol should be calculated by the getWeightImpl
   * functions.
   *
   * @param amb the Symbol to find the probability of
   * @return the probability that one of the symbols matching amb was emitted
   * @throws IllegalSymbolException if for any reason the symbols within amb
   *         are not recognized by this state
   */
  public final double getWeight(Symbol sym)
  throws IllegalSymbolException {
    if(sym instanceof AtomicSymbol) {
      return getWeightImpl((AtomicSymbol) sym);
    } else {  
      Alphabet ambA = sym.getMatches();
      if(ambA instanceof FiniteAlphabet) {
        FiniteAlphabet fa = (FiniteAlphabet) ambA;
        double sum = 0.0;
        double div = 0.0;
        for(Iterator i = fa.iterator(); i.hasNext(); ) {
          Object obj = i.next();
          if(!(obj instanceof AtomicSymbol)) {
            throw new BioError(
              "Assertion Failure: Not an instance of AtomicSymbol: " +
              obj
            );
          }
          //AtomicSymbol as = (AtomicSymbol) i.next();
          AtomicSymbol as = (AtomicSymbol) obj;
          double nm = getNullModel().getWeight(as);
          sum += getWeightImpl(as) * nm;
          div += nm;
        }
        return (sum == 0.0)
           ? 0.0
           : sum / div;
      } else {
        throw new IllegalSymbolException(
           "Can't find weight for infinite set of symbols matched by " +
           sym.getName()
        );
      }
    }
  }
  
  protected abstract double getWeightImpl(AtomicSymbol sym)
  throws IllegalSymbolException;
  
  public Symbol sampleSymbol()
  throws BioError {
    double p = Math.random();
    try {
      for(Iterator i = ((FiniteAlphabet) getAlphabet()).iterator(); i.hasNext(); ) {
        AtomicSymbol s = (AtomicSymbol) i.next();
        p -= getWeight(s);
        if( p <= 0) {
          return s;
        }
      }
    
      StringBuffer sb = new StringBuffer();
      for(Iterator i = ((FiniteAlphabet) this.getAlphabet()).iterator(); i.hasNext(); ) {
        AtomicSymbol s = (AtomicSymbol) i.next();
        double w = getWeight(s);
        sb.append("\t" + s.getName() + " -> " + w + "\n");
      }
      throw new BioError(
        "Could not find a symbol to emit from alphabet " + getAlphabet() +
        ". Do the probabilities sum to 1?" + "\np=" + p + "\n" + sb.toString()
      );
    } catch (IllegalSymbolException ire) {
      throw new BioError(
        ire,
        "Unable to itterate over all symbols in alphabet - " +
        "things changed beneath me!"
      );
    }
  }
  
  /**
   * Register an IgnoreCountsTrainer instance as the trainer for this
   * distribution.  Override this if you wish to implement a trainable
   * distribution.
   */
  public void registerWithTrainer(DistributionTrainerContext dtc) {
    dtc.registerTrainer(this, IgnoreCountsTrainer.getInstance());
  }
}
