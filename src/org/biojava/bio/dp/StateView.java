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
 * A State that alters something about an underlying state.
 *
 * @author Matthew Pocock
 */
public abstract class StateView implements EmissionState {
  private final EmissionState source;
  
  /**
   * Translates Symbol r from the view alphabet into the corresponding symbol
   * in the source state alphabet.
   *
   * @param r the Reisdue to translate
   * @return the translated version
   * @throws IllegalSymbolException if r can't be translated
   */
  public abstract Symbol viewToSource(Symbol r)
  throws IllegalSymbolException;
  
  /**
   * Translates Symbol r from the source state alphabet into the corresponding
   * symbol in the view alphabet.
   *
   * @param r the Reisdue to translate
   * @return the translated version
   * @throws IllegalSymbolException if r can't be translated
   */
  public abstract Symbol sourceToView(Symbol r)
  throws IllegalSymbolException;

  public EmissionState getSource() {
    return source;
  }

  public char getToken() {
    return source.getToken();
  }
    
  public String getName() {
    return source.getName();
  }
    
  public Annotation getAnnotation() {
    return source.getAnnotation();
  }
    
  public void registerWithTrainer(ModelTrainer trainer)
  throws BioException {
    Set stateT = trainer.trainersForState(this);
    if(stateT.isEmpty()) {
      source.registerWithTrainer(trainer);
      for(
        Iterator i = trainer.trainersForState(source).iterator();
        i.hasNext();
      ) {
        StateTrainer st = (StateTrainer) i.next();
        trainer.registerTrainerForState(this, new Trainer(st));
      }
    }
  }
    
  public Symbol sampleSymbol() {
    try {
      return sourceToView(source.sampleSymbol());
    } catch (IllegalSymbolException ire) {
      throw new BioError(
        ire,
        "Could not sample symbol as it couldn't be translated"
      );
    }
  }
    
  public double getWeight(Symbol r)
  throws IllegalSymbolException {
    try {
      return source.getWeight(viewToSource(r));
    } catch (IllegalSymbolException ire) {
      throw new IllegalSymbolException(
        ire,
        "Could not retrieve weight for " + r.getName() + " in " + getName()
      );
    }
  }
    
  public void setWeight(Symbol r, double weight)
  throws IllegalSymbolException {
    source.setWeight(viewToSource(r), weight);
  }
    
  public StateView(EmissionState source)
  throws NullPointerException {
    if(source == null) {
      throw new NullPointerException("Can't wrap null");
    }
    this.source = source;
  }
  
  private class Trainer implements StateTrainer {
    private final StateTrainer st;
    
    public void addCount(Symbol res, double times)
    throws IllegalSymbolException {
      st.addCount(viewToSource(res), times);
    }
    
    public void train(EmissionState nullModel, double weight)
    throws IllegalSymbolException {
      st.train(nullModel, weight);
    }
    
    public void clearCounts() {
      st.clearCounts();
    }
    
    public Trainer(StateTrainer st) {
      this.st = st;
    }
  }
}

