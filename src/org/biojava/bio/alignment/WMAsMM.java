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
import org.biojava.bio.seq.*;

/**
 * Wraps a weight matrix up so that it appears to be a very simple hmm.
 */
public class WMAsMM implements MarkovModel {
  private WeightMatrix wm;
  private Alphabet stateAlpha;
  private WMState [] stateList;
  
  public Alphabet queryAlphabet() {
    return wm.alphabet();
  }
  
  public Alphabet stateAlphabet() {
    return stateAlpha;
  }
  
  public double getTransitionScore(State from, State to)
  throws IllegalResidueException, IllegalTransitionException {
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
  throws IllegalResidueException {
    Alphabet sAlpha = stateAlphabet();
    sAlpha.validate(from);
    
    if(from == DP.MAGICAL_STATE)
      return stateList[0];
      
    if(from == stateList[wm.columns()-1])
      return DP.MAGICAL_STATE;

    WMState fromWM = (WMState) from;
    return stateList[fromWM.index()+1];
  }
  
  public Set transitionsFrom(State from)
  throws IllegalResidueException {
    Alphabet sAlpha = stateAlphabet();
    sAlpha.validate(from);

    if(from == DP.MAGICAL_STATE)
      return Collections.singleton(stateList[0]);
      
    if(from == stateList[wm.columns()-1])
      return Collections.singleton(DP.MAGICAL_STATE);

    WMState fromWM = (WMState) from;
    return Collections.singleton(stateList[fromWM.index()+1]);
  }
    
  public Set transitionsTo(State to)
  throws IllegalResidueException {
    Alphabet sAlpha = stateAlphabet();
    sAlpha.validate(to);

    if(to == DP.MAGICAL_STATE)
      return Collections.singleton(stateList[wm.columns()-1]);
      
    if(to == stateList[0])
      return Collections.singleton(DP.MAGICAL_STATE);

    WMState toWM = (WMState) to;
    return Collections.singleton(stateList[toWM.index()-1]);
  }
  
  public void registerWithTrainer(ModelTrainer modelTrainer) {
    for(Iterator i = stateAlphabet().residues().iterator(); i.hasNext(); ) {
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
  
  public boolean containsTransition(State from, State to)
  throws IllegalResidueException {
    Alphabet sAlpha = stateAlphabet();
    sAlpha.validate(from);
    sAlpha.validate(to);
    
    if((from == DP.MAGICAL_STATE) &&
       (to == stateList[0]))
       return true;
    if((from == stateList[wm.columns()-1]) &&
       (to == DP.MAGICAL_STATE))
       return true;

    WMState fromWM = (WMState) from;
    WMState toWM = (WMState) to;
    
    if(fromWM.index() == toWM.index() - 1)
      return true;    

    return false;
  }
  
  public WMAsMM(WeightMatrix wm) throws IllegalResidueException {
    this.wm = wm;
    stateList = new WMState[wm.columns()];
    SimpleAlphabet sa = new SimpleAlphabet();
    sa.addResidue(DP.MAGICAL_STATE);
    for(int i = 0; i < wm.columns(); i++) {
      stateList[i] = new WMState(i);
      sa.addResidue(stateList[i]);
      stateList[i].setName( "" + (i+1) );
    }
    sa.setName("Weight Matrix columns");
    this.stateAlpha = sa;
  }
  
  private class WMState extends AbstractState {
    private int index;
    
    public int index() {
      return index;
    }
    
    public double getWeight(Residue res)
    throws IllegalResidueException {
      return wm.getWeight(res, index());
    }
    
    public void setWeight(Residue res, double weight)
    throws IllegalResidueException {
      wm.setWeight(res, index(), weight);
    }
    
    public void registerWithTrainer(ModelTrainer modelTrainer) {
      Set trainerSet = modelTrainer.trainersForState(this);
      if(trainerSet.isEmpty()) {
        modelTrainer.registerTrainerForState(this, new SimpleStateTrainer(this));
      }
    }
    
    public WMState(int index) {
      super(queryAlphabet());
      this.index = index;
    }
  }
}
