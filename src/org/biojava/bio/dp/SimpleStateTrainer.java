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

public class SimpleStateTrainer implements StateTrainer {
  private EmissionState state;
  private Map c = new HashMap();

  {
    for (Iterator i = state.alphabet().residues().iterator(); i.hasNext();)
      c.put(i.next(), new Double(0.0));
  }

  public void addCount(Residue res, double count) throws IllegalResidueException {
    Double d = (Double) c.get(res);
    if (d == null)
      throw new IllegalResidueException("Residue " + res +
                                        " not found in " + state.alphabet().getName());
    c.put(res, new Double(d.doubleValue() + count));
  }

  public void train(EmissionState nullModel,
                    double weight) throws IllegalResidueException {
    for (Iterator i = state.alphabet().residues().iterator(); i.hasNext();) {
      Residue r = (Residue) i.next();
      addCount(r, nullModel.getWeight(r) * weight);
    }
    
    double sum = 0.0;
    for (Iterator i = state.alphabet().residues().iterator(); i.hasNext();) {
      Residue r = (Residue) i.next();
      sum += ((Double) c.get(r)).doubleValue();
    }
    for (Iterator i = state.alphabet().residues().iterator(); i.hasNext();) {
      Residue res = (Residue) i.next();
      state.setWeight(res,
                      Math.log(((Double) c.get(res)).doubleValue() / sum));
    }
  }

  public void clearCounts() {
    for (Iterator i = state.alphabet().residues().iterator(); i.hasNext();) {
      Residue res = (Residue) i.next();
      c.put(res, new Double(0.0));
    }
  }

  public SimpleStateTrainer(EmissionState s) {
    this.state = s;
  }
}
