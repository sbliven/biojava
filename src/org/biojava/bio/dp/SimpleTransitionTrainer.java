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

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;

/**
 * A simple implementation of a TransitionTrainer.
 */
public class SimpleTransitionTrainer implements TransitionTrainer {
  private MarkovModel model;
  private Transition _tran = new Transition(null, null);
  private Map transCounts = new HashMap();
  
  public SimpleTransitionTrainer(MarkovModel model) {
    this.model = model;
    
    for(Iterator i = model.stateAlphabet().iterator(); i.hasNext(); ) {
      State s = (State) i.next();
      try {
        for(Iterator j = model.transitionsFrom(s).iterator(); j.hasNext(); ) {
          State t = (State) j.next();
          transCounts.put(new Transition(s, t), new Double(0.0));
        }
      } catch (IllegalSymbolException ire) {
        throw new BioError(ire, "State " + s.getName() +
                           " listed in alphabet " +
                           model.stateAlphabet().getName() + " dissapeared.");
      }
    }
  }
  
  public void addCount(State from, State to, double count)
  throws IllegalSymbolException, IllegalTransitionException {
    model.stateAlphabet().validate(from);
    model.stateAlphabet().validate(to);

    _tran.from = from;
    _tran.to = to;
    Double oc = (Double) transCounts.get(_tran);
    if(oc == null)
      throw new IllegalTransitionException(from, to);
    transCounts.put(_tran, new Double(oc.doubleValue() + count));
  }
  
  public void train(double nullModel, double weight)
  throws IllegalSymbolException {
    State [] states = (State []) model.stateAlphabet().symbols().toList().toArray(new State[0]);
    double [] scores = new double[states.length];
    double pseudocount = nullModel * weight;
    
    for(int i = 0; i < states.length; i++) {
      State s = states[i];
      double sum = 0.0;
      Set trans = model.transitionsFrom(s);
      for(int j = 0; j < states.length; j++) {
        State t = states[j];
        if(trans.contains(t)) {
          _tran.from = s;
          _tran.to = t;
          scores[j] = ((Double) transCounts.get(_tran)).doubleValue();
          sum += scores[j] += pseudocount;
          scores[j] = Math.log(scores[j]);
        }
      }
      sum = Math.log(sum);
      for(int j = 0; j < states.length; j++) {
        State t = states[j];
        if(trans.contains(t)) {
          try {
            model.setTransitionScore(s, t, scores[j] - sum);
          } catch (IllegalTransitionException ite) {
            throw new BioError(ite, "State " + t.getName() +
                               " listed in transitionsFrom(" + s.getName() +
                               ") but setTransition blows chunks");
          } catch (ModelVetoException mve) {
            throw new BioError(mve, "Change in transitioin score from " +
                               s.getName() + " to " + t.getName() + " vetoed");
          }
        }
      }
    }
  }
    
  public void clearCounts() {
    for(Iterator i = transCounts.keySet().iterator(); i.hasNext(); ) {
      transCounts.put(i.next(), new Double(0.0));
    }
  }
}

