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


package org.biojava.bio.dp;

import java.util.*;
import org.biojava.bio.symbol.*;

public class SimpleStateTrainer implements StateTrainer {
  private final EmissionState state;
  private final Map c;

  public void addCount(Symbol res, double count) throws IllegalSymbolException {
    Double d = (Double) c.get(res);
    if (d == null) {
      throw new IllegalSymbolException(
        "Symbol " + res.getName() +
        " not found in " + state.alphabet().getName() +
        " within state " + state.getName()
      );
    }
    if(count < 0) {
      throw new Error(
        "Can't add a negative count to " + state.getName() +
        " of " + count
      );
    }
    c.put(res, new Double(d.doubleValue() + count));
  }

  public void train(
    EmissionState nullModel,
    double weight
  ) throws IllegalSymbolException {
    if(nullModel != null) {
      for (
        Iterator i = ((FiniteAlphabet) state.alphabet()).iterator();
        i.hasNext();
      ) {
        Symbol r = (Symbol) i.next();
        addCount(r, Math.exp(nullModel.getWeight(r) + weight));
      }
    }
    
    double sum = 0.0;
    for(
      Iterator i = ((FiniteAlphabet) state.alphabet()).iterator();
      i.hasNext();
    ) {
      Symbol r = (Symbol) i.next();
      sum += ((Double) c.get(r)).doubleValue();
    }
    //System.out.println(state.getName() + ": sum=" + sum);
    for(
      Iterator i = ((FiniteAlphabet) state.alphabet()).iterator();
      i.hasNext();
    ) {
      Symbol res = (Symbol) i.next();
      Double d = (Double) c.get(res);
      /*System.out.println(
        state.getName() + ": Setting " + res.getName() +
        " counts to " + d
      );*/
      state.setWeight(
        res,
        Math.log(d.doubleValue() / sum)
      );
      //System.out.println(state.getName() + ": Done");
    }
  }

  public void clearCounts() {
    for(
      Iterator i = ((FiniteAlphabet) state.alphabet()).iterator();
      i.hasNext();
    ) {
      Symbol res = (Symbol) i.next();
      c.put(res, new Double(0.0));
    }
  }

  public SimpleStateTrainer(EmissionState s)
  throws IllegalAlphabetException {
    Alphabet a = s.alphabet();
    if(! (a instanceof FiniteAlphabet)) {
      throw new IllegalAlphabetException(
        "Can't create a SimpleStateTrainer for non-finite alphabet " +
        a.getName() + " of type " + a.getClass()
      );
    }
    this.c = new HashMap();
    this.state = s;
    for (Iterator i = ((FiniteAlphabet) a).iterator(); i.hasNext();) {
      c.put(i.next(), new Double(0.0));
    }
  }
}
