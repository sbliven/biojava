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

import java.io.Serializable;
import java.util.Iterator;

import org.biojava.bio.BioError;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.AtomicSymbol;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.ReversibleTranslationTable;
import org.biojava.bio.symbol.Symbol;
import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeForwarder;
import org.biojava.utils.ChangeListener;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeVetoException;

/**
 * Creates a distribution that is a translated view of an underlying
 * distribution.
 *
 * @author Matthew Pocock
 */
public class TranslatedDistribution
  extends
    AbstractChangeable
  implements
    Distribution,
    Serializable
{
  private final Distribution other;
  private final Distribution delegate;
  private final ReversibleTranslationTable table;
  private transient ChangeListener forwarder;

  /**
   * Users should make these thigs via getDistribuiton.
   */
  public TranslatedDistribution(
    ReversibleTranslationTable table,
    Distribution other,
    DistributionFactory distFact
  ) throws IllegalAlphabetException {
    if(!table.getTargetAlphabet().equals(other.getAlphabet())) {
      throw new IllegalAlphabetException(
        "Table target alphabet and distribution alphabet don't match: " +
        table.getTargetAlphabet().getName() + " and " +
        other.getAlphabet().getName() + " without symbol "
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
            throw new BioError("Assertion Failed: Can't train", ise);
          }
        }
        subCtxt.train();
      }

      public void clearCounts(DistributionTrainerContext dtc) {
      }
    });
  }

  protected ChangeSupport getChangeSupport(ChangeType ct) {
    ChangeSupport cs = super.getChangeSupport(ct);

    if(forwarder == null &&
       (Distribution.WEIGHTS.isMatchingType(ct) || ct.isMatchingType(Distribution.WEIGHTS)))
    {
      forwarder = new Forwarder(this, cs);
      delegate.addChangeListener(forwarder, Distribution.WEIGHTS);
    }

    return cs;
  }

  private class Forwarder extends ChangeForwarder {
    public Forwarder(Object source, ChangeSupport changeSupport) {
      super(source, changeSupport);
    }

    protected ChangeEvent generateChangeEvent(ChangeEvent ce) {
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
              throw new BioError("Couldn't translate symbol", ise);
            }
          }
        }
        if( (previous != null) && (previous instanceof Object[]) ) {
          Object[] pa = (Object[]) previous;
          if( (pa.length == 2) && (pa[0] instanceof Symbol) ) {
            try {
              previous = new Object[] { table.translate((Symbol) pa[0]), pa[1] };
            } catch (IllegalSymbolException ise) {
              throw new BioError("Couldn't translate symbol", ise);
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
  }
}
