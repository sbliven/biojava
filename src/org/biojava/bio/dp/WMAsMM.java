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
 * Wraps a weight matrix up so that it appears to be a very simple hmm.
 */
public class WMAsMM implements MarkovModel {
  private static final int [] advance = {1};
  private static MagicalState magicalState = MagicalState.getMagicalState(1);
  
  private WeightMatrix wm;
  private FiniteAlphabet stateAlpha;
  private WMState [] stateList;
  
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
      return stateList[0];
      
    if(from == stateList[wm.columns()-1])
      return magicalState;

    WMState fromWM = (WMState) from;
    return stateList[fromWM.index()+1];
  }
  
  public Set transitionsFrom(State from)
  throws IllegalSymbolException {
    Alphabet sAlpha = stateAlphabet();
    sAlpha.validate(from);

    if(from == magicalState)
      return Collections.singleton(stateList[0]);
      
    if(from == stateList[wm.columns()-1])
      return Collections.singleton(magicalState);
    
    WMState fromWM = (WMState) from;
    return Collections.singleton(stateList[fromWM.index()+1]);
  }
    
  public Set transitionsTo(State to)
  throws IllegalSymbolException {
    Alphabet sAlpha = stateAlphabet();
    sAlpha.validate(to);

    if(to == magicalState)
      return Collections.singleton(stateList[wm.columns()-1]);
      
    if(to == stateList[0])
      return Collections.singleton(magicalState);

    WMState toWM = (WMState) to;
    return Collections.singleton(stateList[toWM.index()-1]);
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
       (to == stateList[0]))
       return true;
    if((from == stateList[wm.columns()-1]) &&
       (to == magicalState))
       return true;

    WMState fromWM = (WMState) from;
    WMState toWM = (WMState) to;
    
    if(fromWM.index() == toWM.index() - 1)
      return true;    

    return false;
  }
  
  public WMAsMM(WeightMatrix wm) throws IllegalSymbolException {
    this.wm = wm;
    this.stateList = new WMState[wm.columns()];
    SimpleAlphabet sa = new SimpleAlphabet();
    sa.addSymbol(magicalState);
    this.stateAlpha = sa;
    for(int i = 0; i < wm.columns(); i++) {
      stateList[i] = new WMState(wm.alphabet(), i);
      sa.addSymbol(stateList[i]);
      stateList[i].setName( "" + (i+1) );
    }
    sa.setName("Weight Matrix columns");
  }
  
  private class WMState extends AbstractState {
    private final int index;
    
    public int index() {
      return index;
    }
    
    public final int [] getAdvance() {
      return advance;
    }
    
    public double getWeight(Symbol res)
    throws IllegalSymbolException {
      return wm.getWeight(res, index());
    }
    
    public void setWeight(Symbol res, double weight)
    throws IllegalSymbolException {
      wm.setWeight(res, index(), weight);
    }
    
    public void registerWithTrainer(ModelTrainer modelTrainer) {
      Set trainerSet = modelTrainer.trainersForState(this);
      if(trainerSet.isEmpty()) {
        try {
          modelTrainer.registerTrainerForState(
            this, new SimpleStateTrainer(this)
          );
        } catch (IllegalAlphabetException iae) {
          throw new BioError(
            iae, "I am sure that I am over a finite alphabet - the world is mad."
          );
        }
      }
    }
    
    public WMState(FiniteAlphabet alpha, int index) {
      super(alpha);
      this.index = index;
    }
  }
}
