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

import org.biojava.bio.BioError;
import org.biojava.bio.seq.*;

/**
 * A State that alters something about an underlying state.
 *
 * @author Matthew Pocock
 */
public abstract class StateView implements EmissionState {
  private final EmissionState source;
  
  /**
   * Translates Residue r from the view alphabet into the corresponding symbol
   * in the source state alphabet.
   *
   * @param r the Reisdue to translate
   * @return the translated version
   * @throws IllegalResidueException if r can't be translated
   */
  public abstract Residue viewToSource(Residue r)
  throws IllegalResidueException;
  
  /**
   * Translates Residue r from the source state alphabet into the corresponding
   * symbol in the view alphabet.
   *
   * @param r the Reisdue to translate
   * @return the translated version
   * @throws IllegalResidueException if r can't be translated
   */
  public abstract Residue sourceToView(Residue r)
  throws IllegalResidueException;

  public EmissionState getSource() {
    return source;
  }

  public char getSymbol() {
    return source.getSymbol();
  }
    
  public String getName() {
    return source.getName();
  }
    
  public Annotation getAnnotation() {
    return source.getAnnotation();
  }
    
  public void registerWithTrainer(ModelTrainer trainer)
  throws SeqException {
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
    
  public Residue sampleResidue() {
    try {
      return sourceToView(source.sampleResidue());
    } catch (IllegalResidueException ire) {
      throw new BioError(
        ire,
        "Could not sample residue as it couldn't be translated"
      );
    }
  }
    
  public double getWeight(Residue r)
  throws IllegalResidueException {
    try {
      return source.getWeight(viewToSource(r));
    } catch (IllegalResidueException ire) {
      throw new IllegalResidueException(
        ire,
        "Could not retrieve weight for " + r.getName() + " in " + getName()
      );
    }
  }
    
  public void setWeight(Residue r, double weight)
  throws IllegalResidueException {
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
    
    public void addCount(Residue res, double times)
    throws IllegalResidueException {
      st.addCount(viewToSource(res), times);
    }
    
    public void train(EmissionState nullModel, double weight)
    throws IllegalResidueException {
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

