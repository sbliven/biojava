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
import org.biojava.bio.dist.*;

public class SimpleModelTrainer
extends SimpleDistributionTrainerContext
implements ModelTrainer, Serializable {
  private Set models = new HashSet();
  
  public void registerModel(MarkovModel model) {
    if(!models.contains(model)) {
      for(Iterator i = model.stateAlphabet().iterator(); i.hasNext(); ) {
        State s = (State) i.next();
        try {
          Distribution dist = model.getWeights(s);
          registerDistribution(dist);
          System.out.println("Registered " + dist);
        } catch (IllegalSymbolException ise) {
          throw new BioError(ise, "Couldn't register states from model");
        }
        if(s instanceof EmissionState) {
          Distribution dist = ((EmissionState) s).getDistribution();
          registerDistribution(dist);
          System.out.println("Registered " + dist);
        }
      }
    }
  }
}
