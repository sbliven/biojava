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

public class SimpleState extends AbstractState {
  private Map residueToProb = new HashMap();
  public final int [] advance;
  
  public final int [] getAdvance() {
    return advance;
  }
  
  public double getWeight(Residue r) throws IllegalResidueException {
    if(r == MagicalState.MAGICAL_RESIDUE) {
      return Double.NEGATIVE_INFINITY;
    }
    try {
      alphabet().validate(r);
    } catch (IllegalResidueException ire) {
      throw new IllegalResidueException(
        ire,
        "Couldn't retrieve weight in state " + getName()
      );
    }
    Double d = (Double) residueToProb.get(r);
    if(d == null) {
      return Double.NEGATIVE_INFINITY;
    } else {
      return d.doubleValue();
    }
  }

  public void setWeight(Residue r, double val) throws IllegalResidueException {
    alphabet().validate(r);
    if(Double.isNaN(val)) {
      throw new Error("Can't set weight for " + r.getName() + " to " + val);
    }
    residueToProb.put(r, new Double(val));
  }
  
  public SimpleState(FiniteAlphabet alpha, int [] advance) {
    super(alpha);
    this.advance = advance;
  }
  
  public void registerWithTrainer(ModelTrainer modelTrainer) {
    Set trainerSet = modelTrainer.trainersForState(this);
    if(trainerSet.isEmpty()) {
      try {
        modelTrainer.registerTrainerForState(this, new SimpleStateTrainer(this));
      } catch (IllegalAlphabetException iae) {
        throw new BioError(
          iae,
          "I was sure that I was over a finite alphabet. What is wrong with me?"
        );
      }
    }
  }
}
