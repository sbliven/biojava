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

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.dist.*;

/**
 * Wraps a weight matrix up so that it appears to be a very simple hmm.
 */
public class WMAsMM implements MarkovModel, Serializable {
  private static final int [] advance = {1};
  
  private final WeightMatrix wm;
  private final FiniteAlphabet stateAlpha;
  private final MagicalState magicalState;
  private final EmissionState [] states;
  
  private final Map transFrom;
  private final Map transTo;
  private final Map transWeights;
  
  private final transient ChangeSupport changeSupport;
  private final MarkovModel.DistributionForwarder distForwarder;
  
  public Alphabet emissionAlphabet() {
    return wm.getAlphabet();
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
  
  public Distribution getWeights(State source)
  throws IllegalSymbolException {
    stateAlpha.validate(source);
    return (Distribution) transWeights.get(source);
  }
  
  public void setWeights(State source, Distribution dist)
  throws ChangeVetoException {
    throw new ChangeVetoException(
      "Can't replace distribution in immutable model"
    );
  }
  
  public FiniteAlphabet transitionsFrom(State from)
  throws IllegalSymbolException {
    Alphabet sAlpha = stateAlphabet();
    sAlpha.validate(from);

    return (FiniteAlphabet) transFrom.get(from);
  }
    
  public FiniteAlphabet transitionsTo(State to)
  throws IllegalSymbolException {
    Alphabet sAlpha = stateAlphabet();
    sAlpha.validate(to);

    return (FiniteAlphabet) transTo.get(to);
  }
  
  public void registerWithTrainer(ModelTrainer modelTrainer)
  throws BioException {
/*    for(Iterator i = stateAlphabet().iterator(); i.hasNext(); ) {
      EmissionState s = (EmissionState) i.next();
      s.registerWithTrainer(modelTrainer);
    }*/
  }
  
  public void createTransition(State from, State to)
  throws ChangeVetoException {
    throw new ChangeVetoException(
      "destroyTransition not supported by " + getClass());
  }

  public void destroyTransition(State from, State to)
  throws ChangeVetoException {
    throw new ChangeVetoException(
      "destroyTransition not supported by " + getClass());
  }
  
  public void addState(State toAdd)
  throws IllegalSymbolException, ChangeVetoException {
    if(stateAlphabet().contains(toAdd)) {
      throw new IllegalSymbolException(
        toAdd, 
        "Can't add a state to a model that already contains it"
      );
    }
    
    throw new ChangeVetoException("addState not supported by " + getClass());
  }
  
  public void removeState(State toAdd)
  throws IllegalSymbolException, ChangeVetoException {
    stateAlphabet().validate(toAdd);
    
    throw new ChangeVetoException("removeState not supported by " + getClass());
  }

  public boolean containsTransition(State from, State to)
  throws IllegalSymbolException {
    Alphabet sAlpha = stateAlphabet();
    sAlpha.validate(from);
    sAlpha.validate(to);

    return transitionsFrom(from).contains(to);
  }
  
  protected int index(State s) {
    for(int i = 0; i < states.length; i++) {
      if(s == states[i]) {
        return i;
      }
    }
    
    return -1;
  }
  
  public void addChangeListener(ChangeListener cl) {
    changeSupport.addChangeListener(cl);
  }

  public void addChangeListener(ChangeListener cl, ChangeType ct) {
    changeSupport.addChangeListener(cl, ct);
  }
  
  public void removeChangeListener(ChangeListener cl) {
    changeSupport.removeChangeListener(cl);
  }

  public void removeChangeListener(ChangeListener cl, ChangeType ct) {
    changeSupport.removeChangeListener(cl, ct);
  }
  
  public WMAsMM(WeightMatrix wm) throws IllegalSymbolException {
    changeSupport = new ChangeSupport();
    distForwarder = new MarkovModel.DistributionForwarder(this, changeSupport);
    transFrom = new HashMap();
    transTo = new HashMap();
    transWeights = new HashMap();
    this.wm = wm;
    this.magicalState = MagicalState.getMagicalState(wm.getAlphabet(), 1);
    SimpleAlphabet sa = new SimpleAlphabet();
    sa.addSymbol(magicalState);
    this.stateAlpha = sa;
    this.states = new EmissionState[wm.columns()];
    for(int i = 0; i <= wm.columns(); i++) {
      if(i < wm.columns()) {
        sa.addSymbol(
          this.states[i] = new SimpleEmissionState(
            i + "",
            Annotation.EMPTY_ANNOTATION,
            this.advance,
            wm.getColumn(i)
          )
        );
        wm.getColumn(i).addChangeListener(distForwarder);
      }
      State prev = (i == 0) ? magicalState : states[i-1];
      State current = (i == wm.columns()) ? magicalState : states[i];
      FiniteAlphabet fa = (FiniteAlphabet) prev.getMatches();
      transFrom.put(prev, current.getMatches());
      transTo.put(current, fa);
      Distribution dist = new UniformDistribution(fa);
      transWeights.put(prev, fa);
    }
    sa.setName("Weight Matrix columns");
  }
}
