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

public class SimpleMarkovModel implements MarkovModel {
  private final Alphabet emissionAlpha;
  private final FiniteAlphabet stateAlpha;
  private final MagicalState magicalState;

  private final Map transFrom;
  private final Map transTo;
  private final Map transitionScores;
  
  private final List transitionListeners;

  private Transition _tran = new Transition(null, null);
  
  {
    transFrom = new HashMap();
    transTo = new HashMap();
    transitionScores = new HashMap();
    transitionListeners = new ArrayList();
  }

  public Alphabet emissionAlphabet() { return emissionAlpha; }
  public FiniteAlphabet stateAlphabet() { return stateAlpha; }
  public int heads() { return magicalState().getAdvance().length; }
  public MagicalState magicalState() { return magicalState; }

  public double getTransitionScore(State from, State to)
  throws IllegalSymbolException, IllegalTransitionException {
    stateAlphabet().validate(from);
    stateAlphabet().validate(to);

    _tran.from = from;
    _tran.to = to;
    Double ts = (Double) transitionScores.get(_tran);
    if(ts != null)
      return ts.doubleValue();
    throw new IllegalTransitionException(from, to);
  }

  public State sampleTransition(State from) throws IllegalSymbolException {
    stateAlphabet().validate(from);
    
    double p = Math.random();
    try {
      for(Iterator i = transitionsFrom(from).iterator(); i.hasNext(); ) {
        State s = (State) i.next();
        if( (p -= Math.exp(getTransitionScore(from, s))) <= 0 )
        return s;
      }
    } catch (IllegalSymbolException ire) {
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
    throw new IllegalSymbolException("Could not find transition from state " +
                                      from.getName() +
                                      ". Do the probabilities sum to 1?" +
                                      "\np=" + p + "\n" + sb.toString());
  }
  
  public void setTransitionScore(State from, State to, double value)
  throws IllegalSymbolException, IllegalTransitionException, ModelVetoException {
    stateAlphabet().validate(from);
    stateAlphabet().validate(to);

    TransitionEvent te = new TransitionEvent(
      this, from, to, getTransitionScore(from, to), value
    );
    List tl;
    synchronized(transitionListeners) {
      tl = new ArrayList(transitionListeners);
    }
    
    for(Iterator i = tl.iterator(); i.hasNext(); ) {
      ((TransitionListener) i.next()).preChangeTransitionScore(te);
    }

    _tran.from = from;
    _tran.to = to;
    if(transitionScores.containsKey(_tran)) {
      transitionScores.put(_tran, new Double(value));
    } else {
      throw new IllegalTransitionException(
        from, to,
        "No transition from " + from.getName() +
        " to " + to.getName() + " defined"
      );
    }

    for(Iterator i = tl.iterator(); i.hasNext(); ) {
      ((TransitionListener) i.next()).postChangeTransitionScore(te);
    }
  }

  public void createTransition(State from, State to)
  throws IllegalSymbolException, ModelVetoException {
    stateAlphabet().validate(from);
    stateAlphabet().validate(to);

    TransitionEvent te = new TransitionEvent(this, from, to);
    List tl;
    synchronized(transitionListeners) {
      tl = new ArrayList(transitionListeners);
    }
    
    for(Iterator i = tl.iterator(); i.hasNext(); ) {
      ((TransitionListener) i.next()).preCreateTransition(te);
    }
    
    transitionScores.put(new Transition(from, to),
                         new Double(Double.NEGATIVE_INFINITY));
    Set t = transitionsTo(to);
    Set f = transitionsFrom(from);
    f.add(to);
    t.add(from);

    for(Iterator i = tl.iterator(); i.hasNext(); ) {
      ((TransitionListener) i.next()).postCreateTransition(te);
    }
  }
  
  public void destroyTransition(State from, State to)
  throws IllegalSymbolException, ModelVetoException {
    stateAlphabet().validate(from);
    stateAlphabet().validate(to);

    TransitionEvent te = new TransitionEvent(this, from, to);
    List tl;
    synchronized(transitionListeners) {
      tl = new ArrayList(transitionListeners);
    }
    
    for(Iterator i = tl.iterator(); i.hasNext(); ) {
      ((TransitionListener) i.next()).preDestroyTransition(te);
    }
    
    _tran.from = from;
    _tran.to = to;
    transitionScores.remove(_tran);
    transitionsFrom(from).remove(to);
    transitionsTo(to).remove(from);

    for(Iterator i = tl.iterator(); i.hasNext(); ) {
      ((TransitionListener) i.next()).postDestroyTransition(te);
    }
  }
  
  public boolean containsTransition(State from, State to)
  throws IllegalSymbolException {
    stateAlphabet().validate(from);
    stateAlphabet().validate(to);
    return transitionsFrom(from).contains(to);
  }
  
  public Set transitionsFrom(State from) throws IllegalSymbolException {
    stateAlphabet().validate(from);
    
    Set s = (Set) transFrom.get(from);
    if(s == null)
      throw new IllegalSymbolException("State " + from.getName() +
                                        " not known in states " +
                                        stateAlphabet().getName());
    return s;
  }
    
  public Set transitionsTo(State to) throws IllegalSymbolException {
    stateAlphabet().validate(to);

    Set s = (Set) transTo.get(to);
    if(s == null)
      throw new IllegalSymbolException("State " + to +
                                        " not known in states " +
                                        stateAlphabet().getName());
    return s;
  }

  public void addState(State toAdd) throws IllegalSymbolException {
    if(toAdd instanceof MagicalState) {
      throw new IllegalSymbolException("Can not add a MagicalState");
    }
    
    if(stateAlphabet().contains(toAdd)) {
      throw new IllegalSymbolException("We already contain " + toAdd.getName());
    }
    
    ((SimpleAlphabet) stateAlphabet()).addSymbol(toAdd);
    transFrom.put(toAdd, new HashSet());
    transTo.put(toAdd, new HashSet());
  }
  
  public void removeState(State toGo)
  throws IllegalSymbolException, IllegalTransitionException {
    stateAlphabet().validate(toGo);
    if(toGo instanceof MagicalState) {
      throw new IllegalSymbolException("You can not remove the MagicalState");
    }
    Set t;
    if(!(t = transitionsFrom(toGo)).isEmpty()) {
      throw new IllegalTransitionException(
        toGo, (State) t.iterator().next(),
        "You can not remove a state untill all transitions to and from it " +
        "have been destroyed"
      );
    }

    if(!(t = transitionsTo(toGo)).isEmpty()) {
      throw new IllegalTransitionException(
        (State) t.iterator().next(), toGo,
        "You can not remove a state untill all transitions to and from it " +
        "have been destroyed"
      );
    }

    ((SimpleAlphabet) stateAlphabet()).removeSymbol(toGo);
    transFrom.remove(toGo);
    transTo.remove(toGo);
  }

  public SimpleMarkovModel(int heads, Alphabet emissionAlpha, String name) {
    this(heads, emissionAlpha);
    ((SimpleAlphabet) stateAlpha).setName(name);
  }
  
  public SimpleMarkovModel(int heads, Alphabet emissionAlpha) {
    this.emissionAlpha = emissionAlpha;
    this.stateAlpha = new SimpleAlphabet();
    this.magicalState = MagicalState.getMagicalState(heads);
    
    try {
      ((SimpleAlphabet) stateAlpha).addSymbol(magicalState);
    } catch (IllegalSymbolException ire) {
      throw new BioError(
        ire,
        "Alphabet went screwey on me & wouldn't accept the magical symbol"
      );
    }

    transFrom.put(magicalState, new HashSet());
    transTo.put(magicalState, new HashSet());
  }

  public void registerWithTrainer(ModelTrainer modelTrainer)
  throws BioException {
    if(modelTrainer.getTrainerForModel(this) == null) {
      TransitionTrainer tTrainer = new SimpleTransitionTrainer(this);
      modelTrainer.registerTrainerForModel(this, tTrainer);
      for(Iterator i = stateAlphabet().iterator(); i.hasNext(); ) {
        State s = (State) i.next();
        if(s instanceof Trainable) {
          ((Trainable) s).registerWithTrainer(modelTrainer);
        }
        try {
          for(Iterator j = transitionsFrom(s).iterator(); j.hasNext(); ) {
            State t = (State) j.next();
            modelTrainer.registerTrainerForTransition(s, t, tTrainer, s, t);
          }
        } catch (IllegalSymbolException ire) {
          throw new BioException(
            ire,
            "State " + s.getName() +
            " listed in alphabet " +
            stateAlphabet().getName() + " dissapeared."
          );
        }
      }
    }
  }
  
  public void addTransitionListener(TransitionListener tl) {
    synchronized(transitionListeners) {
      transitionListeners.add(tl);
    }
  }
  
  public void removeTransitionListener(TransitionListener tl) {
    synchronized(transitionListeners) {
      transitionListeners.remove(tl);
    }
  }
}
