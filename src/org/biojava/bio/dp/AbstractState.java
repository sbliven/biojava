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
import org.biojava.bio.seq.*;

/**
 * An abstract implementation of the State interface.
 * <P>
 * You only need to define the methods getWeight, setWeight, getTrainer.
 * If you know a lot about the probability distribution of your residues,
 * you may wish to override sampleResidue as well.
 *
 * @author Matthew Pocock
 */
public abstract class AbstractState implements EmissionState {
  private String name;
  private Alphabet alpha;
  private Annotation annotation;
  
  public char getSymbol() {
    return name.charAt(0);
  }

  public String getName() {
    return name;
  }

  public Annotation getAnnotation() {
    if(annotation == null)
      annotation = new SimpleAnnotation();
    return annotation;
  }


  public Alphabet alphabet() {
    return alpha;
  }

  public void setName(String name) {
    this.name = name;
  }

  public abstract void setWeight(Residue res, double weight) throws IllegalResidueException;
  
  public AbstractState(Alphabet alpha) throws IllegalArgumentException {
    if(alpha == null)
      throw new IllegalArgumentException("alpha can not be null");
    this.alpha = alpha;
  }

  public Residue sampleResidue()
  throws BioError {
    double p = Math.random();
    try {
      for(Iterator i = alphabet().residues().iterator(); i.hasNext(); ) {
        Residue r = (Residue) i.next();
        p -= Math.exp(getWeight(r));
        if( p <= 0) {
          return r;
        }
      }
    
      StringBuffer sb = new StringBuffer();
      for(Iterator i = alphabet().residues().iterator(); i.hasNext(); ) {
        Residue r = (Residue) i.next();
        double w = getWeight(r);
        if(w > 0.0)
          sb.append("\t" + r.getName() + " -> " + w + "\n");
      }
      throw new BioError(
        "Could not find a residue emitted from state " + this.getName() +
        this.getName() + ". Do the probabilities sum to 1?" +
        "\np=" + p + "\n" + sb.toString()
      );
    } catch (IllegalResidueException ire) {
      throw new BioError(
        ire,
        "Unable to itterate over all residues in alphabet - " +
        "things changed beneath me!"
      );
    }
  }
}
