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
import java.lang.ref.*;
import java.io.Serializable;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;

/**
 * Creates a distribution that is a translated view of an underlying
 * distribution.
 *
 * @author Matthew Pocock
 */
public class TranslatedDistribution
implements Distribution, Serializable {
  private final Distribution other;
  private final Distribution delegate;
  private final ReversibleTranslationTable table;
  protected transient ChangeSupport changeSupport = null;
  private transient ChangeListener forwarder = new Forwarder();
  
  public void addChangeListener(ChangeListener cl) {
    if(changeSupport == null) {
      changeSupport = new ChangeSupport();
    }
    
    synchronized(changeSupport) {
      changeSupport.addChangeListener(cl);
    }
  }
  
  public void addChangeListener(ChangeListener cl, ChangeType ct) {
    if(changeSupport == null) {
      changeSupport = new ChangeSupport();
    }

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
  
  /**
   * Users should make these thigs via getDistribuiton.
   */     
  public TranslatedDistribution(
    ReversibleTranslationTable table,
    Distribution other,
    DistributionFactory distFact
  ) throws IllegalAlphabetException {
    if(table.getTargetAlphabet() != other.getAlphabet()) {
      throw new IllegalAlphabetException(
        "Table target alphabet and distribution alphabet don't match: " +
        table.getTargetAlphabet().getName() + " and " +
        other.getAlphabet().getName()
      );
    }
    this.other = other;
    this.table = table;
    this.delegate = distFact.createDistribution(table.getSourceAlphabet());
  }

  public Alphabet getAlphabet() {
    return table.getSourceAlphabet();
  }
  
  public double getWeight(Symbol sym)
  throws IllegalSymbolException {
    return delegate.getWeight(sym);
  }
  
  public void setWeight(Symbol sym, double weight)
  throws IllegalSymbolException, ChangeVetoException {
    delegate.setWeight(sym, weight);
  }
  
  public Symbol sampleSymbol() {
    return delegate.sampleSymbol();
  }
  
  public Distribution getNullModel() {
    return delegate.getNullModel();
  }
  
  public void setNullModel(Distribution dist)
  throws IllegalAlphabetException, ChangeVetoException {
    delegate.setNullModel(dist);
  }
  
  /**
   * Retrieve the translation table encapsulating the map from this emission
   * spectrum to the underlying one.
   *
   * @return a ReversibleTranslationtTable
   */
  public ReversibleTranslationTable getTable() {
    return table;
  }
  
  public void registerWithTrainer(DistributionTrainerContext dtc) {
    dtc.registerDistribution(other);
    
    dtc.registerTrainer(this, new DistributionTrainer() {
      public void addCount(
        DistributionTrainerContext dtc,
        AtomicSymbol s,
        double count
      ) throws IllegalSymbolException {
        dtc.addCount(other, table.translate(s), count);
      }
      
      public double getCount(
        DistributionTrainerContext dtc,
        AtomicSymbol s
      ) throws IllegalSymbolException {
        return dtc.getCount(other, table.translate(s));
      }
      
      public void train(DistributionTrainerContext dtc, double weight)
      throws ChangeVetoException {
        DistributionTrainerContext subCtxt
          = new SimpleDistributionTrainerContext();
        subCtxt.setNullModelWeight(weight);
        subCtxt.registerDistribution(delegate);

        for(
          Iterator i = ((FiniteAlphabet) other.getAlphabet()).iterator();
          i.hasNext();
        ) {
          AtomicSymbol sym = (AtomicSymbol) i.next();
          try {
            subCtxt.addCount(
                delegate,
                table.translate(sym),
                dtc.getCount(other, sym)
            );
          } catch (IllegalSymbolException ise) {
            throw new BioError(ise, "Assertion Failed: Can't train");
          }
        }
        subCtxt.train();
      }
      
      public void clearCounts(DistributionTrainerContext dtc) {
      }
    });
  }
  
  private class Forwarder implements ChangeListener {
    private ChangeEvent generateChangeEvent(ChangeEvent ce) {
      ChangeType ct = ce.getType();
      Object change = ce.getChange();
      Object previous = ce.getPrevious();
      if(ct == Distribution.WEIGHTS) {
        if( (change != null) && (change instanceof Object[]) ) {
          Object[] ca = (Object[]) change;
          if( (ca.length == 2) && (ca[0] instanceof Symbol) ) {
            try {
              change = new Object[] { table.translate((Symbol) ca[0]), ca[1] };
            } catch (IllegalSymbolException ise) {
              throw new BioError(ise, "Couldn't translate symbol");
            }
          }
        }
        if( (previous != null) && (previous instanceof Object[]) ) {
          Object[] pa = (Object[]) previous;
          if( (pa.length == 2) && (pa[0] instanceof Symbol) ) {
            try {
              previous = new Object[] { table.translate((Symbol) pa[0]), pa[1] };
            } catch (IllegalSymbolException ise) {
              throw new BioError(ise, "Couldn't translate symbol");
            }
          }
        }
      } else if(ct == Distribution.NULL_MODEL) {
        change = null;
        previous = null;
      }
      return new ChangeEvent(
        TranslatedDistribution.this, ct,
        change, previous, ce
      ); 
    }
    
    public void preChange(ChangeEvent ce) throws ChangeVetoException {
      if(changeSupport != null) {
        ChangeEvent nce = generateChangeEvent(ce);
        synchronized(changeSupport) {
          changeSupport.firePreChangeEvent(nce);
        }
      }
    }
    
    public void postChange(ChangeEvent ce) {
      if(changeSupport != null) {
        ChangeEvent nce = generateChangeEvent(ce);
        synchronized(changeSupport) {
          changeSupport.firePostChangeEvent(nce);
        }
      }
    }
  }
}
