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

public class SimpleState extends AbstractState {
  private Map residueToProb = new HashMap();
  
  public double getWeight(Residue r) throws IllegalResidueException {
    alphabet().validate(r);
    return ((Double) residueToProb.get(r)).doubleValue();
  }

  public void setWeight(Residue r, double val) throws IllegalResidueException {
    alphabet().validate(r);
    residueToProb.put(r, new Double(val));
  }
  
  public SimpleState(Alphabet alpha) {
    super(alpha);
  }
  
  public void registerWithTrainer(ModelTrainer modelTrainer) {
    Set trainerSet = modelTrainer.trainersForState(this);
    if(trainerSet.isEmpty()) {
      modelTrainer.registerTrainerForState(this, new SimpleStateTrainer(this));
    }
  }
}

