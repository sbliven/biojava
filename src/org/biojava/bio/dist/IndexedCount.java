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

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.utils.*;

/**
 * An encapsulation of a count over the Symbols within a FiniteAlphabet using
 * an AlphabetIndex object.
 *
 * @author Matthew Pocock
 */
public class IndexedCount implements Count {
  private final AlphabetIndex indexer;
  private final double[] counts;
  protected transient ChangeSupport changeSupport = null;
  
  protected void createChangeSupport(ChangeType ct) {
    if(changeSupport == null) {
      changeSupport = new ChangeSupport();
    }
  }
  
  public void addChangeListener(ChangeListener cl) {
    createChangeSupport(null);
    synchronized(changeSupport) {
      changeSupport.addChangeListener(cl);
    }
  }
  
  public void addChangeListener(ChangeListener cl, ChangeType ct) {
    createChangeSupport(ct);
    synchronized(changeSupport) {
      changeSupport.addChangeListener(cl, ct);
    }
  }
  
  public void removeChangeListener(ChangeListener cl) {
    createChangeSupport(null);
    synchronized(changeSupport) {
      changeSupport.removeChangeListener(cl);
    }
  }
  
  public void removeChangeListener(ChangeListener cl, ChangeType ct) {
    createChangeSupport(ct);
    synchronized(changeSupport) {
      changeSupport.removeChangeListener(cl, ct);
    }
  }

  public Alphabet getAlphabet() {
    return indexer.getAlphabet();
  }
    
  public double getCount(AtomicSymbol s) throws IllegalSymbolException {
    return counts[indexer.indexForSymbol(s)];
  }
  
  public void setCount(AtomicSymbol s, double c)
  throws IllegalSymbolException, ChangeVetoException {
    if(changeSupport == null) {
      counts[indexer.indexForSymbol(s)] = c;
    } else {
      synchronized(changeSupport) {
        int index = indexer.indexForSymbol(s);
        ChangeEvent ce = new ChangeEvent(
          this, COUNTS,
          new Object[] { s, new Double(counts[index]) },
          new Object[] { s, new Double(c) }
        );
        changeSupport.firePreChangeEvent(ce);
        counts[index] = c;
        changeSupport.firePostChangeEvent(ce);
      }
    }
  }
  
  public void increaseCount(AtomicSymbol s, double c)
  throws IllegalSymbolException, ChangeVetoException {
    if(changeSupport == null) {
      counts[indexer.indexForSymbol(s)] += c;
    } else {
      synchronized(changeSupport) {
        int index = indexer.indexForSymbol(s);
        double oc = counts[index];
        double nc = oc + c;
        ChangeEvent ce = new ChangeEvent(
          this, COUNTS,
          new Object[] { s, new Double(oc) },
          new Object[] { s, new Double(nc) }
        );
        changeSupport.firePreChangeEvent(ce);
        counts[index] = nc;
        changeSupport.firePostChangeEvent(ce);        
      }
    }
  }
  
  public void setCounts(Count c)
  throws IllegalAlphabetException, ChangeVetoException {
    if(c.getAlphabet() != getAlphabet()) {
      throw new IllegalAlphabetException(
        "Alphabet must match: " + c.getAlphabet().getName() +
        " != " + c.getAlphabet().getName()
      );
    }

    try {    
      if(changeSupport == null) {
        for(int i = 0; i < counts.length; i++) {
          counts[i] = c.getCount((AtomicSymbol) indexer.symbolForIndex(i));
        }
      } else {
        synchronized(changeSupport) {
          ChangeEvent ce = new ChangeEvent(
            this, COUNTS
          );
          changeSupport.firePreChangeEvent(ce);
          for(int i = 0; i < counts.length; i++) {
            counts[i] = c.getCount((AtomicSymbol) indexer.symbolForIndex(i));
          }
          changeSupport.firePostChangeEvent(ce);
        }
      }
    } catch (IllegalSymbolException ise) {
      throw new BioError(
        ise, "Assertion Failure: Should have no illegal symbols"
      );
    }
  }
  
  public void zeroCounts()
  throws ChangeVetoException {
    if(changeSupport == null) {
      for(int i = 0; i < counts.length; i++) {
        counts[i] = 0.0;
      }
    } else {
      synchronized(changeSupport) {
        ChangeEvent ce = new ChangeEvent(
          this, COUNTS
        );
        changeSupport.firePreChangeEvent(ce);
        for(int i = 0; i < counts.length; i++) {
          counts[i] = 0.0;
        }
        changeSupport.firePostChangeEvent(ce);
      }
    }
  }
  
  public IndexedCount(FiniteAlphabet fa) {
    this(AlphabetManager.getAlphabetIndex(fa));
  }
  
  public IndexedCount(AlphabetIndex indexer) {
    indexer.addChangeListener(ChangeListener.ALWAYS_VETO, AlphabetIndex.INDEX);
    this.indexer = indexer;
    this.counts = new double[indexer.getAlphabet().size()];
  }
}
