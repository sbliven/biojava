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
import java.io.Serializable; 
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.dist.*;

public class SimpleMarkovModel implements MarkovModel, Serializable {
  private final Alphabet emissionAlpha;
  private final FiniteAlphabet stateAlpha;
  private final MagicalState magicalState;

  private final Map transFrom;
  private final Map transTo;
  private final Map transWeights;
  
  private transient List transitionListeners;

  private Transition _tran = new Transition(null, null);
  
  {
    transFrom = new HashMap();
    transTo = new HashMap();
    transWeights = new HashMap();
  }

  protected List getTransitionListeners() {
    if(transitionListeners == null) {
      transitionListeners = new ArrayList();
    }
    return transitionListeners;
  }
  
  public Alphabet emissionAlphabet() { return emissionAlpha; }
  public FiniteAlphabet stateAlphabet() { return stateAlpha; }
  public int heads() { return magicalState().getAdvance().length; }
  public MagicalState magicalState() { return magicalState; }

  public Distribution getWeights(State source)
  throws IllegalSymbolException {
    stateAlphabet().validate(source);

    Distribution dist = (Distribution) transWeights.get(source);
    if(dist == null) {
      throw new BioError(
        "Model does contain " + source.getName() +
        " but the associated transition distribution is missing."
      );
    }
    return dist;
  }

  /**
   * Use this methods to customize the transition probabilities.
   * <P>
   * By default, the distribution P(destination | source) is a totaly free
   * distribution. This allows the different probabilities to vary. If you
   * wish to change this behaviour (for example, to make one set of transition
   * probabilities equal to another), then use this method to replace the
   * Distribution with one of your own.
   *
   * @param the source State
   * @param dist  the new Distribution over the transition probabilites from source
   * @throws IllegalSymbolException if source is not a member of this model
   * @throws IllegalAlphabetException if dist is not a distribution over the
   *         states returned by model.transitionsFrom(source)
   */
  public void setWeights(State source, Distribution dist)
  throws IllegalSymbolException, IllegalAlphabetException {
    FiniteAlphabet ta = transitionsFrom(source);
    if(dist.getAlphabet() != ta) {
      throw new IllegalAlphabetException(
        "Can't set distribution from state " + source.getName() +
        " as the distribution alphabet is not the alphabet of transitions: " +
        ta.getName() + " and " + dist.getAlphabet().getName()
      );
    }
    
    transWeights.put(source, dist);
  }
  
  public void createTransition(State from, State to)
  throws IllegalSymbolException, ModelVetoException {
    stateAlphabet().validate(from);
    stateAlphabet().validate(to);
    TransitionEvent te = new TransitionEvent(this, from, to);
    
    FiniteAlphabet f = transitionsFrom(from);
    FiniteAlphabet t = transitionsTo(to);

    if(f.contains(to)) {
      throw new ModelVetoException(
        "Transition already exists: " + from.getName() + " -> " + to.getName(),
        te
      );
    }

    List transitionListeners = getTransitionListeners();
    List tl;
    synchronized(transitionListeners) {
      tl = new ArrayList(transitionListeners);
    }
    
    for(Iterator i = tl.iterator(); i.hasNext(); ) {
      ((TransitionListener) i.next()).preCreateTransition(te);
    }

    f.addSymbol(to);
    t.addSymbol(from);

    for(Iterator i = tl.iterator(); i.hasNext(); ) {
      ((TransitionListener) i.next()).postCreateTransition(te);
    }
  }
  
  public void destroyTransition(State from, State to)
  throws IllegalSymbolException, ModelVetoException {
    stateAlphabet().validate(from);
    stateAlphabet().validate(to);

    TransitionEvent te = new TransitionEvent(this, from, to);

    FiniteAlphabet f = transitionsFrom(from);
    FiniteAlphabet t = transitionsTo(to);
    
    if(!f.contains(to)) {
      throw new ModelVetoException(
        "Transition does not exists: " + from.getName() + " -> " + to.getName(),
        te
      );
    }

    Distribution dist = getWeights(from);
    double w = dist.getWeight(to); 
    if(w != 0.0) {
      throw new ModelVetoException(
        "Can't remove transition as its weight is not zero: " +
        from.getName() + " -> " + to.getName() + " = " + w,
        te
      );
    }

    List transitionListeners = getTransitionListeners();
    List tl;
    synchronized(transitionListeners) {
      tl = new ArrayList(transitionListeners);
    }
    
    for(Iterator i = tl.iterator(); i.hasNext(); ) {
      ((TransitionListener) i.next()).preDestroyTransition(te);
    }

    transitionsFrom(from).removeSymbol(to);
    transitionsTo(to).removeSymbol(from);
    
    for(Iterator i = tl.iterator(); i.hasNext(); ) {
      ((TransitionListener) i.next()).postDestroyTransition(te);
    }
  }
  
  public boolean containsTransition(State from, State to)
  throws IllegalSymbolException {
    stateAlphabet().validate(to);
    return transitionsFrom(from).contains(to);
  }
  
  public FiniteAlphabet transitionsFrom(State from)
  throws IllegalSymbolException {
    stateAlphabet().validate(from);
    
    FiniteAlphabet s = (FiniteAlphabet) transFrom.get(from);
    if(s == null) {
      throw new BioError(
        "State " + from.getName() +
        " is known in states " +
        stateAlphabet().getName() +
        " but is not listed in the transFrom table"
      );
    }
    return s;
  }
    
  public FiniteAlphabet transitionsTo(State to)
  throws IllegalSymbolException {
    stateAlphabet().validate(to);

    FiniteAlphabet s = (FiniteAlphabet) transTo.get(to);
    if(s == null) {
      throw new BioError(
        "State " + to +
        " is known in states " +
        stateAlphabet().getName() +
        " but is not listed in the transTo table"
      );
    }
    return s;
  }

  public void addState(State toAdd) throws IllegalSymbolException {
    if(toAdd instanceof MagicalState && toAdd != magicalState) {
      throw new IllegalSymbolException("Can not add a MagicalState");
    }
    
    if(stateAlphabet().contains(toAdd)) {
      throw new IllegalSymbolException("We already contain " + toAdd.getName());
    }
    
    if(toAdd instanceof EmissionState) {
      int esh = ((EmissionState) toAdd).getAdvance().length;
      if(esh != heads()) {
        throw new IllegalSymbolException(
          "This model " + stateAlphabet().getName() +
          " has " + heads() + " heads, but the state " +
          toAdd.getName() + " has " + esh + " heads"
        );
      }
    }
    
    if(toAdd instanceof ModelInState) {
      int esh = ((ModelInState) toAdd).getModel().heads();
      if(esh != heads()) {
        throw new IllegalSymbolException(
          "This model " + stateAlphabet().getName() +
          " has " + heads() + " heads, but the model-in-state " +
          toAdd.getName() + " has " + esh + " heads"
        );
      }
    }
      
    stateAlphabet().addSymbol(toAdd);
    transFrom.put(toAdd, new SimpleAlphabet("Transitions from " + toAdd.getName()));
    transTo.put(toAdd, new SimpleAlphabet("Transitions to " + toAdd.getName()));
  }
  
  public void removeState(State toGo)
  throws IllegalSymbolException, IllegalTransitionException {
    stateAlphabet().validate(toGo);
    if(toGo instanceof MagicalState) {
      throw new IllegalSymbolException("You can not remove the MagicalState");
    }
    FiniteAlphabet t;
    if((t = transitionsFrom(toGo)).size() != 0) {
      throw new IllegalTransitionException(
        toGo, (State) t.iterator().next(),
        "You can not remove a state untill all transitions to and from it " +
        "have been destroyed"
      );
    }

    if((t = transitionsTo(toGo)).size() != 0) {
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
  
  /**
   * @deprecated
   */
  public SimpleMarkovModel(int heads, Alphabet emissionAlpha) {
    this.emissionAlpha = emissionAlpha;
    this.stateAlpha = new SimpleAlphabet();
    this.magicalState = MagicalState.getMagicalState(emissionAlpha, heads);
    
    try {
      ((SimpleAlphabet) stateAlpha).addSymbol(magicalState);
    } catch (IllegalSymbolException ire) {
      throw new BioError(
        ire,
        "Alphabet went screwey on me & wouldn't accept the magical state"
      );
    }

    try {
      addState(magicalState);
    } catch (IllegalSymbolException ise) {
      throw new BioError(ise, "Couldn't add magical state");
    }
  }
  
  public void addTransitionListener(TransitionListener tl) {
    List transitionListeners = getTransitionListeners();
    synchronized(transitionListeners) {
      transitionListeners.add(tl);
    }
  }
  
  public void removeTransitionListener(TransitionListener tl) {
    List transitionListeners = getTransitionListeners();
    synchronized(transitionListeners) {
      transitionListeners.remove(tl);
    }
  }
}
