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
import java.io.Serializable;

import org.biojava.bio.symbol.*;

/**
*An implemenation of a simple distribution trainer
*/

public final class SimpleDistributionTrainer
implements DistributionTrainer, Serializable {
  private final Distribution dis;
  private final Map c;

  {
    this.c = new HashMap();
  }

  public void addCount(
    DistributionTrainerContext dtc,
    Symbol sym,
    double count
  ) throws IllegalSymbolException {
    Double d = (Double) c.get(sym);
    if (d == null) {
      throw new IllegalSymbolException(
        "Symbol " + sym.getName() +
        " not found in " + dis.getAlphabet().getName()
      );
    }
    if(count < 0) {
      throw new Error(
        "Can't add a negative count for " + sym.getName() +
        " of " + count
      );
    }
    c.put(sym, new Double(d.doubleValue() + count));
  }

  public void train(
    Distribution nullModel,
    double weight
  ) throws IllegalSymbolException {
    double sum = 0.0;
    for(
      Iterator i = ((FiniteAlphabet) dis.getAlphabet()).iterator();
      i.hasNext();
    ) {
      Symbol s = (Symbol) i.next();
      Double d = (Double) c.get(s);
      sum += d.doubleValue() +
             nullModel.getWeight(s) * weight;
    }
    //System.out.println(state.getName() + ": sum=" + sum);
    for(
      Iterator i = ((FiniteAlphabet) dis.getAlphabet()).iterator();
      i.hasNext();
    ) {
      Symbol sym = (Symbol) i.next();
      Double d = (Double) c.get(sym);
      dis.setWeight(
        sym,
        (d.doubleValue() + nullModel.getWeight(sym) * weight) / sum
      );
    }
  }

  public void clearCounts() {
    for(
      Iterator i = ((FiniteAlphabet) dis.getAlphabet()).iterator();
      i.hasNext();
    ) {
      c.put(i.next(), new Double(0.0));
    }
  }

  public SimpleDistributionTrainer(Distribution dis)
  throws IllegalAlphabetException {
    Alphabet a = dis.getAlphabet();
    if(! (a instanceof FiniteAlphabet)) {
      throw new IllegalAlphabetException(
        "Can't create a SimpleDistributionTrainer for non-finite alphabet " +
        a.getName() + " of type " + a.getClass()
      );
    }
    this.dis = dis;
    this.clearCounts();
  }
}
