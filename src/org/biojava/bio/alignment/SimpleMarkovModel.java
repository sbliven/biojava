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


package org.biojava.bio.alignment;

import java.util.*;
import org.biojava.bio.BioError;
import org.biojava.bio.seq.*;

public class SimpleMarkovModel implements MarkovModel {
  private Alphabet queryAlpha;
  private Alphabet stateAlpha;

  private Map transFrom;
  private Map transTo;
  private Map transitionScores;

  private Transition _tran = new Transition(null, null);
  
  {
    transFrom = new HashMap();
    transTo = new HashMap();
    transitionScores = new HashMap();
  }

  public Alphabet queryAlphabet() { return queryAlpha; }
  public Alphabet stateAlphabet() { return stateAlpha; }

  public double getTransitionScore(State from, State to)
  throws IllegalResidueException, IllegalTransitionException {
    stateAlphabet().validate(from);
    stateAlphabet().validate(to);

    _tran.from = from;
    _tran.to = to;
    Double ts = (Double) transitionScores.get(_tran);
    if(ts != null)
      return ts.doubleValue();
    throw new IllegalTransitionException(from, to);
  }

  public State sampleTransition(State from) throws IllegalResidueException {
    stateAlphabet().validate(from);
    
    double p = Math.random();
    try {
      for(Iterator i = transitionsFrom(from).iterator(); i.hasNext(); ) {
        State s = (State) i.next();
        if( (p -= Math.exp(getTransitionScore(from, s))) <= 0 )
        return s;
      }
    } catch (IllegalResidueException ire) {
    } catch (IllegalTransitionException ite) {
      throw new BioError(ite, "Transition listend in transitionsFrom(" +
                         from.getName() + "has dissapeared.");
    }
    
    StringBuffer sb = new StringBuffer();

    for(Iterator i = transitionsFrom(from).iterator(); i.hasNext(); ) {
      try {
      State s = (State) i.next();
      double t = getTransitionScore(from, s);
      if(t > 0.0)
        sb.append("\t" + s.getName() + " -> " + t + "\n");
      } catch (IllegalTransitionException ite) {
        throw new BioError(ite, "Transition listend in transitionsFrom(" +
                           from.getName() + "has dissapeared.");
      }
    }
    throw new IllegalResidueException("Could not find transition from state " +
                                      from.getName() +
                                      ". Do the probabilities sum to 1?" +
                                      "\np=" + p + "\n" + sb.toString());
  }
  
  public void setTransitionScore(State from, State to, double value)
  throws IllegalResidueException, IllegalTransitionException {
    stateAlphabet().validate(from);
    stateAlphabet().validate(to);

    _tran.from = from;
    _tran.to = to;
    if(transitionScores.containsKey(_tran)) {
      transitionScores.put(_tran, new Double(value));
    }
    throw new IllegalTransitionException(from, to, "No transition from " + from.getName() +
                                         " to " + to.getName() + " defined");
  }

  public void createTransition(State from, State to)
  throws IllegalResidueException {
    stateAlphabet().validate(from);
    stateAlphabet().validate(to);
    
    transitionScores.put(new Transition(from, to),
                         new Double(Double.NEGATIVE_INFINITY));
    Set t = transitionsTo(to);
    Set f = transitionsFrom(from);
    f.add(to);
    t.add(from);
  }
  
  public void destroyTransition(State from, State to)
  throws IllegalResidueException {
    stateAlphabet().validate(from);
    stateAlphabet().validate(to);
    
    _tran.from = from;
    _tran.to = to;
    transitionScores.remove(_tran);
    transitionsFrom(from).remove(to);
    transitionsTo(to).remove(from);
  }
  
  public boolean containsTransition(State from, State to)
  throws IllegalResidueException {
    stateAlphabet().validate(from);
    stateAlphabet().validate(to);
    return transitionsFrom(from).contains(to);
  }
  
  public Set transitionsFrom(State from) throws IllegalResidueException {
    stateAlphabet().validate(from);
    
    Set s = (Set) transFrom.get(from);
    if(s == null)
      throw new IllegalResidueException("State " + from.getName() +
                                        " not known in states " +
                                        stateAlphabet().getName());
    return s;
  }
    
  public Set transitionsTo(State to) throws IllegalResidueException {
    stateAlphabet().validate(to);

    Set s = (Set) transTo.get(to);
    if(s == null)
      throw new IllegalResidueException("State " + to +
                                        " not known in states " +
                                        stateAlphabet().getName());
    return s;
  }

  public SimpleMarkovModel(Alphabet queryAlpha, Alphabet stateAlpha)
  throws SeqException {
    this.queryAlpha = queryAlpha;
    this.stateAlpha = stateAlpha;

    if(!stateAlpha.contains(DP.MAGICAL_STATE))
      throw new SeqException("Use new SimpleMarkovModel(queryAlpha, stateAlpha). " +
                             "stateAlpha did not contain DP.MAGICAL_STATE.");
    for(Iterator i = stateAlpha.residues().iterator(); i.hasNext(); ) {
      Object o = i.next();
      transFrom.put(o, new HashSet());
      transTo.put(o, new HashSet());
    }
  }

  public void registerWithTrainer(ModelTrainer modelTrainer) {
    if(modelTrainer.getTrainerForModel(this) == null) {
      TransitionTrainer tTrainer = new SimpleTransitionTrainer(this);
      try {
        modelTrainer.registerTrainerForModel(this, tTrainer);
      } catch (SeqException se) {
        throw new BioError("Can't register trainer for model, even though " + 
          " there is no trainer associated with the model");
      }
      for(Iterator i = stateAlphabet().residues().iterator(); i.hasNext(); ) {
        State s = (State) i.next();
        s.registerWithTrainer(modelTrainer);
        try {
          for(Iterator j = transitionsFrom(s).iterator(); j.hasNext(); ) {
            State t = (State) j.next();
            modelTrainer.registerTrainerForTransition(s, t, tTrainer, s, t);
          }
        } catch (IllegalResidueException ire) {
          throw new BioError(ire, "State " + s.getName() +
                             " listed in alphabet " +
                             stateAlphabet().getName() + " dissapeared.");
        } catch (SeqException se) {
          throw new BioError(se, "Somehow, my trainer is not registered.");
        }
      }
    }
  }
}
