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
import org.biojava.bio.seq.*;

public class MarkovModelWrapper implements MarkovModel {
  private MarkovModel delegate;
  
  public Alphabet queryAlphabet() {
    return delegate.queryAlphabet();
  }
  
  public Alphabet stateAlphabet() {
    return delegate.stateAlphabet();
  }
  
  public double getTransitionScore(State from, State to)
  throws IllegalResidueException, IllegalTransitionException {
    return delegate.getTransitionScore(from, to);
  }
  
  public void setTransitionScore(State from, State to, double score)
  throws IllegalResidueException, IllegalTransitionException,
  UnsupportedOperationException {
    delegate.setTransitionScore(from, to, score);
  }
  
  public State sampleTransition(State from) throws IllegalResidueException {
    return delegate.sampleTransition(from);
  }
  
  public Set transitionsFrom(State from) throws IllegalResidueException {
    return delegate.transitionsFrom(from);
  }
  
  public Set transitionsTo(State to) throws IllegalResidueException {
    return delegate.transitionsTo(to);
  }
  
  public void registerWithTrainer(ModelTrainer modelTrainer) {
    delegate.registerWithTrainer(modelTrainer);
  }
  
  public void createTransition(State from, State to) 
  throws IllegalResidueException {
    delegate.createTransition(from, to);
  }
  
  public void destroyTransition(State from, State to)
  throws IllegalResidueException {
    delegate.destroyTransition(from, to);
  }

  public boolean containsTransition(State from, State to)
  throws IllegalResidueException {
    return delegate.containsTransition(from, to);
  }
  
  public MarkovModelWrapper(MarkovModel model) {
    this.delegate = model;
  }
}
