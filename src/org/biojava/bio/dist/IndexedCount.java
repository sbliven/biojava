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
import java.io.*;
import java.util.*;

/**
 * An encapsulation of a count over the Symbols within a FiniteAlphabet using
 * an AlphabetIndex object.
 *
 * @author Matthew Pocock
 * @author Mark Schreiber (serialization)
 */
public final class IndexedCount
  extends
    AbstractChangeable
  implements
    Count, Serializable
{
  //must be transient as indices may vary between VM's
  private transient AlphabetIndex indexer;
  private transient double[] counts;
  private Map symbolIndices = null; //for serialization
  private FiniteAlphabet alpha;

  public Alphabet getAlphabet() {
    return alpha;
  }

  public double getCount(AtomicSymbol s) throws IllegalSymbolException {
    return counts[indexer.indexForSymbol(s)];
  }

  public void setCount(AtomicSymbol s, double c)
  throws IllegalSymbolException, ChangeVetoException {
    if(!hasListeners()) {
      counts[indexer.indexForSymbol(s)] = c;
    } else {
      ChangeSupport changeSupport = getChangeSupport(COUNTS);
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
    if(!hasListeners()) {
      counts[indexer.indexForSymbol(s)] += c;
    } else {
      ChangeSupport changeSupport = getChangeSupport(COUNTS);
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
      if(!hasListeners()) {
        for(int i = 0; i < counts.length; i++) {
          counts[i] = c.getCount((AtomicSymbol) indexer.symbolForIndex(i));
        }
      } else {
        ChangeSupport changeSupport = getChangeSupport(COUNTS);
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
    if(!hasListeners()) {
      for(int i = 0; i < counts.length; i++) {
        counts[i] = 0.0;
      }
    } else {
        ChangeSupport changeSupport = getChangeSupport(COUNTS);
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

  private void writeObject(ObjectOutputStream stream)throws IOException{
    symbolIndices = new HashMap(counts.length);
    for (int i = 0; i < counts.length; i++) {
      symbolIndices.put(indexer.symbolForIndex(i), new Double(counts[i]));
    }

    stream.defaultWriteObject();
  }

  private void readObject(ObjectInputStream stream)
         throws IOException, ClassNotFoundException{

    stream.defaultReadObject();
    indexer = AlphabetManager.getAlphabetIndex(alpha);
    counts = new double[alpha.size()];

    for(int i = 0; i < alpha.size(); i++){
      Symbol key = indexer.symbolForIndex(0);
      Double d  = (Double)symbolIndices.get(key);
      if (d == null) {//no value, set to 0.0
        counts[i] = 0.0;
      }
      else {
        counts[i] = d.doubleValue();
      }
    }

    //mark for garbage collection
    symbolIndices = null;
  }

  public IndexedCount(FiniteAlphabet fa) {
    this(AlphabetManager.getAlphabetIndex(fa));
  }

  public IndexedCount(AlphabetIndex indexer) {
    indexer.addChangeListener(ChangeListener.ALWAYS_VETO, AlphabetIndex.INDEX);
    this.indexer = indexer;
    this.counts = new double[indexer.getAlphabet().size()];
    this.alpha = indexer.getAlphabet();
  }
}
