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

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.utils.*;

/**
 * Creates a distribution that is a translated view of an underlying
 * distribution.
 *
 * @author Matthew Pocock
 */
public class TranslatedDistribution
implements Distribution, Serializable {
  private static Map cache;
  
  static {
    cache = new HashMap();
  }
  
  private static ListWrapper gopher = new ListWrapper();
  
  public static TranslatedDistribution getDistribution(
    FiniteAlphabet alphabet, Distribution source
  ) throws IllegalAlphabetException {
    Alphabet oa = source.getAlphabet();
    if(! (oa instanceof FiniteAlphabet) ) {
      throw new IllegalAlphabetException(
        "Source distribution must have a finite alphabet: " + oa.getName()
      );
    }
    return getDistribution(
      new SimpleReversibleTranslationTable(
        alphabet, (FiniteAlphabet) oa
      ),
      source
    );
  }

  public static TranslatedDistribution getDistribution(
    ReversibleTranslationTable table, Distribution source
  ) {
    synchronized(cache) {
      List get = Arrays.asList(new Object[] { table, source });
      gopher.setList(get);
      SoftReference ref = (SoftReference) cache.get(get);
      TranslatedDistribution dist;
      try {
        if(ref == null) {
          dist = new TranslatedDistribution(table, source);
          cache.put(new ListWrapper(get), new SoftReference(dist));
        } else {
          dist = (TranslatedDistribution) ref.get();
          if(dist == null) {
            dist = new TranslatedDistribution(table, source);
            cache.put(new ListWrapper(get), new SoftReference(dist));
          }
        }
      } catch (IllegalAlphabetException iae) {
        throw new BioError(
          iae,
          "The parent's null distribution is not complementable, " +
          "but the parent is. Something is wrong with the parent"
        );
      }
      return dist;
    }
  }

  private final Distribution other;
  private final ReversibleTranslationTable table;
  
  /**
   * Retrieve the translation table encapsulating the map from this emission
   * spectrum to the underlying one.
   *
   * @return a ReversibleTranslationtTable
   */
  public ReversibleTranslationTable getTable() {
    return table;
  }
  
  public double getWeight(Symbol s) throws IllegalSymbolException {
    return other.getWeight(table.translate(s));
  }
  
  public void setWeight(Symbol s, double score)
  throws IllegalSymbolException, UnsupportedOperationException {
    other.setWeight(table.translate(s), score);
  }
  
  public Alphabet getAlphabet() {
    return table.getSourceAlphabet();
  }
  
  public Symbol sampleSymbol() {
    try {
      return table.untranslate(other.sampleSymbol());
    } catch (IllegalSymbolException ise) {
      throw new BioError(
        ise,
        "Somehow, I have been unable to untranslate a symbol"
      );
    }
  }
  
  public Distribution getNullModel() {
    return getDistribution(table, other.getNullModel()); 
  }
  
  public void registerWithTrainer(DistributionTrainerContext dtc) {
    dtc.registerDistribution(other);
    dtc.registerTrainer(this, new IgnoreCountsTrainer() {
      public void addCount(
        DistributionTrainerContext dtc,
        Symbol s,
        double count
      ) throws IllegalSymbolException {
        dtc.addCount(other, table.translate(s), count);
      }
    });
  }
    
  private TranslatedDistribution(
    ReversibleTranslationTable table, Distribution other
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
  }
}
