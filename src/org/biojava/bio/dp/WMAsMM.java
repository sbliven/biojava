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

/**
 * Wraps a weight matrix up so that it appears to be a very simple hmm.
 */
public class WMAsMM implements MarkovModel, Serializable {
  private static final int [] advance = {1};
  private static final MagicalState magicalState = MagicalState.getMagicalState(1);
  
  private final WeightMatrix wm;
  private final FiniteAlphabet stateAlpha;
  
  public Alphabet emissionAlphabet() {
    return wm.alphabet();
  }
  
  public FiniteAlphabet stateAlphabet() {
    return stateAlpha;
  }
  
  public int heads() {
    return 1;
  }
  
  public MagicalState magicalState() {
    return magicalState;
  }
  
  public double getTransitionScore(State from, State to)
  throws IllegalSymbolException, IllegalTransitionException {
    if(containsTransition(from, to)) {
      return 0.0;
    }
    throw new IllegalTransitionException(from, to);
  }
  
  public void setTransitionScore(State from, State to, double weight)
  throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
      "setTranstionScore not implemented in " + getClass()
    );
  }
  
  public State sampleTransition(State from)
  throws IllegalSymbolException {
    Alphabet sAlpha = stateAlphabet();
    sAlpha.validate(from);
    
    if(from == magicalState)
      return wm.getColumn(0);
      
    if(from == wm.getColumn(wm.columns()-1))
      return magicalState;

    return wm.getColumn(index((EmissionState) from)+1);
  }
  
  public Set transitionsFrom(State from)
  throws IllegalSymbolException {
    Alphabet sAlpha = stateAlphabet();
    sAlpha.validate(from);

    if(from == magicalState)
      return Collections.singleton(wm.getColumn(0));
      
    if(from == wm.getColumn(wm.columns()-1))
      return Collections.singleton(magicalState);
    
    return Collections.singleton(wm.getColumn(index((EmissionState) from)+1));
  }
    
  public Set transitionsTo(State to)
  throws IllegalSymbolException {
    Alphabet sAlpha = stateAlphabet();
    sAlpha.validate(to);

    if(to == magicalState)
      return Collections.singleton(wm.getColumn(wm.columns()-1));
      
    if(to == wm.getColumn(0))
      return Collections.singleton(magicalState);

    return Collections.singleton(wm.getColumn(index((EmissionState) to)-1));
  }
  
  public void registerWithTrainer(ModelTrainer modelTrainer)
  throws BioException {
    for(Iterator i = stateAlphabet().iterator(); i.hasNext(); ) {
      EmissionState s = (EmissionState) i.next();
      s.registerWithTrainer(modelTrainer);
    }
  }
  
  public void createTransition(State from, State to)
  throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
      "destroyTransition not supported by " + getClass());
  }

  public void destroyTransition(State from, State to)
  throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
      "destroyTransition not supported by " + getClass());
  }
  
  public void addState(State toAdd)
  throws IllegalSymbolException, UnsupportedOperationException {
    if(stateAlphabet().contains(toAdd)) {
      throw new IllegalSymbolException(
        toAdd, 
        "Can't add a state to a model that already contains it"
      );
    }
    
    throw new UnsupportedOperationException("addState not supported by " + getClass());
  }
  
  public void removeState(State toAdd)
  throws IllegalSymbolException, UnsupportedOperationException {
    stateAlphabet().validate(toAdd);
    
    throw new UnsupportedOperationException("removeState not supported by " + getClass());
  }

  public boolean containsTransition(State from, State to)
  throws IllegalSymbolException {
    Alphabet sAlpha = stateAlphabet();
    sAlpha.validate(from);
    sAlpha.validate(to);
    
    if((from == magicalState) &&
       (to == wm.getColumn(0)))
       return true;
    if((from == wm.getColumn(wm.columns()-1)) &&
       (to == magicalState))
       return true;

    if(index((EmissionState) from) == index((EmissionState) to) - 1)
      return true;    

    return false;
  }
  
  public void addTransitionListener(TransitionListener tl) {}
  public void removeTransitionListener(TransitionListener tl) {}
  
  protected int index(EmissionState es) {
    for(int i = 0; i < wm.columns(); i++) {
      if(es == wm.getColumn(i)) {
        return i;
      }
    }
    
    return -1;
  }

  public WMAsMM(WeightMatrix wm) throws IllegalSymbolException {
    this.wm = wm;
    SimpleAlphabet sa = new SimpleAlphabet();
    sa.addSymbol(magicalState);
    this.stateAlpha = sa;
    for(int i = 0; i < wm.columns(); i++) {
      sa.addSymbol(wm.getColumn(i));
    }
    sa.setName("Weight Matrix columns");
  }
}
